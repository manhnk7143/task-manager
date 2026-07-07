package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.AgentFirmwareEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentFirmwareRepository extends JpaRepository<AgentFirmwareEntity, String> {

    AgentFirmwareEntity findFirstByOrderByBuildNumberDesc();

    AgentFirmwareEntity findFirstByBuildNumber(Integer buildNumber);

}
