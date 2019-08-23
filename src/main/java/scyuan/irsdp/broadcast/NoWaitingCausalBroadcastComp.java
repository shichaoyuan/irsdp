package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * Fail-Silent Algorithm
 *
 *
 * Algorithm 3.13: No-Waiting Causal Broadcast
 * Implements:
 *     CausalOrderReliableBroadcast, instance crb.
 *
 * Uses:
 *     ReliableBroadcast, instance rb.
 *
 * upon event [ crb, Init ] do
 *     delivered := ∅;
 *     past := [];
 *
 * upon event [ crb, Broadcast | m ] do
 *     trigger [ rb, Broadcast | [DATA, past, m] ];
 *     append(past, (self, m));
 *
 * upon event [ rb, Deliver | p, [DATA, mpast, m] ] do
 *     if m !∈ delivered then
 *         forall (s, n) ∈ mpast do // by the order in the list
 *             if n !∈ delivered then
 *                 trigger [ crb, Deliver | s, n ];
 *                 delivered := delivered ∪ {n};
 *                 if (s, n) !∈ past then
 *                     append(past, (s, n));
 *         trigger [ crb, Deliver | p, m ];
 *         delivered := delivered ∪ {m};
 *         if (p, m) !∈ past then
 *             append(past, (p, m));
 *
 */
public class NoWaitingCausalBroadcastComp extends ComponentDefinition {
    private final Negative<CausalOrderReliableBroadcast> crb = provides(CausalOrderReliableBroadcast.class);
    private final Positive<ReliableBroadcast> rb = requires(ReliableBroadcast.class);

    private final NetAddress self;
    private final Set<NetAddress> all;

    private Set<KompicsEvent> delivered;
    private List<ProcessMessageTuple> past;

    public NoWaitingCausalBroadcastComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);

        this.delivered = new HashSet<>();
        this.past = new LinkedList<>();

        subscribe(broadcastHandler, crb);
        subscribe(deliverHandler, rb);
    }

    private final Handler<Broadcast> broadcastHandler = new Handler<Broadcast>() {
        @Override
        public void handle(Broadcast event) {
            trigger(new Broadcast(new CausalOrderReliableBroadcastMessage(self, event.payload, new LinkedList<>(past)), event.nodes, self), rb);
            past.add(new ProcessMessageTuple(self, event.payload));
        }
    };

    private final ClassMatchedHandler<CausalOrderReliableBroadcastMessage, BroadcastDeliver> deliverHandler = new ClassMatchedHandler<CausalOrderReliableBroadcastMessage, BroadcastDeliver>() {
        @Override
        public void handle(CausalOrderReliableBroadcastMessage content, BroadcastDeliver context) {
            if (!delivered.contains(content.payload)) {
                for (ProcessMessageTuple tuple : content.mpast) {
                    if (!delivered.contains(tuple.m)) {
                        trigger(new BroadcastDeliver(tuple.m, tuple.p), crb);
                        delivered.add(tuple.m);
                        if (!past.contains(tuple)) {
                            past.add(tuple);
                        }
                    }
                }
                trigger(new BroadcastDeliver(content.payload, content.src), crb);
                delivered.add(content.payload);
                ProcessMessageTuple t = new ProcessMessageTuple(content.src, content.payload);
                if (past.contains(t)) {
                    past.add(t);
                }
            }
        }
    };

    public static class Init extends se.sics.kompics.Init<NoWaitingCausalBroadcastComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }


}
