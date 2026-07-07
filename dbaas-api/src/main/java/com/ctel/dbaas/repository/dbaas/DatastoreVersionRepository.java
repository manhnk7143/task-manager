package com.ctel.dbaas.repository.dbaas;
 
import com.ctel.dbaas.entity.dbaas.DatastoreVersionEntity;
import com.ctel.dbaas.repository.dbaas.projection.DatastoreVersionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DatastoreVersionRepository extends JpaRepository<DatastoreVersionEntity, String> {

    @Query(value = "SELECT datastore.name AS datastoreName, datastore.code AS datastoreCode, ver.version AS datastoreVersion FROM DatastoreVersionEntity AS ver INNER JOIN DatastoreEntity datastore ON ver.datastoreId = datastore.id WHERE ver.id = :datastoreVersionId")
    DatastoreVersionInfo getByDatastoreVersionId(@Param("datastoreVersionId") String datastoreVersionId);

    Optional<DatastoreVersionEntity> findFirstByDatastoreIdAndVersion(String datastoreId, String version); // for init data

    Optional<DatastoreVersionEntity> findFirstByDatastoreIdAndIdAndStatus(String datastoreId, String datastoreVersionId, String status);

    List<DatastoreVersionEntity> findAllByDatastoreIdAndStatus(String datastoreId, String status);
}
