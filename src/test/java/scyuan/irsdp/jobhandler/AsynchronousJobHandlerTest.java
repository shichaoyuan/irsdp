package scyuan.irsdp.jobhandler;

import org.junit.Test;
import se.sics.kompics.Component;
import se.sics.kompics.Init;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.testing.Direction;
import se.sics.kompics.testing.TestContext;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

import java.sql.Time;

import static org.junit.Assert.*;

public class AsynchronousJobHandlerTest {

    @Test
    public void test() {
        TestContext<AsynchronousJobHandler> tc = TestContext.newInstance(AsynchronousJobHandler.class);
        Component comp = tc.getComponentUnderTest();

        Positive<JobHandler> jh = comp.getPositive(JobHandler.class);
        Negative<Timer> t = comp.getNegative(Timer.class);

        Component timer = tc.create(JavaTimer.class);
        tc.connect(comp.getNegative(Timer.class), timer.getPositive(Timer.class));

        final Job job = new TestJob();
        Submit submit = new Submit(job);

        tc.body()
                .expect(SchedulePeriodicTimeout.class, t, Direction.OUT)
                .expect(AsynchronousJobHandler.JobProcessTimeout.class, t, Direction.IN)
                .trigger(submit, jh)
                .expect(Confirm.class, (Confirm m) -> (m.getJob() == job), jh, Direction.OUT);
        assertTrue(tc.check());
    }

}