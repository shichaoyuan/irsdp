package scyuan.irsdp.jobhandler;

import lombok.Getter;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

class Submit implements KompicsEvent, Serializable {

    @Getter
    private final Job job;

    public Submit(Job job) {
        this.job = job;
    }
}
