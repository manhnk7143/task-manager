package com.ctel.dbaas.datastore.api_gateway;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.ComputeStatus;
import com.ctel.dbaas.common.enums.DatastoreMode;
import com.ctel.dbaas.dto.compute.CreateComputeInfo;
import com.ctel.dbaas.dto.instance.InstanceCreateReq;
import com.ctel.dbaas.dto.instance.InstanceDetail;
import com.ctel.dbaas.dto.instance.InstanceInfo;
import com.ctel.dbaas.entity.dbaas.AgentEntity;
import com.ctel.dbaas.entity.dbaas.AgentFirmwareEntity;
import com.ctel.dbaas.entity.dbaas.ComputeEntity;
import com.ctel.dbaas.repository.dbaas.*;
import com.ctel.dbaas.service.AgentFirmwareService;
import com.ctel.dbaas.datastore.DatastoreInstanceAbstract;
import com.ctel.dbaas.service.grpc_monitoring.MonitorGrpcClientService;
import com.ctel.dbaas.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ApiGatewayInstanceManager implements DatastoreInstanceAbstract {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AgentFirmwareService agentFirmwareService;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private VolumeRepository volumeRepository;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private FlavorRepository flavorRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MonitorGrpcClientService monitorGrpcClientService;

    @Override
    public void createInstance(InstanceCreateReq request, RequestInfo requestCtx) {
        InstanceCreateReq.DtValid req = request.getDtValid();
        DatastoreMode.ApiGateway apiGateway = DatastoreMode.ApiGateway.get(req.getDatastoreMode());
        switch (apiGateway) {
            case STANDALONE -> {
//                this.apiGatewayStandaloneCreate(req);
            }
        }
    }

    @Override
    public InstanceDetail getInstanceDetail(InstanceInfo instanceInfo, RequestInfo requestCtx) {
        return null;
    }

//    @SneakyThrows
//    private void apiGatewayStandaloneCreate(InstanceCreateReq.DtValid req) {
//        AgentFirmwareEntity agentFirmware = this.getAgentFirmwareLatest();
//        boolean isEnableMonitor = "enabled".equals(req.getEnableMonitor());
//
//        CreateComputeInfo databaseServer = this.createComputeServer(req.getInstanceId(), req.getFlavorId(),
//                ApiGatewayRole.DATABASE.getName(), isEnableMonitor, agentFirmware.getId(), agentFirmware.getBuildNumber());
//
//        JSONObject standaloneRequest = new JSONObject();
//        standaloneRequest.put("CheckPrerequisitesStandalone", ApiGatewayBuildRequest.checkPrerequisites(req.getInstanceId(),
//                databaseServer.getComputeId(), req.getFlavorId(), req.getGlanceImageTag(), req.getVpcId(), req.getSubnetId()));
//
//        standaloneRequest.put("CreateNetworkDatabase", ApiGatewayBuildRequest.createNetwork(
//                databaseServer.getComputeId(), req.getVpcId(),
//                req.getSubnetId(), req.getDatastoreName()));
//
//        standaloneRequest.put("CreateVolumeDatabase", ApiGatewayBuildRequest.createVolume(
//                databaseServer.getComputeId(), req.getVolumeSize(), req.getZoneName()));
//
//        JSONObject cloudInitDbServer = ApiGatewayBuildRequest.generateCloudInit(
//                req.getDatastoreName(), req.getDatastoreVersion(), req.getDatastoreMode(),
//                ApiGatewayRole.DATABASE.getName(), databaseServer.getAgentId(), databaseServer.getEncryptedKey(),
//                databaseServer.getComputeId(), databaseServer.getCurlAgentMonitor());
//        cloudInitDbServer.put("db_port", EnvConfig.DBAAS_APIG_CONF_DB_PORT);
//        cloudInitDbServer.put("db_name", EnvConfig.DBAAS_APIG_CONF_DB_NAME);
//        cloudInitDbServer.put("db_username", EnvConfig.DBAAS_APIG_CONF_DB_USERNAME);
//        cloudInitDbServer.put("db_password", CommonUtils.generateString(30));
//        standaloneRequest.put("GenerateCloudInitDatabase", cloudInitDbServer);
//
//        standaloneRequest.put("CreateComputeDatabase", ApiGatewayBuildRequest.createCompute(
//                databaseServer.getComputeId(), req.getZoneName(), req.getDatastoreName()));
//
//        standaloneRequest.put("CreateNetworkVip", ApiGatewayBuildRequest.createNetwork(
//                databaseServer.getComputeId(), req.getVpcId(), req.getSubnetId(), req.getDatastoreName()));
//
//        // create gateway server
//        for (int i = 0; i < 2; i++) {
//            CreateComputeInfo activeServer = this.createComputeServer(req.getInstanceId(),
//                    req.getFlavorId(), ApiGatewayRole.API_GATEWAY.getName(), isEnableMonitor, agentFirmware.getId(),
//                    agentFirmware.getBuildNumber());
//            standaloneRequest.put("CreateNetworkApiGateway" + i, ApiGatewayBuildRequest.createNetwork(
//                    activeServer.getComputeId(), req.getVpcId(), req.getSubnetId(), req.getDatastoreName()));
//
//            standaloneRequest.put("CreateVolumeApiGateway" + i, ApiGatewayBuildRequest.createVolume(
//                    activeServer.getComputeId(), req.getVolumeSize(), req.getZoneName()));
//
//            JSONObject cloudInitGatewayServer = ApiGatewayBuildRequest.generateCloudInit(
//                    req.getDatastoreName(), req.getDatastoreVersion(), req.getDatastoreMode(),
//                    ApiGatewayRole.API_GATEWAY.getName(), activeServer.getAgentId(), activeServer.getEncryptedKey(),
//                    activeServer.getComputeId(), activeServer.getCurlAgentMonitor());
//            standaloneRequest.put("GenerateCloudInitApiGateway" + i, cloudInitGatewayServer);
//
//            standaloneRequest.put("CreateComputeApiGateway" + i, ApiGatewayBuildRequest.createCompute(
//                    activeServer.getComputeId(), req.getZoneName(), req.getDatastoreName()));
//        }
//
//        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
//        taskManagerReq.setServiceId("create_api_gateway_standalone");
//        taskManagerReq.setData(standaloneRequest.toString());
//
//        log.info("json api gateway send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));
//        rabbitTemplate.convertAndSend(RabbitMQConfig.DBAAS_TASK_MANAGER, RabbitMQConfig.ROUTING_KEY_TASK_MANAGER,
//                taskManagerReq.toMap());
//    }

    private CreateComputeInfo createComputeServer(String instanceId, String flavorId, String role,
                                                  boolean enableMonitor, String agentFirmwareId, Integer buildNumber) {
        ComputeEntity compute = new ComputeEntity();
        compute.setFlavorId(flavorId);
        compute.setRole(role);
        compute.setStatus(ComputeStatus.BUILDING.getName());
        compute.setInstanceId(instanceId);
        compute.setProjectId(GrpcCtx.getReqCtx().getProjectId());
        compute.setOrgId(GrpcCtx.getReqCtx().getOrgId());
        compute.setRegionId(GrpcCtx.getReqCtx().getRegionId());
        computeRepository.save(compute);

        AgentEntity agent = new AgentEntity();
        agent.setName("compute-" + compute.getId());
        agent.setEncryptedKey(CommonUtils.generateString(32));
        agent.setAgentFirmwareId(agentFirmwareId);
        agent.setAgentVersion(String.valueOf(buildNumber == null ? 0 : buildNumber));
        agent.setInstanceId(instanceId);
        agent.setComputeId(compute.getId());
        agent.setProjectId(GrpcCtx.getReqCtx().getProjectId());
        agent.setOrgId(GrpcCtx.getReqCtx().getOrgId());
        agent.setStatus("NONE");
        agentRepository.save(agent);

        if (enableMonitor) {
            // create agent monitor in here
            log.info("Create agent monitor api gateway");
        }

        return new CreateComputeInfo(compute.getId(), agent.getId(), agent.getEncryptedKey());
    }

    private AgentFirmwareEntity getAgentFirmwareLatest() {
        AgentFirmwareEntity agentFirmware = agentFirmwareService.getAgentFirmwareLatest();
        if (agentFirmware == null) {
            return new AgentFirmwareEntity();
        }

        return agentFirmware;
    }
}
