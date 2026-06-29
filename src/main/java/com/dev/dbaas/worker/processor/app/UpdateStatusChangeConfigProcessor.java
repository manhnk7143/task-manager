package com.dev.dbaas.worker.processor.app;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.config.APIServiceType;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.ClusterManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.AppPacket;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.AppJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class UpdateStatusChangeConfigProcessor implements ProcessorBase<AppJob> {

    private static final Logger LOGGER = Logger.getLogger(UpdateStatusChangeConfigProcessor.class);

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
        responsePacket.setServiceId(AppServiceType.UPDATE_STATUS_CHANGE_CONFIG);

        packet.decodePacket();
        JSONObject data = new JSONObject();
        data.put("serviceId", APIServiceType.UPDATE_STATUS_CHANGE_CONFIG);
        data.put("data", packet.getJsonData().toString());
        data.put("messageId", "");
        data.put("time", System.currentTimeMillis());

        ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);
        int result = SUCCESSFUL;

        LOGGER.info("Result : " + result);
        responsePacket.setResult(result);
        responsePacket.setMessage("");
        responsePacket.setMessageId(packet.getMessageId());
        account.receivePacket(responsePacket);
        return true;
    }
}
