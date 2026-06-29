package com.dev.dbaas.worker.processor.control.api_gateway;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.manager.WorkFlowManager;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateApiGatewayStandaloneProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(CreateApiGatewayStandaloneProcessor.class);

    @Override
    public boolean process(ControlJob job) throws Exception {
        LOGGER.info("[4CMS] " + job.getServiceId() + " - " + job.getData());
        job.decodePacket();
        JSONObject input = job.getJsonData();
        String workFlowId = WorkFlowManager.getInstance().startCreateApiGatewayStandalone(input);
        LOGGER.info("WorkFlowId = " + workFlowId);
        return true;
    }
}
