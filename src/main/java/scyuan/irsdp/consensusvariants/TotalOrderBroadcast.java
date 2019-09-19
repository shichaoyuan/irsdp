package scyuan.irsdp.consensusvariants;

import scyuan.irsdp.broadcast.Broadcast;
import scyuan.irsdp.broadcast.BroadcastDeliver;
import se.sics.kompics.PortType;

/**
 *
 *
 * Module 6.1: Interface and properties of regular total-order broadcast
 * Module:
 *     Name: TotalOrderBroadcast, instance tob.
 *
 * Events:
 *     Request: [tob, Broadcast | m]: Broadcasts a message m to all processes.
 *     Indication: [tob, Deliver | p, m]: Delivers a message m broadcast by process p.
 *
 * Properties:
 *     TOB1: Validity: If a correct process p broadcasts a message m, then p eventually delivers m.
 *     TOB2: No duplication: No message is delivered more than once.
 *     TOB3: No creation: If a process delivers a message m with sender s, then m was previously broadcast by process s.
 *     TOB4: Agreement: If a message m is delivered by some correct process, then m is eventually delivered by every correct process.
 *     TOB5: Total order: Let m1 and m2 be any two messages and suppose p and q are any two correct processes that deliver m1 and m2. If p delivers m1 before m2, then q delivers m1 before m2.
 *
 */
public class TotalOrderBroadcast extends PortType {
    {
        request(Broadcast.class);
        indication(BroadcastDeliver.class);
    }
}
