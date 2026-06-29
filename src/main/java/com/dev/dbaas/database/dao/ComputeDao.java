package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbCompute;
import org.hibernate.Session;

import java.util.List;

public abstract class ComputeDao extends GenericDao<TbCompute>{

    public ComputeDao(Class<TbCompute> parameterClass) {
        super(parameterClass);
    }
    public abstract List<TbCompute> findByInstanceId(Session session, String instanceId);
    public abstract List<TbCompute> findByInstanceIdAndRole(Session session, String instanceId, String role);
}
