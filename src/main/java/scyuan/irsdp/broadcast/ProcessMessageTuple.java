package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;

public class ProcessMessageTuple {
    public final NetAddress p;
    public final KompicsEvent m;

    public ProcessMessageTuple(NetAddress p, KompicsEvent m) {
        this.p = p;
        this.m = m;
    }
}
