package com.ctel.dbaas.init_data;

import com.ctel.dbaas.common.enums.DatastoreMode;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.entity.dbaas.DatastoreEntity;
import com.ctel.dbaas.entity.dbaas.DatastoreModeEntity;
import com.ctel.dbaas.entity.dbaas.DatastoreVersionEntity;
import com.ctel.dbaas.exception.AppException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/init-datastore")
public class InitDatastoreController {

    @Autowired
    private InitDatabaseService initDatabaseService;

//    @PostMapping("/postgresql")
//    public void initPostgresql(@RequestBody InitRequest request) {
//        request.validate();
//        DatastoreEntity datastore = initDatabaseService.initDatastore(request.getDatastoreCode());
//        DatastoreVersionEntity datastoreVersion = initDatabaseService.initDatastoreVersion(datastore, request.getVersion());
//
//        DatastoreModeEntity datastoreModeStandalone = initDatabaseService.initDatastoreMode(datastoreVersion, "Standalone", "standalone");
//        initDatabaseService.initDatastoreConfiguration(datastoreModeStandalone, request.getDatastoreCode(), request.getVersion());
//
//        DatastoreModeEntity datastoreModeMasterSlave = initDatabaseService.initDatastoreMode(datastoreVersion, "Master/Slave", "master_slave");
//        initDatabaseService.initDatastoreConfiguration(datastoreModeMasterSlave, request.getDatastoreCode(), request.getVersion());
//
//        DatastoreModeEntity datastoreModeClusterHa = initDatabaseService.initDatastoreMode(datastoreVersion, "Cluster HA", "cluster_ha");
//        initDatabaseService.initDatastoreConfiguration(datastoreModeClusterHa, request.getDatastoreCode(), request.getVersion());
//
//        log.info("initPostgresql done !");
//    }

    @PostMapping("/redis")
    public void initRedis(@RequestBody InitRequest request) {
        request.setDatastoreCode(DatastoreSupport.REDIS.getCode());
        request.validate();
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(request.getDatastoreCode());
        DatastoreEntity datastore = initDatabaseService.initDatastore(datastoreSupport.getCode());
        DatastoreVersionEntity datastoreVersion = initDatabaseService.initDatastoreVersion(datastore, request.getVersion());

        for (String mode : request.getModes()) {
            DatastoreMode.Redis redisMode = DatastoreMode.Redis.get(mode);
            DatastoreModeEntity datastoreModeStandalone = initDatabaseService.initDatastoreMode(datastoreVersion, redisMode.getName(), redisMode.getCode());
            initDatabaseService.initDatastoreConfiguration(datastoreModeStandalone, request.getDatastoreCode(), request.getVersion());
        }

        log.info("init datastore redis done !");
    }

    @PostMapping("/mongodb")
    public void initMongodb(@RequestBody InitRequest request) {
        request.setDatastoreCode(DatastoreSupport.MONGODB.getCode());
        request.validate();
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(request.getDatastoreCode());
        DatastoreEntity datastore = initDatabaseService.initDatastore(datastoreSupport.getCode());
        DatastoreVersionEntity datastoreVersion = initDatabaseService.initDatastoreVersion(datastore, request.getVersion());

        for (String mode : request.getModes()) {
            DatastoreMode.Mongodb mongodbMode = DatastoreMode.Mongodb.get(mode);
            DatastoreModeEntity datastoreModeStandalone = initDatabaseService.initDatastoreMode(datastoreVersion, mongodbMode.getName(), mongodbMode.getCode());
            initDatabaseService.initDatastoreConfiguration(datastoreModeStandalone, request.getDatastoreCode(), request.getVersion());
        }

        log.info("init datastore mongodb done !");
    }

