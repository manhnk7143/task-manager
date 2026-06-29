package com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.secondary;

import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;

public class CreateComputeSecondaryReplicasetUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeSecondaryReplicasetUndo.class);

    public CreateComputeSecondaryReplicasetUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        return ExecutionResult.next();
    }
}



