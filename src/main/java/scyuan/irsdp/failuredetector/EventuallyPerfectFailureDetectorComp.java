package scyuan.irsdp.failuredetector;

import com.google.common.collect.Sets;
import scyuan.irsdp.link.Deliver;
import scyuan.irsdp.link.NetAddress;
import scyuan.irsdp.link.PerfectPointToPointLink;
import scyuan.irsdp.link.Send;
import se.sics.kompics.*;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * partially synchronous system
 *
 *
 * Algorithm 2.7: Increasing Timeout
 * Implements:
 *     EventuallyPerfectFailureDetector, instance ✸P.
 *
 * Uses:
 *     PerfectPointToPointLinks, instance pl.
 *
 * upon event [ ✸P, Init ] do
 *     alive := Π;
 *     suspected := ∅;
 *     delay := Δ;
 *     starttimer(delay);
 *
 * upon event [ Timeout ] do
 *     if alive ∩ suspected != ∅ then
 *         delay := delay + Δ;
 *     forall p ∈ Π do
 *         if (p !∈ alive) ∧ (p !∈ suspected) then
 *             suspected := suspected ∪ {p};
 *             trigger [ ✸P, Suspect | p ];
 *         else if (p ∈ alive) ∧ (p ∈ suspected) then
 *             suspected := suspected \ {p};
 *             trigger [ ✸P, Restore | p ];
 *         trigger [ pl, Send | p, [HEARTBEATREQUEST] ];
 *     alive := ∅;
 *     starttimer(delay);
 *
 * upon event [ pl, Deliver | q, [HEARTBEATREQUEST] ] do
 *     trigger [ pl, Send | q, [HEARTBEATREPLY] ];
 *
 * upon event [ pl, Deliver | p, [HEARTBEATREPLY] ] do
 *     alive := alive ∪ {p};
 *
 */
public class EventuallyPerfectFailureDetectorComp extends ComponentDefinition {

    private final Positive<Timer> timer = requires(Timer.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);
    private final Negative<EventuallyPerfectFailureDetector> epfd = provides(EventuallyPerfectFailureDetector.class);

    private Set<NetAddress> alive;
    private Set<NetAddress> suspected;
    private long delay;

    private int seq = 0;

    private final Set<NetAddress> all;
    private final long delta;

    public EventuallyPerfectFailureDetectorComp(Init init) {
        this.all = new HashSet<>(init.all);
        this.delta = init.delta;

        this.alive = new HashSet<>(init.all);
        this.suspected = new HashSet<>();
        this.delay = init.delta;

        subscribe(startHandler, control);
        subscribe(timeoutHandler, timer);
        subscribe(heartBeatReplyHandler, pl);
        subscribe(heartBeatRequestHandler, pl);
    }

    private final Handler<Start> startHandler = new Handler<>() {
        @Override
        public void handle(Start event) {
            startTimer(delay);
        }
    };

    private final Handler<EpfdTimeout> timeoutHandler = new Handler<>() {
        @Override
        public void handle(EpfdTimeout event) {
            if (!Sets.intersection(alive, suspected).isEmpty()) {
                delay += delta;
            }

            seq += 1;

            for (NetAddress p : all) {
                if (!alive.contains(p) && !suspected.contains(p)) {
                    suspected.add(p);
                    trigger(new Suspect(p), epfd);
                } else if (alive.contains(p) && suspected.contains(p)) {
                    suspected.remove(p);
                    trigger(new Restore(p), epfd);
                }
                trigger(new Send(p, new HeartBeatRequest(seq)), pl);
            }

            alive.clear();
            startTimer(delay);
        }
    };

    private final ClassMatchedHandler<HeartBeatRequest, Deliver> heartBeatRequestHandler = new ClassMatchedHandler<>() {
        @Override
        public void handle(HeartBeatRequest content, Deliver context) {
            trigger(new Send(context.src, new HeartBeatReply(content.seq)), pl);
        }
    };

    private final ClassMatchedHandler<HeartBeatReply, Deliver> heartBeatReplyHandler = new ClassMatchedHandler<>() {
        @Override
        public void handle(HeartBeatReply content, Deliver context) {
            if (content.seq == seq || suspected.contains(context.src)) {
                alive.add(context.src);
            }
        }
    };

    private void startTimer(long delay) {
        ScheduleTimeout st = new ScheduleTimeout(delay);
        st.setTimeoutEvent(new EpfdTimeout(st));
        trigger(st, timer);
    }

    private static class EpfdTimeout extends se.sics.kompics.timer.Timeout {
        public EpfdTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

    public static class Init extends se.sics.kompics.Init<EventuallyPerfectFailureDetectorComp> {
        private final Set<NetAddress> all;
        private final NetAddress self;
        private final long delta;

        public Init(Set<NetAddress> all, NetAddress self, long delta) {
            this.all = all;
            this.self = self;
            this.delta = delta;
        }
    }
}
