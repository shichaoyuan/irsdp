package scyuan.irsdp.sharedmemory;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 4.2: Interface and properties of a (1, N) atomic register
 * Module:
 *     Name: (1, N)-AtomicRegister, instance onar.
 *
 * Events:
 *     Request: [onar, Read]: Invokes a read operation on the register.
 *     Request: [onar, Write | v]: Invokes a write operation with value v on the register.
 *     Indication: [onar, ReadReturn | v]: Completes a read operation on the register with return value v.
 *     Indication: [onar, WriteReturn]: Completes a write operation on the register.
 *
 * Properties:
 *     ONAR1–ONAR2: Same as properties ONRR1–ONRR2 of a (1, N) regular register (Module 4.1).
 *     ONAR3: Ordering: If a read returns a value v and a subsequent read returns a value w, then the write of w does not precede the write of v.
 */
public class ONAtomicRegister extends PortType {
    {
        request(Read.class);
        request(Write.class);
        indication(ReadReturn.class);
        indication(WriteReturn.class);
    }
}
