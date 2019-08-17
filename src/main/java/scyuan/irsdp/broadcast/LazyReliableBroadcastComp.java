package scyuan.irsdp.broadcast;

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
 * Algorithm 3.2: Lazy Reliable Broadcast
 * Implements:
 *     ReliableBroadcast, instance rb.
 * Uses:
 *     BestEffortBroadcast, instance beb;
 *     PerfectFailureDetector, instance P.
 *
 * upon event [ rb, Init ] do
 *     correct := Π;
 *     from[p] := [∅]N;
 *
 * upon event [ rb, Broadcast | m ] do
 *     trigger [ beb, Broadcast | [DATA, self, m] ];
 *
 * upon event [ beb, Deliver | p, [DATA, s, m] ] do
 *     if m !∈ from[s] then
 *         trigger [ rb, Deliver | s, m ];
 *         from[s] := from[s] ∪ {m};
 *         if s !∈ correct then
 *             trigger [ beb, Broadcast | [DATA, s, m] ];
 *
 * upon event [ P, Crash | p ] do
 *     correct := correct \ {p};
 *     forall m ∈ from[p] do
 *         trigger [ beb, Broadcast | [DATA, p, m] ];
 *
 */
public class LazyReliableBroadcastComp extends ComponentDefinition {
    private final Negative<ReliableBroadcast> rb = provides(ReliableBroadcast.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);

    private Set<NetAddress> correct;
    private Map<NetAddress, Set<ReliableBroadcastMessage>> from;

    public LazyReliableBroadcastComp(Init init) {
        this.correct = new HashSet<>(init.all);
        this.from = new HashMap<>();

        subscribe(broadcastHandler, rb);
        subscribe(deliverHandler, beb);
        subscribe(crashHandler, pfd);
    }

    private final Handler<Broadcast> broadcastHandler = new Handler<Broadcast>() {
        @Override
        public void handle(Broadcast event) {
            trigger(new Broadcast(new ReliableBroadcastMessage(event.payload, event.nodes), event.nodes, event.src), beb);
        }
    };

    private final ClassMatchedHandler<ReliableBroadcastMessage, BroadcastDeliver> deliverHandler = new ClassMatchedHandler<ReliableBroadcastMessage, BroadcastDeliver>() {
        @Override
        public void handle(ReliableBroadcastMessage content, BroadcastDeliver context) {
            Set<ReliableBroadcastMessage> deliverSet = from.get(context.src);
            if (deliverSet == null || !deliverSet.contains(content)) {
                trigger(new BroadcastDeliver(content.payload, context.src), rb);
                if (deliverSet == null) {
                    deliverSet = new HashSet<>();
                    from.put(context.src, deliverSet);
                }
                deliverSet.add(content);
                if (!correct.contains(context.src)) {
                    trigger(new Broadcast(new ReliableBroadcastMessage(content.payload, content.nodes), content.nodes, context.src), beb);
                }
            }
        }
    };

    private final Handler<Crash> crashHandler = new Handler<Crash>() {
        @Override
        public void handle(Crash event) {
            correct.remove(event.p);
            Set<ReliableBroadcastMessage> deliverSet = from.get(event.p);
            if (deliverSet != null) {
                for (ReliableBroadcastMessage content : deliverSet) {
                    trigger(new Broadcast(new ReliableBroadcastMessage(content.payload, content.nodes), content.nodes, event.p), beb);

                }
            }
        }
    };

    public static class Init extends se.sics.kompics.Init<LazyReliableBroadcastComp> {
        private final Set<NetAddress> all;

        public Init(Set<NetAddress> all) {
            this.all = all;
        }
    }






}
