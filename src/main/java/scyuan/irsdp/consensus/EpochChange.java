package scyuan.irsdp.consensus;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 5.3: Interface and properties of epoch-change
 * Module:
 *     Name: EpochChange, instance ec.
 *
 * Events:
 *     Indication: [ec, StartEpoch | ts, l] : Starts the epoch identified by timestamp ts with leader l.
 *
 * Properties:
 *     EC1: Monotonicity: If a correct process starts an epoch (ts, l) and later starts an epoch (ts', l'), then ts' > ts.
 *     EC2: Consistency: If a correct process starts an epoch (ts, l) and another correct process starts an epoch (ts', l') with ts = ts', then l = l'.
 *     EC3: Eventual leadership: There is a time after which every correct process has started some epoch and starts no further epoch, such that the last epoch started at every correct process is epoch (ts, ) and process  is correct.
 *
 */
public class EpochChange extends PortType {
    {
        indication(StartEpoch.class);
    }
}
