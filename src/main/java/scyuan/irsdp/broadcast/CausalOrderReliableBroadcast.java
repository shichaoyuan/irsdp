package scyuan.irsdp.broadcast;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 3.9: Interface and properties of causal-order (reliable) broadcast
 * Module:
 *     Name: CausalOrderReliableBroadcast, instance crb.
 *
 * Events:
 *     Request: [crb, Broadcast | m]: Broadcasts a message m to all processes.
 *     Indication: [crb, Deliver | p, m]: Delivers a message m broadcast by process p.
 *
 * Properties:
 *     CRB1–CRB4: Same as properties RB1–RB4 in (regular) reliable broadcast (Module 3.2).
 *     CRB5: Causal delivery: For any message m1 that potentially caused a message m2, i.e., m1 → m2, no process delivers m2 unless it has already delivered m1.
 *
 */
public class CausalOrderReliableBroadcast extends PortType {
    {
        request(Broadcast.class);
        indication(BroadcastDeliver.class);
    }
}
