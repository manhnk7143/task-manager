package com.ctel.dbaas.controller.message_queue.handler;

import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.dto.cmc_cloud.LogResourceReq;
import com.ctel.dbaas.dto.common.TaskManagerRequest;
import com.ctel.dbaas.entity.dbaas.ComputeEntity;
import com.ctel.dbaas.entity.dbaas.DatastoreEntity;
import com.ctel.dbaas.entity.dbaas.InstanceEntity;
import com.ctel.dbaas.entity.dbaas.VolumeEntity;
import com.ctel.dbaas.repository.dbaas.ComputeRepository;
import com.ctel.dbaas.repository.dbaas.DatastoreRepository;
import com.ctel.dbaas.repository.dbaas.InstanceRepository;
import com.ctel.dbaas.repository.dbaas.VolumeRepository;
import com.ctel.dbaas.service.CmcCloudService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class UpdateStatusInstanceHandler implements HandlerBase<TaskManagerRequest> {

    @Autowired
    private CmcCloudService cmcCloudService;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private DatastoreRepository datastoreRepository;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private VolumeRepository volumeRepository;

    @SneakyThrows
    @Override
    public void handle(TaskManagerRequest req) {
        JSONObject input = req.getJsonData();
        log.info("UpdateStatusInstanceHandler params : [{}]", req.getJsonData());

        String instanceId = input.optString("instanceId");
        String status = input.optString("status");
        String action = input.optString("action");
        InstanceEntity instance = instanceRepository.findById(instanceId).orElse(new InstanceEntity());
        DatastoreEntity datastore = datastoreRepository.findById(instance.getDatastoreId()).orElse(new DatastoreEntity());

        if (instance.getId() == null) {
            log.warn("UpdateStatusInstanceHandler => not found instance with instanceId[{}]", instanceId);
            return;
        }

        if (datastore.getId() == null) {
            log.warn("UpdateStatusInstanceHandler => not found datastore with datastoreId[{}]", instance.getDatastoreId());
            return;
        }

        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(datastore.getCode());

        if ("done".equalsIgnoreCase(status)) {
            JSONArray volumeCinderIds = input.optJSONArray("volumeCinderIds");
            JSONArray serverNovaIds = input.optJSONArray("serverNovaIds");

            List<LogResourceReq> lstLogResourceReqs = new ArrayList<>();
            if (volumeCinderIds != null && volumeCinderIds.length() > 0) {
                for (int i = 0; i < volumeCinderIds.length(); i++) {
                    String volumeId = volumeCinderIds.getString(i);

                    VolumeEntity volumeEntity = volumeRepository.findFirstByCinderVolumeId(volumeId);
                    if (volumeEntity != null) {
                        LogResourceReq logResource = new LogResourceReq();
                        logResource.setTeamCode(instance.getOrgId());
                        logResource.setServiceType("EV");
                        logResource.setParentId(instanceId);
                        logResource.setItemId(volumeEntity.getId());
                        logResource.setComputeId("");
                        logResource.setRegionId(instance.getRegionId());
                        logResource.setProjectId(instance.getProjectId());

                        Map<String, Object> metaData = new HashMap<>();
                        metaData.put("volumeId", volumeId);
                        logResource.setMetaData(metaData);

                        lstLogResourceReqs.add(logResource);
                        cmcCloudService.sendLogResource(instanceId, logResource);
                    }
                }
            }

            if (serverNovaIds != null && serverNovaIds.length() > 0) {
                for (int i = 0; i < serverNovaIds.length(); i++) {
                    String serverId = serverNovaIds.getString(i);

                    ComputeEntity computeEntity = computeRepository.findFirstByNovaInstanceId(serverId);
                    if (computeEntity != null) {
                        LogResourceReq logResource = new LogResourceReq();
                        logResource.setTeamCode(instance.getOrgId());
                        logResource.setServiceType(datastoreSupport.getServiceType());
                        logResource.setParentId(instanceId);
                        logResource.setItemId(computeEntity.getId());
                        logResource.setComputeId(serverId);
                        logResource.setRegionId(instance.getRegionId());
                        logResource.setProjectId(instance.getProjectId());

                        lstLogResourceReqs.add(logResource);
                        cmcCloudService.sendLogResource(instanceId, logResource);
                    }
                }
            }
            log.info("send log resource : {}", lstLogResourceReqs);
        }
    }
}
