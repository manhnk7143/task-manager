package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbDatastoreVersion;

public abstract class DatastoreVersionDao extends GenericDao<TbDatastoreVersion>{

    public DatastoreVersionDao(Class<TbDatastoreVersion> parameterClass) {
        super(parameterClass);
    }
}
