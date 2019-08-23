package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class CausalOrderReliableBroadcastMessage implements KompicsEvent, Serializable, PatternExtractor<Class<Object>, KompicsEvent> {
    public final NetAddress src;
    public final KompicsEvent payload;
    public final List<ProcessMessageTuple> mpast;

    public CausalOrderReliableBroadcastMessage(NetAddress src, KompicsEvent payload, List<ProcessMessageTuple> mpast) {
        this.src = src;
        this.payload = payload;
        this.mpast = mpast;
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
        CausalOrderReliableBroadcastMessage that = (CausalOrderReliableBroadcastMessage) o;
        return src.equals(that.src) &&
                payload.equals(that.payload) &&
                mpast.equals(that.mpast);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, payload, mpast);
    }
}
