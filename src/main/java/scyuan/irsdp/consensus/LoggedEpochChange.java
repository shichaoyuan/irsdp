package scyuan.irsdp.consensus;

import se.sics.kompics.PortType;

/**
 *
 * Module 5.6: Interface and properties of logged epoch-change
 * Module:
 *     Name: LoggedEpochChange, instance lec.
 * Events:
 *     Indication: [lec, StartEpoch | startts, startl]: Notifies the upper layer that variables startts and startl in stable storage contain the timestamp and leader of the next epoch to start.
 * Properties:
 *     LEC1–LEC2: Same as properties EC1–EC2 in epoch-change adapted for logstarting epochs.
 *     LEC3: Eventual leadership: There is a time after which every correct process has log-started some epoch and log-starts no further epoch, such that the last epoch log-started at every correct process is epoch (ts, l) and process l never crashes.
 *
 */
public class LoggedEpochChange extends PortType {
    {
        indication(StartEpoch.class);
    }
}
