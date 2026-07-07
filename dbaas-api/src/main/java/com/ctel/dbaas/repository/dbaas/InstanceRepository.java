package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.InstanceEntity;
import com.ctel.dbaas.repository.dbaas.custom.InstanceRepoCustom;
import com.ctel.dbaas.repository.dbaas.projection.instance.InstanceInfoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InstanceRepository extends JpaRepository<InstanceEntity, String>, InstanceRepoCustom {

    List<InstanceEntity> findAllByGroupConfigurationIdInAndOrgIdAndProjectIdAndDeletedAtIsNull(List<String> groupConfigIds, String orgId, String projectId);

    List<InstanceEntity> findAllByOrgIdAndProjectIdAndDeletedAtNullAndGroupConfigurationId( String orgId, String projectId, String groupConfigId);

    Optional<InstanceEntity> findByIdAndDeletedAtIsNull(String instanceId);

    Optional<InstanceEntity> findByIdAndOrgIdAndProjectIdAndDeletedAtIsNull(String instanceId, String orgId, String projectId);

    List<InstanceEntity> findAllByIdInAndOrgIdAndProjectId(List<String> instanceIds, String orgId, String projectId);

    Optional<InstanceEntity> findByIdAndOrgIdAndProjectId(String id, String orgId, String projectId);

    Page<InstanceEntity> findByOrgIdAndDatastoreIdInAndProjectIdAndDeletedAtNullAndNameContainingIgnoreCase(String orgId, List<String> datastoreIds, String projectId, String nameSearch, Pageable pageable);

    Page<InstanceEntity> findByOrgIdAndDatastoreIdInAndProjectIdAndDeletedAtNull(String orgId, List<String> datastoreIds, String projectId, Pageable pageable);

    @Query(value = "SELECT instance.id AS id, instance.name AS name, datastore.name AS datastoreName, datastore.code AS datastoreCode, instance.orgId AS orgId, instance.projectId AS projectId, instance.regionId AS regionId FROM InstanceEntity instance INNER JOIN DatastoreEntity datastore ON instance.datastoreId = datastore.id WHERE instance.id = :instanceId AND instance.deletedAt is null")
    InstanceInfoProjection getInstanceNameById(@Param("instanceId") String instanceId);

    @Query(value = "SELECT instance.id AS id, instance.name AS name FROM InstanceEntity instance WHERE instance.groupConfigurationId = :groupConfigId AND instance.deletedAt IS NULL")
    List<InstanceInfoProjection> getInstanceNameByGroupConfigId(@Param("groupConfigId") String groupConfigId);

    // for report
    @Query(value = "SELECT count (distinct ins.orgId) FROM InstanceEntity ins INNER JOIN DatastoreEntity ds ON ds.id = ins.datastoreId WHERE ds.code = :datastoreCode AND ins.deletedAt IS NULL AND ins.orgId NOT IN :listOrgInternal AND ins.createdAt BETWEEN :fromDate AND :toDate")
    long countCustomersByDatastore(@Param("datastoreCode") String datastoreCode,
                        @Param("listOrgInternal") List<String> listOrgInternal,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

    @Query(value = "SELECT ins.id AS id, ins.orgId AS orgId, ins.status AS status, ins.regionId AS regionId FROM InstanceEntity ins INNER JOIN DatastoreEntity ds ON ds.id = ins.datastoreId WHERE ds.code = :datastoreCode AND ins.deletedAt IS NULL AND ins.orgId NOT IN :listOrgInternal AND ins.createdAt BETWEEN :fromDate AND :toDate")
    List<InstanceInfoProjection> getReportInstances(@Param("datastoreCode") String datastoreCode,
                                                    @Param("listOrgInternal") List<String> listOrgInternal,
                                                    @Param("fromDate") LocalDateTime fromDate,
                                                    @Param("toDate") LocalDateTime toDate);
}
