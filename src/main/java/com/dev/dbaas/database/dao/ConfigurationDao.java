package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbConfiguration;
import org.hibernate.Session;

import java.util.List;

public abstract class ConfigurationDao extends GenericDao<TbConfiguration>{

    public ConfigurationDao(Class<TbConfiguration> parameterClass) {
        super(parameterClass);
    }
    public abstract List<TbConfiguration> findByGroupConfigurationId(Session session, String groupConfigurationId);
}
