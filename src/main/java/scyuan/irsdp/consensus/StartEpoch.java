package scyuan.irsdp.consensus;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class StartEpoch implements KompicsEvent, Serializable {
    final long ts;
    final NetAddress l;

    public StartEpoch(long ts, NetAddress l) {
        this.ts = ts;
        this.l = l;
    }
}
