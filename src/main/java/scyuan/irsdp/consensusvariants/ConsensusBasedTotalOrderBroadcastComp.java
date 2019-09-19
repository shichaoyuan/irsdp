package scyuan.irsdp.consensusvariants;

import se.sics.kompics.ComponentDefinition;

/**
 *
 *
 * Algorithm 6.1: Consensus-Based Total-Order Broadcast
 * Implements:
 *     TotalOrderBroadcast, instance tob.
 *
 * Uses:
 *     ReliableBroadcast, instance rb;
 *     Consensus (multiple instances).
 *
 * upon event [ tob, Init ] do
 *     unordered := ∅;
 *     delivered := ∅;
 *     round := 1;
 *     wait := FALSE;
 *
 * upon event [ tob, Broadcast | m ] do
 *     trigger [ rb, Broadcast | m ];
 *
 * upon event [ rb, Deliver | p, m ] do
 *     if m !∈ delivered then
 *         unordered := unordered ∪ {(p, m)};
 *
 * upon unordered != ∅ ∧ wait = FALSE do
 *     wait := TRUE;
 *     Initialize a new instance c.round of consensus;
 *     trigger [ c.round, Propose | unordered ];
 *
 * upon event [ c.r, Decide | decided ] such that r = round do
 *     forall (s, m) ∈ sort(decided) do // by the order in the resulting sorted list
 *         trigger [ tob, Deliver | s, m ];
 *     delivered := delivered ∪ decided;
 *     unordered := unordered \ decided;
 *     round := round + 1;
 *     wait := FALSE;
 *
 */
public class ConsensusBasedTotalOrderBroadcastComp extends ComponentDefinition {


}
