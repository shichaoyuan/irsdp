package scyuan.irsdp.failuredetector;

import scyuan.irsdp.link.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EpfdScenario {

    private static final Operation1<StartNodeEvent, Integer> startServerOp = new Operation1<StartNodeEvent, Integer>() {
        @Override
        public StartNodeEvent generate(Integer integer) {
            return new StartNodeEvent() {
                final NetAddress selfAddr;
                {
                    try {
                        selfAddr = new NetAddress(InetAddress.getByName(EpfdNode.NODE_ADDR_PREFIX + integer), EpfdNode.NODE_PORT);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAddr;
                }

                @Override
                public Class<? extends ComponentDefinition> getComponentDefinition() {
                    return EpfdNode.class;
                }

                @Override
                public Init getComponentInit() {
                    return new EpfdNode.Init(selfAddr);
                }

                @Override
                public String toString() {
                    return "StartNode<" + selfAddr + ">";
                }
            };
        }
    };

    private static Operation1<KillNodeEvent, Integer> killOp = new Operation1<KillNodeEvent, Integer>() {
        @Override
        public KillNodeEvent generate(Integer integer) {
            return new KillNodeEvent() {
                NetAddress addr;
                {
                    try {
                        addr = new NetAddress(InetAddress.getByName(EpfdNode.NODE_ADDR_PREFIX + integer), EpfdNode.NODE_PORT);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return addr;
                }

                @Override
                public String toString() {
                    return "Kill<" + addr + ">";
                }
            };
        }
    };


    public static SimulationScenario oneFailure(final int servers) {
        return new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess nodes = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess kill = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, killOp, new BasicIntSequentialDistribution(1));
                    }
                };

                nodes.start();
                kill.startAfterTerminationOf(10000, nodes);
                terminateAfterTerminationOf(50000, nodes);
            }
        };
    }

}
