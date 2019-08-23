package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WaitingCausalBroadcastMessage implements KompicsEvent, Serializable, PatternExtractor<Class<Object>, KompicsEvent> {
    public final NetAddress src;
    public final KompicsEvent payload;
    public final long[] vectorClock;

    public WaitingCausalBroadcastMessage(NetAddress src, KompicsEvent payload, long[] vectorClock) {
        this.src = src;
        this.payload = payload;
        this.vectorClock = vectorClock;
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
        WaitingCausalBroadcastMessage that = (WaitingCausalBroadcastMessage) o;
        return src.equals(that.src) &&
                payload.equals(that.payload) &&
                Arrays.equals(vectorClock, that.vectorClock);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(src, payload);
        result = 31 * result + Arrays.hashCode(vectorClock);
        return result;
    }
}
