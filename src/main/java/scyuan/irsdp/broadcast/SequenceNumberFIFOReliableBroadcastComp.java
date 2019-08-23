package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.*;

import java.util.*;

/**
 *
 *
 * Fail-Silent Algorithm
 *
 * Algorithm 3.12: Broadcast with Sequence Number
 * Implements:
 *     FIFOReliableBroadcast, instance frb.
 *
 * Uses:
 *     ReliableBroadcast, instance rb.
 *
 * upon event [ frb, Init ] do
 *     lsn := 0;
 *     pending := ∅;
 *     next := [1]N;
 *
 * upon event [ frb, Broadcast | m ] do
 *     lsn := lsn + 1;
 *     trigger [ rb, Broadcast | [DATA, self, m, lsn]];
 *
 * upon event [ rb, Deliver | p, [DATA, s, m, sn]] do
 *     pending := pending ∪ {(s, m, sn)};
 *     while exists (s, m, sn) ∈ pending such that sn = next[s] do
 *         next[s] := next[s] + 1;
 *         pending := pending \ {(s, m, sn)};
 *         trigger [ frb, Deliver | s, m ];
 *
 */
public class SequenceNumberFIFOReliableBroadcastComp extends ComponentDefinition {
    private final Negative<FIFOReliableBroadcast> frb = provides(FIFOReliableBroadcast.class);
    private final Positive<ReliableBroadcast> rb = requires(ReliableBroadcast.class);

    private final NetAddress self;
    private final Set<NetAddress> all;

    private long lsn;
    private Set<FIFOReliableBroadcastMessage> pending;
    private Map<NetAddress, Long> next;

    public SequenceNumberFIFOReliableBroadcastComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);

        this.lsn = 0;
        this.pending = new HashSet<>();
        this.next = new HashMap<>();
        for (NetAddress p: this.all) {
            this.next.put(p, 1L);
        }

        subscribe(broadcastHandler, frb);
        subscribe(deliverHandler, rb);
    }

    private final Handler<Broadcast> broadcastHandler = new Handler<Broadcast>() {
        @Override
        public void handle(Broadcast event) {
            lsn = lsn + 1;
            trigger(new Broadcast(new FIFOReliableBroadcastMessage(event.src, event.payload, lsn), event.nodes, self), rb);
        }
    };

    private final ClassMatchedHandler<FIFOReliableBroadcastMessage, BroadcastDeliver> deliverHandler = new ClassMatchedHandler<FIFOReliableBroadcastMessage, BroadcastDeliver>() {
        @Override
        public void handle(FIFOReliableBroadcastMessage content, BroadcastDeliver context) {
            pending.add(content);
            Iterator<FIFOReliableBroadcastMessage> iter = pending.iterator();
            while (iter.hasNext()) {
                FIFOReliableBroadcastMessage m = iter.next();
                if (m.sn == next.get(m.src)) {
                    next.compute(m.src, (k, v) -> v + 1L);
                    iter.remove();
                    trigger(new BroadcastDeliver(m.payload, m.src), frb);
                }
            }
        }
    };

    public static class Init extends se.sics.kompics.Init<SequenceNumberFIFOReliableBroadcastComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }

}
