package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbAgentFirmware;

import java.util.List;

public abstract class AgentFirmwareDao extends GenericDao<TbAgentFirmware>{

    public AgentFirmwareDao(Class<TbAgentFirmware> parameterClass) {
        super(parameterClass);
    }
}
