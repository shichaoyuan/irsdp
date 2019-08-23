package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

import java.io.Serializable;
import java.util.Objects;

public class FIFOReliableBroadcastMessage implements KompicsEvent, Serializable, PatternExtractor<Class<Object>, KompicsEvent> {
    public final NetAddress src;
    public final KompicsEvent payload;
    public final long sn;

    public FIFOReliableBroadcastMessage(NetAddress src, KompicsEvent payload, long sn) {
        this.src = src;
        this.payload = payload;
        this.sn = sn;
    }

    @Override
    public Class<Object> extractPattern() {
        Class c = payload.getClass();
        return (Class<Object>) c;
    }

    @Override
    public KompicsEvent extractValue() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FIFOReliableBroadcastMessage that = (FIFOReliableBroadcastMessage) o;
        return sn == that.sn &&
                src.equals(that.src) &&
                payload.equals(that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, payload, sn);
    }
}
