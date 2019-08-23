package scyuan.irsdp.broadcast;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 3.8: Interface and properties of FIFO-order (reliable) broadcast
 * Module:
 *     Name: FIFOReliableBroadcast, instance frb.
 *
 * Events:
 *     Request: [frb, Broadcast | m]: Broadcasts a message m to all processes.
 *     Indication: [frb, Deliver | p, m]: Delivers a message m broadcast by process p.
 *
 * Properties:
 *     FRB1–FRB4: Same as properties RB1–RB4 in (regular) reliable broadcast (Module 3.2).
 *     FRB5: FIFO delivery: If some process broadcasts message m1 before it broadcasts message m2, then no correct process delivers m2 unless it has already delivered m1.
 *
 */
public class FIFOReliableBroadcast extends PortType {
    {
        request(Broadcast.class);
        indication(BroadcastDeliver.class);
    }
}
