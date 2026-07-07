package com.ctel.dbaas.controller.grpc;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.dto.resource.ResourceOverview;
import com.ctel.dbaas.service.ResourceService;
import grpc.model.dbaas_service.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Log4j2
@GrpcService
public class ResourceGrpcController extends ResourceServiceGrpc.ResourceServiceImplBase {

    @Autowired
    private ResourceService resourceService;

    @Override
    public void getResourceOverview(GetResourceOverviewRequest request, StreamObserver<GetResourceOverviewResponse> responseObserver) {
        try {
            log.info("getResourceOverview params => [{}]", request);

            RequestInfo reqCtx = GrpcCtx.getReqCtx();
            ResourceOverview resourceOverview = resourceService.getResourceOverview(reqCtx.getOrgId(), reqCtx.getRegionId(), reqCtx.getProjectId());
            responseObserver.onNext(GetResourceOverviewResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ResourceOverviewRes.newBuilder()
                            .setResource(ResourceUsage.newBuilder()
                                    .setCpu(ResourceUsage.Usage.newBuilder()
                                            .setUsed(resourceOverview.getResource().get("cpu").getUsed())
                                            .setLimit(resourceOverview.getResource().get("cpu").getLimit())
                                            .build())
                                    .setRam(ResourceUsage.Usage.newBuilder()
                                            .setUsed(resourceOverview.getResource().get("ram").getUsed())
                                            .setLimit(resourceOverview.getResource().get("ram").getLimit())
                                            .build())
                                    .setSystemDisk(ResourceUsage.Usage.newBuilder()
                                            .setUsed(resourceOverview.getResource().get("systemDisk").getUsed())
                                            .setLimit(resourceOverview.getResource().get("systemDisk").getLimit())
                                            .build())
                                    .setVolume(ResourceUsage.Usage.newBuilder()
                                            .setUsed(resourceOverview.getResource().get("volume").getUsed())
                                            .setLimit(resourceOverview.getResource().get("volume").getLimit())
                                            .build())
                                    .build())
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetResourceOverviewResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ResourceOverviewRes.newBuilder().build())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
