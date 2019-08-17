package scyuan.irsdp.link;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 2.3: Interface and properties of perfect point-to-point links
 * Module:
 *     Name: PerfectPointToPointLinks, instance pl.
 * Events:
 *     Request: < pl, Send | q, m > : Requests to send message m to process q.
 *     Indication: < pl, Deliver | p, m >: Delivers message m sent by process p.
 * Properties:
 *     PL1: Reliable delivery: If a correct process p sends a message m to a correct process q, then q eventually delivers m.
 *     PL2: No duplication: No message is delivered by a process more than once.
 *     PL3: No creation: If some process q delivers a message m with sender p, then m was previously sent to q by process p.
 *
 */
public class PerfectPointToPointLink extends PortType {
    {
        request(Send.class);
        indication(Deliver.class);
    }

}
