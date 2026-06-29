package com.dev.dbaas.worker.processor.auth;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.manager.SessionManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.protocol.AppPacket;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.utils.NetworkUtil;
import com.dev.dbaas.utils.security.AESAuth;
import com.dev.dbaas.worker.job.AppJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationProcessor implements ProcessorBase<AppJob> {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationProcessor.class);

    public static final int SERVER_INTERNAL_ERROR = -100;
    public static final int ACCESS_TOKEN_INVALID = -4;
    public static final int NOT_FOUND_AGENT = -5;
    public static final int LOGIN_ACCESS_TOKEN_SUCCESSFUL = 1;

    @Override
    public boolean process(AppJob job) throws Exception {

        LOGGER.info("[CMS] " + job.getPacket().getData() + ", " + job.getPacket().getMessageId());
        Account account = null;
        AppPacket packet = job.getPacket();
        JSONObject data = new JSONObject();
        int result = SERVER_INTERNAL_ERROR;

        boolean isPass = true;
        job.getPacket().decodePacket();

        String agentId  = packet.optStringField("agentId", "");
        String accessToken = packet.optStringField("accessToken", "");
        String language = packet.optStringField("language", "en");

        if(agentId.isEmpty() || accessToken.isEmpty()){
            result = ACCESS_TOKEN_INVALID;
            isPass = false;
            LOGGER.info("Require agentId and accessToken");
        }

        if (isPass) {
            String channelUuid = job.getClient().getSessionId().toString();
            String clientAddress = NetworkUtil.parseIpAddress(job.getClient().getRemoteAddress().toString());

            TbAgent agent = AgentManager.findById(agentId);
            if(agent == null){
                result = NOT_FOUND_AGENT;
                isPass = false;
                LOGGER.info("Token is invalid");
            }

            if(isPass){
                String decryptInfo = AESAuth.decrypt(accessToken, agent.getEncryptedKey());
                LOGGER.info("decryptInfo = "+decryptInfo);
                if(!decryptInfo.equalsIgnoreCase(agentId)){
                    result = ACCESS_TOKEN_INVALID;
                    isPass = false;
                    LOGGER.info("Token is invalid");
                }

                if(isPass){
                    SessionManager.addSession(job.getClient().getSessionId(), agentId);
                    account = AgentManager.getAccount(agentId);
                    if (account.isEmpty()) {
                        LOGGER.info("Account is empty "+agentId);
                        account.setAgentVersion(agent.getAgentVersion());
                        account.setComputeId(agent.getComputeId());
                        account.setEncryptedKey(agent.getEncryptedKey());
                        account.setOrgId(agent.getOrgId());
                        account.setProjectId(agent.getProjectId());
                        account.setAgentFirmwareId(agent.getAgentFirmwareId());
                        account.setInstanceId(agent.getInstanceId());
                        account.setAgentId(agent.getId());
                    }
                    if (!account.isExisted(job.getClient())) {
                        account.getClients().add(job.getClient());
                    }
                    account.setLanguageCode(job.getClient(), language);

                    //Account item = AgentManager.getAccount(agentId);
                    LOGGER.info(" clients size : "+account.getClients().size());
                    result = LOGIN_ACCESS_TOKEN_SUCCESSFUL;
                }
            }
        }

        LOGGER.info("Send response for user, " + language + "," + result);

        ResponsePacket responsePacket = new ResponsePacket();
        responsePacket.setServiceId(AppServiceType.AUTHENTICATION);
        responsePacket.setResult(result);
        responsePacket.setMessage("");
        responsePacket.setData(data.toString());
        responsePacket.setMessageId(packet.getMessageId());
        responsePacket.setTime(System.currentTimeMillis());
        LOGGER.info("Result, " + result);
        Account.receivePacketWithChannel(job.getClient(), responsePacket);
        return true;
    }

    private List<String> parseScopes(String scopeStrs) {

        List<String> scopes = new ArrayList<>();
        String[] scopeArr = scopeStrs.split(";");
        for (int i = 0; i < scopeArr.length; i++) {
            scopes.add(scopeArr[i]);
        }
        return scopes;
    }
}
