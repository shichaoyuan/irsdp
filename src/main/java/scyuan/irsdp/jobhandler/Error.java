package scyuan.irsdp.jobhandler;

import lombok.Getter;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

class Error implements KompicsEvent, Serializable {
    @Getter
    private final Job job;

    public Error(Job job) {
        this.job = job;
    }
}
