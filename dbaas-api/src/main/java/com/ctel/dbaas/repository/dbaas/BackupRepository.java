package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.BackupEntity;
import com.ctel.dbaas.repository.dbaas.projection.backup.BackupDropdown;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BackupRepository extends JpaRepository<BackupEntity, String> {

    Optional<BackupEntity> findFirstByIdAndOrgId(String id, String orgId);

    boolean existsByIdAndOrgId(String id, String orgId);

    Page<BackupEntity> findAllByDatastoreCodeInAndNameAndOrgId(List<String> datastoreCodes, String name, String orgId, Pageable pageable);

    Page<BackupEntity> findAllByOrgIdAndDatastoreCodeIn(String orgId, List<String> datastoreCodes, Pageable pageable);

    @Query(value = "SELECT bk.id AS id FROM BackupEntity bk WHERE bk.backupScheduleId = :backupScheduleId AND bk.orgId = :orgId AND bk.status = :status ORDER BY bk.createdAt DESC LIMIT :limitRecord")
    List<BackupDropdown> getBackupIdsToKeep(@Param("backupScheduleId") String backupScheduleId, @Param("orgId") String orgId,
                                            @Param("status") String status, @Param("limitRecord") Integer limitRecord);

    <T> List<T> findAllByBackupScheduleIdAndIdNotIn(String backupScheduleId, List<String> ids, Class<T> type);

    <T> List<T> findAllByInstanceIdAndIdNotInAndBackupScheduleIdIsNotNull(String instanceId, List<String> ids, Class<T> type);

    Page<BackupEntity> findAllByInstanceIdAndOrgIdAndDatastoreCodeIn(String instanceId, String orgId, List<String> datastoreCodes, Pageable pageable);

    @Query(value = "SELECT backup.id AS id, backup.name AS name, backup.datastoreCode AS datastoreCode FROM BackupEntity backup WHERE backup.deletedAt IS NULL AND backup.orgId = :orgId AND backup.datastoreCode in(:datastoreCodes) AND backup.status = 'COMPLETED'")
    List<BackupDropdown> findAllByOrgIdAndDatastoreCodeIn(@Param("orgId") String orgId, @Param("datastoreCodes") List<String> datastoreCodes);

    @Query(value = "SELECT backup.id AS id, backup.name AS name, backup.datastoreCode AS datastoreCode FROM BackupEntity backup WHERE backup.deletedAt IS NULL AND backup.orgId = :orgId AND backup.datastoreCode in (:datastoreCodes) AND backup.name like %:name% AND backup.status = 'COMPLETED'")
    List<BackupDropdown> findAllByOrgIdAndDatastoreCodeInAndName(@Param("orgId") String orgId, @Param("datastoreCodes") List<String> datastoreCodes, @Param("name") String name);

}
