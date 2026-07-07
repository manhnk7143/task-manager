package com.ctel.dbaas.repository.dbaas;
 
import com.ctel.dbaas.entity.dbaas.GroupConfigurationEntity;
import com.ctel.dbaas.repository.dbaas.projection.group_config.GroupConfigDropdown;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupConfigurationRepository extends JpaRepository<GroupConfigurationEntity, String> {

    Optional<GroupConfigurationEntity> findByIdAndOrgId(String id, String orgId);

    @Query(value = """
            SELECT
                gr
            FROM GroupConfigurationEntity gr
                INNER JOIN DatastoreModeEntity mode ON gr.datastoreModeId = mode.id
                INNER JOIN DatastoreVersionEntity ver ON mode.datastoreVersionId = ver.id
                INNER JOIN DatastoreEntity dt ON ver.datastoreId = dt.id
            WHERE gr.orgId = :orgId AND dt.code = :datastoreCode
            """)
    Page<GroupConfigurationEntity> queryAllByOrgIdAndDatastoreCode(@Param("orgId") String orgId,
                                                                   @Param("datastoreCode") String datastoreCode,
                                                                   Pageable pageable);

    @Query(value = """
            SELECT
                groupConfig
            FROM GroupConfigurationEntity groupConfig
                INNER JOIN DatastoreModeEntity mode ON groupConfig.datastoreModeId = mode.id
                INNER JOIN DatastoreVersionEntity ver ON mode.datastoreVersionId = ver.id
                INNER JOIN DatastoreEntity datastore ON ver.datastoreId = datastore.id
            WHERE groupConfig.orgId = :orgId
                AND datastore.code = :datastoreCode
                AND LOWER(groupConfig.name) LIKE LOWER(CONCAT('%', :groupConfigName, '%') )
            """)
    Page<GroupConfigurationEntity> queryAllByOrgIdAndNameLike(@Param("orgId") String orgId,
                                                              @Param("datastoreCode") String datastoreCode,
                                                              @Param("groupConfigName") String groupConfigName,
                                                              Pageable pageable);

    Page<GroupConfigurationEntity> findAllByOrgIdAndDatastoreModeIdAndNameContainingIgnoreCase(String orgId, String datastoreModeId, String name, Pageable pageable);

    Page<GroupConfigurationEntity> findAllByOrgIdAndDatastoreModeId(String orgId, String datastoreModeId, Pageable pageable);

    Optional<GroupConfigurationEntity> findFirstByIdAndOrgId(String groupConfigId, String orgId);

    Optional<GroupConfigurationEntity> findFirstByDatastoreModeId(String datastoreModeId);

    GroupConfigurationEntity findFirstByOrgIdAndDatastoreModeId(String orgId, String datastoreModeId);

    @Query(value = "SELECT group.id FROM GroupConfigurationEntity group WHERE group.orgId = :orgId AND group.id in :ids")
    List<String> getIdsByOrgId(@Param("orgId") String orgId, @Param("ids") List<String> groupIds);

    GroupConfigurationEntity findFirstByIdAndDatastoreModeIdAndOrgId(String id, String datastoreModeId, String orgId);

    boolean existsByDatastoreModeIdAndOrgId(String datastoreModeId, String orgId);

    @Query(value = "SELECT gr.id AS id, gr.name AS name, gr.isDefault as default FROM GroupConfigurationEntity gr WHERE gr.datastoreModeId = :datastoreModeId and gr.orgId in :orgIds order by gr.isDefault desc")
    List<GroupConfigDropdown> getGroupConfigDropdown(@Param("datastoreModeId") String datastoreModeId, @Param("orgIds") List<String> orgIds);
}
