package com.ctel.dbaas.controller.message_queue.handler;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.enums.InstanceAction;
import com.ctel.dbaas.common.enums.InstanceStatus;
import com.ctel.dbaas.dto.common.TaskManagerRequest;
import com.ctel.dbaas.entity.dbaas.ComputeEntity;
import com.ctel.dbaas.entity.dbaas.InstanceEntity;
import com.ctel.dbaas.repository.dbaas.ComputeRepository;
import com.ctel.dbaas.repository.dbaas.InstanceRepository;
import com.ctel.dbaas.service.ActionService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class UpdateStatusComputeHandler implements HandlerBase<TaskManagerRequest> {

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private ActionService actionService;

    @SneakyThrows
    @Override
    public void handle(TaskManagerRequest req) {
        JSONObject input = req.getJsonData();

        String computeId = input.optString("computeId");
        String status = input.optString("status");
        String computeInfos = input.optString("computeInfos");
        if (StringUtils.isNotEmpty(computeId) && StringUtils.isNotEmpty(status)) {

            JSONObject computeJson = new JSONObject(computeInfos);
            String mode = computeJson.optString("mode");
            String computeRole = computeJson.optString("compute_role");

            ComputeEntity compute = computeRepository.findById(computeId).orElse(null);
            if (compute != null) {
                InstanceEntity instance = instanceRepository.findById(compute.getInstanceId()).orElse(null);

                if (instance == null) {
                    log.error("Instance[{}] not exist", compute.getInstanceId());
                    return;
                }

                if (InstanceStatus.DELETING.getStatus().equalsIgnoreCase(instance.getStatus())) {
                    return;
                }

                if (StringUtils.isNotBlank(computeRole)) {
                    compute.setRole(computeRole);
                }
                compute.setStatus(status);
                computeRepository.save(compute);

                List<ComputeEntity> computes = computeRepository.findAllByInstanceId(compute.getInstanceId());
                if (computes != null) {
                    String currentStatusInstance = instance.getStatus();

                    boolean containsStatusRunning = false;
                    boolean containsWithoutRunning = false;

                    for (ComputeEntity tbCompute : computes) {
                        String statusCompute = tbCompute.getStatus();
                        if (InstanceStatus.RUNNING.getStatus().equalsIgnoreCase(statusCompute)) {
                            containsStatusRunning = true;
                        }

                        if (!InstanceStatus.RUNNING.getStatus().equalsIgnoreCase(statusCompute)) {
                            containsWithoutRunning = true;
                        }
                    }

                    if (currentStatusInstance.equalsIgnoreCase(InstanceStatus.WAITING.getStatus())) {
                        if (containsStatusRunning && !containsWithoutRunning) {
                            instance.setStatus(InstanceStatus.RUNNING.getStatus());
                            instanceRepository.save(instance);
                            if (!instance.getApplyGroupConfig()) {
                                Map<String, Object> dataReq = new HashMap<>();
                                dataReq.put("groupConfigId", instance.getGroupConfigurationId());
                                RequestInfo requestInfo = RequestInfo.builder()
                                        .orgId(instance.getOrgId())
                                        .regionId(instance.getRegionId())
                                        .projectId(instance.getProjectId())
                                        .build();
                                actionService.executeAction(instance.getId(), InstanceAction.CHANGE_GROUP_CONFIG, dataReq, requestInfo);
                                instance.setApplyGroupConfig(true);
                                instanceRepository.save(instance);
                            }
                        }
                    } else {
                        if (containsStatusRunning && containsWithoutRunning) {
                            instance.setStatus(InstanceStatus.WARNING.getStatus());
                        } else if (containsStatusRunning) {
                            instance.setStatus(InstanceStatus.RUNNING.getStatus());
                        } else {
                            instance.setStatus(InstanceStatus.SHUTDOWN.getStatus());
                        }

                        instanceRepository.save(instance);
                    }

                }
            }
        }
    }
}
