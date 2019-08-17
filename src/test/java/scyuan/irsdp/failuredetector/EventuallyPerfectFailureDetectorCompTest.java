package scyuan.irsdp.failuredetector;

import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.result.SimulationResultMap;
import se.sics.kompics.simulator.result.SimulationResultSingleton;
import se.sics.kompics.simulator.run.LauncherComp;

import static org.junit.Assert.*;

public class EventuallyPerfectFailureDetectorCompTest {

    @Test
    public void oneFailureSimulationTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario epfdScenario = EpfdScenario.oneFailure(EpfdNode.EPFD_NODES);
        epfdScenario.simulate(LauncherComp.class);
        SimulationResultMap res = SimulationResultSingleton.getInstance();
        int port = EpfdNode.NODE_PORT;
        String nodePrefix = EpfdNode.NODE_ADDR_PREFIX;

        int nodeOneRestored = res.get("/" + nodePrefix + 1 + ":" + port +"restore", Integer.class);
        int nodeOneSuspected = res.get("/" + nodePrefix + 1 + ":" + port + "suspect", Integer.class);

        int nodeTwoRestored = res.get("/" + nodePrefix + 2 + ":" + port +"restore", Integer.class);
        int nodeTwoSuspected = res.get("/" + nodePrefix + 2 + ":" + port + "suspect", Integer.class);

        int nodeThreeRestored = res.get("/" + nodePrefix + 3 + ":" + port +"restore", Integer.class);
        int nodeThreeSuspected = res.get("/" + nodePrefix + 3 + ":" + port + "suspect", Integer.class);

        /*
         * Node1 starts first and directly suspects Node2 and Node3.
         * Node2 starts up -> Node1 restores Node2 and Node2 Suspects Node3.
         * Node3 starts up -> Node1 and Node2 Restores Node3.
         * EpfdScenario.oneFailure sends of a killEvent to Node1,
         * Node2 and Node3 suspects Node1.
         */

        assertEquals(nodeOneRestored, 2);
        assertEquals(nodeOneSuspected, 2);

        assertEquals(nodeTwoSuspected, 2);
        assertEquals(nodeTwoRestored, 1);

        assertEquals(nodeThreeSuspected, 1);
        assertEquals(nodeThreeRestored, 0);

    }

}