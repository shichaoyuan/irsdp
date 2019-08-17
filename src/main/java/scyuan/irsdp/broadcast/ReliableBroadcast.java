package scyuan.irsdp.broadcast;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 3.2: Interface and properties of (regular) reliable broadcast
 * Module:
 *     Name: ReliableBroadcast, instance rb.
 * Events:
 *     Request: [rb, Broadcast | m] : Broadcasts a message m to all processes.
 *     Indication: [rb, Deliver | p, m]: Delivers a message m broadcast by process p.
 * Properties:
 *     RB1: Validity: If a correct process p broadcasts a message m, then p eventually delivers m.
 *     RB2: No duplication: No message is delivered more than once.
 *     RB3: No creation: If a process delivers a message m with sender s, then m was previously broadcast by process s.
 *     RB4: Agreement: If a message m is delivered by some correct process, then m is eventually delivered by every correct process.
 *
 */
public class ReliableBroadcast extends PortType {
    {
        request(Broadcast.class);
        indication(BroadcastDeliver.class);
    }
}
