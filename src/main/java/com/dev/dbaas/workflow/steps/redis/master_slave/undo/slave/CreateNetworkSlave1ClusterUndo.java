package com.dev.dbaas.workflow.steps.redis.master_slave.undo.slave;

import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;

public class CreateNetworkSlave1ClusterUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateNetworkSlave1ClusterUndo.class);

    public CreateNetworkSlave1ClusterUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        return ExecutionResult.next();
    }
}



