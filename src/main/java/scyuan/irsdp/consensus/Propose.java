package scyuan.irsdp.consensus;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Propose implements KompicsEvent, Serializable {
    final Value value;

    public Propose(Value value) {
        this.value = value;
    }
}
