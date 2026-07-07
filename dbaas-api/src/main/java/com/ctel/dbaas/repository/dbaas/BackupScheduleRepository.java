package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.BackupScheduleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface BackupScheduleRepository extends JpaRepository<BackupScheduleEntity, String> {

    BackupScheduleEntity findFirstByInstanceIdAndProjectIdAndOrgId(String instanceId, String projectId, String orgId);

    Optional<BackupScheduleEntity> findFirstByIdAndProjectIdAndOrgId(String id, String projectId, String orgId);

    Page<BackupScheduleEntity> findAllByOrgIdAndProjectIdAndDatastoreCode(String orgId, String projectId, String datastoreCode, Pageable pageable);

    @Transactional
    void deleteByInstanceId(String instanceId);

}
