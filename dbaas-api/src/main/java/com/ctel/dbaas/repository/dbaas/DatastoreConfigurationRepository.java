package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.DatastoreConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatastoreConfigurationRepository extends JpaRepository<DatastoreConfigurationEntity, String> {

    boolean existsByDatastoreModeId(String datastoreModeId); // for init data

    void deleteAllByDatastoreModeId(String datastoreModeId);

    List<DatastoreConfigurationEntity> findAllByDatastoreModeIdAndStatus(String datastoreModeId, String status);

}
