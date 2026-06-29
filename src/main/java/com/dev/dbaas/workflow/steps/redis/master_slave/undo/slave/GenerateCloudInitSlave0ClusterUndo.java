package com.dev.dbaas.workflow.steps.redis.master_slave.undo.slave;

import jlibs.core.util.regex.TemplateMatcher;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;


public class GenerateCloudInitSlave0ClusterUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInitSlave0ClusterUndo.class);
    private static final TemplateMatcher MATCHER = new TemplateMatcher("${", "}");

    public GenerateCloudInitSlave0ClusterUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        return ExecutionResult.next();
    }
}



