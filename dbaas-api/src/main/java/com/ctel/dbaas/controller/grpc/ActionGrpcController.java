package com.ctel.dbaas.controller.grpc;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.InstanceAction;
import com.ctel.dbaas.service.ActionService;
import com.ctel.dbaas.utils.JsonUtils;
import grpc.model.dbaas_service.DatabaseActionServiceGrpc;
import grpc.model.dbaas_service.ExecuteActionRequest;
import grpc.model.dbaas_service.ExecuteActionResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.Level;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@GrpcService
@Log4j2
public class ActionGrpcController extends DatabaseActionServiceGrpc.DatabaseActionServiceImplBase {

    @Autowired
    private ActionService actionService;

    @Override
    public void executeAction(ExecuteActionRequest request, StreamObserver<ExecuteActionResponse> responseObserver) {
        try {
            log.info("executeAction params => [{}]", request);

            // validate request
            InstanceAction action = InstanceAction.getOrThrow(request.getAction());
            Map<String, Object> mapData;
            if (InstanceAction.DB_ACTION.equals(action)) {
                String requestDbAction = request.getRequestDataMap().get("requestDbAction");
                mapData = JsonUtils.toMap(new JSONObject(requestDbAction));
            } else {
                mapData = new HashMap<>(request.getRequestDataMap());
            }

            Map<String, String> res = actionService.executeAction(request.getInstanceId(), action, mapData, GrpcCtx.getReqCtx());
            responseObserver.onNext(ExecuteActionResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .putAllData(res)
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            String messageError = e.getMessage() == null ? "" : e.getMessage();
            responseObserver.onNext(ExecuteActionResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(messageError)
                    .putAllData(Collections.emptyMap())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}