package scyuan.irsdp.broadcast;

import scyuan.irsdp.link.Deliver;
import se.sics.kompics.PortType;

/**
 *
 *
 * Module 3.1: Interface and properties of best-effort broadcast
 *
 * Module:
 *     Name: BestEffortBroadcast, instance beb.
 *
 * Events:
 *     Request: [beb, Broadcast | m]: Broadcasts a message m to all processes.
 *     Indication: [beb, Deliver | p, m]: Delivers a message m broadcast by process p.
 *
 * Properties:
 *     BEB1: Validity: If a correct process broadcasts a message m, then every correct process eventually delivers m.
 *     BEB2: No duplication: No message is delivered more than once.
 *     BEB3: No creation: If a process delivers a message m with sender s, then m was previously broadcast by process s.
 *
 */
public class BestEffortBroadcast extends PortType {
    {
        request(Broadcast.class);
        indication(Deliver.class);
    }
}
