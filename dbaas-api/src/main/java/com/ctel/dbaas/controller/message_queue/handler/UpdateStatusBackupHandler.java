package com.ctel.dbaas.controller.message_queue.handler;

import com.ctel.dbaas.dto.common.TaskManagerRequest;
import com.ctel.dbaas.service.BackupRestoreService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateStatusBackupHandler implements HandlerBase<TaskManagerRequest> {

    @Autowired
    private BackupRestoreService backupRestoreService;

    @Override
    public void handle(TaskManagerRequest req) {
        JSONObject input = req.getJsonData();
        String backupId = input.optString("backupId");
        if (StringUtils.isNotBlank(backupId)) {
            backupRestoreService.deleteRotateBackupRecord(backupId);
        }
    }
}
