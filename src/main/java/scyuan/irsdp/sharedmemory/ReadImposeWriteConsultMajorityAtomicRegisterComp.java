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
 * Algorithm 4.10: Read-Impose Write-Consult-Majority (part 1, read and consult)
 * Implements:
 *     (N, N)-AtomicRegister, instance nnar.
 *
 * Uses:
 *     BestEffortBroadcast, instance beb;
 *     PerfectPointToPointLinks, instance pl.
 *
 * upon event [ nnar, Init ] do
 *     (ts, wr, val) := (0, 0, ⊥);
 *     acks := 0;
 *     writeval := ⊥;
 *     rid := 0;
 *     readlist := [⊥]N;
 *     readval := ⊥;
 *     reading := FALSE;
 *
 * upon event [ nnar, Read ] do
 *     rid := rid + 1;
 *     acks := 0;
 *     readlist := [⊥]N;
 *     reading := TRUE;
 *     trigger [ beb, Broadcast | [READ, rid] ];
 *
 * upon event [ beb, Deliver | p, [READ, r] ] do
 *     trigger [ pl, Send | p, [VALUE, r, ts, wr, val] ];
 *
 * upon event [ pl, Deliver | q, [VALUE, r, ts', wr', v'] ] such that r = rid do
 *     readlist[q] := (ts', wr', v');
 *     if #(readlist) > N/2 then
 *         (maxts, rr, readval) := highest(readlist);
 *         readlist := [⊥]N;
 *         if reading = TRUE then
 *             trigger [ beb, Broadcast | [WRITE, rid, maxts, rr, readval] ];
 *         else
 *             trigger [ beb, Broadcast | [WRITE, rid, maxts + 1, rank(self), writeval] ];
 *
 * upon event [ nnar, Write | v ] do
 *     rid := rid + 1;
 *     writeval := v;
 *     acks := 0;
 *     readlist := [⊥]N;
 *     trigger [ beb, Broadcast | [READ, rid] ];
 *
 * upon event [ beb, Deliver | p, [WRITE, r, ts', wr', v'] ] do
 *     if (ts', wr') is larger than (ts, wr) then
 *         (ts, wr, val) := (ts', wr', v');
 *     trigger [ pl, Send | p, [ACK, r] ];
 *
 * upon event [ pl, Deliver | q, [ACK, r] ] such that r = rid do
 *     acks := acks + 1;
 *     if acks > N/2 then
 *         acks := 0;
 *         if reading = TRUE then
 *             reading := FALSE;
 *             trigger [ nnar, ReadReturn | readval ];
 *         else
 *             trigger [ nnar, WriteReturn ];
 *
 */
public class ReadImposeWriteConsultMajorityAtomicRegisterComp extends ComponentDefinition {
    private final Negative<ONAtomicRegister> onar = provides(ONAtomicRegister.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);

    private Triple valueTriple;
    private long acks;
    private Object writeval;
    private long rid;
    private Map<NetAddress, Triple> readlist;
    private Object readval;
    private boolean reading;
    private long rankSelf;

    private final NetAddress self;
    private final Set<NetAddress> all;

    public ReadImposeWriteConsultMajorityAtomicRegisterComp(Init init) {
        this.self = init.self;
        this.all = init.all;

        this.valueTriple = new Triple(0, 0, null);
        this.acks = 0L;
        this.rid = 0L;
        this.readlist = new HashMap<>();
        this.readval = null;
        this.reading = false;
        this.writeval = null;
        this.readval = null;
        this.rankSelf = rank(this.self);
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
            trigger(new Send(context.src, new ValueMessage(content.rid, valueTriple.ts, valueTriple.wr, valueTriple.val)), pl);
        }
    };

    private final ClassMatchedHandler<ValueMessage, Deliver> valueDeliverHandler = new ClassMatchedHandler<>() {
        @Override
        public void handle(ValueMessage content, Deliver context) {
            if (content.rid == rid) {
                readlist.put(context.src, new Triple(content.ts, content.wr, content.val));
                if (readlist.size() > all.size()/2) {
                    Triple triple = highestval(readlist);
                    readval = triple.val;
                    readlist = new HashMap<>();
                    if (reading) {
                        trigger(new Broadcast(new WriteMessage(rid, triple.ts, triple.wr, readval), all, self), beb);
                    } else {
                        trigger(new Broadcast(new WriteMessage(rid, triple.ts + 1, rankSelf, writeval), all, self), beb);
                    }
                }
            }
        }
    };

    private final Handler<Write> writeHandler = new Handler<Write>() {
        @Override
        public void handle(Write event) {
            rid += 1;
            writeval = event.value;
            acks = 0;
            readlist = new HashMap<>();
            trigger(new Broadcast(new ReadMessage(rid), all, self), beb);
        }
    };

    private final ClassMatchedHandler<WriteMessage, BroadcastDeliver> writeDeliverHandler = new ClassMatchedHandler<>() {
        @Override
        public void handle(WriteMessage content, BroadcastDeliver context) {
            if (content.ts > valueTriple.ts || (content.ts == valueTriple.ts && content.wr > valueTriple.wr)) {
                valueTriple = new Triple(content.ts, content.wr, content.val);
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

    private Triple highestval(Map<NetAddress, Triple> readlist) {
        Triple r = null;

        long maxts = -1;
        for (Triple triple : readlist.values()) {
            if (triple.ts > maxts) {
                maxts = triple.ts;
            }
        }
        long rr = -1;
        for (Triple triple : readlist.values()) {
            if (triple.ts == maxts) {
                if (triple.wr > rr) {
                    rr = triple.wr;
                    r = triple;
                }
            }
        }

        return r;
    }

    private static class ValueMessage implements KompicsEvent, Serializable {
        public final long rid;
        public final long ts;
        public final long wr;
        public final Object val;

        public ValueMessage(long rid, long ts, long wr, Object val) {
            this.rid = rid;
            this.ts = ts;
            this.wr = wr;
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
        public final long wr;
        public final Object val;

        public WriteMessage(long rid, long ts, long wr, Object val) {
            this.rid = rid;
            this.ts = ts;
            this.wr = wr;
            this.val = val;
        }
    }

    private static class Triple {
        private final long ts;
        private final long wr;
        private final Object val;

        public Triple(long ts, long wr, Object val) {
            this.ts = ts;
            this.wr = wr;
            this.val = val;
        }
    }

    private long rank(NetAddress p) {
        int i = 0;
        for (NetAddress n : this.all) {
            if (p.compareTo(n) >= 0) {
                i++;
            }
        }
        return i;
    }

    public static class Init extends se.sics.kompics.Init<ReadImposeWriteConsultMajorityAtomicRegisterComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }
}
