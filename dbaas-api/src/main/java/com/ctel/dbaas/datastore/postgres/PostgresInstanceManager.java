package com.ctel.dbaas.datastore.postgres;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.enums.DatastoreMode;
import com.ctel.dbaas.datastore.postgres.model.PostgresStandalone;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.instance.InstanceCreateReq;
import com.ctel.dbaas.dto.instance.InstanceDetail;
import com.ctel.dbaas.dto.instance.InstanceInfo;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.*;
import com.ctel.dbaas.service.AgentFirmwareService;
import com.ctel.dbaas.datastore.DatastoreInstanceAbstract;
import com.ctel.dbaas.service.grpc_monitoring.MonitorGrpcClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class PostgresInstanceManager implements DatastoreInstanceAbstract {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AgentFirmwareService agentFirmwareService;

    @Autowired
    private MonitorGrpcClientService monitorGrpcClientService;

    @Autowired
    private VolumeRepository volumeRepository;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private FlavorRepository flavorRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private DatastoreVersionRepository datastoreVersionRepository;

    @Autowired
    private DatastoreRepository datastoreRepository;

    @Autowired
    private DatastoreModeRepository datastoreModeRepository;

    @Autowired
    private BackupStrategyRepository backupStrategyRepository;

    @Autowired
    private BackupRepository backupRepository;

    @SneakyThrows
    @Override
    public void createInstance(InstanceCreateReq request, RequestInfo requestCtx) {
        InstanceCreateReq.DtValid req = request.getDtValid();
        DatastoreMode.Postgresql mode = DatastoreMode.Postgresql.get(req.getDatastoreMode());
//        if (DatastoreMode.Postgresql.MASTER_SLAVE.equals(mode)) {
////            RedisCluster redisCluster = objectMapper
////                    .readValue(request.getRequestMetadata(), RedisCluster.class);
////            this.masterSlaveCreate(req, redisCluster, requestCtx);
//        } else 
        if (DatastoreMode.Postgresql.STANDALONE.equals(mode)) {
            PostgresStandalone standalone = objectMapper
                    .readValue(request.getRequestMetadata(), PostgresStandalone.class);
            this.standaloneCreate(req, standalone, requestCtx);
        } else {
            throw new AppException(new ErrorResponse("Unknown"));
        }
    }

    private void standaloneCreate(InstanceCreateReq.DtValid req, PostgresStandalone standalone, RequestInfo requestCtx) {

    }

    @Override
    public InstanceDetail getInstanceDetail(InstanceInfo instanceInfo, RequestInfo requestCtx) {
        return null;
    }
}
