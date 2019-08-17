package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.Set;

public class Broadcast implements KompicsEvent, Serializable {
    public final KompicsEvent payload;
    public final Set<NetAddress> nodes;
    public final NetAddress src;

    public Broadcast(KompicsEvent payload, Set<NetAddress> nodes, NetAddress src) {
        this.payload = payload;
        this.nodes = nodes;
        this.src = src;
    }
}
