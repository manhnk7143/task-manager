/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.NetworkDao;
import com.dev.dbaas.database.enties.TbNetwork;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class NetworkUtil extends NetworkDao {

    static class Holder {
        private static final NetworkUtil INSTANCE = new NetworkUtil(TbNetwork.class);
    }
    public static NetworkUtil getInstance() {
        return NetworkUtil.Holder.INSTANCE;
    }
    public NetworkUtil(Class<TbNetwork> parameterClass) {
        super(parameterClass);
    }

    @Override
    public List<TbNetwork> findByComputeId(Session session, String computeId) {
        String sql = "FROM TbNetwork a WHERE a.computeId = :computeId";
        Query<TbNetwork> query = session.createQuery(sql, TbNetwork.class);
        query.setParameter("computeId", computeId);
        List<TbNetwork> records = query.getResultList();
        return records;
    }

    @Override
    public TbNetwork findByComputeIdAndMode(Session session, String computeId, String mode) {
        String sql = "FROM TbNetwork a WHERE a.computeId = :computeId AND a.mode = :mode";
        Query<TbNetwork> query = session.createQuery(sql, TbNetwork.class);
        query.setParameter("computeId", computeId);
        query.setParameter("mode", mode);
        query.setMaxResults(1);
        List<TbNetwork> records = query.getResultList();
        if(records != null && !records.isEmpty()){
            return records.get(0);
        }
        return null;
    }
}
