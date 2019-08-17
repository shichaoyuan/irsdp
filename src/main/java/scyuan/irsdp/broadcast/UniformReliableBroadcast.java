package scyuan.irsdp.broadcast;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 3.3: Interface and properties of uniform reliable broadcast
 * Module:
 *     Name: UniformReliableBroadcast, instance urb.
 *
 * Events:
 *     Request: [urb, Broadcast | m]: Broadcasts a message m to all processes.
 *     Indication: [urb, Deliver | p, m]: Delivers a message m broadcast by process p.
 *
 * Properties:
 *     URB1–URB3: Same as properties RB1–RB3 in (regular) reliable broadcast (Module 3.2).
 *     URB4: Uniform agreement: If a message m is delivered by some process (whether correct or faulty), then m is eventually delivered by every correct process.
 *
 */
public class UniformReliableBroadcast extends PortType {
    {
        request(Broadcast.class);
        indication(BroadcastDeliver.class);
    }
}
