package com.ctel.dbaas.controller.message_queue.handler;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.dto.common.TaskManagerRequest;
import com.ctel.dbaas.service.InstanceService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class UpdateStatusChangeConfigHandler implements HandlerBase<TaskManagerRequest> {

    @Autowired
    private InstanceService instanceService;

    @Override
    public void handle(TaskManagerRequest req) {
        JSONObject input = req.getJsonData();

        String agentId = input.optString("agentId");
        String groupConfigId = input.optString("groupConfigId");
        String status = input.optString("status");

        if (StringUtils.isAnyBlank(agentId, status, groupConfigId)) {
            log.warn("Message invalid - data[{}]", input);
            return;
        }

        if (!Constant.COMPLETED.equals(status) && !Constant.ERROR.equals(status)) {
            log.warn("updateStatusApplyConfig:: data[{}]", input);
            return;
        }

        instanceService.updateStatusApplyConfig(agentId, groupConfigId);
    }
}
