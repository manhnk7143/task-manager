package com.dev.dbaas.workflow.steps.instance;

import com.dev.dbaas.utils.datastore.OpenstackResource;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class DeleteComputeNova implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(DeleteComputeNova.class);

    public DeleteComputeNova() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject input = (JSONObject) context.getWorkflow().getData();
        LOGGER.info("Input : " + input.toString());

        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        OpenstackResource.deleteServerAndNotify(datastore, instanceId);

        return ExecutionResult.next();
    }
}



