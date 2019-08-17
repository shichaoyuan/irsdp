package scyuan.irsdp.broadcast;

import org.javatuples.Pair;
import scyuan.irsdp.failuredetector.Crash;
import scyuan.irsdp.failuredetector.PerfectFailureDetector;
import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * Fail-Stop Algorithm
 *
 * Algorithm 3.4: All-Ack Uniform Reliable Broadcast
 * Implements:
 *     UniformReliableBroadcast, instance urb.
 *
 * Uses:
 *     BestEffortBroadcast, instance beb.
 *     PerfectFailureDetector, instance P.
 *
 * upon event [ urb, Init ] do
 *     delivered := ∅;
 *     pending := ∅;
 *     correct := Π;
 *     forall m do ack[m] := ∅;
 *
 * upon event [ urb, Broadcast | m ] do
 *     pending := pending ∪ {(self, m)};
 *     trigger [ beb, Broadcast | [DATA, self, m] ];
 *
 * upon event [ beb, Deliver | p, [DATA, s, m] ] do
 *     ack[m] := ack[m] ∪ {p};
 *     if (s, m) !∈ pending then
 *         pending := pending ∪ {(s, m)};
 *         trigger [ beb, Broadcast | [DATA, s, m] ];
 *
 * upon event [ P, Crash | p ] do
 *     correct := correct \ {p};
 *
 * function candeliver(m) returns Boolean is
 *     return (correct ⊆ ack[m]);
 *
 * upon exists (s, m) ∈ pending such that candeliver(m) ∧ m !∈ delivered do
 *     delivered := delivered ∪ {m};
 *     trigger [ urb, Deliver | s, m ];
 *
 */
public class AllAckUniformReliableBroadcastComp extends ComponentDefinition {
    private Negative<UniformReliableBroadcast> urb = provides(UniformReliableBroadcast.class);
    private Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);

    private Set<KompicsEvent> delivered;
    private Set<Pair<NetAddress, KompicsEvent>> pending;
    private Map<KompicsEvent, Set<NetAddress>> ack;
    private NetAddress self;
    private Set<NetAddress> correct;

    public AllAckUniformReliableBroadcastComp(AllAckUniformReliableBroadcastComp.Init init) {
        this.self = init.self;
        this.delivered = new HashSet<>();
        this.pending = new HashSet<>();
        this.ack = new HashMap<>();
        this.correct = new HashSet<>(init.all);

        subscribe(crashHandler, pfd);
        subscribe(broadcastHandler, urb);
        subscribe(deliverHandler, beb);
    }

    private void check() {
        for (Pair<NetAddress, KompicsEvent> pair : pending) {
            if (candeliver(pair.getValue1()) && !delivered.contains(pair.getValue1())) {
                delivered.add(pair.getValue1());
                trigger(new BroadcastDeliver(pair.getValue1(), pair.getValue0()), urb);
            }
        }
    }

    private boolean candeliver(KompicsEvent m) {
        Set<NetAddress> from = ack.get(m);
        if (from == null) {
            return false;
        }

        return from.containsAll(correct);
    }

    private Handler<Crash> crashHandler = new Handler<Crash>() {
        @Override
        public void handle(Crash event) {
            correct.remove(event.p);
            check();
        }
    };

    private Handler<Broadcast> broadcastHandler = new Handler<Broadcast>() {
        @Override
        public void handle(Broadcast event) {
            pending.add(Pair.with(self, event.payload));
            trigger(new Broadcast(new ReliableBroadcastMessage(event.payload, event.nodes), event.nodes, self), beb);
            check();
        }
    };

    private ClassMatchedHandler<ReliableBroadcastMessage, BroadcastDeliver> deliverHandler = new ClassMatchedHandler<ReliableBroadcastMessage, BroadcastDeliver>() {
        @Override
        public void handle(ReliableBroadcastMessage content, BroadcastDeliver context) {
            Set<NetAddress> from = ack.computeIfAbsent(content.payload, k -> new HashSet<>());
            from.add(context.src);
            if (!pending.contains(Pair.with(context.src, content.payload))) {
                pending.add(Pair.with(context.src, content.payload));
                trigger(new Broadcast(new ReliableBroadcastMessage(content.payload, content.nodes), content.nodes, context.src), beb);
            }
            check();
        }
    };

    public static class Init extends se.sics.kompics.Init<AllAckUniformReliableBroadcastComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }

}
