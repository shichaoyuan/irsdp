package scyuan.irsdp.link;

import se.sics.kompics.*;
import se.sics.kompics.network.Network;

public class PerfectPointToPointLinkComp extends ComponentDefinition {

    private final Negative<PerfectPointToPointLink> pl = provides(PerfectPointToPointLink.class);
    private final Positive<Network> net = requires(Network.class);

    private NetAddress self;

    public PerfectPointToPointLinkComp(Init init) {
        this.self = init.self;
        subscribe(sendHandler, pl);
        subscribe(deliverHandler, net);
    }

    Handler<Send> sendHandler = new Handler<Send>() {
        @Override
        public void handle(Send event) {
            trigger(new Message(self, event.dest, new Deliver(self, event.payload)), net);
        }
    };

    ClassMatchedHandler<Deliver, Message> deliverHandler = new ClassMatchedHandler<Deliver, Message>() {
        @Override
        public void handle(Deliver content, Message context) {
            trigger(content, pl);
        }
    };

    public static class Init extends se.sics.kompics.Init<PerfectPointToPointLinkComp> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }
}
