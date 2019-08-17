package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Suspect implements KompicsEvent, Serializable {
    public final NetAddress p;

    public Suspect(NetAddress p) {
        this.p = p;
    }
}
