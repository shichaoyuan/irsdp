package scyuan.irsdp.sharedmemory;

import se.sics.kompics.PortType;

/**
 *
 * Module 4.3: Interface and properties of an (N, N) atomic register
 * Module:
 *     Name: (N, N)-AtomicRegister, instance nnar.
 *
 * Events:
 *     Request: [nnar, Read]: Invokes a read operation on the register.
 *     Request: [nnar, Write | v]: Invokes a write operation with value v on the register.
 *     Indication: [nnar, ReadReturn | v]: Completes a read operation on the register with return value v.
 *     Indication: [nnar, WriteReturn]: Completes a write operation on the register.
 *
 * Properties:
 *     NNAR1: Termination: Same as property ONAR1 of a (1, N) atomic register (Module 4.2).
 *     NNAR2: Atomicity: Every read operation returns the value that was written most recently in a hypothetical execution, where every failed operation appears to be complete or does not appear to have been invoked at all, and every complete operation appears to have been executed at some instant between its invocation and its completion.
 */
public class NNAtomicRegister extends PortType {
    {
        request(Read.class);
        request(Write.class);
        indication(ReadReturn.class);
        indication(WriteReturn.class);
    }
}
