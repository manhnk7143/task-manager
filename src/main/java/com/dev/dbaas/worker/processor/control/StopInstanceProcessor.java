package com.dev.dbaas.worker.processor.control;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.List;

public class StopInstanceProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(StopInstanceProcessor.class);

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

        LOGGER.info("[4CMS] StopInstanceProcessor" + job.getServiceId()+" - "+job.getData());
        job.decodePacket();

        JSONObject input = job.getJsonData();
        String instanceId = input.optString("instanceId");

        List<TbAgent> agents = AgentManager.findByInstanceId(instanceId);
        if(agents == null || agents.isEmpty()){
            LOGGER.warn("Not found agents by instanceId = "+instanceId);
            return false;
        }

        for(TbAgent agent : agents){
            Account account = AgentManager.getAccount(agent.getId());
            if(account != null){
                LOGGER.info("Send message to agentId "+agent.getId()+", "+account.getClients().size());
                JSONObject dataJson = new JSONObject();
                dataJson.put("agentId", agent.getId());

                ResponsePacket responsePacket = new ResponsePacket();
                responsePacket.setServiceId(AppServiceType.STOP_INSTANCE);
                responsePacket.setResult(1);
                responsePacket.setMessage("");
                responsePacket.setMessageId(System.currentTimeMillis()+"");
                responsePacket.setData(dataJson.toString());
                account.receivePacket(responsePacket);
            }
        }
        return true;
    }
}
