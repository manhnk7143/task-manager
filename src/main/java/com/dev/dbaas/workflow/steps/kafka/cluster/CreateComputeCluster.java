package com.dev.dbaas.workflow.steps.kafka.cluster;

import com.dev.dbaas.common.RegionResource;
import com.dev.dbaas.config.ComputeStatus;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.database.enties.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.utils.ResourceNameUtil;
import com.dev.dbaas.utils.datastore.OpenstackResource;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstack4j.model.compute.Server;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CreateComputeCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeCluster.class);

    public CreateComputeCluster() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        String instanceId = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("instanceId");
        String regionId = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("regionId");
        JSONArray brokerComputeIds = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optJSONArray("brokerComputeIds");
        String glanceImageId = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("glanceImageId");
        String flavorId = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("flavorId");
        String datastore = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("datastore");
        String orgId = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("orgId");
        int volumeSize = origin.optJSONObject(CreateVolumeCluster.class.getSimpleName()).optInt("volumeSize");

        JSONObject input = origin.optJSONObject(CreateComputeCluster.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());
        System.out.println("Input : " + input);
        String keyPair = new RegionResource(regionId, datastore).getKeypair();
        JSONArray volumeCinderIds = new JSONArray();
        JSONArray serverNovaIds = new JSONArray();

        try {
            String serverGroupId = OSContextManager.getInstance().createServerGroupManual(datastore, regionId, instanceId, Constaint.POLICY_SERVER_GROUP);
            TbInstance instance = InstanceManager.findById(instanceId);
            if (instance != null) {
                instance.setServerGroupId(serverGroupId);
                InstanceManager.update(instance);
            }

            int masterNodes = brokerComputeIds.length();
            for (int i = 0; i < masterNodes; i++) {
                String computeId = brokerComputeIds.getString(i);
                TbCompute compute = ComputeManager.findById(computeId);
                if (compute == null) {
                    throw new Exception("Not found computeId " + computeId);
                }

                // get networks
                List<TbNetwork> networks = NetworkManager.findByComputeId(computeId);
                List<String> networkIds = new ArrayList<>();
                for (TbNetwork port : networks) {
                    networkIds.add(port.getNeutronPortId());
                }

                // get volume
                String cinderVolumeId = null;
                List<TbVolume> volumes = VolumeManager.findByComputeId(computeId);
                if (volumes != null && !volumes.isEmpty()) {
                    cinderVolumeId = volumes.get(0).getCinderVolumeId();
                }

                // get userdata
                String agentUserData = "";
                List<TbAgent> agents = AgentManager.findByComputeId(computeId);
                if (agents != null && !agents.isEmpty()) {
                    agentUserData = agents.get(0).getUserData();
                }

                String userDataEncrypted = Base64.getEncoder().encodeToString(agentUserData.getBytes());
                String computeName = ResourceNameUtil.buildComputeName(orgId, datastore, compute.getRole(), instanceId);
                Server server = OSContextManager.getInstance().createNovaServer(datastore, computeName, regionId, compute.getZoneName(),
                        keyPair, glanceImageId, flavorId, userDataEncrypted, cinderVolumeId, volumeSize, networkIds, serverGroupId);

                // update compute information
                compute.setName(computeName);
                compute.setGlanceImageId(glanceImageId);
                compute.setNovaInstanceId(server.getId());
                compute.setStatus(ComputeStatus.STARTING.getName());
                ComputeManager.update(compute);

                volumeCinderIds.put(cinderVolumeId);
                serverNovaIds.put(server.getId());
            }
            OpenstackResource.sendResourceInstanceToApi(instanceId, getClass().getSimpleName(), volumeCinderIds, serverNovaIds);
        } catch (Exception e) {
            LOGGER.warn("Create compute error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage("create server error");
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }
        return ExecutionResult.next();
    }
}
