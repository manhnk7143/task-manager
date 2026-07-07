package com.ctel.dbaas.socket.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.ctel.dbaas.dto.cmc_cloud.ProjectInfo;
import com.ctel.dbaas.dto.cmc_cloud.UserInfo;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.entity.dbaas.SocketInfoEntity;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.SocketInfoRepository;
import com.ctel.dbaas.service.CmcCloudService;
import com.ctel.dbaas.socket.model.AuthClientRequest;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;

@Log4j2
@Component
public class AuthRequestHandler implements DataListener<AuthClientRequest> {

    @Autowired
    private CmcCloudService cmcCloudService;

    @Autowired
    private SocketInfoRepository socketInfoRepository;

    @Override
    public void onData(SocketIOClient client, AuthClientRequest authReq, AckRequest ackRequest) throws Exception {
        UserInfo userCmc;
        ProjectInfo[] projectInfos;
        log.info("AuthSocket request[{}]", authReq);
        try {
            userCmc = cmcCloudService.getUserInfo(authReq.getToken());
            if (userCmc == null) {
                throw new AppException(new ErrorResponse("user info is null"));
            }
            projectInfos = cmcCloudService.getProjects(authReq.getToken(), authReq.getRegionId());
            if (projectInfos == null) {
                throw new AppException(new ErrorResponse("project info is null"));
            }
        } catch (Exception e) {
            log.error("ERROR AuthRequest::onData => msg[{}]", e.getMessage());
            String messageErr;
            if (e instanceof AppException appException) {
                messageErr = appException.getMessage();
            } else {
                messageErr = "system error";
            }
            client.sendEvent("authen", new JSONObject(Collections.singletonMap("messageError", messageErr)).toString());
            client.disconnect();
            return;
        }

        // token, region has verified
        // validate userId, orgId
        String orgId = userCmc.getUsername().split("_")[0];
        if (!Objects.equals(authReq.getUserId(), userCmc.getUsername()) || !Objects.equals(authReq.getOrgId(), orgId)) {
            client.sendEvent("authen", new JSONObject(Collections.singletonMap("messageError", "data invalid")).toString());
            client.disconnect();
            return;
        }

        SocketInfoEntity socketInfo = new SocketInfoEntity();
        socketInfo.setSocketClientId(client.getSessionId().toString());
        socketInfo.setOrgId(authReq.getOrgId());
        socketInfo.setRegionId(authReq.getRegionId());
        socketInfo.setProjectId(authReq.getProjectId());
        socketInfo.setUserId(authReq.getUserId());
        socketInfoRepository.save(socketInfo);

        client.sendEvent("authen", new JSONObject(Collections.singletonMap("message", "success")).toString());
    }
}
