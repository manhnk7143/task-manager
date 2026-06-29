/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.AgentHeartbeatDao;
import com.dev.dbaas.database.enties.TbAgentHeartbeat;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class AgentHeartbeatUtil extends AgentHeartbeatDao {

    static class Holder {
        private static final AgentHeartbeatUtil INSTANCE = new AgentHeartbeatUtil(TbAgentHeartbeat.class);
    }
    public static AgentHeartbeatUtil getInstance() {
        return Holder.INSTANCE;
    }

    public AgentHeartbeatUtil(Class<TbAgentHeartbeat> parameterClass) {
        super(parameterClass);
    }

    @Override
    public TbAgentHeartbeat getByAgentId(Session session, String agentId) {

        String sql = "FROM TbAgentHeartbeat a WHERE a.agentId = :agentId";
        Query<TbAgentHeartbeat> query = session.createQuery(sql, TbAgentHeartbeat.class);
        query.setParameter("agentId", agentId);
        List<TbAgentHeartbeat> records = query.getResultList();
        if(records != null && !records.isEmpty()){
            return records.get(0);
        }
        return null;
    }
}
