package com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.primary;

import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;

public class CreateComputePrimaryReplicasetUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputePrimaryReplicasetUndo.class);

    public CreateComputePrimaryReplicasetUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        return ExecutionResult.next();
    }
}



