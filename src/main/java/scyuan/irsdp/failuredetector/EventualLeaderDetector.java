package scyuan.irsdp.failuredetector;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 2.9: Interface and properties of the eventual leader detector
 * Module:
 *     Name: EventualLeaderDetector, instance Ω.
 *
 * Events:
 *     Indication: [ Ω, Trust | p ]: Indicates that process p is trusted to be leader.
 *
 * Properties:
 *     ELD1: Eventual accuracy: There is a time after which every correct process trusts some correct process.
 *     ELD2: Eventual agreement: There is a time after which no two correct processes trust different correct processes.
 *
 */
public class EventualLeaderDetector extends PortType {
    {
        indication(Trust.class);
    }
}
