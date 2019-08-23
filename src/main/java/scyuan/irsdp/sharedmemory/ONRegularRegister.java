package scyuan.irsdp.sharedmemory;

import se.sics.kompics.PortType;

/**
 *
 *
 *
 * Module 4.1: Interface and properties of a (1, N) regular register
 * Module:
 *     Name: (1, N)-RegularRegister, instance onrr.
 *
 * Events:
 *     Request:  onrr, Read : Invokes a read operation on the register.
 *     Request:  onrr, Write | v : Invokes a write operation with value v on the register.
 *     Indication:  onrr, ReadReturn | v : Completes a read operation on the register with return value v.
 *     Indication:  onrr, WriteReturn : Completes a write operation on the register.
 *
 * Properties:
 *     ONRR1: Termination: If a correct process invokes an operation, then the operation eventually completes.
 *     ONRR2: Validity: A read that is not concurrent with a write returns the last value written; a read that is concurrent with a write returns the last value written or the value concurrently written.
 *
 */
public class ONRegularRegister extends PortType {
    {
        request(Read.class);
        request(Write.class);
        indication(ReadReturn.class);
        indication(WriteReturn.class);
    }
}
