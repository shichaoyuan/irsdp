package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.NetAddress;
import scyuan.irsdp.link.PerfectPointToPointLink;
import scyuan.irsdp.link.PerfectPointToPointLinkComp;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class EpfdNode extends ComponentDefinition {
    public static final int EPFD_NODES = 3;
    public static final String NODE_ADDR_PREFIX = "192.168.0.";
    public static final int NODE_PORT = 5000;

    private final Positive<Network> net = requires(Network.class);
    private final Positive<Timer> timer = requires(Timer.class);

    private Component epfd;
    private Component epfdClient;
    private Component pl;

    private NetAddress self;
    private long timeout = 200;

    public EpfdNode(Init init) {
        this.self = init.self;
        Set<NetAddress> all = new HashSet<>();
        for (int i = 1; i < EPFD_NODES+1; i++) {
            try {
                NetAddress addr= new NetAddress(InetAddress.getByName(NODE_ADDR_PREFIX + i), NODE_PORT);
                all.add(addr);
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        }

        all.remove(self);

        this.epfd = create(EventuallyPerfectFailureDetectorComp.class, new EventuallyPerfectFailureDetectorComp.Init(all, self, timeout));
        this.pl = create(PerfectPointToPointLinkComp.class, new PerfectPointToPointLinkComp.Init(self));
        this.epfdClient = create(EpfdScenarioClient.class, new EpfdScenarioClient.Init(self));

        connect(epfd.getPositive(EventuallyPerfectFailureDetector.class), epfdClient.getNegative(EventuallyPerfectFailureDetector.class), Channel.TWO_WAY);
        connect(timer, epfd.getNegative(Timer.class), Channel.TWO_WAY);
        connect(pl.getPositive(PerfectPointToPointLink.class), epfd.getNegative(PerfectPointToPointLink.class), Channel.TWO_WAY);
        connect(net, pl.getNegative(Network.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<EpfdNode> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }
}
