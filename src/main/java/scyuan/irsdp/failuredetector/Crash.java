package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Crash implements KompicsEvent, Serializable {
    public final NetAddress p;

    public Crash(NetAddress p) {
        this.p = p;
    }
}
