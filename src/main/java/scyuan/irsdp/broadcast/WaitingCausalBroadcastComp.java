package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.*;

import java.util.*;

/**
 *
 *
 * Fail-Silent Algorithm
 *
 * Algorithm 3.15: Waiting Causal Broadcast
 * Implements:
 *     CausalOrderReliableBroadcast, instance crb.
 *
 * Uses:
 *     ReliableBroadcast, instance rb.
 *
 * upon event [ crb, Init ] do
 *     V := [0]N;
 *     lsn := 0;
 *     pending := ∅;
 *
 * upon event [ crb, Broadcast | m ] do
 *     W := V ;
 *     W [rank(self)] := lsn;
 *     lsn := lsn + 1;
 *     trigger [ rb, Broadcast | [DATA, W, m] ];
 *
 * upon event [ rb, Deliver | p, [DATA, W, m] ] do
 *     pending := pending ∪ {(p, W, m)};
 *     while exists (p', W', m') ∈ pending such that W' ≤ V do
 *         pending := pending \ {(p', W', m')};
 *         V [rank(p')] := V [rank(p')] + 1;
 *         trigger [ crb, Deliver | p, m ];
 *
 */
public class WaitingCausalBroadcastComp extends ComponentDefinition {
    private final Negative<CausalOrderReliableBroadcast> crb = provides(CausalOrderReliableBroadcast.class);
    private final Positive<ReliableBroadcast> rb = requires(ReliableBroadcast.class);

    private final NetAddress self;
    private final Set<NetAddress> all;

    private long[] vectorClock;
    private long lsn;
    private Set<WaitingCausalBroadcastMessage> pending;

    private Map<NetAddress, Integer> rank;

    public WaitingCausalBroadcastComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);
        this.lsn = 0;
        this.vectorClock = new long[this.all.size()];
        Arrays.fill(this.vectorClock, 0L);
        this.pending = new HashSet<>();

        this.rank = new HashMap<>();
        NetAddress[] members = this.all.toArray(new NetAddress[0]);
        Arrays.sort(members);
        for (int i = 0; i < members.length; i++) {
            this.rank.put(members[i], i);
        }

        subscribe(broadcastHandler, crb);
        subscribe(deliverHandler, rb);
    }

    private final Handler<Broadcast> broadcastHandler = new Handler<Broadcast>() {
        @Override
        public void handle(Broadcast event) {
            long[] vectorClockCopy = Arrays.copyOf(vectorClock, vectorClock.length);
            vectorClockCopy[rank.get(self)] = lsn;
            lsn += 1;
            trigger(new Broadcast(new WaitingCausalBroadcastMessage(self, event.payload, vectorClockCopy), event.nodes, self), rb);
        }
    };

    private final ClassMatchedHandler<WaitingCausalBroadcastMessage, BroadcastDeliver> deliverHandler = new ClassMatchedHandler<WaitingCausalBroadcastMessage, BroadcastDeliver>() {
        @Override
        public void handle(WaitingCausalBroadcastMessage content, BroadcastDeliver context) {
            pending.add(content);
            Iterator<WaitingCausalBroadcastMessage> iter = pending.iterator();
            while (iter.hasNext()) {
                WaitingCausalBroadcastMessage msg = iter.next();
                if (lessThanOrEqual(msg.vectorClock, vectorClock)) {
                    iter.remove();
                    vectorClock[rank.get(msg.src)] += 1;
                    trigger(new BroadcastDeliver(msg.payload, msg.src), crb);
                }
            }
        }
    };

    private boolean lessThanOrEqual(long[] w, long[] v) {
        for (int i = 0; i < w.length; i++) {
            if (w[i] > v[i]) {
                return false;
            }
        }
        return true;
    }

    public static class Init extends se.sics.kompics.Init<WaitingCausalBroadcastComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }

}
