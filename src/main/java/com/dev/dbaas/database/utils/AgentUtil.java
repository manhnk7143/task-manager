/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.AgentDao;
import com.dev.dbaas.database.enties.TbAgent;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class AgentUtil extends AgentDao {

    static class Holder {
        private static final AgentUtil INSTANCE = new AgentUtil(TbAgent.class);
    }
    public static AgentUtil getInstance() {
        return AgentUtil.Holder.INSTANCE;
    }

    public AgentUtil(Class<TbAgent> parameterClass) {
        super(parameterClass);
    }

    @Override
    public List<TbAgent> findByInstanceId(Session session, String instanceId) {
        String sql = "FROM TbAgent a WHERE a.instanceId = :instanceId";
        Query<TbAgent> query = session.createQuery(sql, TbAgent.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    @Override
    public List<TbAgent> findByComputeId(Session session, String computeId) {
        String sql = "FROM TbAgent a WHERE a.computeId = :computeId";
        Query<TbAgent> query = session.createQuery(sql, TbAgent.class);
        query.setParameter("computeId", computeId);
        return query.getResultList();
    }
}
