package com.dev.dbaas.workflow.steps.redis.standalone;

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

public class CreateComputeStandalone implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeStandalone.class);

    public CreateComputeStandalone() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateComputeStandalone.class.getSimpleName());
        JSONObject checkPrerequisitesStandInput = origin.optJSONObject(CheckPrerequisitesStandalone.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String regionId = checkPrerequisitesStandInput.optString("regionId");
        String glanceImageId = checkPrerequisitesStandInput.optString("glanceImageId");
        String flavorId = checkPrerequisitesStandInput.optString("flavorId");
        String userData = origin.optJSONObject(GenerateCloudInitStandalone.class.getSimpleName()).optString("userData");
        String cinderVolumeId = origin.optJSONObject(CreateVolumeStandalone.class.getSimpleName()).optString("cinderVolumeId");
        int volumeSize = origin.optJSONObject(CreateVolumeStandalone.class.getSimpleName()).optInt("volumeSize");
        String datastore = checkPrerequisitesStandInput.optString("datastore");
        String orgId = checkPrerequisitesStandInput.optString("orgId");

        String instanceId = checkPrerequisitesStandInput.optString("instanceId");

        String keyPair = input.optString("keyPair");

        JSONArray volumeCinderIds = new JSONArray();
        JSONArray serverNovaIds = new JSONArray();

        try {
            TbCompute compute = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.MASTER_ROLE).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Not found compute with instanceId " + instanceId);
            }

            List<TbNetwork> networks = NetworkManager.findByComputeId(compute.getId());
            List<String> networkIds = new ArrayList<>();
            for (TbNetwork port : networks) {
                networkIds.add(port.getNeutronPortId());
            }

            String userDataEncrypted = Base64.getEncoder().encodeToString(userData.getBytes());
            String computeName = ResourceNameUtil.buildComputeName(orgId, datastore, compute.getRole(), instanceId);
            Server server = OSContextManager.getInstance().createNovaServer(datastore, computeName, regionId, compute.getZoneName(),
                    keyPair, glanceImageId, flavorId, userDataEncrypted, cinderVolumeId, volumeSize, networkIds);

            // update compute information
            compute.setName(computeName);
            compute.setGlanceImageId(glanceImageId);
            compute.setNovaInstanceId(server.getId());
            compute.setStatus(ComputeStatus.STARTING.getName());
            ComputeManager.update(compute);

            volumeCinderIds.put(cinderVolumeId);
            serverNovaIds.put(server.getId());

            input.put("computeId", compute.getId());
            context.getWorkflow().setData(origin);
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



