package com.dev.dbaas.workflow.steps.redis.standalone;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.DatastoreManager;
import com.dev.dbaas.manager.InstanceManager;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.time.LocalDateTime;
import java.util.*;


public class GenerateCloudInitStandalone implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInitStandalone.class);

    public GenerateCloudInitStandalone() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(GenerateCloudInitStandalone.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesStandalone.class.getSimpleName());
        JSONObject createNetworkStandInput = origin.optJSONObject(CreateNetworkStandalone.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String instanceId = checkPrerequisitesInput.optString("instanceId");

        String ipAddress = createNetworkStandInput.optString("ipAddress");
        int portDefault = Integer.parseInt(createNetworkStandInput.optString("portDefault"));

        String datastoreName = input.optString("datastore");
        String datastoreVersion = input.optString("datastoreVersion");
        String datastoreMode = input.optString("datastoreMode");
        String dockerRegistry = input.optString("dockerRegistry");
        String monitorResourceType = checkPrerequisitesInput.optString("monitorResourceType");
        String backupUrl = input.optString("backupUrl");
        String password = input.optString("password");
        String backupMode = input.optString("backupMode");

        try {
            TbCompute compute = ComputeManager.findByInstanceId(instanceId).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Compute not found with instanceId : " + instanceId);
            }

            TbAgent agent = AgentManager.findByComputeId(compute.getId()).stream().findFirst().orElse(null);
            if (agent == null) {
                throw new Exception("Agent not found with computeId : " + compute.getId());
            }

            String configurationAgent = AgentManager.generateConfigurationAgent(instanceId, compute.getId(), datastoreName, datastoreVersion,
                    agent.getEncryptedKey(), agent.getId(), Config.socket_url, dockerRegistry, backupUrl, backupMode, ipAddress, monitorResourceType);
            String configurationDataStore = DatastoreManager.generateConfigurationRedisStandalone(datastoreMode, password, portDefault);
            String configApplication = configurationAgent + configurationDataStore;

            Map<String, Object> yamlObj = new LinkedHashMap<>();
            List<Map<String, Object>> writeFilesList = new ArrayList<>();

            Map<String, Object> writeFilesObj = new LinkedHashMap<>();
            writeFilesObj.put("encoding", "b64");
            writeFilesObj.put("owner", "ubuntu:root");
            writeFilesObj.put("path", Constaint.PATH_FILE_CONFIG_GUEST);
            writeFilesObj.put("content", Base64.getEncoder().encodeToString(configApplication.getBytes()));
            writeFilesList.add(writeFilesObj);
            yamlObj.put("write_files", writeFilesList);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            String userData = "#cloud-config\n" + new Yaml(options).dump(yamlObj);

            input.put("userData", userData);
            context.getWorkflow().setData(origin);
        } catch (Exception e) {
            LOGGER.warn("Generate cloud init error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage("Generate cloud init error : " + e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }
        return ExecutionResult.next();
    }
}





