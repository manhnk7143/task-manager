package com.dev.dbaas.workflow.steps.mongodb.standalone.undo;

import com.dev.dbaas.config.APIServiceType;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.manager.ClusterManager;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class LoadConfigGroupStandaloneUndo implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(LoadConfigGroupStandaloneUndo.class);

    public LoadConfigGroupStandaloneUndo() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {

        LOGGER.info("[4CMS] "+getClass().getName());
        try {
            JSONObject input = (JSONObject) context.getWorkflow().getData();
            LOGGER.info("Input : " + input.toString());
            String instanceId = input.optString("instanceId");

            // send event to client
            JSONObject data = new JSONObject();
            data.put("serviceId", APIServiceType.UPDATE_STATUS_INSTANCE);
            JSONObject bodyData = new JSONObject();
            bodyData.put("action",getClass().getSimpleName());
            bodyData.put("instanceId", instanceId);
            bodyData.put("status", Constaint.FAILURE);

            data.put("data", bodyData.toString());
            data.put("messageId","");
            data.put("time", System.currentTimeMillis());
            ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);


        } catch (Exception e) {
            LOGGER.error(e, e);
            // send event to rabbitmq
        }
        return ExecutionResult.next();
    }
}
