package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbDatastoreMode;

public abstract class DatastoreModeDao extends GenericDao<TbDatastoreMode>{

    public DatastoreModeDao(Class<TbDatastoreMode> parameterClass) {
        super(parameterClass);
    }
}
