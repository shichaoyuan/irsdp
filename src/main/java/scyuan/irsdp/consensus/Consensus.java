package scyuan.irsdp.consensus;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 5.1: Interface and properties of (regular) consensus
 * Module:
 *     Name: Consensus, instance c.
 *
 * Events:
 *     Request: [ c, Propose | v ]: Proposes value v for consensus.
 *     Indication: [ c, Decide | v ]: Outputs a decided value v of consensus.
 *
 * Properties:
 *     C1: Termination: Every correct process eventually decides some value.
 *     C2: Validity: If a process decides v, then v was proposed by some process.
 *     C3: Integrity: No process decides twice.
 *     C4: Agreement: No two correct processes decide differently.
 *
 */
public class Consensus extends PortType {
    {
        request(Propose.class);
        indication(Decide.class);
    }
}
