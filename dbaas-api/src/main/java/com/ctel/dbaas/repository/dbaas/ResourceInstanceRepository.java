package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.ResourceInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceInstanceRepository extends JpaRepository<ResourceInstanceEntity, String> {

    ResourceInstanceEntity findFirstByInstanceIdAndDeletedAtIsNull(String instanceId);

}
