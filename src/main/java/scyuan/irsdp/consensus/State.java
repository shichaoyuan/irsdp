package scyuan.irsdp.consensus;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class State implements KompicsEvent, Serializable, Comparable<State> {
    public final long valts;
    public final Value val;

    public State(long valts, Value val) {
        this.valts = valts;
        this.val = val;
    }

    @Override
    public int compareTo(State o) {
        return Long.compare(valts, o.valts);
    }
}
