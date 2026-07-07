package com.ctel.dbaas.repository.dbaas;
 
import com.ctel.dbaas.entity.dbaas.BackupStrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupStrategyRepository extends JpaRepository<BackupStrategyEntity, String> {

    BackupStrategyEntity findFirstByType(String type);

    BackupStrategyEntity findFirstByTypeAndActiveIsTrue(String type);

}
