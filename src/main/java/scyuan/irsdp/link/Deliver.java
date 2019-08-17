package scyuan.irsdp.link;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

import java.io.Serializable;

public class Deliver implements KompicsEvent, Serializable, PatternExtractor<Class<Object>, KompicsEvent> {

    public final NetAddress src;
    public final KompicsEvent payload;

    public Deliver(NetAddress src, KompicsEvent payload) {
        this.src = src;
        this.payload = payload;
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
