package scyuan.irsdp.broadcast;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 3.6: Interface and properties of logged uniform reliable broadcast
 * Module:
 *     Name: LoggedUniformReliableBroadcast, instance lurb.
 *
 * Events:
 *     Request: [lurb, Broadcast | m]: Broadcasts a message m to all processes.
 *     Indication: [lurb, Deliver | delivered]: Notifies the upper layer of potential updates to variable delivered in stable storage (which log-delivers messages according to the text).
 *
 * Properties:
 *     LURB1–LURB3: Same as properties LBEB1–LBEB3 in logged best-effort broadcast (Module 3.5).
 *     LURB4: Uniform agreement: If a message m is log-delivered by some process (whether correct or faulty), then m is eventually log-delivered by every correct process.
 *
 */
public class LoggedUniformReliableBroadcast extends PortType {


}
