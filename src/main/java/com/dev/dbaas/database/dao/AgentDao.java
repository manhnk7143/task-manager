package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbAgent;
import org.hibernate.Session;

import java.util.List;

public abstract class AgentDao extends GenericDao<TbAgent>{

    public AgentDao(Class<TbAgent> parameterClass) {
        super(parameterClass);
    }
    public abstract List<TbAgent> findByInstanceId(Session session, String instanceId);
    public abstract List<TbAgent> findByComputeId(Session session, String computeId);
}
