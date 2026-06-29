package com.dev.dbaas.workflow.steps.redis.standalone;

import com.dev.dbaas.config.APIServiceType;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbConfiguration;
import com.dev.dbaas.manager.ClusterManager;
import com.dev.dbaas.manager.ConfigurationManager;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.CheckPrerequisitesReplicaset;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class LoadConfigGroupStandalone implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(LoadConfigGroupStandalone.class);

    public LoadConfigGroupStandalone() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {

        LOGGER.info("[4CMS] "+getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(LoadConfigGroupStandalone.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String groupConfigurationId = input.optString("groupConfigurationId");
        String instanceId = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName()).optString("instanceId");

        List<TbConfiguration> records = ConfigurationManager.findByGroupConfigurationId(groupConfigurationId);
        JSONArray configurations = new JSONArray();

        if(records != null){
            for (TbConfiguration configParam : records) {
                JSONObject configuration = new JSONObject();
                configuration.put("paramName", configParam.getParamName());
                configuration.put("paramValue", configParam.getParamValue());
                configurations.put(configuration);
            }
        }

        input.put("configurations", configurations);
        context.getWorkflow().setData(origin);

        // send event to API
        JSONObject data = new JSONObject();
        data.put("serviceId", APIServiceType.UPDATE_STATUS_INSTANCE);
        JSONObject bodyData = new JSONObject();
        bodyData.put("action",getClass().getSimpleName());
        bodyData.put("instanceId", instanceId);
        bodyData.put("status", Constaint.DONE);

        data.put("data", bodyData.toString());
        data.put("messageId","");
        data.put("time", System.currentTimeMillis());
        ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);

        return ExecutionResult.next();
    }
}



