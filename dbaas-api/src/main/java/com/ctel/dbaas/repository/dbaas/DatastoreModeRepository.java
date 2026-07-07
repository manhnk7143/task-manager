package com.ctel.dbaas.repository.dbaas;
 
import com.ctel.dbaas.entity.dbaas.DatastoreModeEntity;
import com.ctel.dbaas.repository.dbaas.projection.DatastoreModeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DatastoreModeRepository extends JpaRepository<DatastoreModeEntity, String> {

    @Query(value = "SELECT datastore.name AS datastoreName, datastore.code AS datastoreCode, ver.version AS datastoreVersion, ver.id AS datastoreVersionId, mode.name AS datastoreMode, mode.code AS datastoreModeCode FROM DatastoreModeEntity AS mode INNER JOIN DatastoreVersionEntity ver ON mode.datastoreVersionId = ver.id INNER JOIN DatastoreEntity datastore ON ver.datastoreId = datastore.id WHERE mode.id = :datastoreModeId")
    DatastoreModeInfo getByDatastoreModeId(@Param("datastoreModeId") String datastoreModeId);

    Optional<DatastoreModeEntity> findFirstByNameAndDatastoreVersionId(String name, String datastoreVersionId); // for init data

    Optional<DatastoreModeEntity> findFirstByCodeAndDatastoreVersionId(String code, String datastoreVersionId); // for init data

    Optional<DatastoreModeEntity> findFirstByIdAndDatastoreVersionId(String id, String datastoreVersionId);

    List<DatastoreModeEntity> findAllByDatastoreVersionIdAndStatus(String datastoreVersionId, String status);

}
