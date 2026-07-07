package com.ctel.dbaas.service.grpc_monitoring;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.entity.dbaas.ComputeEntity;
import com.ctel.dbaas.entity.dbaas.InstanceEntity;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.ComputeRepository;
import com.ctel.dbaas.repository.dbaas.InstanceRepository;
import com.ctel.dbaas.repository.dbaas.projection.instance.InstanceInfoProjection;
import com.ctel.dbaas.service.grpc_monitoring.model.*;
import grpc.model.monitoring.*;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class MonitorGrpcClientService {
    @Autowired
    private InstanceRepository instanceRepository;

    @GrpcClient("monitoring")
    private MonitoringServiceGrpc.MonitoringServiceBlockingStub monitoringService;

    @Autowired
    private ComputeRepository computeRepository;

    public MonitorResourceRes createAgentMonitor(String resourceType, String configJson, MonitorResourceReq req) {
//        CreateAgentMonitorRequest request = CreateAgentMonitorRequest.newBuilder()
//                .setTeamCodeId(GrpcCtx.getReqCtx().getOrgId())
//                .setRegionId(GrpcCtx.getReqCtx().getRegionId())
//                .setPortalProjectId(GrpcCtx.getReqCtx().getProjectId())
//                .setResourceName(req.getResourceName())
//                .setResourceTypeName(resourceType)
//                .setConfig(configJson)
//                .setResourceId(req.getResourceId())
//                .setGroupId(req.getGroupId())
//                .setGroupName(req.getGroupName())
//                .build();
//        log.info("MONITORING::createAgentMonitor request => [{}]", request);
//        CreateAgentMonitorResponse response = monitoringService.createAgentMonitor(request);
//        log.info("MONITORING::createAgentMonitor response => [{}]", response);
//        AgentMonitorData dataRes = response.getData();
//        ConfigAgentData configAgentData = dataRes.getConfigAgent();
//
//        MonitorResourceRes.ConfigRedisMonitorData config = new MonitorResourceRes.ConfigRedisMonitorData();
//        config.setAgentMonitorId(configAgentData.getAgentId());
//        config.setResourceTypeId(configAgentData.getResourceTypeId());
//        config.setResourceTypeValue(configAgentData.getResourceTypeValue());
//        config.setConfig(configAgentData.getConfig());
//
//        return new MonitorResourceRes(dataRes.getResourceId(), dataRes.getLink(), config);
        return new MonitorResourceRes();
    }

//    public void updateResourceAgent(UpdateResourceReq req) {
//        try {
//            UpdateResourcesRequest request = UpdateResourcesRequest.newBuilder()
//                    .setTeamCodeId(GrpcCtx.getReqCtx().getOrgId())
//                    .setRegionId(GrpcCtx.getReqCtx().getRegionId())
//                    .setPortalProjectId(GrpcCtx.getReqCtx().getProjectId())
//                    .setResourceData(ResourceData.newBuilder()
//                            .setName(req.getResourceName())
//                            .setResourceId(req.getResourceId())
//                            .setConfig(req.getJsonConfig())
//                            .build())
//                    .setResourceTypeName(req.getResourceTypeName())
//                    .build();
//
//            log.info("MONITORING::updateResourceAgent request => [{}]", request);
//            UpdateResourcesResponse response = monitoringService.updateResources(request);
//            log.info("MONITORING::updateResourceAgent response => [{}]", response);
//        } catch (Exception e) {
//            log.error("MONITORING::updateResourceAgent ERROR => [{}]", e.getMessage());
//        }
//    }

    public void deleteAgentMonitor(String instanceId, String regionId, String projectId, String orgId) {
        try {
            DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                    .setRegionId(regionId)
                    .setPortalProjectId(projectId)
                    .setTeamCodeId(orgId)
                    .setGroupId(instanceId)
                    .build();
            log.info("MONITORING::deleteAgentMonitor request => [{}]", request);
            DeleteGroupResponse response = monitoringService.deleteGroup(request);
            log.info("MONITORING::deleteAgentMonitor response => [{}]", response);
        } catch (Exception e) {
            log.error("MONITORING::deleteAgentMonitor ERROR => [{}]", e.getMessage());
        }
    }

    public GetGraphInfrasResponse getGraphInfras(GraphInfrasReq req) {
        InstanceEntity instance = instanceRepository.findByIdAndOrgIdAndProjectId(req.getInstanceId(), req.getOrgId(), req.getProjectId()).orElse(null);
        if (instance == null || !instance.getRegionId().equals(req.getRegionId())) {
            log.warn("no access to resources => resourceId[{}] - projectId[{}] - orgId[{}] - regionId[{}]",
                    req.getInstanceId(), req.getProjectId(), req.getOrgId(), req.getRegionId());
            throw new AppException(new ErrorResponse("no access to resources"));
        }

        GetGraphInfrasRequest request = GetGraphInfrasRequest.newBuilder()
                .setTeamCodeId(req.getOrgId())
                .setPortalProjectId(req.getProjectId())
                .setResourceValue(req.getResourceValue())
                .setToken(req.getToken())
                .setRegionId(req.getRegionId())
                .setResourceName(req.getResourceName())
                .setResourceId(req.getInstanceId())
                .setDimensionValue(req.getDimensionValue())
                .build();
        log.info("MONITORING::getGraphInfras request => [{}]", request);
        return monitoringService.getGraphInfras(request);
    }

    public GetMonascaStatisticResponse getMonascaStatistic(MonascaStatisticReq req) {
        GetMonascaStatisticRequest.Builder request = GetMonascaStatisticRequest.newBuilder()
                .setToken(req.getToken())
                .setRegionId(req.getRegionId())
                .setPortalProjectId(req.getProjectId())

                .setResourceValue(req.getResourceValue())
                .setDimensionValue(req.getDimensionValue())
                .setMonitoredObject(StatisticMonitoredObject.newBuilder()
                        .setResourceValue(req.getMonitoredObject().getResourceValue())
                        .setAttachmentId(req.getMonitoredObject().getAttachmentId())
                        .build())
                .setMetricValues(req.getMetricValue())
                .setStartTime(req.getStartTime())
                .setStatistics(req.getStatistics());
        if (req.getPeriod() != null) {
            request.setPeriod(req.getPeriod());
        }
        if (req.getEndTime() != null) {
            request.setEndTime(req.getEndTime());
        }

//        checkPermissionAndGetInstanceId(req.getMonitoredObject().getAttachmentId(), req.getProjectId(), req.getOrgId(), req.getRegionId());
        log.info("MONITORING::getMonascaStatistic request => [{}]", request);
        return monitoringService.getMonascaStatistic(request.build());
    }

    public HealthCheckServiceResponse healthCheckService(HealthCheckMonitorReq req) {
        String instanceId = checkPermissionAndGetInstanceId(req.getResourceId(), req.getProjectId(), req.getTeamCodeId(), req.getRegionId());
        InstanceInfoProjection instanceInfo = instanceRepository.getInstanceNameById(instanceId);
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(instanceInfo.getDatastoreCode());

        HealthCheckServiceRequest request = HealthCheckServiceRequest.newBuilder()
                .setTeamCodeId(req.getTeamCodeId())
                .setToken(req.getToken())
                .setRegionId(req.getRegionId())
                .setPortalProjectId(req.getProjectId())
                .setResourceId(req.getResourceId())
                .setResourceTypeName(datastoreSupport.getMonitorResourceTypeName())
                .build();

        log.info("MONITORING::healthCheckService request => [{}]", request);
        HealthCheckServiceResponse response = monitoringService.healthCheckService(request);
        log.info("MONITORING::healthCheckService response => [{}]", response);

        return response;
    }

    public GetResourceExternalsResponse getResourceExternals(String instanceId, RequestInfo requestInfo) {
        InstanceEntity instance = instanceRepository
                .findByIdAndOrgIdAndProjectId(instanceId, requestInfo.getOrgId(), requestInfo.getProjectId()).orElse(null);

        InstanceInfoProjection instanceInfo = instanceRepository.getInstanceNameById(instanceId);
        String regionId = instanceInfo.getRegionId();
        String orgId = instanceInfo.getOrgId();
        String projectId = instanceInfo.getProjectId();

        if (instance == null || !regionId.equalsIgnoreCase(requestInfo.getRegionId())
                || !orgId.equalsIgnoreCase(requestInfo.getOrgId())
                || !projectId.equalsIgnoreCase(requestInfo.getProjectId())) {
            log.warn("no access to resources => resourceId[{}] - projectId[{}] - orgId[{}] - regionId[{}]",
                    instanceId, requestInfo.getProjectId(), requestInfo.getOrgId(), requestInfo.getRegionId());
            throw new AppException(new ErrorResponse("no access to resources"));
        }

        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(instanceInfo.getDatastoreCode());

        GetResourceExternalsRequest request = GetResourceExternalsRequest.newBuilder()
                .setTeamCodeId(requestInfo.getOrgId())
                .setToken(requestInfo.getToken())
                .setRegionId(requestInfo.getRegionId())
                .setPortalProjectId(requestInfo.getProjectId())
                .setResourceValue(datastoreSupport.getMonitorResourceTypeName())
                .setGroupId(instanceId)
                .build();

        log.info("MONITORING::getResourceExternals request => [{}]", request);
        GetResourceExternalsResponse response = monitoringService.getResourceExternals(request);
        log.info("MONITORING::getResourceExternals response => [{}]", response);

        return response;
    }

    private String checkPermissionAndGetInstanceId(String resourceId, String projectId, String orgId, String regionId) {
        ComputeEntity compute = computeRepository.findFirstByMonitorResourceIdAndProjectIdAndOrgIdAndRegionId(
                resourceId, projectId, orgId, regionId);
        if (compute == null) {
            log.warn("no access to resources => resourceId[{}] - projectId[{}] - orgId[{}] - regionId[{}]",
                    resourceId, projectId, orgId, regionId);
            throw new AppException(new ErrorResponse("no access to resources"));
        }

        return compute.getInstanceId();
    }

}
