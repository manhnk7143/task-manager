package com.ctel.dbaas.repository.dbaas;
 
import com.ctel.dbaas.entity.dbaas.AgentHeartbeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentHeartbeatRepository extends JpaRepository<AgentHeartbeatEntity, String> {

    AgentHeartbeatEntity findFirstByComputeIdAndInstanceId(String computeId, String instanceId);

}