    @PostMapping("/kafka")
    public void initKafka(@RequestBody InitRequest request) {
        request.setDatastoreCode(DatastoreSupport.KAFKA.getCode());
        request.validate();
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(request.getDatastoreCode());
        DatastoreEntity datastore = initDatabaseService.initDatastore(datastoreSupport.getCode());
        DatastoreVersionEntity datastoreVersion = initDatabaseService.initDatastoreVersion(datastore, request.getVersion());

        for (String mode : request.getModes()) {
            DatastoreMode.Kafka kafkaMode = DatastoreMode.Kafka.get(mode);
            DatastoreModeEntity datastoreMode = initDatabaseService.initDatastoreMode(datastoreVersion, kafkaMode.getName(), kafkaMode.getCode());
//            initDatabaseService.initDatastoreConfiguration(datastoreMode, request.getDatastoreCode(), request.getVersion());
        }

        log.info("init kafka done !");
    }

    @DeleteMapping("/postgresql")
    public void removePostgresql(@RequestBody InitRequest request) {
        request.validate();
        DatastoreEntity datastore = initDatabaseService.removeDatastore(request.getDatastoreCode());
        DatastoreVersionEntity datastoreVersion = initDatabaseService.removeDatastoreVersion(datastore, request.getVersion());

        DatastoreModeEntity datastoreModeStandalone = initDatabaseService.removeMode(datastoreVersion, "Standalone");
        initDatabaseService.removeConfigAndGroupConfig(datastoreModeStandalone);

        DatastoreModeEntity datastoreModeMasterSlave = initDatabaseService.removeMode(datastoreVersion, "Master/Slave");
        initDatabaseService.removeConfigAndGroupConfig(datastoreModeMasterSlave);

        DatastoreModeEntity datastoreModeClusterHa = initDatabaseService.removeMode(datastoreVersion, "Cluster HA");
        initDatabaseService.removeConfigAndGroupConfig(datastoreModeClusterHa);

        log.info("delete datastore done !");
    }

    @DeleteMapping("/remove-datastore-mode")
    public Object removeDatastoreMode(@RequestBody InitRequest request) {
        DatastoreEntity datastore = initDatabaseService.getDatastore(request.getDatastoreCode());
        if (datastore != null) {
            DatastoreVersionEntity datastoreVersion = initDatabaseService.getDatastoreVersion(datastore.getId(), request.getVersion());
            if (datastoreVersion != null && datastore.getId().equals(datastoreVersion.getDatastoreId())) {
                for (String modeCode : request.getModes()) {
                    DatastoreModeEntity datastoreMode = initDatabaseService.getDatastoreMode(modeCode, datastoreVersion.getId());
                    if (datastoreMode != null) {
                        initDatabaseService.removeMode(datastoreVersion, modeCode);
                        initDatabaseService.removeConfigAndGroupConfig(datastoreMode);
                        log.info("Remove mode[{}] done !", modeCode);
                    }
                }
                return Collections.singletonMap("message", "success");
            }
        }
        return Collections.singletonMap("message", "failed");
    }

    @Data
    public static class InitRequest {
        private String datastoreCode;
        private String version;
        private List<String> modes;

        public void validate() {
            if (StringUtils.isBlank(this.datastoreCode) || StringUtils.isBlank(this.version)) {
                throw new AppException(new ErrorResponse("datastoreCode and version cannot be empty"));
            }
            DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(this.datastoreCode, this.version);
            switch (datastoreSupport) {
                case REDIS -> {
                    for (String mode : this.modes) {
                        DatastoreMode.Redis.get(mode);
                    }
                }
                case MONGODB -> {
                    for (String mode : this.modes) {
                        DatastoreMode.Mongodb.get(mode);
                    }
                }
                case KAFKA -> {
                    for (String mode : this.modes) {
                        DatastoreMode.Kafka.get(mode);
                    }
                }
                case POSTGRESQL -> {
                    for (String mode : this.modes) {
                        DatastoreMode.Postgresql.get(mode);
                    }
                }
                case API_GATEWAY -> {
                    for (String mode : this.modes) {
                        DatastoreMode.ApiGateway.get(mode);
                    }
                }
            }
        }
    }

}
