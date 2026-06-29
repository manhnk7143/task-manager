package com.dev.dbaas.workflow.steps.api_gateway.standalone;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbVolume;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.OSContextManager;
import com.dev.dbaas.manager.VolumeManager;
import com.dev.dbaas.utils.ResourceNameUtil;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openstack4j.model.storage.block.Volume;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class CreateVolume implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateVolume.class);

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(getClass().getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisites.class.getSimpleName());
        LOGGER.info(getClass().getName() + " INPUT : " + input.toString());

        String datastore = checkPrerequisitesInput.optString("datastore");
        String regionId = checkPrerequisitesInput.optString("regionId");
        String instanceId = checkPrerequisitesInput.optString("instanceId");
        String orgId = checkPrerequisitesInput.optString("orgId");
        String projectId = checkPrerequisitesInput.optString("projectId");

        try {
            int volumeSize = input.optInt("volumeSize");
            TbCompute compute = ComputeManager.findByInstanceId(instanceId).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Not found compute of instance " + instanceId);
            }

            String volumeName = ResourceNameUtil.buildVolumeName(orgId, datastore, compute.getRole(), instanceId);
            Volume volume = OSContextManager.getInstance().createVolume(datastore, regionId, volumeName,
                    volumeSize, "instance_" + instanceId, compute.getZoneName());
            TimeUnit.SECONDS.sleep(2);
            volume = OSContextManager.getInstance().getVolume(datastore, regionId, volume.getId());
            if (volume == null || !volume.getStatus().equals(Volume.Status.AVAILABLE)) {
                throw new Exception("Status of volume not available");
            }

            TbVolume recordVolume = new TbVolume();
            recordVolume.setName(volumeName);
            recordVolume.setSize(volumeSize);
            recordVolume.setFormat("unknown");
            recordVolume.setComputeId(compute.getId());
            recordVolume.setStatus(volume.getStatus().toString());
            recordVolume.setCinderVolumeId(volume.getId());
            recordVolume.setProjectId(projectId);
            recordVolume.setOrgId(orgId);
            recordVolume.setCreatedAt(LocalDateTime.now());
            recordVolume.setUpdatedAt(LocalDateTime.now());
            VolumeManager.add(recordVolume);

        } catch (Exception e) {
            LOGGER.warn("Create volume error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage("Create volume error : " + e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}
