package com.dev.dbaas.workflow.steps.mongodb.replicaset.undo;

import com.dev.dbaas.utils.datastore.OpenstackResource;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.CheckPrerequisitesReplicaset;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CheckPrerequisitesReplicasetUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CheckPrerequisitesReplicasetUndo.class);

    public CheckPrerequisitesReplicasetUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        try {
            JSONObject origin = (JSONObject) context.getWorkflow().getData();
            JSONObject checkPrerequisites = (JSONObject) origin.get(CheckPrerequisitesReplicaset.class.getSimpleName());
            String instanceId = checkPrerequisites.optString("instanceId");
            String datastore = checkPrerequisites.optString("datastore");
            OpenstackResource.deleteVolumeAndNotify(datastore, instanceId);
            OpenstackResource.deleteNetworkAndNotify(datastore, instanceId);
            OpenstackResource.deleteServerAndNotify(datastore, instanceId);
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return ExecutionResult.next();
    }
}



