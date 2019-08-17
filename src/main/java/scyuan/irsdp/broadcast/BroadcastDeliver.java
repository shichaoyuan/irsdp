package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

import java.io.Serializable;
import java.util.Objects;

public class BroadcastDeliver implements KompicsEvent, Serializable, PatternExtractor<Class<Object>, KompicsEvent> {
    public final KompicsEvent payload;
    public final NetAddress src;

    public BroadcastDeliver(KompicsEvent payload, NetAddress src) {
        this.payload = payload;
        this.src = src;
    }

    @Override
    public Class<Object> extractPattern() {
        Class c = payload.getClass();
        return (Class<Object>) c;
    }

    @Override
    public KompicsEvent extractValue() {
        return payload;
    }
}
