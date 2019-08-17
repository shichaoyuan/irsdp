package scyuan.irsdp.jobhandler;

import se.sics.kompics.*;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.UUID;

/**
 *
 *
 * Algorithm 1.3: Job-Transformation by Buffering
 * Implements:
 *     TransformationHandler, instance th.
 * Uses:
 *     JobHandler, instance jh.
 *
 * upon event < th, Init > do
 *     top := 1;
 *     bottom := 1;
 *     handling := FALSE;
 *     buffer := [⊥]M;
 *
 * upon event < th, Submit | job > do
 *     if bottom + M = top then
 *         trigger < th, Error | job > ;
 *     else
 *         buffer[top mod M + 1] := job;
 *         top := top + 1;
 *         trigger < th, Confirm | job >;
 *
 * upon bottom < top ∧ handling = FALSE do
 *     job := buffer[bottom mod M + 1];
 *     bottom := bottom + 1;
 *     handling := TRUE;
 *     trigger < jh, Submit | job >;
 *
 * upon event < jh, Confirm | job > do
 *     handling := FALSE;
 */
public class BufferingJobTransformation extends ComponentDefinition {
    private final Negative<TransformationHandler> th = provides(TransformationHandler.class);
    private final Positive<JobHandler> jh = requires(JobHandler.class);
    private final Positive<Timer> timer = requires(Timer.class);

    private final Job[] buffer;
    private long top;
    private long bottom;
    private boolean handling;

    private UUID timerId;

    public BufferingJobTransformation(Init init) {
        this.buffer = new Job[init.bufferSize];
        this.top = 0;
        this.bottom = 0;
        this.handling = false;

        subscribe(startHandler, control);
        subscribe(submitHandler, th);
        subscribe(confirmHandler, jh);
        subscribe(timeoutHandler, timer);
    }

    Handler<Submit> submitHandler = new Handler<Submit>() {
        @Override
        public void handle(Submit event) {
            if (bottom + buffer.length == top) {
                trigger(new Error(event.getJob()), th);
            } else {
                int i = (int)(top % buffer.length);
                buffer[i] = event.getJob();
                top += 1;
                trigger(new Confirm(event.getJob()), th);
            }
        }
    };

    Handler<Confirm> confirmHandler = new Handler<Confirm>() {
        @Override
        public void handle(Confirm event) {
            handling = false;
        }
    };

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, 1000);
            TransformationTimeout timeout = new TransformationTimeout(spt);
            spt.setTimeoutEvent(timeout);
            trigger(spt, timer);
            timerId = timeout.getTimeoutId();
        }
    };

    Handler<TransformationTimeout> timeoutHandler = new Handler<TransformationTimeout>() {
        @Override
        public void handle(TransformationTimeout event) {
            if (bottom < top && !handling) {
                int i = (int)(bottom % buffer.length);
                Job job = buffer[i];
                buffer[i] = null;
                bottom += 1;
                handling = true;
                trigger(new Submit(job), jh);
            }
        }
    };

    @Override
    public void tearDown() {
        super.tearDown();
        trigger(new CancelPeriodicTimeout(timerId), timer);
    }

    public static class TransformationTimeout extends Timeout {
        public TransformationTimeout(SchedulePeriodicTimeout request) {
            super(request);
        }
    }


    public static class Init extends se.sics.kompics.Init<BufferingJobTransformation> {
        private final int bufferSize;

        public Init(int bufferSize) {
            this.bufferSize = bufferSize;
        }
    }
}
