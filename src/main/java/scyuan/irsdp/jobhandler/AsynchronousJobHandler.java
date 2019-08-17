package scyuan.irsdp.jobhandler;

import se.sics.kompics.*;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

/**
 *
 * Algorithm 1.2: Asynchronous Job Handler
 * Implements:
 *     JobHandler, instance jh.
 * upon event < jh, Init > do
 *     buffer := ∅;
 * upon event < jh, Submit | job > do
 *     buffer := buffer ∪ {job};
 *     trigger < jh, Confirm | job >;
 * upon buffer != ∅ do
 *     job := selectjob(buffer);
 *     process(job);
 *     buffer := buffer \ {job};
 *
 */
public class AsynchronousJobHandler extends ComponentDefinition {

    private final Negative<JobHandler> jh = provides(JobHandler.class);
    private final Positive<Timer> timer = requires(Timer.class);

    private final Queue<Job> buffer;

    private UUID timerId;

    public AsynchronousJobHandler() {
        buffer = new LinkedList<>();
        subscribe(startHandler, control);
        subscribe(jobHandler, jh);
        subscribe(timeoutHandler, timer);
    }

    Handler<Start> startHandler = new Handler<>() {
        @Override
        public void handle(Start event) {
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, 1000);
            JobProcessTimeout timeout = new JobProcessTimeout(spt);
            spt.setTimeoutEvent(timeout);
            trigger(spt, timer);
            timerId = timeout.getTimeoutId();
        }
    };

    Handler<Submit> jobHandler = new Handler<>() {
        @Override
        public void handle(Submit event) {
            buffer.add(event.getJob());
            trigger(new Confirm(event.getJob()), jh);
        }
    };

    Handler<JobProcessTimeout> timeoutHandler = new Handler<>() {
        @Override
        public void handle(JobProcessTimeout event) {
            Job job = buffer.poll();
            if (job != null) {
                job.run();
            }
        }
    };

    @Override
    public void tearDown() {
        super.tearDown();
        trigger(new CancelPeriodicTimeout(timerId), timer);
    }

    public static class JobProcessTimeout extends Timeout {
        public JobProcessTimeout(SchedulePeriodicTimeout request) {
            super(request);
        }
    }

}
