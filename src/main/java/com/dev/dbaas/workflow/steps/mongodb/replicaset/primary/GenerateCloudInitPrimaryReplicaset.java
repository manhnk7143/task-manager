package com.dev.dbaas.workflow.steps.mongodb.replicaset.primary;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.DatastoreManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.CheckPrerequisitesReplicaset;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.time.LocalDateTime;
import java.util.*;


public class GenerateCloudInitPrimaryReplicaset implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInitPrimaryReplicaset.class);

    public GenerateCloudInitPrimaryReplicaset() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(GenerateCloudInitPrimaryReplicaset.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String instanceId = checkPrerequisitesInput.optString("instanceId");
        String datastore = input.optString("datastore");
        String datastoreVersion = input.optString("datastoreVersion");
        String datastoreMode = input.optString("datastoreMode");
        String dockerRegistry = input.optString("dockerRegistry");
        String monitorResourceType = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName()).optString("monitorResourceType");
        String backupUrl = input.optString("backupUrl");

        String agentId = input.optString("agentId");
        String encryptedKey = input.optString("encryptedKey");
        String backupMode = input.optString("backupMode");

        String rootUser = input.optString("rootUser");
        String rootPassword = input.optString("rootPassword");
        String replicasetKey = input.optString("replicasetKey");
        String replicasetName = input.optString("replicasetName");

        String ipPrimary = origin.optJSONObject(CreateNetworkPrimaryReplicaset.class.getSimpleName()).optString("ipPrimary");
        String ipAddress = origin.optJSONObject(CreateNetworkPrimaryReplicaset.class.getSimpleName()).optString("ipAddress");

        try {
            TbCompute compute = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.PRIMARY_ROLE).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Compute not found with instanceId : " + instanceId);
            }

            String configurationAgent = AgentManager.generateConfigurationAgent(instanceId, compute.getId(), datastore, datastoreVersion,
                    encryptedKey, agentId, Config.socket_url, dockerRegistry, backupUrl, backupMode, ipAddress, monitorResourceType);
            String configurationMongoDb = DatastoreManager.generateConfigurationMongoDbPrimaryReplicaset(datastoreMode, Constaint.PRIMARY_ROLE, rootUser, rootPassword, replicasetKey, replicasetName, ipPrimary, ipAddress);
            String configApplication = configurationAgent + configurationMongoDb;

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



