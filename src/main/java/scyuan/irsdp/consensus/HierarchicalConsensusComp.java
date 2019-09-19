package scyuan.irsdp.consensus;

import scyuan.irsdp.broadcast.BestEffortBroadcast;
import scyuan.irsdp.broadcast.Broadcast;
import scyuan.irsdp.broadcast.BroadcastDeliver;
import scyuan.irsdp.failuredetector.Crash;
import scyuan.irsdp.failuredetector.PerfectFailureDetector;
import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * Algorithm 5.2: Hierarchical Consensus
 * Implements:
 *     Consensus, instance c.
 *
 * Uses:
 *     BestEffortBroadcast, instance beb;
 *     PerfectFailureDetector, instance P.
 *
 * upon event [ c, Init ] do
 *     detectedranks := ∅;
 *     round := 1;
 *     proposal := ⊥; proposer := 0;
 *     delivered := [FALSE]N;
 *     broadcast := FALSE;
 *
 * upon event [ P, Crash | p ] do
 *     detectedranks := detectedranks ∪ {rank(p)};
 *
 * upon event [ c, Propose | v ] such that proposal = ⊥ do
 *     proposal := v;
 *
 * upon round = rank(self) ∧ proposal != ⊥ ∧ broadcast = FALSE do
 *     broadcast := TRUE;
 *     trigger [ beb, Broadcast | [DECIDED, proposal] ];
 *     trigger [ c, Decide | proposal ];
 *
 * upon round ∈ detectedranks ∨ delivered[round] = TRUE do
 *     round := round + 1;
 *
 * upon event [ beb, Deliver | p,[DECIDED, v] ] do
 *     r := rank(p);
 *     if r < rank(self) ∧ r > proposer then
 *         proposal := v;
 *         proposer := r;
 *     delivered[r] := TRUE;
 *
 */
public class HierarchicalConsensusComp extends ComponentDefinition {
    private final Negative<Consensus> c = provides(Consensus.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);

    private Set<Long> detectedranks;
    private long round;
    private Value proposal;
    private long proposer;
    private Map<Long, Boolean> delivered;
    private boolean broadcast;

    private final NetAddress self;
    private final Set<NetAddress> all;

    public HierarchicalConsensusComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);

        this.detectedranks = new HashSet<>();
        this.round = 1;
        this.proposal = null;
        this.proposer = 0;
        this.delivered = new HashMap<>();
        this.broadcast = false;

        subscribe(crashHandler, pfd);
        subscribe(proposeHandler, c);
        subscribe(decidedDeliverHandler, beb);
    }

    private final Handler<Crash> crashHandler = new Handler<Crash>() {
        @Override
        public void handle(Crash event) {
            detectedranks.add(rank(event.p));
            checkCrash();
            checkRank();
        }
    };

    private final Handler<Propose> proposeHandler = new Handler<Propose>() {
        @Override
        public void handle(Propose event) {
            if (proposal == null) {
                proposal = event.value;
            }
            checkRank();
        }
    };

    private void checkRank() {
        if (round == rank(self) && proposal != null && broadcast == false) {
            broadcast = true;
            trigger(new Broadcast(new DecidedMessage(proposal), all, self), beb);
            trigger(new Decide(proposal), c);
        }
    }

    private void checkCrash() {
        if (detectedranks.contains(round) || delivered.computeIfAbsent(round, k -> false)) {
            round += 1;
        }
    }

    private final ClassMatchedHandler<DecidedMessage, BroadcastDeliver> decidedDeliverHandler = new ClassMatchedHandler<DecidedMessage, BroadcastDeliver>() {
        @Override
        public void handle(DecidedMessage content, BroadcastDeliver context) {
            long r = rank(context.src);
            if (r < rank(self) && r > proposer) {
                proposal = content.v;
                proposer = r;
            }
            delivered.put(r, true);
            checkCrash();
            checkRank();
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

    private static class Init extends se.sics.kompics.Init<HierarchicalConsensusComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }


}
