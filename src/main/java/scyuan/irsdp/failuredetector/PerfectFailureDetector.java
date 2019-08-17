package scyuan.irsdp.failuredetector;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 2.6: Interface and properties of the perfect failure detector
 * Module:
 *     Name: PerfectFailureDetector, instance P.
 * Events:
 *     Indication: < P, Crash | p >: Detects that process p has crashed.
 * Properties:
 *     PFD1: Strong completeness: Eventually, every process that crashes is permanently detected by every correct process.
 *     PFD2: Strong accuracy: If a process p is detected by any process, then p has crashed.
 */
public class PerfectFailureDetector extends PortType {
    {
        indication(Crash.class);
    }
}
