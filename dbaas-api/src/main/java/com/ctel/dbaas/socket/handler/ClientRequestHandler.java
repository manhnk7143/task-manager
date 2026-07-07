package com.ctel.dbaas.socket.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.context.I18nContext;
import com.ctel.dbaas.common.enums.InstanceAction;
import com.ctel.dbaas.entity.dbaas.SocketInfoEntity;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.SocketInfoRepository;
import com.ctel.dbaas.service.ActionService;
import com.ctel.dbaas.socket.model.ClientRequest;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Locale;

@Log4j2
@Component
@NoArgsConstructor
public class ClientRequestHandler implements DataListener<ClientRequest> {

    @Autowired
    private ActionService actionService;

    @Autowired
    private SocketInfoRepository socketInfoRepository;

    @Override
    public void onData(SocketIOClient client, ClientRequest request, AckRequest ackRequest) throws Exception {
        SocketInfoEntity socketInfo = socketInfoRepository.findFirstBySocketClientId(client.getSessionId().toString());
        if (socketInfo == null) {
            client.sendEvent("chatevent", new JSONObject(Collections.singletonMap("messageError", "Unauthorized")).toString());
            client.disconnect();
            return;
        }

        InstanceAction action = InstanceAction.get(request.getAction());
        if (action == null || !action.equals(InstanceAction.DB_ACTION)) {
            client.sendEvent("chatevent", new JSONObject(Collections.singletonMap("messageError", "action invalid")).toString());
            return;
        }
        log.info("onData chatevent Client[{}] - data[{}]", client.getSessionId(), request);

        String lang = "";
        if (StringUtils.isBlank(lang) || !I18nContext.LANGUAGE_SUPPORT.contains(lang)) {
            lang = "en";
        }
        Locale locale = new Locale(lang);

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setOrgId(socketInfo.getOrgId());
        requestInfo.setRegionId(socketInfo.getRegionId());
        requestInfo.setProjectId(socketInfo.getProjectId());
        requestInfo.setUserId(socketInfo.getUserId());
        requestInfo.setLocale(locale);
        requestInfo.setClientSocketId(client.getSessionId().toString());

        try {
            actionService.executeAction(request.getInstanceId(), action, request.getRequestData(), requestInfo);
        } catch (Exception e) {
            if (e instanceof AppException ex) {
                client.sendEvent("chatevent", new JSONObject(Collections.singletonMap("messageError", ex.getMessage())).toString());
            } else {
                log.error("ERROR onData event chatevent: message[{}]", e.getMessage());
                client.sendEvent("chatevent", new JSONObject(Collections.singletonMap("messageError", "system error")).toString());
            }
        }
    }
}
