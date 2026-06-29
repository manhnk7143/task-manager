package com.dev.dbaas.worker.processor.app;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.database.enties.TbBackup;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.BackupManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.AppPacket;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.AppJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class GetAgentInfoProcessor implements ProcessorBase<AppJob> {

    private static final Logger LOGGER = Logger.getLogger(GetAgentInfoProcessor.class);

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
        responsePacket.setServiceId(AppServiceType.GET_AGENT_INFO);

        int result = SERVER_INTERNAL_ERROR;

        packet.decodePacket();
        String status = packet.optStringField("status", "");
        String backupId = packet.optStringField("backupId", "");
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
            if(backup == null){
                LOGGER.error("Not found backup by Id = "+backupId);
                isPass = false;
            }
            if(isPass){

                backup.setStatus(status);
                backup.setSize(size);
                BackupManager.update(backup);

                JSONObject dataResponse = new JSONObject();
                dataResponse.put("status", status);
                dataResponse.put("agentId", account.getAgentId());
                responsePacket.setData(dataResponse.toString());

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
