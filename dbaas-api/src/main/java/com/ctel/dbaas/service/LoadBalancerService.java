package com.ctel.dbaas.service;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.config.RabbitMQConfig;
import com.ctel.dbaas.dto.cmc_cloud.LoadBalancerInfo;
import com.ctel.dbaas.dto.cmc_cloud.TokenDecode;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.common.TaskManagerRequest;
import com.ctel.dbaas.dto.load_balancer.CreateReplicasLbReq;
import com.ctel.dbaas.dto.load_balancer.ReplicasLbRes;
import com.ctel.dbaas.entity.auto_scale.LoadBalancerEntity;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.auto_scale.LoadBalancerRepository;
import com.ctel.dbaas.utils.CommonUtils;
import io.micrometer.common.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Log4j2
@Service
public class LoadBalancerService {

    @Autowired
    private CmcCloudService cmcCloudService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @SneakyThrows
    @Transactional(value = "autoScaleTransactionManager", isolation = Isolation.SERIALIZABLE)
    public void createReplicasLB(CreateReplicasLbReq req, RequestInfo reqCtx) {
        LoadBalancerInfo loadBalancer = cmcCloudService.getLoadBalancer(req.getLoadBalancerId(), reqCtx);
        if (loadBalancer == null) {
            throw new AppException(new ErrorResponse("not found load balancer with id: %s", req.getLoadBalancerId()));
        }
        cmcCloudService.validateZone(reqCtx.getToken(), reqCtx.getRegionId(), new HashSet<>(req.getLstZone()));

        int quantityOfReplicas = loadBalancerRepository.countAllByOrgIdAndRegionIdAndProjectIdAndDeletedAtIsNull(
                reqCtx.getOrgId(), reqCtx.getRegionId(), reqCtx.getProjectId());
        if (req.getReplicasServer() + quantityOfReplicas > 10) {
            throw new AppException(new ErrorResponse("exceed the load balancer replication number, the maximum replication number is 10"));
        }

        TokenDecode tokenDecode = cmcCloudService.getTokenOpenstack(reqCtx);

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("originLoadBalancerId", req.getLoadBalancerId());
        jsonRequest.put("availabilityZone", req.getZones());
        jsonRequest.put("replica", req.getReplicasServer());

        jsonRequest.put("username", tokenDecode.getUserName());
        jsonRequest.put("token", tokenDecode.getToken());
        jsonRequest.put("teamCodeId", reqCtx.getOrgId());
        jsonRequest.put("regionId", reqCtx.getRegionId());
        jsonRequest.put("projectId", reqCtx.getProjectId());

        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
        taskManagerReq.setServiceId("replicate_load_balancer");
        taskManagerReq.setData(jsonRequest.toString());

        log.info("json replicate_load_balancer send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));
        rabbitTemplate.convertAndSend(RabbitMQConfig.DBAAS_LB_AUTO_SCALE, RabbitMQConfig.ROUTING_KEY_LB_AUTO_SCALE,
                taskManagerReq.toMap());
    }

    public List<ReplicasLbRes> getReplicasLb(RequestInfo reqCtx) {
        List<LoadBalancerEntity> lstLbReplicas = loadBalancerRepository.findAllByOrgIdAndRegionIdAndProjectIdAndDeletedAtIsNull(
                reqCtx.getOrgId(), reqCtx.getRegionId(), reqCtx.getProjectId());

        return lstLbReplicas.stream()
                .filter(lb -> StringUtils.isNotBlank(lb.getLoadBalancerId()))
                .map(lb -> {
                    String status = lb.getStatus() == null ? "" : lb.getStatus();
                    ReplicasLbRes dto = new ReplicasLbRes();
                    dto.setLoadBalancerId(lb.getLoadBalancerId());
                    dto.setParentId(lb.getOriginLoadBalancerId());
                    dto.setStatus(status);
                    return dto;
                }).toList();
    }

    public void deleteLoadBalancer(String loadBalancerId, RequestInfo reqCtx) {
        List<LoadBalancerEntity> listLbReplicas = loadBalancerRepository.findAllByOriginLoadBalancerIdAndOrgIdAndRegionIdAndProjectId(
                loadBalancerId, reqCtx.getOrgId(), reqCtx.getRegionId(), reqCtx.getProjectId());

        for (LoadBalancerEntity lb : listLbReplicas) {
            lb.setDeletedAt(LocalDateTime.now());
            loadBalancerRepository.save(lb);
        }

        LoadBalancerEntity loadBalancer = loadBalancerRepository.findFirstByLoadBalancerIdAndOrgIdAndRegionIdAndProjectId(
                loadBalancerId, reqCtx.getOrgId(), reqCtx.getRegionId(), reqCtx.getProjectId());
        if (loadBalancer != null) {
            loadBalancer.setDeletedAt(LocalDateTime.now());
            loadBalancerRepository.save(loadBalancer);
        }
    }

    @SneakyThrows
    public void triggerSync(String loadBalancerId, RequestInfo reqCtx) {
        LoadBalancerInfo loadBalancer = cmcCloudService.getLoadBalancer(loadBalancerId, reqCtx);
        if (loadBalancer == null) {
            throw new AppException(new ErrorResponse("not found load balancer with id: %s", loadBalancerId));
        }

        JSONObject jsonRequest = new JSONObject();
        TokenDecode tokenDecode = cmcCloudService.getTokenOpenstack(reqCtx);
        jsonRequest.put("originLoadBalancerId", loadBalancerId);

        jsonRequest.put("username", tokenDecode.getUserName());
        jsonRequest.put("token", tokenDecode.getToken());
        jsonRequest.put("teamCodeId", reqCtx.getOrgId());
        jsonRequest.put("regionId", reqCtx.getRegionId());
        jsonRequest.put("projectId", reqCtx.getProjectId());

        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
        taskManagerReq.setServiceId("request_sync_load_balancer");
        taskManagerReq.setData(jsonRequest.toString());

        log.info("json request_sync_load_balancer send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));
        rabbitTemplate.convertAndSend(RabbitMQConfig.DBAAS_LB_AUTO_SCALE, RabbitMQConfig.ROUTING_KEY_LB_AUTO_SCALE,
                taskManagerReq.toMap());
    }

}
