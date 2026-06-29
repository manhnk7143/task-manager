package com.dev.dbaas.workflow.steps.mongodb.replicaset.arbiter;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.DatastoreManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.CheckPrerequisitesReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.primary.CreateNetworkPrimaryReplicaset;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.time.LocalDateTime;
import java.util.*;


public class GenerateCloudInitArbiterReplicaset implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInitArbiterReplicaset.class);

    public GenerateCloudInitArbiterReplicaset() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(GenerateCloudInitArbiterReplicaset.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String instanceId = checkPrerequisitesInput.optString("instanceId");
        String datastore = input.optString("datastore");
        String datastoreVersion = input.optString("datastoreVersion");
        String encryptedKey = input.optString("encryptedKey");
        String agentId = input.optString("agentId");
        String datastoreMode = input.optString("datastoreMode");
        String rootUser = input.optString("rootUser");
        String rootPassword = input.optString("rootPassword");
        String replicasetKey = input.optString("replicasetKey");
        String replicasetName = input.optString("replicasetName");
        String dockerRegistry = input.optString("dockerRegistry");
        String ipPrimary = origin.optJSONObject(CreateNetworkPrimaryReplicaset.class.getSimpleName()).optString("ipPrimary");
        String ipAddress = origin.optJSONObject(CreateNetworkArbiterReplicaset.class.getSimpleName()).optString("ipAddress");
        String monitorResourceType = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName()).optString("monitorResourceType");

        try {
            TbCompute compute = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.ARBITER_ROLE).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("arbiter compute not found with instanceId : " + instanceId);
            }
            String configurationAgent = AgentManager.generateConfigurationAgent(instanceId, compute.getId(), datastore, datastoreVersion,
                    encryptedKey, agentId, Config.socket_url, dockerRegistry, null, null, ipAddress, monitorResourceType);
            String configurationMongoDb = DatastoreManager.generateConfigurationMongoDbArbiterReplicaset(datastoreMode, Constaint.ARBITER_ROLE, rootUser, rootPassword, replicasetKey, replicasetName, ipPrimary, ipAddress);
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

//            if (curlAgentMonitor != null && !curlAgentMonitor.isEmpty()) {
//                String curlStr = curlAgentMonitor.split(" && ")[0];
//                String bashStr = curlAgentMonitor.split(" && ")[1];
//
//                List<String[]> runcmdList = new ArrayList<>();
//                runcmdList.add(curlStr.split(" "));
//                runcmdList.add(bashStr.split(" "));
//                yamlObj.put("runcmd", runcmdList);
//            }

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



