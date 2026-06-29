package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbDatastoreConfiguration;

public abstract class DatastoreConfigurationDao extends GenericDao<TbDatastoreConfiguration>{

    public DatastoreConfigurationDao(Class<TbDatastoreConfiguration> parameterClass) {
        super(parameterClass);
    }
}
