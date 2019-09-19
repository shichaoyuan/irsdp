package scyuan.irsdp.consensus;

import com.google.common.collect.Sets;
import scyuan.irsdp.broadcast.BestEffortBroadcast;
import scyuan.irsdp.broadcast.Broadcast;
import scyuan.irsdp.broadcast.BroadcastDeliver;
import scyuan.irsdp.broadcast.ReliableBroadcast;
import scyuan.irsdp.failuredetector.Crash;
import scyuan.irsdp.failuredetector.PerfectFailureDetector;
import scyuan.irsdp.link.Deliver;
import scyuan.irsdp.link.NetAddress;
import scyuan.irsdp.link.PerfectPointToPointLink;
import scyuan.irsdp.link.Send;
import se.sics.kompics.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.*;

/**
 *
 *
 * Algorithm 5.4: Hierarchical Uniform Consensus
 * Implements:
 *     UniformConsensus, instance uc.
 * Uses:
 *     PerfectPointToPointLinks, instance pl;
 *     BestEffortBroadcast, instance beb;
 *     ReliableBroadcast, instance rb;
 *     PerfectFailureDetector, instance P.
 *
 * upon event [ uc, Init ] do
 *     detectedranks := ∅;
 *     ackranks := ∅;
 *     round := 1;
 *     proposal := ⊥; decision := ⊥;
 *     proposed := [⊥]N;
 *
 * upon event [ P, Crash | p ] do
 *     detectedranks := detectedranks ∪ {rank(p)};
 *
 * upon event [ uc, Propose | v ] such that proposal = ⊥ do
 *     proposal := v;
 *
 * upon round = rank(self) ∧ proposal != ⊥ ∧ decision = ⊥ do
 *     trigger [ beb, Broadcast | [PROPOSAL, proposal] ];
 *
 * upon event [ beb, Deliver | p, [PROPOSAL, v] ] do
 *     proposed[rank(p)] := v;
 *     if rank(p) ≥ round then
 *         trigger [ pl, Send | p, [ACK] ];
 *
 * upon round ∈ detectedranks do
 *     if proposed[round] != ⊥ then
 *         proposal := proposed[round];
 *     round := round + 1;
 *
 * upon event [ pl, Deliver | q, [ACK] ] do
 *     ackranks := ackranks ∪ {rank(q)};
 *
 * upon detectedranks ∪ ackranks = {1, . . . , N} do
 *     trigger [ rb, Broadcast | [DECIDED, proposal] ];
 *
 * upon event [ rb, Deliver | p, [DECIDED, v] ] such that decision = ⊥ do
 *     decision := v;
 *     trigger [ uc, Decide | decision ];
 *
 */
public class HierarchicalUniformConsensusComp extends ComponentDefinition {
    private final Negative<UniformConsensus> uc = provides(UniformConsensus.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<ReliableBroadcast> rb = requires(ReliableBroadcast.class);
    private final Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);

    private Set<Long> detectedranks;
    private Set<Long> ackranks;
    private long round;
    private Value proposal;
    private Value decision;
    private Map<Long, Value> proposed;

    private final NetAddress self;
    private final Set<NetAddress> all;

    public HierarchicalUniformConsensusComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);

        this.detectedranks = new HashSet<>();
        this.ackranks = new HashSet<>();
        this.round = 1;
        this.proposal = null;
        this.decision = null;
        this.proposed = new HashMap<>();

        subscribe(crashHandler, pfd);
        subscribe(proposeHandler, uc);
        subscribe(proposalDeliverHandler, beb);
        subscribe(ackDeliverHandler, pl);
        subscribe(decidedDeliverHandler, rb);
    }

    private final Handler<Crash> crashHandler = new Handler<Crash>() {
        @Override
        public void handle(Crash event) {
            detectedranks.add(rank(event.p));
            checkCrash();
            checkPropose();
            checkAck();
        }
    };

    private final Handler<Propose> proposeHandler = new Handler<Propose>() {
        @Override
        public void handle(Propose event) {
            if (proposal == null) {
                proposal = event.value;
                checkPropose();
            }
        }
    };

    private void checkPropose() {
        if (round == rank(self) && proposal != null && decision == null) {
            trigger(new Broadcast(new ProposalMessage(proposal), all, self), beb);
        }
    }

    private final ClassMatchedHandler<ProposalMessage, BroadcastDeliver> proposalDeliverHandler = new ClassMatchedHandler<ProposalMessage, BroadcastDeliver>() {
        @Override
        public void handle(ProposalMessage content, BroadcastDeliver context) {
            proposed.put(rank(context.src), content.v);
            if (rank(context.src) >= round) {
                trigger(new Send(context.src, new AckMessage()), pl);
            }
        }
    };

    private void checkCrash() {
        if (detectedranks.contains(round)) {
            if (proposed.get(round) != null) {
                proposal = proposed.get(round);
            }
            round += 1;
        }
    }

    private final ClassMatchedHandler<AckMessage, Deliver> ackDeliverHandler = new ClassMatchedHandler<AckMessage, Deliver>() {
        @Override
        public void handle(AckMessage content, Deliver context) {
            ackranks.add(rank(context.src));
            checkAck();
        }
    };

    private void checkAck() {
        Set<Long> allranks = LongStream.range(1, all.size()+1).boxed().collect(toSet());
        if (Sets.union(detectedranks, ackranks).equals(allranks)) {
            trigger(new Broadcast(new DecidedMessage(proposal), all, self), rb);
        }
    }

    private final ClassMatchedHandler<DecidedMessage, BroadcastDeliver> decidedDeliverHandler = new ClassMatchedHandler<DecidedMessage, BroadcastDeliver>() {
        @Override
        public void handle(DecidedMessage content, BroadcastDeliver context) {
            if (decision == null) {
                decision = content.v;
                trigger(new Decide(decision), uc);
            }
        }
    };


    private long rank(NetAddress p) {
        int i = 0;
        for (NetAddress n : this.all) {
            if (p.compareTo(n) <= 0) {
                i++;
            }
        }
        return i;
    }

    private static class DecidedMessage implements KompicsEvent, Serializable {
        final Value v;

        public DecidedMessage(Value v) {
            this.v = v;
        }
    }

    private static class ProposalMessage implements KompicsEvent, Serializable {
        final Value v;

        public ProposalMessage(Value v) {
            this.v = v;
        }
    }

    private static class AckMessage implements KompicsEvent, Serializable {
    }

    private static class Init extends se.sics.kompics.Init<HierarchicalUniformConsensusComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }


}
