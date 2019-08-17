package scyuan.irsdp.jobhandler;

import lombok.extern.slf4j.Slf4j;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.UUID;

@Slf4j
public class JobSubmitter extends ComponentDefinition {

    private final Positive<TransformationHandler> th = requires(TransformationHandler.class);
    private final Positive<Timer> timer = requires(Timer.class);

    private UUID timerId;

    public JobSubmitter() {
        subscribe(startHandler, control);
        subscribe(confirmHandler, th);
        subscribe(errorHandler, th);
        subscribe(timeoutHandler, timer);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, 100);
            SubmitTimeout timeout = new SubmitTimeout(spt);
            spt.setTimeoutEvent(timeout);
            trigger(spt, timer);
            timerId = timeout.getTimeoutId();
        }
    };

    Handler<SubmitTimeout> timeoutHandler = new Handler<SubmitTimeout>() {
        @Override
        public void handle(SubmitTimeout event) {
            trigger(new Submit(new TestJob()), th);
        }
    };

    Handler<Confirm> confirmHandler = new Handler<Confirm>() {
        @Override
        public void handle(Confirm event) {
            log.info("confirm job - {}", event.getJob());
        }
    };

    Handler<Error> errorHandler = new Handler<Error>() {
        @Override
        public void handle(Error event) {
            log.info("error job - {}", event.getJob());
        }
    };

    @Override
    public void tearDown() {
        super.tearDown();
        trigger(new CancelPeriodicTimeout(timerId), timer);
    }

    public static class SubmitTimeout extends Timeout {
        public SubmitTimeout(SchedulePeriodicTimeout request) {
            super(request);
        }
    }


}
