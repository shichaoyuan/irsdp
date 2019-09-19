package scyuan.irsdp.consensus;

import scyuan.irsdp.broadcast.BestEffortBroadcast;
import scyuan.irsdp.broadcast.Broadcast;
import scyuan.irsdp.broadcast.BroadcastDeliver;
import scyuan.irsdp.failuredetector.Crash;
import scyuan.irsdp.failuredetector.PerfectFailureDetector;
import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.*;

import java.io.Serializable;
import java.util.*;

/**
 *
 *
 * Fail-Stop Algorithm: Flooding Consensus
 *
 * Algorithm 5.1: Flooding Consensus
 * Implements:
 *     Consensus, instance c.
 * Uses:
 *     BestEffortBroadcast, instance beb;
 *     PerfectFailureDetector, instance P.
 *
 * upon event [ c, Init ] do
 *     correct := Π;
 *     round := 1;
 *     decision := ⊥;
 *     receivedfrom := [∅]N;
 *     proposals := [∅]N;
 *     receivedfrom[0] := Π;
 *
 * upon event [ P, Crash | p ] do
 *     correct := correct \ {p};
 *
 * upon event [ c, Propose | v ] do
 *     proposals[1] := proposals[1] ∪ {v};
 *     trigger [ beb, Broadcast | [PROPOSAL, 1, proposals[1]] ];
 *
 * upon event [ beb, Deliver | p, [PROPOSAL, r, ps] ] do
 *     receivedfrom[r] := receivedfrom[r] ∪ {p};
 *     proposals[r] := proposals[r] ∪ ps;
 *
 * upon correct ⊆ receivedfrom[round] ∧ decision = ⊥ do
 *     if receivedfrom[round] = receivedfrom[round − 1] then
 *         decision := min(proposals[round]);
 *         trigger [ beb, Broadcast | [DECIDED, decision] ];
 *         trigger [ c, Decide | decision ];
 *     else
 *         round := round + 1;
 *         trigger [ beb, Broadcast | [PROPOSAL, round, proposals[round − 1]] ];
 *
 * upon event [ beb, Deliver | p, [DECIDED, v] ] such that p ∈ correct ∧ decision = ⊥ do
 *     decision := v;
 *     trigger [ beb, Broadcast | [DECIDED, decision] ];
 *     trigger [ c, Decide | decision ];
 *
 */
public class FloodingConsensusComp extends ComponentDefinition {
    private final Negative<Consensus> c = provides(Consensus.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);

    private Set<NetAddress> correct;
    private long round;
    private Value decision;
    private Map<Long, Set<NetAddress>> receivedfrom;
    private Map<Long, Set<Value>> proposals;

    private final NetAddress self;
    private final Set<NetAddress> all;

    public FloodingConsensusComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);

        this.correct = new HashSet<>(this.all);
        this.round = 1;
        this.decision = null;
        this.receivedfrom = new HashMap<>();
        this.proposals = new HashMap<>();

        this.receivedfrom.put(0L, new HashSet<>(this.all));
        this.receivedfrom.put(1L, new HashSet<>());

        subscribe(proposeHandler, c);
        subscribe(proposeDeliverHandler, beb);
        subscribe(crashHandler, pfd);
        subscribe(decideDeliverHandler, beb);
    }

    private final Handler<Propose> proposeHandler = new Handler<Propose>() {
        @Override
        public void handle(Propose event) {
            Set<Value> ps = proposals.computeIfAbsent(1L, k -> new HashSet<>());
            ps.add(event.value);
            trigger(new Broadcast(new ProposeMessage(1L, new HashSet<>(ps)), all, self), beb);
        }
    };

    private final ClassMatchedHandler<ProposeMessage, BroadcastDeliver> proposeDeliverHandler = new ClassMatchedHandler<ProposeMessage, BroadcastDeliver>() {
        @Override
        public void handle(ProposeMessage content, BroadcastDeliver context) {
            Set<NetAddress> rs = receivedfrom.computeIfAbsent(content.r, k -> new HashSet<>());
            rs.add(context.src);
            Set<Value> ps = proposals.computeIfAbsent(content.r, k -> new HashSet<>());
            ps.addAll(content.ps);

            check();
        }
    };

    private final Handler<Crash> crashHandler = new Handler<Crash>() {
        @Override
        public void handle(Crash event) {
            correct.remove(event.p);

            check();
        }
    };

    private void check() {
        if (receivedfrom.get(round).containsAll(correct) && decision == null) {
            if (receivedfrom.get(round).equals(receivedfrom.get(round-1))) {
                decision = Collections.min(proposals.get(round));
                trigger(new Broadcast(new DecideMessage(decision), all, self), beb);
                trigger(new Decide(decision), c);
            } else {
                round += 1;
                trigger(new Broadcast(new ProposeMessage(round, proposals.get(round-1)), all, self), beb);
            }
        }
    }

    private final ClassMatchedHandler<DecideMessage, BroadcastDeliver> decideDeliverHandler = new ClassMatchedHandler<DecideMessage, BroadcastDeliver>() {
        @Override
        public void handle(DecideMessage content, BroadcastDeliver context) {
            if (correct.contains(context.src) && decision == null) {
                decision = content.v;
                trigger(new Broadcast(new DecideMessage(decision), all, self), beb);
                trigger(new Decide(decision), c);
            }
        }
    };

    private static class ProposeMessage implements KompicsEvent, Serializable {
        final long r;
        final Set<Value> ps;

        public ProposeMessage(long r, Set<Value> ps) {
            this.r = r;
            this.ps = ps;
        }
    }

    private static class DecideMessage implements KompicsEvent, Serializable {
        final Value v;

        public DecideMessage(Value v) {
            this.v = v;
        }
    }

    private static class Init extends se.sics.kompics.Init<FloodingConsensusComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }
}
