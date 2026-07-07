package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.DataReceiveAgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataReceiveAgentRepository extends JpaRepository<DataReceiveAgentEntity, String> {

    DataReceiveAgentEntity findFirstByInstanceId(String instanceId);

}
