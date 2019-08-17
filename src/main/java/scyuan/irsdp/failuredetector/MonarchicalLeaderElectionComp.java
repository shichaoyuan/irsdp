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
 * Algorithm 2.6: Monarchical Leader Election
 * Implements:
 *     LeaderElection, instance le.
 * Uses:
 *     PerfectFailureDetector, instance P.
 *
 * upon event < le, Init > do
 *     suspected := ∅;
 *     leader := ⊥;
 *
 * upon event < P, Crash | p > do
 *     suspected := suspected ∪ {p};
 *
 * upon leader != maxrank(Π \ suspected) do
 *     leader := maxrank(Π \ suspected);
 *     trigger < le, Leader | leader > ;
 *
 */
public class MonarchicalLeaderElectionComp extends ComponentDefinition {

    private final Negative<LeaderElection> le = provides(LeaderElection.class);
    private final Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);

    private final Set<NetAddress> all;

    private Set<NetAddress> suspected;
    private NetAddress leader;

    public MonarchicalLeaderElectionComp(Init init) {
        this.all = new HashSet<>(init.all);
        this.suspected = new HashSet<>();
        this.leader = null;

        subscribe(startHandler, control);
        subscribe(crashHandler, pfd);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            leader = maxrank(getCandidates(all, suspected));
        }
    };


    Handler<Crash> crashHandler = new Handler<Crash>() {
        @Override
        public void handle(Crash event) {
            suspected.add(event.p);
            NetAddress newLeader = maxrank(getCandidates(all, suspected));
            if (!Objects.equals(leader, newLeader)) {
                leader = newLeader;
                trigger(new Leader(newLeader), le);
            }
        }
    };

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
