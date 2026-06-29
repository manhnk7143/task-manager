package com.dev.dbaas.workflow.steps.mysql.replicaset;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.manager.DatastoreManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
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

public class GenerateCloudInit implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInit.class);

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(getClass().getSimpleName());
        LOGGER.info(getClass().getName() + " INPUT : " + input.toString());

        int portDefault = Integer.parseInt(origin.optJSONObject(CreateNetwork.class.getSimpleName()).optString("portDefault"));

        JSONObject checkPrerequisites = origin.optJSONObject(CheckPrerequisites.class.getSimpleName());
        JSONArray masterComputeIds = checkPrerequisites.optJSONArray("masterComputeIds");
        JSONArray slaveComputeIds = checkPrerequisites.optJSONArray("slaveComputeIds");
        String regionId = checkPrerequisites.optString("regionId");
        String projectId = checkPrerequisites.optString("projectId");
        String userId = checkPrerequisites.optString("userId");
        String orgId = checkPrerequisites.optString("orgId");
        String instanceId = checkPrerequisites.optString("instanceId");
        String monitorResourceType = checkPrerequisites.optString("monitorResourceType");

        String datastore = input.optString("datastore");
        String datastoreVersion = input.optString("datastoreVersion");
        String datastoreMode = input.optString("datastoreMode");
        String dockerRegistry = input.optString("dockerRegistry");
        String rootPassword = input.optString("rootPassword");
        String backupUrl = input.optString("backupUrl");
        String backupMode = input.optString("backupMode");

        String replicaUser = input.optString("replicaUser");
        String replicaPassword = input.optString("replicaPassword");

        try {
            List<String> listIpMaster = new LinkedList<>();
            Map<String, String> mapNetwork = new HashMap<>();
            int masterNodes = masterComputeIds.length();
            for (int i = 0; i < masterNodes; i++) {
                String computeId = masterComputeIds.getString(i);
                TbNetwork network = NetworkManager.findByComputeIdAndMode(computeId, Constaint.MODE_USER);
                listIpMaster.add(network.getIpAddress());
                mapNetwork.put(computeId, network.getIpAddress());
            }

            List<String> listIpSlave = new LinkedList<>();
            int slaveNodes = slaveComputeIds.length();
            for (int i = 0; i < slaveNodes; i++) {
                String computeId = slaveComputeIds.getString(i);
                TbNetwork network = NetworkManager.findByComputeIdAndMode(computeId, Constaint.MODE_USER);
                listIpSlave.add(network.getIpAddress());
                mapNetwork.put(computeId, network.getIpAddress());
            }

            String masterIps = String.join(",", listIpMaster);
            String slaveIps = String.join(",", listIpSlave);

            // generate cloud init for master
            for (int i = 0; i < masterNodes; i++) {
                String computeId = masterComputeIds.getString(i);
                List<TbAgent> agents = AgentManager.findByComputeId(computeId);
                if (agents != null) {
                    TbAgent agent = agents.get(0);
                    String configurationAgent;
                    if (i == 0) {
                        // download backup in first master
                        configurationAgent = AgentManager.generateConfigurationAgent(instanceId, computeId, datastore,
                                datastoreVersion, agent.getEncryptedKey(), agent.getId(), Config.socket_url,
                                dockerRegistry, backupUrl, backupMode, mapNetwork.get(computeId), monitorResourceType);
                    } else {
                        configurationAgent = AgentManager.generateConfigurationAgent(instanceId, computeId, datastore,
                                datastoreVersion, agent.getEncryptedKey(), agent.getId(), Config.socket_url,
                                dockerRegistry, backupUrl, backupMode, mapNetwork.get(computeId), monitorResourceType);

                    }
                    String configurationMaster = DatastoreManager.generateConfigurationMysqlMasterReplicaset(datastoreMode,
                            rootPassword, Constaint.MASTER_ROLE, slaveIps, portDefault, replicaUser, replicaPassword, i + 2);
                    String configApplication = configurationAgent + configurationMaster;
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

                    agent.setUserData(userData);
                    agent.setUpdatedAt(LocalDateTime.now());
                    AgentManager.update(agent);
                } else {
                    throw new Exception("Not found agent by computeId = " + computeId);
                }
            }

            // generate cloud init for slave
            for (int i = 0; i < slaveNodes; i++) {
                String computeId = slaveComputeIds.getString(i);
                List<TbAgent> agents = AgentManager.findByComputeId(computeId);
                if (agents != null && !agents.isEmpty()) {
                    TbAgent agent = agents.get(0);
                    String configurationAgent = AgentManager.generateConfigurationAgent(instanceId, computeId, datastore,
                            datastoreVersion, agent.getEncryptedKey(), agent.getId(), Config.socket_url, dockerRegistry,
                            null, null, mapNetwork.get(computeId), monitorResourceType);
                    String configurationSlave = DatastoreManager.generateConfigurationMysqlSlaveReplicaset(datastoreMode,
                            rootPassword, Constaint.SLAVE_ROLE, masterIps, portDefault, replicaUser, replicaPassword, i + 1, portDefault);
                    String configApplication = configurationAgent + configurationSlave;
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

                    agent.setUserData(userData);
                    agent.setUpdatedAt(LocalDateTime.now());
                    AgentManager.update(agent);
                } else {
                    throw new Exception("Not found agent by computeId = " + computeId);
                }
            }

        } catch (Exception e) {
            LOGGER.warn(getClass().getSimpleName() + "-ERROR : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage(getClass().getSimpleName() + " error : " + e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}
