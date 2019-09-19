package scyuan.irsdp.consensus;

import scyuan.irsdp.broadcast.BestEffortBroadcast;
import scyuan.irsdp.broadcast.Broadcast;
import scyuan.irsdp.broadcast.BroadcastDeliver;
import scyuan.irsdp.link.Deliver;
import scyuan.irsdp.link.NetAddress;
import scyuan.irsdp.link.PerfectPointToPointLink;
import scyuan.irsdp.link.Send;
import se.sics.kompics.*;

import java.io.Serializable;
import java.util.*;


/**
 *
 *
 * Algorithm 5.6: Read/Write Epoch Consensus
 * Implements:
 *     EpochConsensus, instance ep, with timestamp ets and leader l.
 * Uses:
 *     PerfectPointToPointLinks, instance pl;
 *     BestEffortBroadcast, instance beb.
 *
 * upon event [ ep, Init | state ] do
 *     (valts, val) := state;
 *     tmpval := ⊥;
 *     states := [⊥]N;
 *     accepted := 0;
 *
 * upon event [ ep, Propose | v ] do // only leader l
 *     tmpval := v;
 *     trigger [ beb, Broadcast | [READ] ];
 *
 * upon event [ beb, Deliver | l, [READ] ] do
 *     trigger [ pl, Send | l, [STATE, valts, val] ];
 *
 * upon event [ pl, Deliver | q, [STATE, ts, v] ] do // only leader 
 *     states[q] := (ts, v);
 *
 * upon #(states) > N/2 do // only leader 
 *     (ts, v) := highest(states);
 *     if v != ⊥ then
 *         tmpval := v;
 *     states := [⊥]N;
 *     trigger [ beb, Broadcast | [WRITE, tmpval] ];
 *
 * upon event [ beb, Deliver | l, [WRITE, v] ] do
 *     (valts, val) := (ets, v);
 *     trigger [ pl, Send | l, [ACCEPT] ];
 *
 * upon event [ pl, Deliver | q, [ACCEPT] ] do // only leader 
 *     accepted := accepted + 1;
 *
 * upon accepted > N/2 do // only leader 
 *     accepted := 0;
 *     trigger [ beb, Broadcast | [DECIDED, tmpval] ];
 *
 * upon event [ beb, Deliver | l, [DECIDED, v] ] do
 *     trigger [ ep, Decide | v ];
 *
 * upon event [ ep, Abort ] do
 *     trigger [ ep, Aborted | (valts, val) ];
 *     halt; // stop operating when aborted
 *
 */
public class ReadWriteEpochConsensusComp extends ComponentDefinition {
    private final Negative<EpochConsensus> ep = provides(EpochConsensus.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);
    private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);

    private final NetAddress self;
    private final Set<NetAddress> all;

    private State state;
    private Value tmpval;
    private Map<NetAddress, State> states;
    private long accepted;
    private int N;
    private long ets;

    public ReadWriteEpochConsensusComp(Init init) {
        this.self = init.self;
        this.all = new HashSet<>(init.all);
        this.state = init.state;
        this.tmpval = null;
        this.states = new HashMap<>();
        this.accepted = 0;
        this.N = all.size();
        this.ets = init.ets;

        subscribe(proposeHandler, ep);
        subscribe(readDeliverHandler, beb);
        subscribe(stateDeliverHandler, pl);
        subscribe(writeDeliverHandler, beb);
        subscribe(acceptDeliverHandler, pl);
        subscribe(decidedDeliverHandler, beb);
        subscribe(abortHandler, ep);
    }

    // leader
    private final Handler<Propose> proposeHandler = new Handler<Propose>() {
        @Override
        public void handle(Propose event) {
            tmpval = event.value;
            trigger(new Broadcast(new Read(), all, self), beb);
        }
    };

    private final ClassMatchedHandler<Read, BroadcastDeliver> readDeliverHandler = new ClassMatchedHandler<>() {
        @Override
        public void handle(Read content, BroadcastDeliver context) {
            trigger(new Send(context.src, state), pl);
        }
    };

    private final ClassMatchedHandler<State, Deliver> stateDeliverHandler = new ClassMatchedHandler<State, Deliver>() {
        @Override
        public void handle(State content, Deliver context) {
            states.put(context.src, content);
            checkStates();
        }
    };

    private void checkStates() {
        if (states.size() > N/2) {
            State s = Collections.max(states.values());
            if (s.val != null) {
                tmpval = s.val;
            }
            states = new HashMap<>();
            trigger(new Broadcast(new Write(tmpval), all, self), beb);
        }
    }

    private final ClassMatchedHandler<Write, BroadcastDeliver> writeDeliverHandler = new ClassMatchedHandler<Write, BroadcastDeliver>() {
        @Override
        public void handle(Write content, BroadcastDeliver context) {
            state = new State(ets, content.v);
            trigger(new Send(context.src, new Accept()), pl);
        }
    };

    private final ClassMatchedHandler<Accept, Deliver> acceptDeliverHandler = new ClassMatchedHandler<Accept, Deliver>() {
        @Override
        public void handle(Accept content, Deliver context) {
            accepted += 1;
            if (accepted > N/2) {
                accepted = 0;
                trigger(new Broadcast(new Decided(tmpval), all, self), beb);
            }
        }
    };

    private final ClassMatchedHandler<Decided, BroadcastDeliver> decidedDeliverHandler = new ClassMatchedHandler<Decided, BroadcastDeliver>() {
        @Override
        public void handle(Decided content, BroadcastDeliver context) {
            trigger(new Decide(content.v), ep);
        }
    };

    private final Handler<Abort> abortHandler = new Handler<Abort>() {
        @Override
        public void handle(Abort event) {
            trigger(new Aborted(state), ep);
            suicide();
        }
    };

    private static class Decided implements KompicsEvent, Serializable {
        final Value v;

        public Decided(Value v) {
            this.v = v;
        }
    }


    private static class Write implements KompicsEvent, Serializable {
        final Value v;

        public Write(Value v) {
            this.v = v;
        }
    }

    private static class Read implements KompicsEvent, Serializable {
    }

    private static class Accept implements KompicsEvent, Serializable {
    }

    private static class Init extends se.sics.kompics.Init<ReadWriteEpochConsensusComp> {
        final NetAddress self;
        final Set<NetAddress> all;
        final State state;
        final long ets;

        public Init(NetAddress self, Set<NetAddress> all, State state, long ets) {
            this.self = self;
            this.all = all;
            this.state = state;
            this.ets = ets;
        }
    }


}
