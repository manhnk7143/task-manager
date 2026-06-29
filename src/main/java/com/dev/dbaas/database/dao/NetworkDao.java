package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbNetwork;
import org.hibernate.Session;

import java.util.List;

public abstract class NetworkDao extends GenericDao<TbNetwork>{

    public NetworkDao(Class<TbNetwork> parameterClass) {
        super(parameterClass);
    }
    public abstract List<TbNetwork> findByComputeId(Session session, String computeId);
    public abstract TbNetwork findByComputeIdAndMode(Session session, String computeId, String mode);
}
