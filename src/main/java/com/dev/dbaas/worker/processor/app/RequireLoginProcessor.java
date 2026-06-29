package com.dev.dbaas.worker.processor.app;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.AppJob;
import org.apache.log4j.Logger;

public class RequireLoginProcessor implements ProcessorBase<AppJob> {

    private static final Logger LOGGER = Logger.getLogger(RequireLoginProcessor.class);

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
        ResponsePacket responsePacket = new ResponsePacket();
        responsePacket.setServiceId(AppServiceType.REQUIRE_LOGIN);
        responsePacket.setResult(1);
        responsePacket.setMessage("");
        responsePacket.setMessageId("");
        Account.receivePacketWithChannelDetail(job.getClient(), responsePacket);
        return true;
    }
}
