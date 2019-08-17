package scyuan.irsdp.broadcast;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 3.5: Interface and properties of logged best-effort broadcast
 * Module:
 *     Name: LoggedBestEffortBroadcast, instance lbeb.
 *
 * Events:
 *     Request: [lbeb, Broadcast | m]: Broadcasts a message m to all processes.
 *     Indication: [lbeb, Deliver | delivered]: Notifies the upper layer of potential updates to variable delivered in stable storage (which log-delivers messages according to the text).
 *
 * Properties:
 *     LBEB1: Validity: If a process that never crashes broadcasts a message m, then every correct process eventually log-delivers m.
 *     LBEB2: No duplication: No message is log-delivered more than once.
 *     LBEB3: No creation: If a process log-delivers a message m with sender s, then m was previously broadcast by process s.
 *
 */
public class LoggedBestEffortBroadcast extends PortType {
}
