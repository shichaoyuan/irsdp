package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.Deliver;
import scyuan.irsdp.link.NetAddress;
import scyuan.irsdp.link.PerfectPointToPointLink;
import scyuan.irsdp.link.Send;
import se.sics.kompics.*;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 *
 *
 * Algorithm 2.9: Elect Lower Epoch
 * Implements:
 *     EventualLeaderDetector, instance Ω.
 * Uses:
 *     FairLossPointToPointLinks, instance fll.
 *
 * upon event [ Ω, Init ] do
 *     epoch := 0;
 *     store(epoch);
 *     candidates := ∅;
 *     trigger [ Ω, Recovery ]; // recovery procedure completes the initialization
 *
 * upon event 【 Ω, Recovery ] do
 *     leader := maxrank(Π);
 *     trigger  Ω, Trust | leader ;
 *     delay := Δ;
 *     retrieve(epoch);
 *     epoch := epoch + 1;
 *     store(epoch);
 *     forall p ∈ Π do
 *         trigger [ fll, Send | p, [HEARTBEAT, epoch] ];
 *     candidates := ∅;
 *     starttimer(delay);
 *
 * upon event [ Timeout ] do
 *     newleader := select(candidates);
 *     if newleader != leader then
 *         delay := delay + Δ;
 *         leader := newleader;
 *         trigger [ Ω, Trust | leader ];
 *     forall p ∈ Π do
 *         trigger [ fll, Send | p, [HEARTBEAT, epoch] ];
 *     candidates := ∅;
 *     starttimer(delay);
 *
 * upon event [ fll, Deliver | q, [HEARTBEAT, ep] ] do
 *     if exists (s, e) ∈ candidates such that s = q ∧ e < ep then
 *         candidates := candidates \ {(q, e)};
 *     candidates := candidates ∪ (q, ep);
 *
 */
public class ElectLowerEpochEventualLeaderDetectorComp extends ComponentDefinition {
    private final Negative<EventualLeaderDetector> eld = provides(EventualLeaderDetector.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);
    private final Positive<Timer> timer = requires(Timer.class);

    private final Set<NetAddress> all;
    private final Path storeDir;
    private final long delta;

    private Map<NetAddress, Long> candidates;
    private NetAddress leader;

    private long epoch;
    private long delay;

    public ElectLowerEpochEventualLeaderDetectorComp(Init init) {
        this.all = new HashSet<>(init.all);
        this.storeDir = init.storeDir;
        this.delta = init.delta;
        this.epoch = 0L;
        this.candidates = new HashMap<>();

        subscribe(startHandler, control);
        subscribe(timeoutHandler, timer);
        subscribe(heartBeatHandler, pl);
    }

    private final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            leader = maxrank(all);
            trigger(new Trust(leader), eld);
            delay = delta;
            retrieveEpoch();
            epoch += 1;
            storeEpoch();
            for (NetAddress p : all) {
                trigger(new Send(p, new HeartBeat(epoch)), pl);
            }
            candidates.clear();
            startTimer(delay);
        }
    };

    private final Handler<EleTimeout> timeoutHandler = new Handler<EleTimeout>() {
        @Override
        public void handle(EleTimeout event) {
            NetAddress newLeader = select();
            if (!Objects.equals(newLeader, leader)) {
                delay += delta;
                leader = newLeader;
                trigger(new Trust(leader), eld);
            }
            for (NetAddress p : all) {
                trigger(new Send(p, new HeartBeat(epoch)), pl);
            }
            candidates.clear();
            startTimer(delay);
        }
    };

    private final ClassMatchedHandler<HeartBeat, Deliver> heartBeatHandler = new ClassMatchedHandler<HeartBeat, Deliver>() {
        @Override
        public void handle(HeartBeat content, Deliver context) {
            if (candidates.containsKey(context.src)) {
                long e = candidates.get(context.src);
                if (e < content.epoch) {
                    candidates.remove(context.src);
                    candidates.put(context.src, content.epoch);
                }
            } else {
                candidates.put(context.src, content.epoch);
            }
        }
    };

    private void startTimer(long delay) {
        ScheduleTimeout st = new ScheduleTimeout(delay);
        st.setTimeoutEvent(new EleTimeout(st));
        trigger(st, timer);
    }

    private static class EleTimeout extends se.sics.kompics.timer.Timeout {
        public EleTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

    private NetAddress select() {
        if (candidates.isEmpty()) {
            return null;
        }

        long lowEpoch = Collections.min(candidates.values());
        Set<NetAddress> set = new HashSet<>();
        for (Map.Entry<NetAddress, Long> entry : candidates.entrySet()) {
            if (lowEpoch == entry.getValue()) {
                set.add(entry.getKey());
            }
        }
        return maxrank(set);
    }

    private NetAddress maxrank(Set<NetAddress> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        return Collections.max(candidates);
    }

    private void storeEpoch() {
        try {
            Files.write(storeDir.resolve("epoch"), String.valueOf(epoch).getBytes(),
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void retrieveEpoch() {
        try {
            String str = Files.readString(storeDir.resolve("epoch"));
            if (str == null || str.length() == 0) {
                this.epoch = 0;
            } else {
                this.epoch = Long.parseLong(str);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Init extends se.sics.kompics.Init<ElectLowerEpochEventualLeaderDetectorComp> {
        private final Set<NetAddress> all;
        private final NetAddress self;
        private final long delta;
        private final Path storeDir;

        public Init(Set<NetAddress> all, NetAddress self, long delta, Path storeDir) {
            this.all = all;
            this.self = self;
            this.delta = delta;
            this.storeDir = storeDir;
        }
    }
}
