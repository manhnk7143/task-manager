package com.ctel.dbaas.init_data;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.enums.Datastore;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.common.enums.Status;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.repository.dbaas.*;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
@Transactional
public class InitDatabaseService {

    @Autowired
    private DatastoreRepository datastoreRepository;

    @Autowired
    private DatastoreVersionRepository datastoreVersionRepository;

    @Autowired
    private DatastoreModeRepository datastoreModeRepository;

    @Autowired
    private DatastoreConfigurationRepository datastoreConfigurationRepository;

    @Autowired
    private GroupConfigurationRepository groupConfigurationRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private BackupStrategyRepository backupStrategyRepository;

    public DatastoreEntity initDatastore(String datastoreCode) {
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(datastoreCode);
        DatastoreEntity datastore = datastoreRepository.findFirstByCode(datastoreSupport.getCode());
        if (datastore == null) {
            datastore = new DatastoreEntity();
            datastore.setName(datastoreSupport.getName());
            datastore.setCode(datastoreSupport.getCode());
            datastore.setStatus(Status.ACTIVE.getStatus());
            datastore.setTag("");
            datastoreRepository.save(datastore);
            log.info("Insert new datastore : {}", datastoreSupport.getCode());
        }

        return datastore;
    }

    public DatastoreEntity getDatastore(String datastoreCode) {
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(datastoreCode);
        return datastoreRepository.findFirstByCode(datastoreSupport.getCode());
    }

    public DatastoreEntity removeDatastore(String datastoreName) {
        Datastore datastoreEnum = Datastore.get(datastoreName);
        DatastoreEntity datastore = datastoreRepository.findFirstByName(datastoreEnum.getDatastoreName())
                .orElse(new DatastoreEntity());
        if (datastore.getId() != null) {
            datastoreRepository.delete(datastore);
        }

        return datastore;
    }

    public DatastoreVersionEntity initDatastoreVersion(DatastoreEntity datastore, String version) {
        DatastoreVersionEntity datastoreVersion = datastoreVersionRepository
                .findFirstByDatastoreIdAndVersion(datastore.getId(), version).orElse(new DatastoreVersionEntity());
        if (datastoreVersion.getId() == null) {
            datastoreVersion.setRepoInformation("{}");
            datastoreVersion.setProvider("CMC");
            datastoreVersion.setPlatformSupports("docker");
            datastoreVersion.setVersion(version);
            datastoreVersion.setGlanceImageTags(Constant.GLANCE_IMAGE_TAG);
            datastoreVersion.setStatus(Status.ACTIVE.getStatus());
            datastoreVersion.setDatastoreId(datastore.getId());
            datastoreVersionRepository.save(datastoreVersion);

            datastore.setDefaultVersionId(datastoreVersion.getId());
            datastoreRepository.save(datastore);
            log.info("Insert new version {} for datastore {}", version, datastore.getName());
        }
        return datastoreVersion;
    }

    public DatastoreVersionEntity getDatastoreVersion(String datastoreId, String datastoreVersion) {
        return datastoreVersionRepository.findFirstByDatastoreIdAndVersion(datastoreId, datastoreVersion).orElse(null);
    }

    public DatastoreVersionEntity removeDatastoreVersion(DatastoreEntity datastore, String version) {
        DatastoreVersionEntity datastoreVersion = datastoreVersionRepository
                .findFirstByDatastoreIdAndVersion(datastore.getId(), version).orElse(new DatastoreVersionEntity());
        if (datastoreVersion.getId() != null) {
            datastoreVersionRepository.delete(datastoreVersion);
        }

        return datastoreVersion;
    }

