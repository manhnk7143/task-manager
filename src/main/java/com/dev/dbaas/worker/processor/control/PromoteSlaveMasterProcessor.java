package com.dev.dbaas.worker.processor.control;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class PromoteSlaveMasterProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(PromoteSlaveMasterProcessor.class);

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

        LOGGER.info("[4CMS] PromoteMasterSlaveProcessor" + job.getServiceId()+" - "+job.getData());
        job.decodePacket();

        JSONObject input = job.getJsonData();
        String instanceId = input.optString("instanceId");
        String regionId = input.optString("regionId");
        String masterComputeId = input.optString("masterComputeId");
        String datastoreModeCode = input.optString("datastoreModeCode");
        String newMasterIp = input.optString("newMasterIp");
        String projectId = input.optString("projectId");
        String orgId = input.optString("orgId");
        String userId = input.optString("userId");
        JSONArray slaveComputeIds = new JSONArray(input.optString("slaveComputeIds"));
        JSONObject overridesConfig = input.optJSONObject("overridesConfig");

        List<TbAgent> agents = AgentManager.findByInstanceId(instanceId);
        if(agents == null || agents.isEmpty()){
            LOGGER.warn("Not found agents by instanceId = "+instanceId);
            return false;
        }

        for(int i = 0; i < slaveComputeIds.length(); i++){

            String computeId = slaveComputeIds.getString(i);
            List<TbAgent> itemAgents = AgentManager.findByComputeId(computeId);

            if(itemAgents != null){
                for(TbAgent itemAgent : itemAgents){

                    JSONObject dataJson = new JSONObject();
                    dataJson.put("mode","slave");
                    dataJson.put("master_ip",newMasterIp);

                    LOGGER.info("Send to agent slave "+itemAgent.getId()+" - "+dataJson.toString());

                    Account account = AgentManager.getAccount(itemAgent.getId());
                    ResponsePacket responsePacket = new ResponsePacket();
                    responsePacket.setServiceId(AppServiceType.PROMOTE_SLAVE_MASTER);
                    responsePacket.setResult(1);
                    responsePacket.setMessage("");
                    responsePacket.setMessageId(System.currentTimeMillis()+"");
                    responsePacket.setData(dataJson.toString());
                    account.receivePacket(responsePacket);
                }
            }
        }

        List<TbAgent> itemMasterAgents = AgentManager.findByComputeId(masterComputeId);
        if(itemMasterAgents != null){
            for(TbAgent itemAgent : itemMasterAgents){

                JSONObject dataJson = new JSONObject();
                dataJson.put("mode","master");
                dataJson.put("master_ip",newMasterIp);

                LOGGER.info("Send to agent master "+itemAgent.getId()+" - "+dataJson.toString());

                Account account = AgentManager.getAccount(itemAgent.getId());
                ResponsePacket responsePacket = new ResponsePacket();
                responsePacket.setServiceId(AppServiceType.PROMOTE_SLAVE_MASTER);
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
