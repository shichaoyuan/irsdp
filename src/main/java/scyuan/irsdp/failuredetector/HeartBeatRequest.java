package scyuan.irsdp.failuredetector;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class HeartBeatRequest implements KompicsEvent, Serializable {
    public final int seq;

    public HeartBeatRequest(int seq) {
        this.seq = seq;
    }
}
