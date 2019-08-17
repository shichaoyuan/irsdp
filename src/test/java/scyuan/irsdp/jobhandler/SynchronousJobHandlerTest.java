package scyuan.irsdp.jobhandler;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import se.sics.kompics.Component;
import se.sics.kompics.Positive;
import se.sics.kompics.testing.Direction;
import se.sics.kompics.testing.TestContext;

import static org.junit.Assert.*;


public class SynchronousJobHandlerTest {

    @Test
    public void test() {
        TestContext<SynchronousJobHandler> tc = TestContext.newInstance(SynchronousJobHandler.class);
        Component comp = tc.getComponentUnderTest();

        Positive<JobHandler> jh = comp.getPositive(JobHandler.class);

        final Job job = new TestJob();
        Submit submit = new Submit(job);

        tc.body().trigger(submit, jh).expect(Confirm.class, (Confirm m) -> (m.getJob() == job), jh, Direction.OUT);

        assertTrue(tc.check());

    }
}