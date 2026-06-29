package com.dev.dbaas.workflow.steps.redis.cluster.undo;

import com.dev.dbaas.workflow.steps.redis.cluster.LoadConfigGroupCluster;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class LoadConfigGroupClusterUndo implements StepBody {
    private static final Logger LOGGER = Logger.getLogger(LoadConfigGroupClusterUndo.class);

    public LoadConfigGroupClusterUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        try {
            LOGGER.info("[4CMS] " + getClass().getName());
            JSONObject origin = (JSONObject) context.getWorkflow().getData();
            JSONObject input = origin.optJSONObject(LoadConfigGroupCluster.class.getSimpleName());
            LOGGER.info("Input : " + input.toString());

            context.getWorkflow().setData(origin);
        } catch (Exception e) {
            LOGGER.error(e, e);
        }

        return ExecutionResult.next();
    }
}
