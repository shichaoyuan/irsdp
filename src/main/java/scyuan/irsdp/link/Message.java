package scyuan.irsdp.link;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.network.Transport;

public class Message extends NetMessage implements PatternExtractor<Class<Object>, KompicsEvent> {

    public final KompicsEvent payload;

    public Message(NetAddress src, NetAddress dst, KompicsEvent payload) {
        super(src, dst, Transport.TCP);
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Message) {
            Message that = (Message) o;
            return Objects.equal(this.header, that.header) && Objects.equal(this.payload, that.payload);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + java.util.Objects.hashCode(this.header);
        hash = 47 * hash + java.util.Objects.hashCode(this.payload);
        return hash;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("payload", this.payload)
                .toString();
    }
}
