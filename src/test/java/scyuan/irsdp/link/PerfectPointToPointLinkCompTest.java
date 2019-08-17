package scyuan.irsdp.link;

import org.junit.Test;
import se.sics.kompics.Component;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.testing.Direction;
import se.sics.kompics.testing.TestContext;

import java.net.InetAddress;

import static org.junit.Assert.*;

public class PerfectPointToPointLinkCompTest {

    @Test
    public void test() {
        NetAddress src = new NetAddress(InetAddress.getLoopbackAddress(), 12346);
        NetAddress dst = new NetAddress(InetAddress.getLoopbackAddress(), 12347);

        TestContext<PerfectPointToPointLinkComp> tc = TestContext.newInstance(PerfectPointToPointLinkComp.class, new PerfectPointToPointLinkComp.Init(src));
        Component comp = tc.getComponentUnderTest();
        Negative<Network> net = comp.getNegative(Network.class);
        Positive<PerfectPointToPointLink> pl = comp.getPositive(PerfectPointToPointLink.class);

        TestEvent event = new TestEvent();
        Send send = new Send(dst, event);

        tc.body()
                .trigger(send, pl)
                .expect(Message.class, (Message m) -> isValid(m, src, dst), net, Direction.OUT);

        assertTrue(tc.check());

    }

    private boolean isValid(Message m, NetAddress src, NetAddress dst) {
        boolean isDeliver = m.payload instanceof Deliver;
        return (isDeliver && m.header.src.equals(src) && m.header.dst.equals(dst));
    }

}