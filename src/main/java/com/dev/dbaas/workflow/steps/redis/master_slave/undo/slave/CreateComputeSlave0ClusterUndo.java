package com.dev.dbaas.workflow.steps.redis.master_slave.undo.slave;

import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;

public class CreateComputeSlave0ClusterUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeSlave0ClusterUndo.class);

    public CreateComputeSlave0ClusterUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        return ExecutionResult.next();
    }
}



