package com.ctel.dbaas.controller.message_queue.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.common.enums.DbAction;
import com.ctel.dbaas.common.enums.InstanceAction;
import com.ctel.dbaas.dto.common.TaskManagerRequest;
import com.ctel.dbaas.entity.dbaas.AgentEntity;
import com.ctel.dbaas.entity.dbaas.ComputeEntity;
import com.ctel.dbaas.entity.dbaas.DataReceiveAgentEntity;
import com.ctel.dbaas.entity.dbaas.SocketInfoEntity;
import com.ctel.dbaas.repository.dbaas.AgentRepository;
import com.ctel.dbaas.repository.dbaas.ComputeRepository;
import com.ctel.dbaas.repository.dbaas.DataReceiveAgentRepository;
import com.ctel.dbaas.repository.dbaas.SocketInfoRepository;
import com.ctel.dbaas.service.ActionService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Component
@Log4j2
public class ResponseDbActionHandler implements HandlerBase<TaskManagerRequest> {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private DataReceiveAgentRepository dataReceiveAgentRepository;

    @Autowired
    private SocketIOServer socketIOServer;

    @Autowired
    private SocketInfoRepository socketInfoRepository;

    @Autowired
    private ActionService actionService;

    @SneakyThrows
    @Override
    public void handle(TaskManagerRequest req) {
        try {
            JSONObject input = new JSONObject(req.getData());
            String response = input.optString("response", "{}");
            String request = input.optString("request", "{}");
            String datastore = input.optString("datastore", "");
            String agentId = input.optString("agentId", "");
            DatastoreSupport datastoreSupport = DatastoreSupport.get(datastore);

            JSONObject clientInfo = new JSONObject(request).optJSONObject("client_info");
            if (clientInfo != null) {
                String clientSocketId = clientInfo.optString("clientSocketId");
                log.info("clientSocketId[{}] - request[{}] - response[{}]", clientSocketId, request, response);
                SocketInfoEntity socketInfo = socketInfoRepository.findFirstBySocketClientId(clientSocketId);
                if (socketInfo != null) {
                    List<SocketInfoEntity> lstClient = socketInfoRepository.findAllByUserIdAndOrgIdAndRegionId(
                            socketInfo.getUserId(), socketInfo.getOrgId(), socketInfo.getRegionId());
                    for (SocketInfoEntity socketInfoEntity : lstClient) {
                        SocketIOClient client = socketIOServer.getClient(UUID.fromString(socketInfoEntity.getSocketClientId()));
                        if (client != null) {
                            client.sendEvent("chatevent", response);
                        }
                    }
                }

                if (datastoreSupport != null) {
                    String action = new JSONObject(response).optJSONObject("data").getString("type");
                    switch (datastoreSupport) {
                        case KAFKA -> {
                            DbAction.Kafka kafkaAction = DbAction.Kafka.get(action);
                            if (kafkaAction != null) {
                                switch (kafkaAction) {
                                    case EDIT_CLUSTER_CONFIG -> {
                                        AgentEntity agent = agentRepository.findById(agentId).orElse(null);
                                        if (agent != null) {
                                            ComputeEntity compute = computeRepository.findById(agent.getComputeId()).orElse(null);
                                            if (compute != null) {
                                                String instanceId = agent.getInstanceId();
                                                RequestInfo reqCtx = new RequestInfo();
                                                reqCtx.setRegionId(compute.getRegionId());
                                                reqCtx.setProjectId(compute.getProjectId());
                                                reqCtx.setOrgId(compute.getOrgId());
                                                actionService.executeAction(instanceId, InstanceAction.RESTART_INSTANCE, new HashMap<>(), reqCtx);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                JSONObject responseJson = new JSONObject(response).optJSONObject("data");
                AgentEntity agent = agentRepository.findById(agentId).orElse(null);
                if (agent != null) {
                    ComputeEntity compute = computeRepository.findById(agent.getComputeId()).orElse(null);
                    if (compute != null) {
                        DataReceiveAgentEntity dataReceiveAgent = dataReceiveAgentRepository.findFirstByInstanceId(compute.getInstanceId());
                        if (dataReceiveAgent == null) {
                            dataReceiveAgent = new DataReceiveAgentEntity();
                            dataReceiveAgent.setInstanceId(compute.getInstanceId());
                        }

                        String responseType = responseJson.optString("type");
                        JSONArray result = responseJson.optJSONArray("result") != null ? responseJson.optJSONArray("result") : new JSONArray();

                        if (responseType.equalsIgnoreCase("list_user")) {
                            dataReceiveAgent.setUsersInfo(result.toString());
                        } else if (responseType.equalsIgnoreCase("list_database")) {
                            dataReceiveAgent.setDatabasesInfo(result.toString());
                        }

                        dataReceiveAgentRepository.save(dataReceiveAgent);
                    }
                }
            }

        } catch (Exception e) {
            log.error("ERROR ResponseDbActionHandler : {}", e.getMessage());
        }
    }
}
