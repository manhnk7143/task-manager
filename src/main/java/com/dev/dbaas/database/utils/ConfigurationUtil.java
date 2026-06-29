/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.ConfigurationDao;
import com.dev.dbaas.database.enties.TbConfiguration;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class ConfigurationUtil extends ConfigurationDao {

    static class Holder {
        private static final ConfigurationUtil INSTANCE = new ConfigurationUtil(TbConfiguration.class);
    }
    public static ConfigurationUtil getInstance() {
        return ConfigurationUtil.Holder.INSTANCE;
    }
    public ConfigurationUtil(Class<TbConfiguration> parameterClass) {
        super(parameterClass);
    }

    @Override
    public List<TbConfiguration> findByGroupConfigurationId(Session session, String groupConfigurationId) {
        String sql = "FROM TbConfiguration a WHERE a.groupConfigurationId = :groupConfigurationId";
        Query<TbConfiguration> query = session.createQuery(sql, TbConfiguration.class);
        query.setParameter("groupConfigurationId", groupConfigurationId);
        List<TbConfiguration> records = query.getResultList();
        return records;
    }
}
