package scyuan.irsdp.failuredetector;

import se.sics.kompics.PortType;

/**
 *
 *
 * partially synchronous system
 *
 *
 * Module 2.8: Interface and properties of the eventually perfect failure detector
 * Module:
 *     Name: EventuallyPerfectFailureDetector, instance ✸P.
 *
 * Events:
 *     Indication: [ ✸P, Suspect | p ]: Notifies that process p is suspected to have crashed.
 *     Indication: [ ✸P, Restore | p ]: Notifies that process p is not suspected anymore.
 *
 * Properties:
 *     EPFD1: Strong completeness: Eventually, every process that crashes is permanently suspected by every correct process.
 *     EPFD2: Eventual strong accuracy: Eventually, no correct process is suspected by any correct process.
 *
 */
public class EventuallyPerfectFailureDetector extends PortType {
    {
        indication(Suspect.class);
        indication(Restore.class);
    }
}
