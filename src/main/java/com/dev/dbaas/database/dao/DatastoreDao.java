package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbDatastore;

public abstract class DatastoreDao extends GenericDao<TbDatastore>{

    public DatastoreDao(Class<TbDatastore> parameterClass) {
        super(parameterClass);
    }
}
