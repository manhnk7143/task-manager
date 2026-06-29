package com.dev.dbaas.worker;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.common.WorkerBase;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.packet.ControlServiceType;
import com.dev.dbaas.worker.job.ControlJob;
import com.dev.dbaas.worker.processor.control.*;
import com.dev.dbaas.worker.processor.control.*;
import com.dev.dbaas.worker.processor.control.api_gateway.CreateApiGatewayStandaloneProcessor;
import com.dev.dbaas.worker.processor.control.kafka.CreateKafkaClusterProcessor;
import com.dev.dbaas.worker.processor.control.kafka.CreateKafkaSingleNodeProcessor;
import com.dev.dbaas.worker.processor.control.mongodb.CreateMongodbReplicasetProcessor;
import com.dev.dbaas.worker.processor.control.mongodb.CreateMongodbStandaloneProcessor;
import com.dev.dbaas.worker.processor.control.mysql.CreateMysqlReplicasetProcessor;
import com.dev.dbaas.worker.processor.control.mysql.CreateMysqlStandaloneProcessor;
import com.dev.dbaas.worker.processor.control.redis.CreateRedisClusterProcessor;
import com.dev.dbaas.worker.processor.control.redis.CreateRedisMasterSlaveProcessor;
import com.dev.dbaas.worker.processor.control.redis.CreateRedisStandaloneProcessor;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ControlWorker extends WorkerBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(ControlWorker.class);
    private static final ConcurrentMap<String, ProcessorBase> MAP_PROCESSORS = new ConcurrentHashMap<>();

    static {
        MAP_PROCESSORS.put(ControlServiceType.CREATE_BACKUP.getValue(), new CreateBackupProcessor());
        MAP_PROCESSORS.put(ControlServiceType.RESTORE_BACKUP.getValue(), new RestoreBackupProcessor());
        MAP_PROCESSORS.put(ControlServiceType.START_INSTANCE.getValue(), new StartInstanceProcessor());
        MAP_PROCESSORS.put(ControlServiceType.STOP_INSTANCE.getValue(), new StopInstanceProcessor());
        MAP_PROCESSORS.put(ControlServiceType.RESTART_INSTANCE.getValue(), new RestartInstanceProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CHANGE_GROUP_CONFIG.getValue(), new ChangeGroupConfigProcessor());
        MAP_PROCESSORS.put(ControlServiceType.SET_PASSWORD.getValue(), new SetPasswordInstanceProcessor());
        MAP_PROCESSORS.put(ControlServiceType.DELETE_INSTANCE.getValue(), new DeleteInstanceProcessor());
        MAP_PROCESSORS.put(ControlServiceType.PROMOTE_SLAVE_MASTER.getValue(), new PromoteSlaveMasterProcessor());
        MAP_PROCESSORS.put(ControlServiceType.UPDATE_MONITOR_SERVICE.getValue(), new UpdateMonitorServiceProcessor());
        MAP_PROCESSORS.put(ControlServiceType.TEST_COMMAND.getValue(), new TestCommandProcessor());
        MAP_PROCESSORS.put(ControlServiceType.DB_ACTION.getValue(), new DbActionProcessor());
        MAP_PROCESSORS.put(ControlServiceType.ATTACH_SECURITY_GROUP.getValue(), new AttachSecurityGroupProcessor());
        MAP_PROCESSORS.put(ControlServiceType.DETACH_SECURITY_GROUP.getValue(), new DetachSecurityGroupProcessor());
        MAP_PROCESSORS.put(ControlServiceType.RESIZE_INSTANCE.getValue(), new ResizeInstanceProcessor());
        MAP_PROCESSORS.put(ControlServiceType.RESIZE_VOLUME.getValue(), new ResizeVolumeProcessor());

        MAP_PROCESSORS.put(ControlServiceType.CREATE_REDIS_STANDALONE.getValue(), new CreateRedisStandaloneProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CREATE_REDIS_MASTER_SLAVE.getValue(), new CreateRedisMasterSlaveProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CREATE_REDIS_CLUSTER.getValue(), new CreateRedisClusterProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CREATE_MONGODB_STANDALONE.getValue(), new CreateMongodbStandaloneProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CREATE_MONGODB_REPLICASET.getValue(), new CreateMongodbReplicasetProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CREATE_KAFKA_CLUSTER.getValue(), new CreateKafkaClusterProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CREATE_KAFKA_SINGLE_NODE.getValue(), new CreateKafkaSingleNodeProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CREATE_MYSQL_STANDALONE.getValue(), new CreateMysqlStandaloneProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CREATE_MYSQL_REPLICASET.getValue(), new CreateMysqlReplicasetProcessor());
        MAP_PROCESSORS.put(ControlServiceType.CREATE_API_GATEWAY_STANDALONE.getValue(), new CreateApiGatewayStandaloneProcessor());
    }

    @Override
    protected void process(ControlJob job) {

        LOGGER.info("Starting ControlJob  " + getNameWorker() + " ....");
        long currentTime = System.currentTimeMillis();
        long delay = currentTime - job.getEventTimestamp();
        ProcessorBase processor = MAP_PROCESSORS.get(job.getServiceId());
        if (processor == null) {
            processor = MAP_PROCESSORS.get(AppServiceType.NOT_FOUND.getValue());
        }
        try {
            processor.process(job);
        } catch (Exception e) {
            LOGGER.error(e, e);
        }

//        long duration = System.currentTimeMillis() - currentTime;
//        if (duration > 100 || delay > 500) {
//            LOGGER.warn("[4CMS] " + job.getServiceId() + " - " + job.getId() + " - Time processing ControlWorker: " + duration + ", delay : " + delay);
//        } else {
//            LOGGER.info("[4CMS] " + job.getServiceId() + " - " + job.getId() + " - Time processing ControlWorker: " + duration + ", delay : " + delay);
//        }
    }

    @Override
    protected void process(List<ControlJob> jobs) {

    }
}
