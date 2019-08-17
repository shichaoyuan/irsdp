package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.Deliver;
import scyuan.irsdp.link.NetAddress;
import scyuan.irsdp.link.PerfectPointToPointLink;
import scyuan.irsdp.link.Send;
import se.sics.kompics.*;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * Algorithm 2.5: Exclude on Timeout
 * Implements:
 *     PerfectFailureDetector, instance P.
 * Uses:
 *     PerfectPointToPointLinks, instance pl.
 *
 * upon event < P, Init > do
 *     alive := Π;
 *     detected := ∅;
 *     starttimer(Δ);
 *
 * upon event <Timeout > do
 *     forall p ∈ Π do
 *         if (p !∈ alive) ∧ (p !∈ detected) then
 *             detected := detected ∪ {p};
 *             trigger < P, Crash | p >;
 *         trigger < pl, Send | p, [HEARTBEATREQUEST] > ;
 *     alive := ∅;
 *     starttimer(Δ);
 *
 * upon event < pl, Deliver | q, [HEARTBEATREQUEST] > do
 *     trigger < pl, Send | q, [HEARTBEATREPLY] >;
 *
 * upon event < pl, Deliver | p, [HEARTBEATREPLY] > do
 *     alive := alive ∪ {p};
 */
public class PerfectFailureDetectorComp extends ComponentDefinition {

    private final Negative<PerfectFailureDetector> pfd = provides(PerfectFailureDetector.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);
    private final Positive<Timer> timer = requires(Timer.class);

    private final Set<NetAddress> all;
    private final Set<NetAddress> alive;
    private final Set<NetAddress> detected;
    private final long delta;

    private int seq = 0;

    public PerfectFailureDetectorComp(Init init) {
        this.all = new HashSet<>(init.all);
        this.alive = new HashSet<>(init.all);
        this.detected = new HashSet<>();
        this.delta = init.delta;

        subscribe(startHandler, control);
        subscribe(timeoutHandler, timer);
        subscribe(heartBeatRequestHandler, pl);
        subscribe(heartBeatReplyHandler, pl);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            startTimer();
        }
    };


    Handler<CheckTimeout> timeoutHandler = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout event) {
            seq += 1;

            for (NetAddress p : all) {
                if (!alive.contains(p) && !detected.contains(p)) {
                    detected.add(p);
                    trigger(new Crash(p), pfd);
                }
                trigger(new Send(p, new HeartBeatRequest(seq)), pl);
            }
            alive.clear();
            startTimer();
        }
    };

    ClassMatchedHandler<HeartBeatRequest, Deliver> heartBeatRequestHandler = new ClassMatchedHandler<HeartBeatRequest, Deliver>() {
        @Override
        public void handle(HeartBeatRequest content, Deliver context) {
            trigger(new Send(context.src, new HeartBeatReply(content.seq)), pl);
        }
    };

    ClassMatchedHandler<HeartBeatReply, Deliver> heartBeatReplyHandler = new ClassMatchedHandler<HeartBeatReply, Deliver>() {
        @Override
        public void handle(HeartBeatReply content, Deliver context) {
            if (content.seq == seq) {
                alive.add(context.src);
            }
        }
    };

    private void startTimer() {
        ScheduleTimeout st = new ScheduleTimeout(delta);
        CheckTimeout timeout = new CheckTimeout(st);
        st.setTimeoutEvent(timeout);
        trigger(st, timer);
    }

    public static class CheckTimeout extends Timeout {
        public CheckTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

    public static class Init extends se.sics.kompics.Init<PerfectFailureDetectorComp> {
        public final NetAddress self;
        public final Set<NetAddress> all;
        public final long delta;

        public Init(NetAddress self, Set<NetAddress> all, long delta) {
            this.self = self;
            this.all = all;
            this.delta = delta;
        }
    }
}
