package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 *
 * Algorithm 2.8: Monarchical Eventual Leader Detection
 * Implements:
 *     EventualLeaderDetector, instance Ω.
 *
 * Uses:
 *     EventuallyPerfectFailureDetector, instance ✸P.
 *
 * upon event [ Ω, Init ] do
 *     suspected := ∅;
 *     leader := ⊥;
 *
 * upon event [ ✸P, Suspect | p ] do
 *     suspected := suspected ∪ {p};
 *
 * upon event [ ✸P, Restore | p ] do
 *     suspected := suspected \ {p};
 *
 * upon leader != maxrank(Π \ suspected) do
 *     leader := maxrank(Π \ suspected);
 *     trigger [ Ω, Trust | leader ];
 *
 */
public class MonarchicalEventualLeaderDetectionComp extends ComponentDefinition {
    private final Negative<EventualLeaderDetector> eld = provides(EventualLeaderDetector.class);
    private final Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);

    private final Set<NetAddress> all;

    private Set<NetAddress> suspected;
    private NetAddress leader;

    public MonarchicalEventualLeaderDetectionComp(Init init) {
        this.all = init.all;
        this.suspected = new HashSet<>();
        this.leader = null;

        subscribe(startHandler, control);
        subscribe(suspectHandler, epfd);
        subscribe(suspectHandler, epfd);
    }

    private final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            leader = maxrank(getCandidates(all, suspected));

        }
    };

    private final Handler<Suspect> suspectHandler = new Handler<Suspect>() {
        @Override
        public void handle(Suspect event) {
            suspected.add(event.p);
            checkLeader();
        }
    };

    private final Handler<Restore> restoreHandler = new Handler<Restore>() {
        @Override
        public void handle(Restore event) {
            suspected.remove(event.p);
            checkLeader();
        }
    };

    private void checkLeader() {
        NetAddress newLeader = maxrank(getCandidates(all, suspected));
        if (!Objects.equals(newLeader, leader)) {
            leader = newLeader;
            trigger(new Leader(leader), eld);
        }
    }

    private Set<NetAddress> getCandidates(Set<NetAddress> all, Set<NetAddress> suspected) {
        Set<NetAddress> candidates = new HashSet<>();
        for (NetAddress p : all) {
            if (!suspected.contains(p)) {
                candidates.add(p);
            }
        }
        return candidates;
    }

    private NetAddress maxrank(Set<NetAddress> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        return Collections.max(candidates);
    }

    public static class Init extends se.sics.kompics.Init {
        private final Set<NetAddress> all;
        private final NetAddress self;

        public Init(Set<NetAddress> all, NetAddress self) {
            this.all = all;
            this.self = self;
        }
    }
}
