package scyuan.irsdp.jobhandler;

import se.sics.kompics.PortType;

/**
 *
 *
 * Module 1.1: Interface and properties of a job handler
 * Module:
 *     Name: JobHandler, instance jh.
 * Events:
 *     Request: { jh, Submit | job }: Requests a job to be processed.
 *     Indication: { jh, Confirm | job }: Confirms that the given job has been (or will be) processed.
 * Properties:
 *     JH1: Guaranteed response: Every submitted job is eventually confirmed.
 */
public class JobHandler extends PortType {
    {
        request(Submit.class);
        indication(Confirm.class);
    }
}
