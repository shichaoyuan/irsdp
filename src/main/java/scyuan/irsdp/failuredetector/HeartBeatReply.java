package scyuan.irsdp.failuredetector;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class HeartBeatReply implements KompicsEvent, Serializable {
    public final int seq;

    public HeartBeatReply(int seq) {
        this.seq = seq;
    }
}
