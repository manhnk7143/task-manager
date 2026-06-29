package com.dev.dbaas.workflow.steps.redis.master_slave.master;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.DatastoreManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.workflow.steps.redis.master_slave.CheckPrerequisitesMasterSlave;
import jlibs.core.util.regex.TemplateMatcher;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.time.LocalDateTime;
import java.util.*;


public class GenerateCloudInitMasterCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(GenerateCloudInitMasterCluster.class);
    private static final TemplateMatcher MATCHER = new TemplateMatcher("${", "}");

    public GenerateCloudInitMasterCluster() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(GenerateCloudInitMasterCluster.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesMasterSlave.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String instanceId = checkPrerequisitesInput.optString("instanceId");
        String monitorResourceType = checkPrerequisitesInput.optString("monitorResourceType");

        String ipAddress = origin.optJSONObject(CreateNetworkMasterCluster.class.getSimpleName()).optString("ipAddress");
        int portDefault = Integer.parseInt(origin.optJSONObject(CreateNetworkMasterCluster.class.getSimpleName()).optString("portDefault"));

        String datastore = input.optString("datastore");
        String datastoreVersion = input.optString("datastoreVersion");
        String datastoreMode = input.optString("datastoreMode");
        String dockerRegistry = input.optString("dockerRegistry");
        String backupUrl = input.optString("backupUrl");
        String password = input.optString("password");
        String backupMode = input.optString("backupMode");

        try {
            TbCompute compute = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.MASTER_ROLE).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Compute not found with instanceId : " + instanceId);
            }

            TbAgent agent = AgentManager.findByComputeId(compute.getId()).stream().findFirst().orElse(null);
            if (agent == null) {
                throw new Exception("Agent not found with computeId : " + compute.getId());
            }

            String configurationAgent = AgentManager.generateConfigurationAgent(instanceId, compute.getId(), datastore, datastoreVersion,
                    agent.getEncryptedKey(), agent.getId(), Config.socket_url, dockerRegistry, backupUrl, backupMode, ipAddress, monitorResourceType);
            String configurationRedis = DatastoreManager.generateConfigurationRedisMaster(datastoreMode, password, portDefault);

            String configApplication = configurationAgent + configurationRedis;

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



