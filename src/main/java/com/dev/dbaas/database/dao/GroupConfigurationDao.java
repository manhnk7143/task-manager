package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbGroupConfiguration;

public abstract class GroupConfigurationDao extends GenericDao<TbGroupConfiguration>{

    public GroupConfigurationDao(Class<TbGroupConfiguration> parameterClass) {
        super(parameterClass);
    }
}
