package de.tuberlin.aura.core.task.common;

import de.tuberlin.aura.core.common.eventsystem.IEventDispatcher;
import de.tuberlin.aura.core.common.statemachine.StateMachine;
import de.tuberlin.aura.core.descriptors.Descriptors;
import de.tuberlin.aura.core.iosystem.IOEvents;
import de.tuberlin.aura.core.iosystem.QueueManager;
import de.tuberlin.aura.core.statistic.MeasurementManager;
import de.tuberlin.aura.core.statistic.record.RecordReader;
import de.tuberlin.aura.core.statistic.record.RecordWriter;

/**
 *
 */
public final class TaskDriverContext {

    public final TaskDriverLifecycle taskDriver;

    public final TaskManagerContext managerContext;

    public final Descriptors.TaskDescriptor taskDescriptor;

    public final Descriptors.TaskBindingDescriptor taskBindingDescriptor;

    public final IEventDispatcher driverDispatcher;

    public final QueueManager<IOEvents.DataIOEvent> queueManager;

    public final StateMachine.FiniteStateMachine<TaskStates.TaskState, TaskStates.TaskTransition> taskFSM;

    public final MeasurementManager measurementManager;

    public final RecordReader recordReader;

    public final RecordWriter recordWriter;

    public TaskDriverContext(final TaskDriverLifecycle taskDriver,
                             final TaskManagerContext managerContext,
                             final Descriptors.TaskDescriptor taskDescriptor,
                             final Descriptors.TaskBindingDescriptor taskBindingDescriptor,
                             final IEventDispatcher driverDispatcher,
                             final QueueManager<IOEvents.DataIOEvent> queueManager,
                             final StateMachine.FiniteStateMachine<TaskStates.TaskState, TaskStates.TaskTransition> taskFSM,
                             MeasurementManager measurementManager) {

        this.taskDriver = taskDriver;

        this.managerContext = managerContext;

        this.taskDescriptor = taskDescriptor;

        this.taskBindingDescriptor = taskBindingDescriptor;

        this.driverDispatcher = driverDispatcher;

        this.measurementManager = measurementManager;

        this.recordReader = new RecordReader();

        this.recordWriter = new RecordWriter();

        this.queueManager = queueManager;

        this.taskFSM = taskFSM;
    }

    // -------------- Execution Unit Assignment --------------

    private int assignedExecutionUnitIndex = -1;

    public void setAssignedExecutionUnitIndex(int assignedExecutionUnitIndex) {
        this.assignedExecutionUnitIndex = assignedExecutionUnitIndex;
    }

    public int getAssignedExecutionUnitIndex() {
        return this.assignedExecutionUnitIndex;
    }

    // ------------ Producer/Consumer assignment -------------

    private DataConsumer dataConsumer;

    private DataProducer dataProducer;

    public void setDataProducer(final DataProducer dataProducer) {
        this.dataProducer = dataProducer;
    }

    public DataProducer getDataProducer() {
        return dataProducer;
    }

    public void setDataConsumer(final DataConsumer dataConsumer) {
        this.dataConsumer = dataConsumer;
    }

    public DataConsumer getDataConsumer() {
        return dataConsumer;
    }

}
