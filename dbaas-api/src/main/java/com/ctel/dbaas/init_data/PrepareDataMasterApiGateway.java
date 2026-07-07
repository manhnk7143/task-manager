//package com.ctel.dbaas.init_data;
//
//import com.ctel.dbaas.common.enums.Datastore;
//import com.ctel.dbaas.common.enums.Status;
//import com.ctel.dbaas.entity.DatastoreEntity;
//import com.ctel.dbaas.entity.DatastoreModeEntity;
//import com.ctel.dbaas.entity.DatastoreVersionEntity;
//import com.ctel.dbaas.repository.dbaas.DatastoreModeRepository;
//import com.ctel.dbaas.repository.dbaas.DatastoreRepository;
//import com.ctel.dbaas.repository.dbaas.DatastoreVersionRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class PrepareDataMasterApiGateway {
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
//    public void init() {
//        DatastoreEntity datastore = datastoreRepository.findFirstByName(Datastore.API_GATEWAY.getDatastoreName()).orElse(new DatastoreEntity());
//        if (datastore.getId() == null) {
//            datastore.setName(Datastore.API_GATEWAY.getDatastoreName());
//            datastore.setStatus(Status.ACTIVE.getStatus());
//            datastoreRepository.save(datastore);
//        }
//
//        // insert version 1.0 for api gateway
//        DatastoreVersionEntity datastoreVersion = datastoreVersionRepository
//                .findFirstByDatastoreIdAndVersion(datastore.getId(), "1.0").orElse(new DatastoreVersionEntity());
//        if (datastoreVersion.getId() == null) {
//            datastoreVersion.setRepoInformation("{}");
//            datastoreVersion.setProvider("CMC");
//            datastoreVersion.setPlatformSupports("docker");
//            datastoreVersion.setVersion("1.0");
//            datastoreVersion.setGlanceImageTags("cmc-dbaas-agent");
//            datastoreVersion.setStatus(Status.ACTIVE.getStatus());
//            datastoreVersion.setDatastoreId(datastore.getId());
//            datastoreVersionRepository.save(datastoreVersion);
//
//            datastore.setDefaultVersionId(datastoreVersion.getId());
//            datastoreRepository.save(datastore);
//        }
//
//        // insert mode standalone for api gateway 1.0
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
//    }
//
//}
