package com.ctel.dbaas.repository.dbaas;
 
import com.ctel.dbaas.entity.dbaas.DatastoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatastoreRepository extends JpaRepository<DatastoreEntity, String> {

    Optional<DatastoreEntity> findFirstByName(String name); // for init data

    DatastoreEntity findFirstByCode(String code);

    Optional<DatastoreEntity> findFirstByCodeAndStatus(String code, String status);

    List<DatastoreEntity> findAllByStatus(String status);

    List<DatastoreEntity> findByCodeInAndStatus(List<String> datastoreCodes, String status);

    List<DatastoreEntity> findAllByCodeInAndStatus(List<String> lstDatastoreCode, String status);

}
