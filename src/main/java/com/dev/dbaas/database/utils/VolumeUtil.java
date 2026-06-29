/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.VolumeDao;
import com.dev.dbaas.database.enties.TbVolume;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class VolumeUtil extends VolumeDao {

    static class Holder {
        private static final VolumeUtil INSTANCE = new VolumeUtil(TbVolume.class);
    }
    public static VolumeUtil getInstance() {
        return VolumeUtil.Holder.INSTANCE;
    }
    public VolumeUtil(Class<TbVolume> parameterClass) {
        super(parameterClass);
    }

    @Override
    public List<TbVolume> findByComputeId(Session session, String computeId) {
        String sql = "FROM TbVolume a WHERE a.computeId = :computeId";
        Query<TbVolume> query = session.createQuery(sql, TbVolume.class);
        query.setParameter("computeId", computeId);
        List<TbVolume> records = query.getResultList();
        return records;
    }
}
