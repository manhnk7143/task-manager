package com.dev.dbaas.workflow.steps.redis.master_slave.undo;

import com.dev.dbaas.config.APIServiceType;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.manager.ClusterManager;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class LoadConfigGroupMasterSlaveUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(LoadConfigGroupMasterSlaveUndo.class);

    public LoadConfigGroupMasterSlaveUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {

        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(LoadConfigGroupMasterSlaveUndo.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String instanceId = input.optString("instanceId");

        // send event to API
        JSONObject data = new JSONObject();
        data.put("serviceId", APIServiceType.UPDATE_STATUS_INSTANCE);
        JSONObject bodyData = new JSONObject();
        bodyData.put("action", getClass().getSimpleName());
        bodyData.put("instanceId", instanceId);
        bodyData.put("status", Constaint.FAILURE);

        data.put("data", bodyData.toString());
        data.put("messageId", "");
        data.put("time", System.currentTimeMillis());
        ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);

        return ExecutionResult.next();
    }
}



