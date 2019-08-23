package scyuan.irsdp.sharedmemory;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Write implements KompicsEvent, Serializable {
    public Object value;

    public Write(Object value) {
        this.value = value;
    }
}
