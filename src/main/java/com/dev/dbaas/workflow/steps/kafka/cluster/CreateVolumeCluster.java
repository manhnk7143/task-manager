package com.dev.dbaas.workflow.steps.kafka.cluster;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.config.ResourceStatus;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstack4j.model.storage.block.Volume;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class CreateVolumeCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateVolumeCluster.class);

    public CreateVolumeCluster() {

    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateVolumeCluster.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String datastore = checkPrerequisitesInput.optString("datastore");
        String regionId = checkPrerequisitesInput.optString("regionId");
        String instanceId = checkPrerequisitesInput.optString("instanceId");
        String orgId = checkPrerequisitesInput.optString("orgId");
        String projectId = checkPrerequisitesInput.optString("projectId");
        JSONArray brokerComputeIds = checkPrerequisitesInput.optJSONArray("brokerComputeIds");

        try {
            int volumeSize = input.optInt("volumeSize");

            int masterNodes = brokerComputeIds.length();
            for (int i = 0; i < masterNodes; i++) {
                String computeId = brokerComputeIds.getString(i);
                TbCompute compute = ComputeManager.findById(computeId);
                if (compute != null) {
                    String volumeName = ResourceNameUtil.buildVolumeName(orgId, datastore, compute.getRole(), instanceId);
                    Volume volume = OSContextManager.getInstance().createVolume(datastore, regionId, volumeName,
                            volumeSize, volumeName, compute.getZoneName());
                    TimeUnit.SECONDS.sleep(2);
                    volume = OSContextManager.getInstance().getVolume(datastore, regionId, volume.getId());
                    if (volume == null || !volume.getStatus().equals(Volume.Status.AVAILABLE)) {
                        TbInstance instance = InstanceManager.findById(instanceId);
                        instance.setMessage("Create volume for master error");
                        instance.setStatus(Constaint.STATUS_ERROR);
                        InstanceManager.update(instance);
                        throw new Exception("Status of volume not available");
                    }

                    TbVolume recordVolume = new TbVolume();
                    recordVolume.setName(volumeName);
                    recordVolume.setSize(volumeSize);
                    recordVolume.setFormat("unknown");
                    recordVolume.setComputeId(computeId);
                    recordVolume.setStatus(ResourceStatus.CREATED);
                    recordVolume.setCinderVolumeId(volume.getId());
                    recordVolume.setProjectId(projectId);
                    recordVolume.setOrgId(orgId);
                    recordVolume.setZoneName(compute.getZoneName());
                    recordVolume.setCreatedAt(LocalDateTime.now());
                    recordVolume.setUpdatedAt(LocalDateTime.now());
                    VolumeManager.add(recordVolume);
                }
            }

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
