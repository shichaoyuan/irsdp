package scyuan.irsdp.broadcast;

import se.sics.kompics.PortType;

/**
 *
 *
 *
 * Module 3.10: Interface and properties of causal-order uniform (reliable) broadcast
 * Module:
 *     Name: CausalOrderUniformReliableBroadcast, instance curb.
 * Events:
 *     Request: [curb, Broadcast | m]: Broadcasts a message m to all processes.
 *     Indication: [curb, Deliver | p, m]: Delivers a message m broadcast by process p.
 * Properties:
 *     CURB1–CURB4: Same as properties URB1–URB4 in uniform reliable broadcast (Module 3.3).
 *     CURB5: Same as property CRB5 in causal-order broadcast (Module 3.9).
 */
public class CausalOrderUniformReliableBroadcast extends PortType {
    {
        request(Broadcast.class);
        indication(BroadcastDeliver.class);
    }
}
