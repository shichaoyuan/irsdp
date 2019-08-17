package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class ReliableBroadcastMessage implements KompicsEvent, Serializable, PatternExtractor<Class<Object>, KompicsEvent> {
    public final KompicsEvent payload;
    public final Set<NetAddress> nodes;

    public ReliableBroadcastMessage(KompicsEvent payload, Set<NetAddress> nodes) {
        this.payload = payload;
        this.nodes = nodes;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReliableBroadcastMessage that = (ReliableBroadcastMessage) o;
        return payload.equals(that.payload) &&
                nodes.equals(that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload, nodes);
    }
}
