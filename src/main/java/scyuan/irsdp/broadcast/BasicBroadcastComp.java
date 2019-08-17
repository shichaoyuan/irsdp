package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.Deliver;
import scyuan.irsdp.link.NetAddress;
import scyuan.irsdp.link.PerfectPointToPointLink;
import scyuan.irsdp.link.Send;
import se.sics.kompics.*;

/**
 *
 * Fail-Silent Algorithm
 *
 * Algorithm 3.1: Basic Broadcast
 * Implements:
 *     BestEffortBroadcast, instance beb.
 * Uses:
 *     PerfectPointToPointLinks, instance pl.
 *
 * upon event [ beb, Broadcast | m ] do
 *     forall q ∈ Π do
 *         trigger [ pl, Send | q, m ];
 *
 * upon event [ pl, Deliver | p, m ] do
 *     trigger [ beb, Deliver | p, m ];
 *
 */
public class BasicBroadcastComp extends ComponentDefinition {
    private final Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
    private final Positive<PerfectPointToPointLink> pl = requires(PerfectPointToPointLink.class);

    public BasicBroadcastComp() {
        subscribe(broadcastHandler, beb);
        subscribe(deliverHandler, pl);
    }

    private final Handler<Broadcast> broadcastHandler = new Handler<Broadcast>() {
        @Override
        public void handle(Broadcast event) {
            for (NetAddress q : event.nodes) {
                trigger(new Send(q, event.payload), pl);
            }
        }
    };

    private final Handler<Deliver> deliverHandler = new Handler<Deliver>() {
        @Override
        public void handle(Deliver event) {
            trigger(new BroadcastDeliver(event.payload, event.src), beb);
        }
    };
}
