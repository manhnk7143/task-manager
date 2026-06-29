package com.dev.dbaas.worker.processor.app;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.config.APIServiceType;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.database.enties.TbBackup;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.BackupManager;
import com.dev.dbaas.manager.ClusterManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.AppPacket;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.AppJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class UpdateStatusBackupProcessor implements ProcessorBase<AppJob> {

    private static final Logger LOGGER = Logger.getLogger(UpdateStatusBackupProcessor.class);

    private static final int SERVER_INTERNAL_ERROR = -100;
    private static final int SUCCESSFUL = 1;
    private static final int REQUIRE_NAMESPACE = -5;
    private static final int REQUIRE_STATUS = -6;
    private static final int REQUIRE_BACKUP_ID = -7;
    private static final int REQUIRE_ACCOUNT_ID = -8;
    private static final int NOT_FOUND_ACCOUNT_MEMBER = -9;
    private static final int PERMISSION_DENIED = -99;

    @Override
    public boolean process(AppJob job) throws Exception {

        LOGGER.info("[4CMS] " + job.getPacket().getData());
        String jobId = "JobId : " + job.getId();
        LOGGER.info(jobId);

        Account account = job.getAccount();
        AppPacket packet = job.getPacket();

        ResponsePacket responsePacket = new ResponsePacket();
        responsePacket.setServiceId(AppServiceType.UPDATE_STATUS_BACKUP);

        int result = SERVER_INTERNAL_ERROR;

        packet.decodePacket();
        LOGGER.info("===== UpdateStatusBackupProcessor: " + packet.getJsonData().toString());
        String status = packet.optStringField("status", "");
        String backupId = packet.optStringField("backupId", "");
        String message = packet.optStringField("message", "");
        long size = packet.optLongField("size", -1);

        boolean isPass = true;
        if (status.isEmpty()) {
            result = REQUIRE_STATUS;
            isPass = false;
        }
        if (isPass && backupId.isEmpty()) {
            result = REQUIRE_BACKUP_ID;
            isPass = false;
        }
        if (isPass) {
            // update status for instance
            TbBackup backup = BackupManager.findById(backupId);
            if (backup == null) {
                LOGGER.error("Not found backup by Id = " + backupId);
                isPass = false;
            }
            if (isPass) {
                backup.setMessage(message);
                backup.setStatus(status);
                if (size > 0) {
                    backup.setSize(size);
                }
                BackupManager.update(backup);

                JSONObject dataResponse = new JSONObject();
                dataResponse.put("status", status);
                dataResponse.put("agentId", account.getAgentId());
                responsePacket.setData(dataResponse.toString());

                // send event to API
                JSONObject data = new JSONObject();
                data.put("serviceId", APIServiceType.UPDATE_STATUS_BACKUP);

                JSONObject bodyData = new JSONObject();
                bodyData.put("backupId", backupId);
                bodyData.put("status", status);
                bodyData.put("message", message);

                data.put("data", bodyData.toString());
                data.put("messageId", "");
                data.put("time", packet.getTime());

                ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);

                result = SUCCESSFUL;
            }
        }

        LOGGER.info("Result : " + result);
        responsePacket.setResult(result);
        responsePacket.setMessage("");
        responsePacket.setMessageId(packet.getMessageId());
        account.receivePacket(responsePacket);
        return true;
    }
}
