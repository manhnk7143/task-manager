//package com.ctel.dbaas.init_data;
//
//import com.ctel.dbaas.common.enums.Datastore;
//import com.ctel.dbaas.common.enums.Status;
//import com.ctel.dbaas.entity.*;
//import com.ctel.dbaas.repository.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class PrepareDataMasterMongodb {
//
//    @Autowired
//    private DatastoreRepository datastoreRepository;
//
//    @Autowired
//    private DatastoreVersionRepository datastoreVersionRepository;
//
//    @Autowired
//    private DatastoreModeRepository datastoreModeRepository;
//
//    @Autowired
//    private DatastoreConfigurationRepository datastoreConfigurationRepository;
//
//    @Autowired
//    private GroupConfigurationRepository groupConfigurationRepository;
//
//    public void init() {
//        DatastoreEntity datastore = datastoreRepository.findFirstByName(Datastore.MONGODB.getDatastoreName()).orElse(new DatastoreEntity());
//        if (datastore.getId() == null) {
//            datastore.setName(Datastore.MONGODB.getDatastoreName());
//            datastore.setStatus(Status.ACTIVE.getStatus());
//            datastoreRepository.save(datastore);
//        }
//
//        DatastoreVersionEntity datastoreVersion = datastoreVersionRepository
//                .findFirstByDatastoreIdAndVersion(datastore.getId(), "7.0").orElse(new DatastoreVersionEntity());
//        if (datastoreVersion.getId() == null) {
//            datastoreVersion.setRepoInformation("{}");
//            datastoreVersion.setProvider("CMC");
//            datastoreVersion.setPlatformSupports("docker");
//            datastoreVersion.setVersion("7.0");
//            datastoreVersion.setGlanceImageTags("cmc-dbaas-agent");
//            datastoreVersion.setStatus(Status.ACTIVE.getStatus());
//            datastoreVersion.setDatastoreId(datastore.getId());
//            datastoreVersionRepository.save(datastoreVersion);
//
//            datastore.setDefaultVersionId(datastoreVersion.getId());
//            datastoreRepository.save(datastore);
//        }
//
//        // insert mode standalone for mongodb 7.0
//        DatastoreModeEntity datastoreModeStandalone = datastoreModeRepository.findFirstByNameAndDatastoreVersionId(
//                "Standalone", datastoreVersion.getId()).orElse(new DatastoreModeEntity());
//        if (datastoreModeStandalone.getId() == null) {
//            datastoreModeStandalone.setName("Standalone");
//            datastoreModeStandalone.setCode("standalone");
//            datastoreModeStandalone.setDatastoreVersionId(datastoreVersion.getId());
//            datastoreModeStandalone.setRequirement("{}");
//            datastoreModeStandalone.setDeploymentSteps("{}");
//            datastoreModeStandalone.setStatus(Status.ACTIVE.getStatus());
//
//            datastoreModeRepository.save(datastoreModeStandalone);
//        }
//
//        // insert group config default for mongodb standalone
//        boolean existsGroupConfigDefaultStandalone = groupConfigurationRepository
//                .existsByDatastoreModeIdAndOrgId(datastoreModeStandalone.getId(), "system");
//        if (!existsGroupConfigDefaultStandalone) {
//            GroupConfigurationEntity groupConfigDefault = new GroupConfigurationEntity();
//            groupConfigDefault.setName("default_config_mongodb_7.0_standalone");
//            groupConfigDefault.setDescription("Default config of mongodb 7.0 standalone");
//            groupConfigDefault.setDatastoreModeId(datastoreModeStandalone.getId());
//            groupConfigDefault.setOrgId("system");
//            groupConfigDefault.setDefault(true);
//            groupConfigurationRepository.save(groupConfigDefault);
//        }
//
//        // insert mode replica set for mongodb 7.0
//        DatastoreModeEntity datastoreModeReplicaSet = datastoreModeRepository.findFirstByNameAndDatastoreVersionId(
//                "Replica Set", datastoreVersion.getId()).orElse(new DatastoreModeEntity());
//        if (datastoreModeReplicaSet.getId() == null) {
//            datastoreModeReplicaSet.setName("Replica Set");
//            datastoreModeReplicaSet.setCode("replica_set");
//            datastoreModeReplicaSet.setDatastoreVersionId(datastoreVersion.getId());
//            datastoreModeReplicaSet.setRequirement("{}");
//            datastoreModeReplicaSet.setDeploymentSteps("{}");
//            datastoreModeReplicaSet.setStatus(Status.ACTIVE.getStatus());
//
//            datastoreModeRepository.save(datastoreModeReplicaSet);
//        }
//
//        // insert group config default for redis replica set
//        boolean existsGroupConfigDefaultReplicaSet = groupConfigurationRepository
//                .existsByDatastoreModeIdAndOrgId(datastoreModeReplicaSet.getId(), "system");
//        if (!existsGroupConfigDefaultReplicaSet) {
//            GroupConfigurationEntity groupConfigDefault = new GroupConfigurationEntity();
//            groupConfigDefault.setName("default_config_mongodb_7.0_replicaset");
//            groupConfigDefault.setDescription("Default config of mongodb 7.0 replicaset");
//            groupConfigDefault.setDatastoreModeId(datastoreModeReplicaSet.getId());
//            groupConfigDefault.setOrgId("system");
//            groupConfigDefault.setDefault(true);
//            groupConfigurationRepository.save(groupConfigDefault);
//        }
//
//        // insert mode shared cluster for mongodb 7.0
//        DatastoreModeEntity datastoreModeShardedCluster = datastoreModeRepository.findFirstByNameAndDatastoreVersionId(
//                "Sharded Cluster", datastoreVersion.getId()).orElse(new DatastoreModeEntity());
//        if (datastoreModeShardedCluster.getId() == null) {
//            datastoreModeShardedCluster.setName("Sharded Cluster");
//            datastoreModeShardedCluster.setCode("sharded_cluster");
//            datastoreModeShardedCluster.setDatastoreVersionId(datastoreVersion.getId());
//            datastoreModeShardedCluster.setRequirement("{}");
//            datastoreModeShardedCluster.setDeploymentSteps("{}");
//            datastoreModeShardedCluster.setStatus(Status.ACTIVE.getStatus());
//
//            datastoreModeRepository.save(datastoreModeShardedCluster);
//        }
//
//        // insert group config default for mongodb shared cluster
//        boolean existsGroupConfigDefaultSharedCluster = groupConfigurationRepository
//                .existsByDatastoreModeIdAndOrgId(datastoreModeShardedCluster.getId(), "system");
//        if (!existsGroupConfigDefaultSharedCluster) {
//            GroupConfigurationEntity groupConfigDefault = new GroupConfigurationEntity();
//            groupConfigDefault.setName("default_config_mongodb_7.0_sharded_cluster");
//            groupConfigDefault.setDescription("Default config of mongodb 7.0 shared cluster");
//            groupConfigDefault.setDatastoreModeId(datastoreModeShardedCluster.getId());
//            groupConfigDefault.setOrgId("system");
//            groupConfigDefault.setDefault(true);
//            groupConfigurationRepository.save(groupConfigDefault);
//        }
//
////        // insert config for redis 7.0 standalone
//        boolean existsDefaultConfigStandalone = datastoreConfigurationRepository
//                .existsByDatastoreModeId(datastoreModeStandalone.getId());
//        if (!existsDefaultConfigStandalone) {
//            System.out.println("===== Save config of mongodb standalone");
//            DatastoreConfigurationEntity entity = new DatastoreConfigurationEntity();
//            entity.setDatastoreModeId(datastoreModeStandalone.getId());
//            entity.setStatus("active");
//            entity.setParamName("security.authorization");
//            entity.setDefaultValue("disabled");
//            entity.setRangeValue("enabled,disabled");
//            entity.setTypeValue("Enum");
//            entity.setDescription("Enable or disable Role-Based Access Control (RBAC) to govern each user's access to database resources and operations.");
//            datastoreConfigurationRepository.save(entity);
//        }
//
//        boolean existsDefaultConfigReplicaSet = datastoreConfigurationRepository
//                .existsByDatastoreModeId(datastoreModeReplicaSet.getId());
//        if (!existsDefaultConfigReplicaSet) {
//            System.out.println("===== Save config of mongodb replica_set");
//            DatastoreConfigurationEntity entity = new DatastoreConfigurationEntity();
//            entity.setDatastoreModeId(datastoreModeReplicaSet.getId());
//            entity.setStatus("active");
//            entity.setParamName("replication.oplogSizeMB");
//            entity.setDefaultValue("10000");
//            entity.setRangeValue("10000-32000000");
//            entity.setTypeValue("Integer");
//            entity.setDescription("The maximum size in megabytes for the replication operation log.");
//            datastoreConfigurationRepository.save(entity);
//        }
//    }
//
//}
