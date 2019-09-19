package scyuan.irsdp.consensus;

import scyuan.irsdp.broadcast.BestEffortBroadcast;
import scyuan.irsdp.broadcast.Broadcast;
import scyuan.irsdp.broadcast.BroadcastDeliver;
import scyuan.irsdp.failuredetector.EventualLeaderDetector;
import scyuan.irsdp.failuredetector.Trust;
import scyuan.irsdp.link.Deliver;
import scyuan.irsdp.link.NetAddress;
import scyuan.irsdp.link.PerfectPointToPointLink;
import scyuan.irsdp.link.Send;
import se.sics.kompics.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * Algorithm 5.5: Leader-Based Epoch-Change
 * Implements:
 *     EpochChange, instance ec.
 *
 * Uses:
 *     PerfectPointToPointLinks, instance pl;
 *     BestEffortBroadcast, instance beb;
 *     EventualLeaderDetector, instance Ω.
 *
 * upon event [ ec, Init ] do
 *     trusted := l0;
 *     lastts := 0;
 *     ts := rank(self);
 *
 * upon event [ Ω, Trust | p ] do
 *     if p != trusted then
 *         trigger [ pl,Send|trusted,[NACK] ]
 *     trusted := p;
 *     if p = self then
 *         ts := ts + N;
 *         trigger [ beb, Broadcast | [NEWEPOCH, ts] ];
 *
 * upon event [ beb, Deliver | l, [NEWEPOCH, newts] ] do
 *     if l = trusted ∧ newts > lastts then
 *         lastts := newts;
 *         trigger [ ec, StartEpoch | newts, l];
 *     else
 *         trigger [ pl, Send | l, [NACK] ];
 *
 * upon event [ pl, Deliver | p, [NACK] ] do
 *     if trusted = self then
 *         ts := ts + N;
 *         trigger [ beb, Broadcast | [NEWEPOCH, ts] ];
 *
 */
public class LeaderBasedEpochChangeComp extends ComponentDefinition {
    private final Negative<EpochChange> ec = provides(EpochChange.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<EventualLeaderDetector> eld = requires(EventualLeaderDetector.class);

    private final NetAddress self;
    private final Set<NetAddress> all;

    private NetAddress trusted;
    private long lastts;
    private long ts;

    private int N;

    public LeaderBasedEpochChangeComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);
        this.trusted = init.l0;
        this.lastts = 0L;
        this.ts = rank(self);
        this.N = this.all.size();

        subscribe(trustHandler, eld);
        subscribe(newepochDeliverHandler, beb);
        subscribe(nackDeliverHandler, pl);
    }

    private final Handler<Trust> trustHandler = new Handler<Trust>() {
        @Override
        public void handle(Trust event) {
            if (!event.p.equals(trusted)) {
                trigger(new Send(trusted, new Nack()), pl);
            }
            trusted = event.p;
            if (self.equals(event.p)) {
                ts += N;
                trigger(new Broadcast(new NewEpoch(ts), all ,self), beb);
            }
        }
    };

    private final ClassMatchedHandler<NewEpoch, BroadcastDeliver> newepochDeliverHandler = new ClassMatchedHandler<NewEpoch, BroadcastDeliver>() {
        @Override
        public void handle(NewEpoch content, BroadcastDeliver context) {
            if (context.src.equals(trusted) && content.newts > lastts) {
                lastts = content.newts;
                trigger(new StartEpoch(lastts, context.src), ec);
            } else {
                trigger(new Send(context.src, new Nack()), pl);
            }
        }
    };

    public final ClassMatchedHandler<Nack, Deliver> nackDeliverHandler = new ClassMatchedHandler<Nack, Deliver>() {
        @Override
        public void handle(Nack content, Deliver context) {
            if (self.equals(trusted)) {
                ts += N;
                trigger(new Broadcast(new NewEpoch(ts), all ,self), beb);
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

    private static class NewEpoch implements KompicsEvent, Serializable {
        final long newts;

        public NewEpoch(long newts) {
            this.newts = newts;
        }
    }

    private static class Nack implements KompicsEvent, Serializable {

    }

    private static class Init extends se.sics.kompics.Init<LeaderBasedEpochChangeComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;
        private final NetAddress l0;

        public Init(NetAddress self, Set<NetAddress> all, NetAddress l0) {
            this.self = self;
            this.all = all;
            this.l0 = l0;
        }
    }


}
