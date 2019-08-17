package scyuan.irsdp.link;

import se.sics.kompics.network.Msg;
import se.sics.kompics.network.Transport;

import java.io.Serializable;

public class NetMessage implements Serializable, Msg<NetAddress, NetHeader> {

    public final NetHeader header;

    public NetMessage(NetAddress src, NetAddress dst, Transport protocol) {
        this.header = new NetHeader(src, dst, protocol);
    }


    @Override
    public NetHeader getHeader() {
        return this.header;
    }

    @Override
    public NetAddress getSource() {
        return this.header.src;
    }

    @Override
    public NetAddress getDestination() {
        return this.header.dst;
    }

    @Override
    public Transport getProtocol() {
        return this.header.proto;
    }
}
