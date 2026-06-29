package com.dev.dbaas.workflow.steps.redis.cluster.undo;

import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;

public class CreateComputeClusterUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeClusterUndo.class);

    public CreateComputeClusterUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        return ExecutionResult.next();
    }
}
