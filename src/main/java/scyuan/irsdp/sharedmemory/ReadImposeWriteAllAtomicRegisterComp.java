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
 *
 * Fail-Stop Algorithm
 *
 * Algorithm 4.5: Read-Impose Write-All
 * Implements:
 *     (1, N)-AtomicRegister, instance onar.
 *
 * Uses:
 *     BestEffortBroadcast, instance beb;
 *     PerfectPointToPointLinks, instance pl;
 *     PerfectFailureDetector, instance P.
 *
 * upon event [ onar, Init ] do
 *     (ts, val) := (0, ⊥);
 *     correct := Π;
 *     writeset := ∅;
 *     readval := ⊥;
 *     reading := FALSE;
 *
 * upon event [ P, Crash | p ] do
 *     correct := correct \ {p};
 *
 * upon event [ onar, Read ] do
 *     reading := TRUE;
 *     readval := val;
 *     trigger [ beb, Broadcast | [WRITE, ts, val] ];
 *
 * upon event [ onar, Write | v ] do
 *     trigger [ beb, Broadcast | [WRITE, ts + 1, v] ];
 *
 * upon event [ beb, Deliver | p, [WRITE, ts', v'] ] do
 *     if ts' > ts then
 *         (ts, val) := (ts', v');
 *     trigger [ pl, Send | p, [ACK] ];
 *
 * upon event [ pl, Deliver | p, [ACK] ] then
 *     writeset := writeset ∪ {p};
 *
 * upon correct ⊆ writeset do
 *     writeset := ∅;
 *     if reading = TRUE then
 *         reading := FALSE;
 *         trigger [ onar, ReadReturn | readval ];
 *     else
 *         trigger [ onar, WriteReturn ];
 *
 */
public class ReadImposeWriteAllAtomicRegisterComp extends ComponentDefinition {
    private final Negative<ONAtomicRegister> onar = provides(ONAtomicRegister.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);
    private final Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);

    private Set<NetAddress> correct;
    private Set<NetAddress> writeset;
    private Object readval;
    private boolean reading;
    private TimeValueTuple timeValueTuple;


    private final NetAddress self;
    private final Set<NetAddress> all;

    public ReadImposeWriteAllAtomicRegisterComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);

        this.correct = new HashSet<>(init.all);
        this.writeset = new HashSet<>();
        this.readval = null;
        this.reading = false;
        this.timeValueTuple = new TimeValueTuple(0, null);

        subscribe(crashHandler, pfd);
        subscribe(readHandler, onar);
        subscribe(writeHandler, onar);
        subscribe(writeDeliverHandler, beb);
        subscribe(ackDeliverHandler, pl);
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
            readval = timeValueTuple.value;
            trigger(new Broadcast(new WriteMessage(new TimeValueTuple(timeValueTuple.timestamp, timeValueTuple.value)), all, self), beb);
        }
    };

    private final Handler<Write> writeHandler = new Handler<Write>() {
        @Override
        public void handle(Write event) {
            trigger(new Broadcast(new WriteMessage(new TimeValueTuple(timeValueTuple.timestamp+1, timeValueTuple.value)), all, self), beb);
        }
    };

    private final ClassMatchedHandler<WriteMessage, BroadcastDeliver> writeDeliverHandler = new ClassMatchedHandler<WriteMessage, BroadcastDeliver>() {
        @Override
        public void handle(WriteMessage content, BroadcastDeliver context) {
            if (content.timeValueTuple.timestamp > timeValueTuple.timestamp) {
                timeValueTuple = content.timeValueTuple;
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
                trigger(new ReadReturn(readval), onar);
            } else {
                trigger(new WriteReturn(), onar);
            }
        }
    }

    private static class AckMessage implements KompicsEvent, Serializable {

    }


    private static class WriteMessage implements KompicsEvent, Serializable {
        public final TimeValueTuple timeValueTuple;

        public WriteMessage(TimeValueTuple timeValueTuple) {
            this.timeValueTuple = timeValueTuple;
        }
    }

    public static class Init extends se.sics.kompics.Init<ReadImposeWriteAllAtomicRegisterComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }


    }

}
