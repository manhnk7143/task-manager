package com.dev.dbaas.workflow.steps.api_gateway.standalone;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.manager.*;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.time.LocalDateTime;
import java.util.*;

public class GenerateCloudInit implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInit.class);

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(GenerateCloudInit.class.getSimpleName());
        LOGGER.info(getClass().getName() + " INPUT : " + input.toString());

        JSONObject checkPrerequisites = origin.optJSONObject(CheckPrerequisites.class.getSimpleName());
        String instanceId = checkPrerequisites.optString("instanceId");

        String monitorResourceType = input.optString("monitorResourceType");
        String datastoreMode = input.optString("datastoreMode");
        String datastore = input.optString("datastore");
        String datastoreVersion = input.optString("datastoreVersion");
        String dockerRegistry = input.optString("dockerRegistry");

        String role = input.optString("role");

        try {
            TbCompute compute = ComputeManager.findByInstanceId(instanceId).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Compute not found with instanceId : " + instanceId);
            }

            TbNetwork network = NetworkManager.findByComputeIdAndMode(compute.getId(), Constaint.MODE_USER);
            List<TbAgent> agents = AgentManager.findByComputeId(compute.getId());

            if (agents == null || agents.isEmpty()) {
                throw new Exception("Not found agent by computeId : " + compute.getId());
            }

            TbAgent agent = agents.get(0);
            String configurationAgent = AgentManager.generateConfigurationAgent(instanceId, compute.getId(), datastore,
                    datastoreVersion, agent.getEncryptedKey(), agent.getId(), Config.socket_url,
                    dockerRegistry, null, "", network.getIpAddress(), monitorResourceType);
            String configurationApiGStand = DatastoreManager
                    .generateConfigAPIGStandalone(role, datastoreMode);

            String configApplication = configurationAgent + configurationApiGStand;

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

            LOGGER.info("UserData -------");
            LOGGER.info(userData);
            LOGGER.info("----------------");

            agent.setUserData(userData);
            agent.setUpdatedAt(LocalDateTime.now());
            AgentManager.update(agent);

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
