package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbFlavor;
import org.hibernate.Session;

import java.util.List;

public abstract class FlavorDao extends GenericDao<TbFlavor>{

    public FlavorDao(Class<TbFlavor> parameterClass) {
        super(parameterClass);
    }
    public abstract TbFlavor findByFlavorId(Session session, String osFlavorId);
}
