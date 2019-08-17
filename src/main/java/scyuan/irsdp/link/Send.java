package scyuan.irsdp.link;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Send implements KompicsEvent, Serializable {

    public final NetAddress dest;
    public final KompicsEvent payload;

    public Send(NetAddress dest, KompicsEvent payload) {
        this.dest = dest;
        this.payload = payload;
    }
}
