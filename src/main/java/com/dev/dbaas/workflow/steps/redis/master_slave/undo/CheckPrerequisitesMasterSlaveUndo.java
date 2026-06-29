package com.dev.dbaas.workflow.steps.redis.master_slave.undo;

import com.dev.dbaas.utils.datastore.OpenstackResource;
import com.dev.dbaas.workflow.steps.redis.master_slave.CheckPrerequisitesMasterSlave;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CheckPrerequisitesMasterSlaveUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CheckPrerequisitesMasterSlaveUndo.class);

    public CheckPrerequisitesMasterSlaveUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        try {
            LOGGER.info("[4CMS] " + getClass().getName());
            JSONObject origin = (JSONObject) context.getWorkflow().getData();
            JSONObject input = origin.optJSONObject(CheckPrerequisitesMasterSlave.class.getSimpleName());
            LOGGER.info("Input : " + input.toString());
            String instanceId = input.optString("instanceId");
            String datastore = input.optString("datastore");
            OpenstackResource.deleteVolumeAndNotify(datastore, instanceId);
            OpenstackResource.deleteNetworkAndNotify(datastore, instanceId);
            OpenstackResource.deleteServerAndNotify(datastore, instanceId);
        } catch (Exception e) {
            LOGGER.error(e, e);
        }

        return ExecutionResult.next();
    }
}



