package scyuan.irsdp.failuredetector;

import lombok.extern.slf4j.Slf4j;
import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.simulator.result.SimulationResultMap;
import se.sics.kompics.simulator.result.SimulationResultSingleton;

@Slf4j
public class EpfdScenarioClient extends ComponentDefinition {

    private final Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);

    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    private final NetAddress self;
    private int suspectCount = 0;
    private int restoreCount = 0;

    public EpfdScenarioClient(Init init) {
        this.self = init.self;

        subscribe(startHandler, control);
        subscribe(suspectHandler, epfd);
        subscribe(restoreHandler, epfd);
    }

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            res.put(self.toString() + "suspect", 0);
            res.put(self.toString() + "restore", 0);
        }
    };

    private Handler<Suspect> suspectHandler = new Handler<Suspect>() {
        @Override
        public void handle(Suspect event) {
            log.info("{} suspected {}", self, event.p);
            res.put(self.toString() + "suspect", ++suspectCount);
        }
    };

    private Handler<Restore> restoreHandler = new Handler<Restore>() {
        @Override
        public void handle(Restore event) {
            log.info("{} restored {}", self, event.p);
            res.put(self.toString() + "restore", ++restoreCount);
        }
    };

    public static class Init extends se.sics.kompics.Init<EpfdScenarioClient> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }
}
