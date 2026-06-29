package com.dev.dbaas.workflow.steps.kafka.cluster;

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

public class GenerateCloudInitCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInitCluster.class);

    public GenerateCloudInitCluster() {

    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        String instanceId = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("instanceId");
        JSONArray brokerComputeIds = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optJSONArray("brokerComputeIds");
        String monitorResourceType = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("monitorResourceType");

        JSONObject input = origin.optJSONObject(GenerateCloudInitCluster.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());
        System.out.println("Input : " + input);

        String datastore = input.optString("datastore");
        String datastoreVersion = input.optString("datastoreVersion");
        String datastoreMode = input.optString("datastoreMode");
        String dockerRegistry = input.optString("dockerRegistry");
        String backupUrl = input.optString("backupUrl");
        String backupMode = input.optString("backupMode");
        boolean enableBasicAuth = input.optBoolean("enableBasicAuth");
        String authInfo = input.optString("authInfo");
        int portDefault = Integer.parseInt(origin.optJSONObject(CreateNetworkCluster.class.getSimpleName()).optString("portDefault"));

        try {
            List<String> listIpV4 = new LinkedList<>();
            int brokerNodes = brokerComputeIds.length();
            Map<String, String> mapNetwork = new HashMap<>();
            for (int i = 0; i < brokerNodes; i++) {
                String computeId = brokerComputeIds.getString(i);
                TbNetwork network = NetworkManager.findByComputeIdAndMode(computeId, Constaint.MODE_USER);
                listIpV4.add(network.getIpAddress());
                mapNetwork.put(computeId, network.getIpAddress());
            }
            String clusterNodes = String.join(",", listIpV4);
            int brokerId = 0;

            // generate cloud init for master
            for (int i = 0; i < brokerNodes; i++) {
                String computeId = brokerComputeIds.getString(i);
                List<TbAgent> agents = AgentManager.findByComputeId(computeId);
                if (agents != null) {
                    TbAgent agent = agents.get(0);

                    String configurationAgent = AgentManager.generateConfigurationAgent(instanceId, computeId, datastore,
                            datastoreVersion, agent.getEncryptedKey(), agent.getId(), Config.socket_url,
                            dockerRegistry, backupUrl, backupMode, mapNetwork.get(computeId), monitorResourceType);

                    String configurationKafkaCluster = DatastoreManager.generateConfigurationKafkaCluster(datastoreMode,
                            instanceId, brokerId, brokerId, portDefault, clusterNodes, enableBasicAuth, authInfo);
                    brokerId = brokerId + 1;

                    String configApplication = configurationAgent + configurationKafkaCluster;
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
                } else {
                    LOGGER.warn("Not found agent by computeId = " + computeId);
                }
            }

        } catch (Exception e) {
            LOGGER.warn("generate cloud init error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage("generate cloud init error");
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}
