package scyuan.irsdp.consensus;

import se.sics.kompics.PortType;

/**
 *
 *
 *
 * Module 5.4: Interface and properties of epoch consensus
 * Module:
 *     Name: EpochConsensus, instance ep, with timestamp ts and leader process l.
 *
 * Events:
 *     Request: [ep, Propose | v]: Proposes value v for epoch consensus. Executed only by the leader l.
 *     Request: [ep, Abort]: Aborts epoch consensus.
 *     Indication: [ep, Decide | v]: Outputs a decided value v of epoch consensus.
 *     Indication: [ep, Aborted | state]: Signals that epoch consensus has completed the abort and outputs internal state state.
 *
 * Properties:
 *     EP1: Validity: If a correct process ep-decides v, then v was ep-proposed by the leader l' of some epoch consensus with timestamp ts' ≤ ts and leader l'.
 *     EP2: Uniform agreement: No two processes ep-decide differently.
 *     EP3: Integrity: Every correct process ep-decides at most once.
 *     EP4: Lock-in: If a correct process has ep-decided v in an epoch consensus with timestamp ts' < ts, then no correct process ep-decides a value different from v.
 *     EP5: Termination: If the leader  is correct, has ep-proposed a value, and no correct process aborts this epoch consensus, then every correct process eventually ep-decides some value.
 *     EP6: Abort behavior: When a correct process aborts an epoch consensus, it eventually will have completed the abort; moreover, a correct process completes an abort only if the epoch consensus has been aborted by some correct process.
 *
 */
public class EpochConsensus extends PortType {
    {
        request(Propose.class);
        request(Abort.class);
        indication(Decide.class);
        indication(Aborted.class);
    }
}
