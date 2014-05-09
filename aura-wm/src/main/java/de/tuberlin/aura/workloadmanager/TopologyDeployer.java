package de.tuberlin.aura.workloadmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuberlin.aura.core.common.statemachine.StateMachine;
import de.tuberlin.aura.core.common.utils.PipelineAssembler.AssemblyPhase;
import de.tuberlin.aura.core.descriptors.Descriptors;
import de.tuberlin.aura.core.descriptors.Descriptors.TaskDeploymentDescriptor;
import de.tuberlin.aura.core.iosystem.RPCManager;
import de.tuberlin.aura.core.protocols.WM2TMProtocol;
import de.tuberlin.aura.core.topology.AuraDirectedGraph.*;
import de.tuberlin.aura.core.topology.TopologyStates.TopologyTransition;

public class TopologyDeployer extends AssemblyPhase<AuraTopology, AuraTopology> {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private static final Logger LOG = LoggerFactory.getLogger(TopologyDeployer.class);

    private final RPCManager rpcManager;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    /**
     * @param rpcManager
     */
    public TopologyDeployer(final RPCManager rpcManager) {
        // sanity check.
        if (rpcManager == null)
            throw new IllegalArgumentException("rpcManager == null");

        this.rpcManager = rpcManager;
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    @Override
    public AuraTopology apply(AuraTopology topology) {

        deployTopology(topology);

        dispatcher.dispatchEvent(new StateMachine.FSMTransitionEvent<>(TopologyTransition.TOPOLOGY_TRANSITION_DEPLOY));

        return topology;
    }

    // ---------------------------------------------------
    // Private Methods.
    // ---------------------------------------------------

    /**
     * @param topology
     */
    private synchronized void deployTopology(final AuraTopology topology) {

        final Map<Descriptors.MachineDescriptor, List<TaskDeploymentDescriptor>> machineDeployment = new HashMap<>();

        // Collect all deployment descriptors for a machine.
        TopologyBreadthFirstTraverser.traverseBackwards(topology, new Visitor<Node>() {

            @Override
            public void visit(final Node element) {
                for (final ExecutionNode en : element.getExecutionNodes()) {

                    final TaskDeploymentDescriptor tdd =
                            new TaskDeploymentDescriptor(en.getTaskDescriptor(),
                                                         en.getTaskBindingDescriptor(),
                                                         en.logicalNode.dataPersistenceType,
                                                         en.logicalNode.executionType);

                    final Descriptors.MachineDescriptor machineDescriptor = en.getTaskDescriptor().getMachineDescriptor();

                    List<TaskDeploymentDescriptor> deploymentDescriptors = machineDeployment.get(machineDescriptor);

                    if (deploymentDescriptors == null) {
                        deploymentDescriptors = new ArrayList<>();
                        machineDeployment.put(machineDescriptor, deploymentDescriptors);
                    }

                    deploymentDescriptors.add(tdd);
                }
            }
        });

        // Ship the deployment descriptors to the task managers.
        TopologyBreadthFirstTraverser.traverseBackwards(topology, new Visitor<Node>() {

            @Override
            public void visit(final Node element) {
                for (final ExecutionNode en : element.getExecutionNodes()) {

                    final Descriptors.MachineDescriptor machineDescriptor = en.getTaskDescriptor().getMachineDescriptor();

                    List<TaskDeploymentDescriptor> tddList = machineDeployment.get(machineDescriptor);

                    // If TDD are not yet shipped, then do it...
                    if (tddList != null) {

                        // TODO: Remove
                        StringBuilder builder = new StringBuilder();
                        for (TaskDeploymentDescriptor desc : tddList) {
                            builder.append(desc.taskDescriptor.name);
                            builder.append(" ");
                            builder.append(desc.taskDescriptor.taskIndex);
                            builder.append(" ");
                            builder.append(desc.taskDescriptor.taskID);
                            builder.append(", ");
                        }

                        LOG.debug("Deploy: {} on machine {}", builder.toString(), machineDescriptor.address);

                        final WM2TMProtocol tmProtocol = rpcManager.getRPCProtocolProxy(WM2TMProtocol.class, machineDescriptor);
                        tmProtocol.installTasks(tddList);

                        // ... and remove it from our mapping.
                        machineDeployment.remove(machineDescriptor);
                    }
                }
            }
        });
    }
}
