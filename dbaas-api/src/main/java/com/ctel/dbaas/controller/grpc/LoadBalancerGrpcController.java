package com.ctel.dbaas.controller.grpc;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.dto.load_balancer.CreateReplicasLbReq;
import com.ctel.dbaas.dto.load_balancer.ReplicasLbRes;
import com.ctel.dbaas.service.LoadBalancerService;
import grpc.model.dbaas_service.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@GrpcService
public class LoadBalancerGrpcController extends LoadBalancerServiceGrpc.LoadBalancerServiceImplBase {

    @Autowired
    private LoadBalancerService loadBalancerService;

    @Override
    public void createReplicasLB(CreateReplicasLBRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("createReplicasLB params => [{}]", request);

            CreateReplicasLbReq req = new CreateReplicasLbReq();
            req.setLoadBalancerId(request.getLoadBalancerId());
            req.setReplicasServer(request.getReplicasServer());
            req.setZones(request.getZones());
            req.validate();

            loadBalancerService.createReplicasLB(req, GrpcCtx.getReqCtx());

            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            String message = e.getMessage() == null ? "" : e.getMessage();
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(message)
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getReplicasLB(GetReplicasLBRequest request, StreamObserver<GetReplicasLBResponse> responseObserver) {
        try {
            log.info("getReplicasLB params => [{}]", request);
            List<ReplicasLbRes> lstLbReplicas = loadBalancerService.getReplicasLb(GrpcCtx.getReqCtx());

            List<GetReplicasLBResponse.ReplicasInfo> replicasInfo = lstLbReplicas.stream()
                    .map(lb -> GetReplicasLBResponse.ReplicasInfo.newBuilder()
                            .setId(lb.getLoadBalancerId())
                            .setParentId(lb.getParentId())
                            .setStatus(lb.getStatus())
                            .build())
                    .toList();
            responseObserver.onNext(GetReplicasLBResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(GetReplicasLBResponse.Data.newBuilder()
                            .addAllReplicasInfo(replicasInfo)
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            String message = e.getMessage() == null ? "" : e.getMessage();
            responseObserver.onNext(GetReplicasLBResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(message)
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteReplicasLB(DeleteReplicasLBRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("deleteReplicasLB params => params[{}] - requestInfo[{}]", request, GrpcCtx.getReqCtx());
            loadBalancerService.deleteLoadBalancer(request.getLoadBalancerId(), GrpcCtx.getReqCtx());

            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            String message = e.getMessage() == null ? "" : e.getMessage();
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(message)
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void triggerSync(TriggerSyncRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("triggerSync params => [{}]", request);
            loadBalancerService.triggerSync(request.getLoadBalancerId(), GrpcCtx.getReqCtx());

            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            String message = e.getMessage() == null ? "" : e.getMessage();
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(message)
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
