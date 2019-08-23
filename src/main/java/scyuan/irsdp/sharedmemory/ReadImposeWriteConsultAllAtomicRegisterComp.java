package scyuan.irsdp.sharedmemory;

import scyuan.irsdp.broadcast.BestEffortBroadcast;
import scyuan.irsdp.broadcast.Broadcast;
import scyuan.irsdp.broadcast.BroadcastDeliver;
import scyuan.irsdp.failuredetector.Crash;
import scyuan.irsdp.failuredetector.PerfectFailureDetector;
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
 * Algorithm 4.9: Read-Impose Write-Consult-All
 * Implements:
 *     (N, N)-AtomicRegister, instance nnar.
 *
 * Uses:
 *     BestEffortBroadcast, instance beb;
 *     PerfectPointToPointLinks, instance pl;
 *     PerfectFailureDetector, instance P.
 *
 * upon event [ nnar, Init ] do
 *     (ts, wr, val) := (0, 0, ⊥);
 *     correct := Π;
 *     writeset := ∅;
 *     readval := ⊥;
 *     reading := FALSE;
 *
 * upon event [ P, Crash | p ] do
 *     correct := correct \ {p};
 *
 * upon event [ nnar, Read ] do
 *     reading := TRUE;
 *     readval := val;
 *     trigger [ beb, Broadcast | [WRITE, ts, wr, val] ];
 *
 * upon event [ nnar, Write | v ] do
 *     trigger [ beb, Broadcast | [WRITE, ts + 1, rank(self), v] ];
 *
 * upon event [ beb, Deliver | p, [WRITE, ts', wr', v'] ] do
 *     if (ts', wr') is larger than (ts, wr) then
 *         (ts, wr, val) := (ts', wr', v');
 *     trigger [ pl, Send | p, [ACK] ];
 *
 * upon event [ pl, Deliver | p, [ACK] ] then
 *     writeset := writeset ∪ {p};
 *
 * upon correct ⊆ writeset do
 *     writeset := ∅;
 *     if reading = TRUE then
 *         reading := FALSE;
 *         trigger [ nnar, ReadReturn | readval ];
 *     else
 *         trigger [ nnar, WriteReturn ];
 */
public class ReadImposeWriteConsultAllAtomicRegisterComp extends ComponentDefinition {
    private final Negative<NNAtomicRegister> nnar = provides(NNAtomicRegister.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);
    private final Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);

    private long ts;
    private long ws;
    private Object val;
    private Set<NetAddress> correct;
    private Set<NetAddress> writeset;
    private Object readval;
    private boolean reading;
    private long rankSelf;


    private final NetAddress self;
    private final Set<NetAddress> all;

    public ReadImposeWriteConsultAllAtomicRegisterComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);

        this.ts = 0L;
        this.ws = 0L;
        this.correct = new HashSet<>(this.all);
        this.writeset = new HashSet<>();
        this.readval = null;
        this.reading = false;
        this.rankSelf = rank(this.self);

        subscribe(crashHandler, pfd);
        subscribe(readHandler, nnar);
        subscribe(writeHandler, nnar);
        subscribe(ackDeliverHandler, pl);
        subscribe(writeDeliverHandler, beb);
    }

    private final Handler<Crash> crashHandler = new Handler<Crash>() {
        @Override
        public void handle(Crash event) {
            correct.remove(event.p);
            check();
        }
    };

    private final Handler<Read> readHandler = new Handler<Read>() {
        @Override
        public void handle(Read event) {
            reading = true;
            readval = val;
            trigger(new Broadcast(new WriteMessage(ts, ws, val), all, self), beb);
        }
    };

    private final Handler<Write> writeHandler = new Handler<Write>() {
        @Override
        public void handle(Write event) {
            trigger(new Broadcast(new WriteMessage(ts+1, rankSelf, event.value), all, self), beb);
        }
    };

    private final ClassMatchedHandler<WriteMessage, BroadcastDeliver> writeDeliverHandler = new ClassMatchedHandler<WriteMessage, BroadcastDeliver>() {
        @Override
        public void handle(WriteMessage content, BroadcastDeliver context) {
            if (content.ts > ts || (content.ts == ts && content.ws > ws)) {
                ts = content.ts;
                ws = content.ws;
                val = content.val;
            }
            trigger(new Send(context.src, new AckMessage()), pl);
        }
    };

    private final ClassMatchedHandler<AckMessage, Deliver> ackDeliverHandler = new ClassMatchedHandler<AckMessage, Deliver>() {
        @Override
        public void handle(AckMessage content, Deliver context) {
            writeset.add(context.src);
            check();
        }
    };

    private void check() {
        if (writeset.containsAll(correct)) {
            writeset = new HashSet<>();
            if (reading) {
                reading = false;
                trigger(new ReadReturn(readval), nnar);
            } else {
                trigger(new WriteReturn(), nnar);
            }
        }
    }

    private static class AckMessage implements KompicsEvent, Serializable {
    }

    private static class WriteMessage implements KompicsEvent, Serializable {
        public final long ts;
        public final long ws;
        public final Object val;

        public WriteMessage(long ts, long ws, Object val) {
            this.ts = ts;
            this.ws = ws;
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

    public static class Init extends se.sics.kompics.Init<ReadImposeWriteConsultAllAtomicRegisterComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }
}
