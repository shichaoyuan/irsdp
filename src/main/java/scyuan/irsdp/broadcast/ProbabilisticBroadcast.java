package scyuan.irsdp.broadcast;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 3.7: Interface and properties of probabilistic broadcast
 * Module:
 *     Name: ProbabilisticBroadcast, instance pb.
 * Events:
 *     Request: [pb, Broadcast | m]: Broadcasts a message m to all processes.
 *     Indication: [pb, Deliver | p, m]: Delivers a message m broadcast by process p.
 *
 * Properties:
 *     PB1: Probabilistic validity: There is a positive value ε such that when a correct process broadcasts a message m, the probability that every correct process eventually delivers m is at least 1 − ε.
 *     PB2: No duplication: No message is delivered more than once.
 *     PB3: No creation: If a process delivers a message m with sender s, then m was previously broadcast by process s.
 *
 */
public class ProbabilisticBroadcast extends PortType {
}
