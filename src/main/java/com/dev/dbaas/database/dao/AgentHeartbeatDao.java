package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbAgentHeartbeat;
import org.hibernate.Session;

public abstract class AgentHeartbeatDao extends GenericDao<TbAgentHeartbeat>{

    public AgentHeartbeatDao(Class<TbAgentHeartbeat> parameterClass) {
        super(parameterClass);
    }

    public abstract TbAgentHeartbeat getByAgentId(Session session, String agentId);
}
