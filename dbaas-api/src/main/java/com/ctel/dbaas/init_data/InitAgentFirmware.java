package com.ctel.dbaas.init_data;

import com.ctel.dbaas.entity.dbaas.AgentFirmwareEntity;
import com.ctel.dbaas.repository.dbaas.AgentFirmwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InitAgentFirmware {

    @Autowired
    private AgentFirmwareRepository agentFirmwareRepository;

    public void init() {
        AgentFirmwareEntity entity = agentFirmwareRepository.findFirstByBuildNumber(1);
        if (entity == null) {
            entity = new AgentFirmwareEntity();
            entity.setBuildNumber(1);
            entity.setOsSupport("ubuntu_22.04");
            agentFirmwareRepository.save(entity);
        }
    }

}
