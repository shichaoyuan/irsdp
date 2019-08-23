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

import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * Algorithm 4.1: Read-One Write-All
 * Implements:
 *     (1, N)-RegularRegister, instance onrr.
 *
 * Uses:
 *     BestEffortBroadcast, instance beb;
 *     PerfectPointToPointLinks, instance pl;
 *     PerfectFailureDetector, instance P.
 *
 * upon event [ onrr, Init ] do
 *     val := ⊥;
 *     correct := Π;
 *     writeset := ∅;
 *
 * upon event [ P, Crash | p ] do
 *     correct := correct \ {p};
 *
 * upon event [ onrr, Read ] do
 *     trigger [ onrr, ReadReturn | val ];
 *
 * upon event [ onrr, Write | v ] do
 *     trigger [ beb, Broadcast | [WRITE, v] ];
 *
 * upon event [ beb, Deliver | q, [WRITE, v] ] do
 *     val := v;
 *     trigger [ pl, Send | q, ACK ];
 *
 * upon event [ pl, Deliver | p, ACK ] do
 *     writeset := writeset ∪ {p};
 *
 * upon correct ⊆ writeset do
 *     writeset := ∅;
 *     trigger [ onrr, WriteReturn ];
 */
public class ReadOneWriteAllRegularRegisterComp extends ComponentDefinition {
    private final Negative<ONRegularRegister> onrr = provides(ONRegularRegister.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);
    private final Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);

    private Object val;
    private Set<NetAddress> correct;
    private Set<NetAddress> writeset;

    private final NetAddress self;
    private final Set<NetAddress> all;

    public ReadOneWriteAllRegularRegisterComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);

        this.correct = new HashSet<>(init.all);
        this.writeset = new HashSet<>();

        subscribe(crashHandler, pfd);
        subscribe(readHandler, onrr);
        subscribe(writeHandler, onrr);
        subscribe(writeDeliverHandler, beb);
        subscribe(ackDeliverHandler, pl);
    }

    private final Handler<Crash> crashHandler = new Handler<Crash>() {
        @Override
        public void handle(Crash event) {
            correct.remove(event.p);
            checkWriteReturn();
        }
    };

    private final Handler<Read> readHandler = new Handler<Read>() {
        @Override
        public void handle(Read event) {
            trigger(new ReadReturn(val), onrr);
        }
    };

    private final Handler<Write> writeHandler = new Handler<Write>() {
        @Override
        public void handle(Write event) {
            trigger(new Broadcast(event, all, self), beb);
        }
    };

    private final ClassMatchedHandler<Write, BroadcastDeliver> writeDeliverHandler = new ClassMatchedHandler<Write, BroadcastDeliver>() {
        @Override
        public void handle(Write content, BroadcastDeliver context) {
            val = content.value;
            trigger(new Send(context.src, new AckMessage()), pl);
        }
    };

    private final ClassMatchedHandler<AckMessage, Deliver> ackDeliverHandler = new ClassMatchedHandler<AckMessage, Deliver>() {
        @Override
        public void handle(AckMessage content, Deliver context) {
            writeset.add(context.src);
            checkWriteReturn();
        }
    };

    private void checkWriteReturn() {
        if (writeset.containsAll(all)) {
            writeset.clear();
            trigger(new WriteReturn(), onrr);
        }
    }

    public static class Init extends se.sics.kompics.Init<ReadOneWriteAllRegularRegisterComp> {
        private final NetAddress self;
        private final Set<NetAddress> all;

        public Init(NetAddress self, Set<NetAddress> all) {
            this.self = self;
            this.all = all;
        }
    }

}
