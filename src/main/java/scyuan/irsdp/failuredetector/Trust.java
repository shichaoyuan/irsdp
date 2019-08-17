package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Trust implements KompicsEvent, Serializable {
    public final NetAddress p;

    public Trust(NetAddress p) {
        this.p = p;
    }
}
