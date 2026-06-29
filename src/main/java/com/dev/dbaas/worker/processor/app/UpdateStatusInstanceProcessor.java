package com.dev.dbaas.worker.processor.app;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.worker.job.AppJob;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UpdateStatusInstanceProcessor implements ProcessorBase<AppJob> {

    private static final Logger LOGGER = Logger.getLogger(UpdateStatusInstanceProcessor.class);

    private static final int SERVER_INTERNAL_ERROR = -100;
    private static final int SUCCESSFUL = 1;
    private static final int REQUIRE_NAMESPACE = -5;
    private static final int REQUIRE_STATUS = -6;
    private static final int NOT_FOUND_GROUP = -7;
    private static final int REQUIRE_ACCOUNT_ID = -8;
    private static final int NOT_FOUND_ACCOUNT_MEMBER = -9;
    private static final int PERMISSION_DENIED = -99;

    @Override
    public boolean process(AppJob job) throws Exception {

        LOGGER.info("[4CMS] " + job.getPacket().getData());

        String jobId = "JobId : " + job.getId();
        LOGGER.info(jobId);

//        Account account = job.getAccount();
//        AppPacket packet = job.getPacket();
//
//        ResponsePacket responsePacket = new ResponsePacket();
//        responsePacket.setServiceId(AppServiceType.UPDATE_STATUS_INSTANCE);
//
//        int result = SERVER_INTERNAL_ERROR;
//
//        packet.decodePacket();
//        String status = packet.optStringField("status", "");
//
//        boolean isPass = true;
//        if (status.isEmpty()) {
//            result = REQUIRE_STATUS;
//            isPass = false;
//        }
//        if (isPass) {
//
//            LOGGER.info("InstanceId : "+account.getInstanceId()+" - agentId : "+account.getAgentId()+" - "+status);
//
//            // update status for instance
//            TbInstance instance = InstanceManager.findById(account.getInstanceId());
//            if(instance != null && instance.getStatus() != null && !instance.getStatus().equalsIgnoreCase(status)){
//                LOGGER.info("Update state instance "+account.getInstanceId()+" to "+status.toUpperCase());
//                instance.setStatus(status.toUpperCase());
//                instance.setUpdatedAt(LocalDateTime.now());
//                InstanceManager.update(instance);
//            }
//
//            // update all compute
//            List<TbCompute> computes = ComputeManager.findByInstanceId(account.getInstanceId());
//            if(computes != null){
//                for(TbCompute compute : computes){
//                    compute.setStatus(status.toUpperCase());
//                    compute.setUpdatedAt(LocalDateTime.now());
//                    ComputeManager.update(compute);
//                }
//            }
//
//            // update status
//            TbAgentHeartbeat agentHeartbeat = AgentHeartbeatManager.findByAgentId(account.getAgentId());
//            if(agentHeartbeat == null){
//                agentHeartbeat = new TbAgentHeartbeat();
//                agentHeartbeat.setId(UUID.randomUUID().toString());
//                agentHeartbeat.setInstanceId(account.getInstanceId());
//                agentHeartbeat.setAgentId(account.getAgentId());
//                agentHeartbeat.setComputeId(account.getComputeId());
//                agentHeartbeat.setProjectId(account.getProjectId());
//                agentHeartbeat.setOrgId(account.getOrgId());
//                agentHeartbeat.setCreatedAt(LocalDateTime.now());
//                agentHeartbeat.setUpdatedAt(LocalDateTime.now());
//                agentHeartbeat.setDeletedAt(null);
//                //AgentHeartbeatManager.add(agentHeartbeat);
//            }
//            else{
//                agentHeartbeat.setUpdatedAt(LocalDateTime.now());
//                AgentHeartbeatManager.update(agentHeartbeat);
//            }
//
//            JSONObject dataResponse = new JSONObject();
//            dataResponse.put("status", status);
//            dataResponse.put("agentId", account.getAgentId());
//            responsePacket.setData(dataResponse.toString());
//
//            result = SUCCESSFUL;
//        }
//
//        LOGGER.info("Result : " + result);
//        responsePacket.setResult(result);
//        responsePacket.setMessage("");
//        responsePacket.setMessageId(packet.getMessageId());
//
//        LOGGER.info("Clients: "+account.getClients().size());
//        account.receivePacket(responsePacket);
        return true;
    }
}