    public DatastoreModeEntity initDatastoreMode(DatastoreVersionEntity datastoreVersion, String modeName, String modeCode) {
        DatastoreModeEntity datastoreMode = datastoreModeRepository.findFirstByNameAndDatastoreVersionId(
                modeName, datastoreVersion.getId()).orElse(new DatastoreModeEntity());
        if (datastoreMode.getId() == null) {
            datastoreMode.setName(modeName);
            datastoreMode.setCode(modeCode);
            datastoreMode.setDatastoreVersionId(datastoreVersion.getId());
            datastoreMode.setRequirement("{}");
            datastoreMode.setDeploymentSteps("{}");
            datastoreMode.setStatus(Status.ACTIVE.getStatus());
            datastoreMode.setTag("");

            datastoreModeRepository.save(datastoreMode);
        }
        return datastoreMode;
    }

    public DatastoreModeEntity getDatastoreMode(String modeCode, String datastoreVersionId) {
        return datastoreModeRepository.findFirstByCodeAndDatastoreVersionId(modeCode, datastoreVersionId).orElse(null);
    }

    public DatastoreModeEntity removeMode(DatastoreVersionEntity datastoreVersion, String modeCode) {
        DatastoreModeEntity datastoreMode = datastoreModeRepository.findFirstByCodeAndDatastoreVersionId(
                modeCode, datastoreVersion.getId()).orElse(new DatastoreModeEntity());
        if (datastoreMode.getId() != null) {
            datastoreModeRepository.delete(datastoreMode);
        }

        return datastoreMode;
    }

    public void initDatastoreConfiguration(DatastoreModeEntity datastoreMode, String datastoreCode, String datastoreVersion) {
        boolean existsDefaultConfig = datastoreConfigurationRepository
                .existsByDatastoreModeId(datastoreMode.getId());
        if (!existsDefaultConfig) {
            String datastoreNameVersion = datastoreCode.concat(":").concat(datastoreVersion).concat(":").concat(datastoreMode.getCode());
            List<FactoryConfigDatastore.ConfigDatastore> lstConfig = FactoryConfigDatastore.loadConfig(datastoreNameVersion);
            List<DatastoreConfigurationEntity> lstEntity = new ArrayList<>();
            for (FactoryConfigDatastore.ConfigDatastore conf : lstConfig) {
                DatastoreConfigurationEntity entity = new DatastoreConfigurationEntity();
                entity.setParamName(conf.getParamName());
                entity.setDefaultValue(conf.getDefaultValue());
                entity.setRangeValue(conf.getRangeValue());
                entity.setTypeValue(conf.getTypeValue());
                entity.setDescription(conf.getDescription());
                entity.setNeedRestart(conf.getNeedRestart());
                entity.setDatastoreModeId(datastoreMode.getId());
                entity.setStatus(Status.ACTIVE.getStatus());
                lstEntity.add(entity);
            }
            datastoreConfigurationRepository.saveAll(lstEntity);
        }

        GroupConfigurationEntity groupConfiguration = groupConfigurationRepository
                .findFirstByOrgIdAndDatastoreModeId(Constant.SYSTEM, datastoreMode.getId());
        if (groupConfiguration == null) {
            groupConfiguration = new GroupConfigurationEntity();
            groupConfiguration.setName(String.format("default_config_%s_%s_%s", datastoreCode, datastoreVersion, datastoreMode.getCode()));
            groupConfiguration.setDescription(String.format("Default config of %s %s %s", datastoreCode, datastoreVersion, datastoreMode.getCode()));
            groupConfiguration.setDatastoreModeId(datastoreMode.getId());
            groupConfiguration.setOrgId(Constant.SYSTEM);
            groupConfiguration.setDefault(true);
            groupConfigurationRepository.save(groupConfiguration);
        }
    }

    public void removeConfigAndGroupConfig(DatastoreModeEntity datastoreMode) {
        GroupConfigurationEntity groupConfiguration = groupConfigurationRepository
                .findFirstByOrgIdAndDatastoreModeId(Constant.SYSTEM, datastoreMode.getId());
        if (groupConfiguration != null) {
            groupConfigurationRepository.delete(groupConfiguration);
        }
        datastoreConfigurationRepository.deleteAllByDatastoreModeId(datastoreMode.getId());
    }

}
