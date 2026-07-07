package com.ctel.dbaas.controller.grpc;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.service.grpc_monitoring.MonitorGrpcClientService;
import com.ctel.dbaas.service.grpc_monitoring.model.GraphInfrasReq;
import com.ctel.dbaas.service.grpc_monitoring.model.HealthCheckMonitorReq;
import com.ctel.dbaas.service.grpc_monitoring.model.MonascaStatisticReq;
import com.ctel.dbaas.utils.CommonUtils;
import grpc.model.dbaas_service.*;
import grpc.model.monitoring.GetGraphInfrasResponse;
import grpc.model.monitoring.GetMonascaStatisticResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@GrpcService
@Log4j2
public class MonitorGrpcController extends MonitorServiceGrpc.MonitorServiceImplBase {

    @Autowired
    private MonitorGrpcClientService monitorGrpcClientService;

    @Override
    public void getGraphInfras(GetGraphInfrasMonitorRequest request, StreamObserver<GetGraphInfrasMonitorResponse> responseObserver) {
        try {
            log.info("getGraphInfras params => [{}]", request);
            GraphInfrasReq req = GraphInfrasReq.builder()
                    .orgId(GrpcCtx.getReqCtx().getOrgId())
                    .projectId(GrpcCtx.getReqCtx().getProjectId())
                    .token(GrpcCtx.getReqCtx().getToken())
                    .regionId(GrpcCtx.getReqCtx().getRegionId())

                    .resourceValue(request.getResourceValue())
                    .resourceName(request.getResourceName())
                    .instanceId(request.getResourceId())
                    .dimensionValue(request.getDimensionValue())
                    .build();
            GetGraphInfrasResponse response = monitorGrpcClientService.getGraphInfras(req);
            String res = CommonUtils.toJson(response.getData());
            if (res == null) {
                res = "";
            }

            responseObserver.onNext(GetGraphInfrasMonitorResponse.newBuilder()
                    .setCurrentTime(response.getCurrentTime())
                    .setStatus(response.getStatus())
                    .setMsg(response.getMsg())
                    .setData(GraphDataMonitor.newBuilder()
                            .setJsonData(res)
                            .build())
                    .build());

        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            String message = "System error";
            if (e instanceof AppException appEx) {
                message = appEx.getMessage();
            }
            responseObserver.onNext(GetGraphInfrasMonitorResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(message)
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getStatisticMonitor(GetStatisticMonitorRequest request, StreamObserver<GetStatisticMonitorResponse> responseObserver) {
        try {
            MonascaStatisticReq req = MonascaStatisticReq.builder()
                    .token(GrpcCtx.getReqCtx().getToken())
                    .regionId(GrpcCtx.getReqCtx().getRegionId())
                    .projectId(GrpcCtx.getReqCtx().getProjectId())
                    .orgId(GrpcCtx.getReqCtx().getOrgId())

                    .period(request.getPeriod())
                    .resourceValue(request.getResourceValue())
                    .monitoredObject(MonascaStatisticReq.MonitoredObject.builder()
                            .resourceValue(request.getMonitoredObject().getResourceValue())
                            .attachmentId(request.getMonitoredObject().getAttachmentId())
                            .build())
                    .metricValue(request.getMetricValues())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .statistics(request.getStatistics())
                    .dimensionValue(request.getDimensionValue())
                    .build();
            GetMonascaStatisticResponse response = monitorGrpcClientService.getMonascaStatistic(req);
            String resData = CommonUtils.toJson(response.getData());
            if (resData == null) {
                resData = "";
            }

            responseObserver.onNext(GetStatisticMonitorResponse.newBuilder()
                    .setCurrentTime(response.getCurrentTime())
                    .setStatus(response.getStatus())
                    .setMsg(response.getMsg())
                    .setData(StatisticData.newBuilder()
                            .setJsonData(resData)
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            String message = "System error";
            if (e instanceof AppException appEx) {
                message = appEx.getMessage();
            }
            responseObserver.onNext(GetStatisticMonitorResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(message)
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void healthCheckService(HealthCheckServiceRequest request, StreamObserver<HealthCheckServiceResponse> responseObserver) {
        try {
            log.info("healthCheckService params => [{}]", request);
            HealthCheckMonitorReq req = HealthCheckMonitorReq.builder()
                    .teamCodeId(GrpcCtx.getReqCtx().getOrgId())
                    .token(GrpcCtx.getReqCtx().getToken())
                    .regionId(GrpcCtx.getReqCtx().getRegionId())
                    .projectId(GrpcCtx.getReqCtx().getProjectId())

                    .resourceId(request.getResourceId())
                    .build();
            grpc.model.monitoring.HealthCheckServiceResponse response = monitorGrpcClientService.healthCheckService(req);
            String res = CommonUtils.toJson(response.getData());
            if (res == null) {
                res = "";
            }

            responseObserver.onNext(HealthCheckServiceResponse.newBuilder()
                    .setCurrentTime(response.getCurrentTime())
                    .setStatus(response.getStatus())
                    .setMsg(response.getMsg())
                    .setData(HealthCheckServiceData.newBuilder()
                            .setJsonData(res)
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            String message = "System error";
            if (e instanceof AppException appEx) {
                message = appEx.getMessage();
            }
            responseObserver.onNext(HealthCheckServiceResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(message)
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getResourceExternals(GetResourceExternalsRequest request, StreamObserver<GetResourceExternalsResponse> responseObserver) {
        try {
            log.info("getResourceExternals params => [{}]", request);
            grpc.model.monitoring.GetResourceExternalsResponse response = monitorGrpcClientService.getResourceExternals(request.getInstanceId(), GrpcCtx.getReqCtx());
            grpc.model.monitoring.ResourceExternalData data = response.getData();

            List<ResourceExternalData> docs = data.getDocsList().stream().map(i -> {
                List<Attachment> attachmentList = i.getAttachmentsList().stream().map(a -> Attachment.newBuilder()
                                .setId(a.getId())
                                .setName(a.getName())
                                .build())
                        .toList();
                return ResourceExternalData.newBuilder()
                        .setEncryptedKey(i.getEncryptedKey())
                        .setResourceId(i.getResourceId())
                        .setName(i.getName())
                        .setStatus(i.getStatus())
                        .setConfigStatus(i.getConfigStatus())
                        .setAgentCode(i.getAgentCode())
                        .setResourceName(i.getResourceName())
                        .setGroupId(i.getGroupId())
                        .addAllAttachments(attachmentList)
                        .build();
            }).toList();

            responseObserver.onNext(GetResourceExternalsResponse.newBuilder()
                    .setCurrentTime(response.getCurrentTime())
                    .setStatus(response.getStatus())
                    .setMsg(response.getMsg())
                    .setData(ResourceExternalRes.newBuilder()
                            .addAllDocs(docs)
                            .setIsAttachment(data.getIsAttachment())
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            String message = "System error";
            if (e instanceof AppException appEx) {
                message = appEx.getMessage();
            }
            responseObserver.onNext(GetResourceExternalsResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(message)
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
