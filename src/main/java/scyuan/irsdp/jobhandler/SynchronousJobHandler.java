package scyuan.irsdp.jobhandler;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;

/**
 *
 *
 * Algorithm 1.1: Synchronous Job Handler
 * Implements:
 *     JobHandler, instance jh.
 *
 * upon event < jh, Submit | job > do
 *     process(job);
 *     trigger < jh, Confirm | job >;
 *
 */
public class SynchronousJobHandler extends ComponentDefinition {

    private final Negative<JobHandler> jh = provides(JobHandler.class);

    public SynchronousJobHandler() {
        subscribe(handler, jh);
    }

    Handler<Submit> handler = new Handler<>() {
        @Override
        public void handle(Submit submit) {
            process(submit.getJob());
            trigger(new Confirm(submit.getJob()), jh);
        }
    };

    private void process(Job job) {
        job.run();
    }
}
