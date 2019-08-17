package scyuan.irsdp.broadcast;

import se.sics.kompics.ComponentDefinition;

/**
 *
 *
 * Fail-Silent Algorithm
 *
 * N > 2f if we assume that up to f processes may crash.
 *
 * Algorithm 3.5: Majority-Ack Uniform Reliable Broadcast
 * Implements:
 *     UniformReliableBroadcast, instance urb.
 * Uses:
 *     BestEffortBroadcast, instance beb.
 *     // Except for the function candeliver(·) below and for the absence of  Crash  events
 *     // triggered by the perfect failure detector, it is the same as Algorithm 3.4.
 *
 *     function candeliver(m) returns Boolean is
 *         return #(ack[m]) > N/2;
 *
 */
public class MajorityAckUniformReliableBroadcast extends ComponentDefinition {
}
