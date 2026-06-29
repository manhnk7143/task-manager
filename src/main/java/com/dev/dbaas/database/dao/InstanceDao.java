package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbInstance;

import java.util.List;

public abstract class InstanceDao extends GenericDao<TbInstance>{

    public InstanceDao(Class<TbInstance> parameterClass) {
        super(parameterClass);
    }
}
