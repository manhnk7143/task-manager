package com.dev.dbaas.worker.processor.control.kafka;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.manager.WorkFlowManager;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateKafkaClusterProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(CreateKafkaClusterProcessor.class);

    private static final int SERVER_INTERNAL_ERROR = -100;
    private static final int SUCCESSFUL = 1;
    private static final int REQUIRE_NAMESPACE = -5;
    private static final int REQUIRE_STATUS = -6;
    private static final int REQUIRE_BACKUP_ID = -7;
    private static final int REQUIRE_ACCOUNT_ID = -8;
    private static final int NOT_FOUND_ACCOUNT_MEMBER = -9;
    private static final int PERMISSION_DENIED = -99;

    @Override
    public boolean process(ControlJob job) throws Exception {
        LOGGER.info("[4CMS] " + job.getServiceId() + " - " + job.getData());
        job.decodePacket();
        JSONObject input = job.getJsonData();
        String workFlowId = WorkFlowManager.getInstance().startCreateKafkaCluster(input);
        LOGGER.info("WorkFlowId = " + workFlowId);
        return true;
    }
}
