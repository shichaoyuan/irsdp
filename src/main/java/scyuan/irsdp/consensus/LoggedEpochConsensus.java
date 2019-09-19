package scyuan.irsdp.consensus;

import se.sics.kompics.PortType;

/**
 *
 *
 *
 * Module 5.7: Interface and properties of logged epoch consensus
 * Module:
 *     Name: LoggedEpochConsensus, instance lep, with timestamp ts and leader process l.
 *
 * Events:
 *     Request: [lep, Propose | v]: Proposes value v for logged epoch consensus. Executed only by the leader l.
 *     Request: [lep, Abort]: Aborts logged epoch consensus.
 *     Indication: [lep, Decide | epochdecision] Notifies the upper layer that the variable epochdecision in stable storage contains the decided value of logged epoch consensus.
 *     Indication: [lep, Aborted | state]: Signals that logged epoch consensus has completed the abort and outputs internal state state.
 *
 * Properties:
 *     LEP1–LEP4: Same as the validity (EP1), uniform agreement (EP2), lock-in (EP4), and abort behavior (EP6) properties in epoch consensus (Module 5.4), adapted for log-deciding.
 *     LEP5: Termination: If the leader  never crashes, has proposed a value, and no correct process aborts this logged epoch consensus instance, then every correct process eventually log-decides some value in this instance.
 *
 */
public class LoggedEpochConsensus extends PortType {
    {
        request(Propose.class);
        request(Abort.class);
        indication(Decide.class);
        indication(Aborted.class);
    }
}
