package com.ctel.dbaas.repository.dbaas;
 
import com.ctel.dbaas.entity.dbaas.ConfigurationEntity;
import com.ctel.dbaas.repository.dbaas.projection.QueryConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConfigurationRepository extends JpaRepository<ConfigurationEntity, String> {

    List<ConfigurationEntity> findAllByGroupConfigurationIdAndOrgId(String groupConfigurationId, String orgId);

    @Query("SELECT config.id AS id, config.datastoreConfigurationId as datastoreConfigId, config.paramName AS paramName, config.paramValue AS paramValue, config.createdAt AS createdAt, config.updatedAt AS updatedAt, dc.defaultValue AS defaultValue, dc.rangeValue AS valueRange, dc.typeValue AS valueType, dc.description AS description FROM ConfigurationEntity config INNER JOIN DatastoreConfigurationEntity dc ON config.datastoreConfigurationId = dc.id WHERE config.groupConfigurationId = :groupConfigurationId AND config.orgId = :orgId")
    List<QueryConfiguration> getConfigurationInfo(String groupConfigurationId, String orgId);

    List<ConfigurationEntity> findAllByGroupConfigurationId(String groupConfigurationId);

    void deleteAllByGroupConfigurationIdInAndOrgId(List<String> groupConfigIds, String orgId);

}
