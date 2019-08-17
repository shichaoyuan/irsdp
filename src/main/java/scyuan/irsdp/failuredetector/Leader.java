package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Leader implements KompicsEvent, Serializable {

    public final NetAddress leader;

    public Leader(NetAddress leader) {
        this.leader = leader;
    }
}
