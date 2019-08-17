package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.*;

import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * Fail-Silent Algorithm
 *
 * Algorithm 3.3: Eager Reliable Broadcast
 * Implements:
 *     ReliableBroadcast, instance rb.
 * Uses:
 *     BestEffortBroadcast, instance beb.
 *
 * upon event [ rb, Init ] do
 *     delivered := ∅;
 *
 * upon event [ rb, Broadcast | m ] do
 *     trigger [ beb, Broadcast | [DATA, self, m] ];
 *
 * upon event [ beb, Deliver | p, [DATA, s, m] ] do
 *     if m !∈ delivered then
 *         delivered := delivered ∪ {m};
 *         trigger [ rb, Deliver | s, m ];
 *         trigger [ beb, Broadcast | [DATA, s, m] ];
 *
 */
public class EagerReliableBroadcastComp extends ComponentDefinition {

    private final Negative<ReliableBroadcast> rb = provides(ReliableBroadcast.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);

    private Set<KompicsEvent> delivered;
    private NetAddress self;

    public EagerReliableBroadcastComp(Init init) {
        this.self = init.self;
        this.delivered = new HashSet<>();

        subscribe(broadcastHandler, rb);
        subscribe(deliverHandler, beb);
    }

    Handler<Broadcast> broadcastHandler = new Handler<Broadcast>() {
        @Override
        public void handle(Broadcast event) {
            trigger(new Broadcast(new ReliableBroadcastMessage(event.payload, event.nodes), event.nodes, self), beb);

        }
    };

    ClassMatchedHandler<ReliableBroadcastMessage, BroadcastDeliver> deliverHandler = new ClassMatchedHandler<ReliableBroadcastMessage, BroadcastDeliver>() {
        @Override
        public void handle(ReliableBroadcastMessage content, BroadcastDeliver context) {
            if (!delivered.contains(content.payload)) {
                delivered.add(content.payload);
                trigger(new BroadcastDeliver(content.payload, context.src), rb);
                trigger(new Broadcast(new ReliableBroadcastMessage(content.payload, content.nodes), content.nodes, context.src), beb);
            }
        }
    };


    public static class Init extends se.sics.kompics.Init<EagerReliableBroadcastComp> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }
}
