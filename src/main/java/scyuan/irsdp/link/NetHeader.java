package scyuan.irsdp.link;

import com.google.common.base.MoreObjects;
import se.sics.kompics.network.Header;
import se.sics.kompics.network.Transport;

import java.io.Serializable;

public class NetHeader implements Serializable, Header<NetAddress> {

    public final NetAddress src;
    public final NetAddress dst;
    public final Transport proto;

    public NetHeader(NetAddress src, NetAddress dst, Transport proto) {
        this.src = src;
        this.dst = dst;
        this.proto = proto;
    }

    @Override
    public NetAddress getSource() {
        return src;
    }

    @Override
    public NetAddress getDestination() {
        return dst;
    }

    @Override
    public Transport getProtocol() {
        return proto;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + java.util.Objects.hashCode(this.src);
        hash = 19 * hash + java.util.Objects.hashCode(this.dst);
        hash = 19 * hash + java.util.Objects.hashCode(this.proto);
        return hash;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("src", this.src)
                .add("dst", this.dst)
                .add("proto", this.proto)
                .toString();
    }
}
