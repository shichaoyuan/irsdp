package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Restore implements KompicsEvent, Serializable {
    public final NetAddress p;

    public Restore(NetAddress p) {
        this.p = p;
    }
}
