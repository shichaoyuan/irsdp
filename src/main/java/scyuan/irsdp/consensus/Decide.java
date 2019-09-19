package scyuan.irsdp.consensus;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Decide implements KompicsEvent, Serializable {
    final Value value;

    public Decide(Value value) {
        this.value = value;
    }
}
