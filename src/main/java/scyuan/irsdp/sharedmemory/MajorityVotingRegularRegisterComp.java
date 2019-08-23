package scyuan.irsdp.sharedmemory;

import scyuan.irsdp.broadcast.BestEffortBroadcast;
import scyuan.irsdp.broadcast.Broadcast;
import scyuan.irsdp.broadcast.BroadcastDeliver;
import scyuan.irsdp.link.Deliver;
import scyuan.irsdp.link.NetAddress;
import scyuan.irsdp.link.PerfectPointToPointLink;
import scyuan.irsdp.link.Send;
import se.sics.kompics.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 *
 * Algorithm 4.2: Majority Voting Regular Register
 * Implements:
 *     (1, N)-RegularRegister, instance onrr.
 *
 * Uses:
 *     BestEffortBroadcast, instance beb;
 *     PerfectPointToPointLinks, instance pl.
 *
 * upon event [ onrr, Init ] do
 *     (ts, val) := (0, ⊥);
 *     wts := 0;
 *     acks := 0;
 *     rid := 0;
 *     readlist := [⊥]N;
 *
 * upon event [ onrr, Write | v ] do
 *     wts := wts + 1;
 *     acks := 0;
 *     trigger [ beb, Broadcast | [WRITE, wts, v] ];
 *
 * upon event [ beb, Deliver | p, [WRITE, ts, v] ] do
 *     if ts' > ts then
 *         (ts, val) := (ts', v');
 *     trigger [ pl, Send | p, [ACK, ts'] ];
 *
 * upon event [ pl, Deliver | q, [ACK, ts'] ] such that ts' = wts do
 *     acks := acks + 1;
 *     if acks > N/2 then
 *         acks := 0;
 *         trigger [ onrr, WriteReturn ];
 *
 * upon event [ onrr, Read ] do
 *     rid := rid + 1;
 *     readlist := [⊥]N;
 *     trigger [ beb, Broadcast | [READ, rid] ];
 *
 * upon event [ beb, Deliver | p, [READ, r] ] do
 *     trigger [ pl, Send | p, [VALUE, r, ts, val] ];
 *
 * upon event [ pl, Deliver | q, [VALUE, r, ts', v'] ] such that r = rid do
 *     readlist[q] := (ts', v');
 *     if #(readlist) > N/2 then
 *         v := highestval(readlist);
 *         readlist := [⊥]N;
 *         trigger [ onrr, ReadReturn | v ];
 *
 */
public class MajorityVotingRegularRegisterComp extends ComponentDefinition {
    private final Negative<ONRegularRegister> onrr = provides(ONRegularRegister.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);

    private TimeValueTuple timeValueTuple;
    private long wts;
    private long acks;
    private long rid;
    private Map<NetAddress, TimeValueTuple> readlist;

    private final NetAddress self;
    private final Set<NetAddress> all;

    public MajorityVotingRegularRegisterComp(Init init) {
        this.self = init.self;
        this.all = init.all;

        this.timeValueTuple = new TimeValueTuple(0, null);
        this.wts = 0;
        this.acks = 0;
        this.rid = 0;
        this.readlist = new HashMap<>();

        subscribe(writeHandler, onrr);
        subscribe(writeDeliverHandler, beb);
        subscribe(ackDeliverHandler, pl);
        subscribe(readHandler, onrr);
        subscribe(readDeliverHandler, beb);
        subscribe(valueDeliverHandler, pl);
    }

    private final Handler<Write> writeHandler = new Handler<Write>() {
        @Override
        public void handle(Write event) {
            wts = wts + 1;
            acks = 0;
            trigger(new Broadcast(new WriteMessage(wts, event.value), all, self), beb);
        }
    };

    private final ClassMatchedHandler<WriteMessage, BroadcastDeliver> writeDeliverHandler = new ClassMatchedHandler<WriteMessage, BroadcastDeliver>() {
        @Override
        public void handle(WriteMessage content, BroadcastDeliver context) {
            if (content.wts > timeValueTuple.timestamp) {
                timeValueTuple = new TimeValueTuple(content.wts, content.v);
            }
            trigger(new Send(context.src, new AckMessage(content.wts)), pl);
        }
    };

    private final ClassMatchedHandler<AckMessage, Deliver> ackDeliverHandler = new ClassMatchedHandler<AckMessage, Deliver>() {
        @Override
        public void handle(AckMessage content, Deliver context) {
            if (content.ts == wts) {
                acks += 1;
                if (acks > all.size()/2) {
                    acks = 0;
                    trigger(new WriteReturn(), onrr);
                }
            }
        }
    };

    private final Handler<Read> readHandler = new Handler<Read>() {
        @Override
        public void handle(Read event) {
            rid += 1;
            readlist = new HashMap<>();
            trigger(new Broadcast(new ReadMessage(rid), all, self), beb);
        }
    };

    private final ClassMatchedHandler<ReadMessage, BroadcastDeliver> readDeliverHandler = new ClassMatchedHandler<ReadMessage, BroadcastDeliver>() {
        @Override
        public void handle(ReadMessage content, BroadcastDeliver context) {
            trigger(new Send(context.src, new ValueMessage(content.rid, timeValueTuple.timestamp, timeValueTuple.value)), pl);
        }
    };

    private final ClassMatchedHandler<ValueMessage, Deliver> valueDeliverHandler = new ClassMatchedHandler<ValueMessage, Deliver>() {
        @Override
        public void handle(ValueMessage content, Deliver context) {
            if (content.r == rid) {
                readlist.put(context.src, new TimeValueTuple(content.ts, content.val));
                if (readlist.size() > all.size()/2) {
                    Object v = highestval(readlist);
                    trigger(new ReadReturn(v), onrr);
                }
            }
        }
    };

    private Object highestval(Map<NetAddress, TimeValueTuple> readlist) {
        long timestamp = -1;
        Object value = null;
        for (TimeValueTuple tuple : readlist.values()) {
            if (tuple.timestamp > timestamp) {
                timestamp = tuple.timestamp;
                value = tuple.value;
            }
        }

        return value;
    }

    private static class ValueMessage implements KompicsEvent, Serializable {
        public final long r;
        public final long ts;
        public final Object val;

        public ValueMessage(long r, long ts, Object val) {
            this.r = r;
            this.ts = ts;
            this.val = val;
        }
    }

    private static class AckMessage implements KompicsEvent, Serializable {
        public final long ts;

        public AckMessage(long ts) {
            this.ts = ts;
        }
    }

    private static class ReadMessage implements KompicsEvent, Serializable {
        public final long rid;

        public ReadMessage(long rid) {
            this.rid = rid;
        }
    }

    private static class WriteMessage implements KompicsEvent, Serializable {
        public final long wts;
        public final Object v;

        public WriteMessage(long wts, Object v) {
            this.wts = wts;
            this.v = v;
        }
    }

    public static class Init extends se.sics.kompics.Init<MajorityVotingRegularRegisterComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }
}
