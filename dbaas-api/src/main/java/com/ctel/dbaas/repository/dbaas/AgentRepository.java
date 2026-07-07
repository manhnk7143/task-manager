package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<AgentEntity, String> {

    AgentEntity findFirstByComputeIdAndInstanceId(String computeId, String instanceId);

    AgentEntity findFirstByComputeId(String computeId);

}
