package scyuan.irsdp.jobhandler;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class TestJob implements Job {

    private static final AtomicLong counter = new AtomicLong();

    private final long id;

    public TestJob() {
        this.id = counter.incrementAndGet();
    }

    @Override
    public void run() {
        log.info("Job process - {}", this);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestJob{");
        sb.append("id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}
