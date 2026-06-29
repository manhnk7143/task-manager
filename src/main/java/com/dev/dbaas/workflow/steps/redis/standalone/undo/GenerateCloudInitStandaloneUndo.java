package com.dev.dbaas.workflow.steps.redis.standalone.undo;

import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;

public class GenerateCloudInitStandaloneUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInitStandaloneUndo.class);

    public GenerateCloudInitStandaloneUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        return ExecutionResult.next();
    }
}
