package scyuan.irsdp.jobhandler;

import lombok.Getter;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

class Confirm implements KompicsEvent, Serializable {
    @Getter
    private final Job job;

    public Confirm(Job job) {
        this.job = job;
    }
}
