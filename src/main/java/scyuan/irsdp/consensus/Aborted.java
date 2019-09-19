package scyuan.irsdp.consensus;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Aborted implements KompicsEvent, Serializable {
    public final State state;

    public Aborted(State state) {
        this.state = state;
    }
}
