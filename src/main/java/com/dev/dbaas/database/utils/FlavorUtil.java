/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.FlavorDao;
import com.dev.dbaas.database.enties.TbFlavor;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class FlavorUtil extends FlavorDao {

    static class Holder {
        private static final FlavorUtil INSTANCE = new FlavorUtil(TbFlavor.class);
    }
    public static FlavorUtil getInstance() {
        return FlavorUtil.Holder.INSTANCE;
    }

    public FlavorUtil(Class<TbFlavor> parameterClass) {
        super(parameterClass);
    }

    @Override
    public TbFlavor findByFlavorId(Session session, String osFlavorId) {
        String sql = "FROM TbFlavor a WHERE a.osFlavorId = :osFlavorId";
        Query<TbFlavor> query = session.createQuery(sql, TbFlavor.class);
        query.setParameter("osFlavorId", osFlavorId);
        List<TbFlavor> records = query.getResultList();
        if(records != null && !records.isEmpty()){
            return records.get(0);
        }
        return null;
    }
}
