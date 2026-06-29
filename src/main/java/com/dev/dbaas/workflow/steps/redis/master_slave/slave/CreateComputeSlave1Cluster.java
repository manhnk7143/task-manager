package com.dev.dbaas.workflow.steps.redis.master_slave.slave;

import com.dev.dbaas.config.ComputeStatus;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
import com.dev.dbaas.manager.OSContextManager;
import com.dev.dbaas.utils.ResourceNameUtil;
import com.dev.dbaas.utils.datastore.OpenstackResource;
import com.dev.dbaas.workflow.steps.redis.master_slave.CheckPrerequisitesMasterSlave;
import com.dev.dbaas.workflow.steps.redis.master_slave.master.CreateComputeMasterCluster;
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

public class CreateComputeSlave1Cluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeSlave1Cluster.class);

    public CreateComputeSlave1Cluster() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateComputeSlave1Cluster.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesMasterSlave.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String regionId = checkPrerequisitesInput.optString("regionId");
        String glanceImageId = checkPrerequisitesInput.optString("glanceImageId");
        String flavorId = checkPrerequisitesInput.optString("flavorId");
        String userData = origin.optJSONObject(GenerateCloudInitSlave1Cluster.class.getSimpleName()).optString("userData");
        String cinderVolumeId = origin.optJSONObject(CreateVolumeSlave1Cluster.class.getSimpleName()).optString("cinderVolumeId");
        int volumeSize = origin.optJSONObject(CreateVolumeSlave1Cluster.class.getSimpleName()).optInt("volumeSize");
        String datastore = checkPrerequisitesInput.optString("datastore");
        String orgId = checkPrerequisitesInput.optString("orgId");
        String serverGroupId = origin.optJSONObject(CreateComputeMasterCluster.class.getSimpleName()).optString("serverGroupId");

        String instanceId = checkPrerequisitesInput.optString("instanceId");

        String keyPair = input.optString("keyPair");

        JSONArray volumeCinderIds = new JSONArray();
        JSONArray serverNovaIds = new JSONArray();

        try {
            List<TbCompute> computes = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.SLAVE_ROLE);
            if (computes.isEmpty()) {
                throw new Exception("Not found compute with instanceId " + instanceId);
            }
            TbCompute compute = computes.get(1);

            List<TbNetwork> networks = NetworkManager.findByComputeId(compute.getId());
            List<String> networkIds = new ArrayList<>();
            for (TbNetwork port : networks) {
                networkIds.add(port.getNeutronPortId());
            }

            String userDataEncrypted = Base64.getEncoder().encodeToString(userData.getBytes());
            String computeName = ResourceNameUtil.buildComputeName(orgId, datastore, compute.getRole(), instanceId);
            Server server = OSContextManager.getInstance().createNovaServer(datastore, computeName, regionId, compute.getZoneName(), keyPair,
                    glanceImageId, flavorId, userDataEncrypted, cinderVolumeId, volumeSize, networkIds, serverGroupId);

            // update compute information
            compute.setName(computeName);
            compute.setGlanceImageId(glanceImageId);
            compute.setNovaInstanceId(server.getId());
            compute.setStatus(ComputeStatus.STARTING.getName());
            ComputeManager.update(compute);

            volumeCinderIds.put(cinderVolumeId);
            serverNovaIds.put(server.getId());
            OpenstackResource.sendResourceInstanceToApi(instanceId, getClass().getSimpleName(), volumeCinderIds, serverNovaIds);
        } catch (Exception e) {
            LOGGER.warn("Create compute error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage("Create compute error : " + e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}



