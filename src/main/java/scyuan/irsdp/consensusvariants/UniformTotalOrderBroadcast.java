package scyuan.irsdp.consensusvariants;

import scyuan.irsdp.broadcast.Broadcast;
import scyuan.irsdp.broadcast.BroadcastDeliver;
import se.sics.kompics.PortType;

/**
 *
 *
 * Module 6.2: Interface and properties of uniform total-order broadcast
 * Module:
 *     Name: UniformTotalOrderBroadcast, instance utob.
 *
 * Events:
 *     Request: [utob, Broadcast | m] : Broadcasts a message m to all processes.
 *     Indication: [utob, Deliver | p, m]: Delivers a message m broadcast by process p.
 *
 * Properties:
 *     UTOB1–UTOB3: Same as properties TOB1–TOB3 in regular total-order broadcast (Module 6.1).
 *     UTOB4: Uniform agreement: If a message m is delivered by some process (whether correct or faulty), then m is eventually delivered by every correct process.
 *     UTOB5: Uniform total order: Let m1 and m2 be any two messages and suppose p and q are any two processes that deliver m1 and m2 (whether correct or faulty). If p delivers m1 before m2, then q delivers m1 before m2.
 *
 */
public class UniformTotalOrderBroadcast extends PortType {
    {
        request(Broadcast.class);
        indication(BroadcastDeliver.class);
    }
}
