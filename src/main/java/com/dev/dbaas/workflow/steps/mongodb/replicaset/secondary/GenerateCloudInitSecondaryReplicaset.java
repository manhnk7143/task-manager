package com.dev.dbaas.workflow.steps.mongodb.replicaset.secondary;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.manager.DatastoreManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.CheckPrerequisitesReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.primary.CreateNetworkPrimaryReplicaset;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.time.LocalDateTime;
import java.util.*;


public class GenerateCloudInitSecondaryReplicaset implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInitSecondaryReplicaset.class);

    public GenerateCloudInitSecondaryReplicaset() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(GenerateCloudInitSecondaryReplicaset.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        JSONArray secondaryComputeIds = checkPrerequisitesInput.optJSONArray("secondaryComputeIds");
        String monitorResourceType = checkPrerequisitesInput.optString("monitorResourceType");
        String instanceId = checkPrerequisitesInput.optString("instanceId");

        String datastore = input.optString("datastore");
        String datastoreVersion = input.optString("datastoreVersion");
        String datastoreMode = input.optString("datastoreMode");
        String rootUser = input.optString("rootUser");
        String rootPassword = input.optString("rootPassword");
        String replicasetKey = input.optString("replicasetKey");
        String replicasetName = input.optString("replicasetName");
        String dockerRegistry = input.optString("dockerRegistry");
        String ipPrimary = origin.optJSONObject(CreateNetworkPrimaryReplicaset.class.getSimpleName()).optString("ipPrimary");

        try {
            int secondaryNodes = secondaryComputeIds.length();
            for (int i = 0; i < secondaryNodes; i++) {
                String computeId = secondaryComputeIds.getString(i);
                List<TbAgent> agents = AgentManager.findByComputeId(computeId);
                TbNetwork network = NetworkManager.findByComputeIdAndMode(computeId, Constaint.MODE_USER);

                if (agents != null) {
                    for (TbAgent agent : agents) {
                        String configurationAgent = AgentManager.generateConfigurationAgent(instanceId, computeId, datastore,
                                datastoreVersion, agent.getEncryptedKey(), agent.getId(), Config.socket_url, dockerRegistry,
                                null, null, network.getIpAddress(), monitorResourceType);
                        String configurationMongoDb = DatastoreManager.generateConfigurationMongoDbSecondaryReplicaset(
                                datastoreMode, Constaint.SECONDARY_ROLE, rootUser, rootPassword, replicasetKey,
                                replicasetName, ipPrimary, network.getIpAddress());

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

//                        if (agent.getCmdInstallMonitor() != null && !agent.getCmdInstallMonitor().isEmpty()) {
//                            String curlStr = agent.getCmdInstallMonitor().split(" && ")[0];
//                            String bashStr = agent.getCmdInstallMonitor().split(" && ")[1];
//
//                            List<String[]> runcmdList = new ArrayList<>();
//                            runcmdList.add(curlStr.split(" "));
//                            runcmdList.add(bashStr.split(" "));
//                            yamlObj.put("runcmd", runcmdList);
//                        }
                        DumperOptions options = new DumperOptions();
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

                        String userData = "#cloud-config\n" + new Yaml(options).dump(yamlObj);
                        agent.setUserData(userData);
                        agent.setUpdatedAt(LocalDateTime.now());
                        AgentManager.update(agent);
                    }
                } else {
                    LOGGER.warn("Not found agent by computeId = " + computeId);
                }
            }
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



