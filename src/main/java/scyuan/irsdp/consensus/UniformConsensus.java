package scyuan.irsdp.consensus;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 5.2: Interface and properties of uniform consensus
 * Module:
 *     Name: UniformConsensus, instance uc.
 * Events:
 *     Request:  uc, Propose | v : Proposes value v for consensus.
 *     Indication:  uc, Decide | v : Outputs a decided value v of consensus.
 * Properties:
 *     UC1–UC3: Same as properties C1–C3 in (regular) consensus (Module 5.1).
 *     UC4: Uniform agreement: No two processes decide differently.
 *
 */
public class UniformConsensus extends PortType {
    {
        request(Propose.class);
        indication(Decide.class);
    }
}
