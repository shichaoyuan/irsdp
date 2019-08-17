package scyuan.irsdp.failuredetector;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class HeartBeat implements KompicsEvent, Serializable {
    public final long epoch;

    public HeartBeat(long epoch) {
        this.epoch = epoch;
    }
}
