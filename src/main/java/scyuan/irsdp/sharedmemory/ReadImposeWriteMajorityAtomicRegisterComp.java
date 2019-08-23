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
 * Fail-Silent Algorithm
 *
 * Algorithm 4.6: Read-Impose Write-Majority
 * Implements:
 *     (1, N)-AtomicRegister, instance onar.
 *
 * Uses:
 *     BestEffortBroadcast, instance beb;
 *     PerfectPointToPointLinks, instance pl.
 *
 * upon event [ onar, Init ] do
 *     (ts, val) := (0, ⊥);
 *     wts := 0;
 *     acks := 0;
 *     rid := 0;
 *     readlist := [⊥]N;
 *     readval := ⊥;
 *     reading := FALSE;
 *
 * upon event [ onar, Read ] do
 *     rid := rid + 1;
 *     acks := 0;
 *     readlist := [⊥]N;
 *     reading := TRUE;
 *     trigger [ beb, Broadcast | [READ, rid] ];
 *
 * upon event [ beb, Deliver | p, [READ, r] ] do
 *     trigger [ pl, Send | p, [VALUE, r, ts, val] ];
 *
 * upon event [ pl, Deliver | q, [VALUE, r, ts', v'] ] such that r = rid do
 *     readlist[q] := (ts', v');
 *     if #(readlist) > N/2 then
 *         (maxts, readval) := highest(readlist);
 *         readlist := [⊥]N;
 *         trigger  beb, Broadcast | [WRITE, rid, maxts, readval] ;
 *
 * upon event [ onar, Write | v ] do
 *     rid := rid + 1;
 *     wts := wts + 1;
 *     acks := 0;
 *     trigger [ beb, Broadcast | [WRITE, rid, wts, v] ];
 *
 * upon event [ beb, Deliver | p, [WRITE, r, ts', v'] ] do
 *     if ts' > ts then
 *         (ts, val) := (ts', v');
 *         trigger [ pl, Send | p, [ACK, r] ];
 *
 * upon event [ pl, Deliver | q, [ACK, r] ] such that r = rid do
 *     acks := acks + 1;
 *     if acks > N/2 then
 *         acks := 0;
 *         if reading = TRUE then
 *             reading := FALSE;
 *             trigger [ onar, ReadReturn | readval ];
 *         else
 *             trigger [ onar, WriteReturn ];
 */
public class ReadImposeWriteMajorityAtomicRegisterComp extends ComponentDefinition {
    private final Negative<ONAtomicRegister> onar = provides(ONAtomicRegister.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);

    private TimeValueTuple timeValueTuple;
    private long wts;
    private long acks;
    private long rid;
    private Map<NetAddress, TimeValueTuple> readlist;
    private Object readval;
    private boolean reading;

    private final NetAddress self;
    private final Set<NetAddress> all;

    public ReadImposeWriteMajorityAtomicRegisterComp(Init init) {
        this.self = init.self;
        this.all = init.all;

        this.timeValueTuple = new TimeValueTuple(0, null);
        this.wts = 0L;
        this.acks = 0L;
        this.rid = 0L;
        this.readlist = new HashMap<>();
        this.readval = null;
        this.reading = false;
    }

    private final Handler<Read> readHandler = new Handler<Read>() {
        @Override
        public void handle(Read event) {
            rid += 1;
            acks = 0;
            readlist = new HashMap<>();
            reading = true;

            trigger(new Broadcast(new ReadMessage(rid), all, self), beb);
        }
    };

    private final ClassMatchedHandler<ReadMessage, BroadcastDeliver> readDeliverHandler = new ClassMatchedHandler<>() {
        @Override
        public void handle(ReadMessage content, BroadcastDeliver context) {
            trigger(new Send(context.src, new ValueMessage(content.rid, timeValueTuple.timestamp, timeValueTuple.value)), pl);
        }
    };

    private final ClassMatchedHandler<ValueMessage, Deliver> valueDeliverHandler = new ClassMatchedHandler<>() {
        @Override
        public void handle(ValueMessage content, Deliver context) {
            if (content.rid == rid) {
                readlist.put(context.src, new TimeValueTuple(content.ts, content.val));
                if (readlist.size() > all.size()/2) {
                    TimeValueTuple tuple = highestval(readlist);
                    readval = tuple.value;
                    readlist = new HashMap<>();
                    trigger(new Broadcast(new WriteMessage(rid, tuple.timestamp, readval), all, self), beb);
                }
            }

        }
    };

    private final Handler<Write> writeHandler = new Handler<Write>() {
        @Override
        public void handle(Write event) {
            rid += 1;
            wts += 1;
            acks = 0;
            trigger(new Broadcast(new WriteMessage(rid, wts, event.value), all, self), beb);
        }
    };

    private final ClassMatchedHandler<WriteMessage, BroadcastDeliver> writeDeliverHandler = new ClassMatchedHandler<>() {
        @Override
        public void handle(WriteMessage content, BroadcastDeliver context) {
            if (content.ts > timeValueTuple.timestamp) {
                timeValueTuple = new TimeValueTuple(content.ts, content.val);
            }
            trigger(new Send(context.src, new AckMessage(content.rid)), pl);
        }
    };

    private final ClassMatchedHandler<AckMessage, Deliver> ackDeliverHandler = new ClassMatchedHandler<>() {
        @Override
        public void handle(AckMessage content, Deliver context) {
            acks += 1;
            if (acks > all.size()/2) {
                acks = 0;
                if (reading) {
                    reading = false;
                    trigger(new ReadReturn(readval), onar);
                } else {
                    trigger(new WriteReturn(), onar);
                }
            }

        }
    };

    private TimeValueTuple highestval(Map<NetAddress, TimeValueTuple> readlist) {
        long timestamp = -1;
        Object value = null;
        for (TimeValueTuple tuple : readlist.values()) {
            if (tuple.timestamp > timestamp) {
                timestamp = tuple.timestamp;
                value = tuple.value;
            }
        }

        return new TimeValueTuple(timestamp, value);
    }

    private static class ValueMessage implements KompicsEvent, Serializable {
        public final long rid;
        public final long ts;
        public final Object val;

        public ValueMessage(long rid, long ts, Object val) {
            this.rid = rid;
            this.ts = ts;
            this.val = val;
        }
    }

    private static class ReadMessage implements KompicsEvent, Serializable {
        public final long rid;

        public ReadMessage(long rid) {
            this.rid = rid;
        }
    }

    private static class AckMessage implements KompicsEvent, Serializable {
        public final long rid;

        public AckMessage(long rid) {
            this.rid = rid;
        }
    }

    private static class WriteMessage implements KompicsEvent, Serializable {
        public final long rid;
        public final long ts;
        public final Object val;

        public WriteMessage(long rid, long ts, Object val) {
            this.rid = rid;
            this.ts = ts;
            this.val = val;
        }
    }

    public static class Init extends se.sics.kompics.Init<ReadImposeWriteMajorityAtomicRegisterComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }
}
