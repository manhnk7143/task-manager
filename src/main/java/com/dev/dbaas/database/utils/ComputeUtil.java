/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.ComputeDao;
import com.dev.dbaas.database.enties.TbCompute;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class ComputeUtil extends ComputeDao {

    static class Holder {
        private static final ComputeUtil INSTANCE = new ComputeUtil(TbCompute.class);
    }
    public static ComputeUtil getInstance() {
        return ComputeUtil.Holder.INSTANCE;
    }

    public ComputeUtil(Class<TbCompute> parameterClass) {
        super(parameterClass);
    }

    @Override
    public List<TbCompute> findByInstanceId(Session session, String instanceId) {
        String sql = "FROM TbCompute a WHERE a.instanceId = :instanceId";
        Query<TbCompute> query = session.createQuery(sql, TbCompute.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    @Override
    public List<TbCompute> findByInstanceIdAndRole(Session session, String instanceId, String role) {
        String sql = "FROM TbCompute a WHERE a.instanceId = :instanceId AND a.role = :role";
        Query<TbCompute> query = session.createQuery(sql, TbCompute.class);
        query.setParameter("instanceId", instanceId);
        query.setParameter("role", role);
        return query.getResultList();
    }
}
