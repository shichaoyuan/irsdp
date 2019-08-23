package scyuan.irsdp.sharedmemory;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class ReadReturn implements KompicsEvent, Serializable {
    public final Object val;

    public ReadReturn(Object val) {
        this.val = val;
    }
}
