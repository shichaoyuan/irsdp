package scyuan.irsdp.consensus;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 5.5: Interface and properties of logged uniform consensus
 * Module:
 *     Name: LoggedUniformConsensus, instance luc.
 * Events:
 *     Request: [luc, Propose | v]: Proposes value v for consensus.
 *     Indication: [luc, Decide | decision]: Notifies the upper layer that variable decision in stable storage contains the decided value of consensus.
 * Properties:
 *     LUC1: Termination: Every correct process that never crashes eventually logdecides some value.
 *     LUC2: Validity: If a process log-decides v, then v was proposed by some process.
 *     LUC3: Uniform agreement: No two processes log-decide differently.
 *
 */
public class LoggedUniformConsensus extends PortType {
    {
        request(Propose.class);
        indication(Decide.class);
    }
}
