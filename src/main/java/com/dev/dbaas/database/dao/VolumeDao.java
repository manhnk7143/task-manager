package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbVolume;
import org.hibernate.Session;

import java.util.List;

public abstract class VolumeDao extends GenericDao<TbVolume>{

    public VolumeDao(Class<TbVolume> parameterClass) {
        super(parameterClass);
    }
    public abstract List<TbVolume> findByComputeId(Session session, String computeId);
}
