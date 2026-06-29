/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.DatastoreVersionDao;
import com.dev.dbaas.database.enties.TbDatastoreVersion;

/**
 *
 * @author hieutrinh
 */
public class DatastoreVersionUtil extends DatastoreVersionDao {

    static class Holder {
        private static final DatastoreVersionUtil INSTANCE = new DatastoreVersionUtil(TbDatastoreVersion.class);
    }
    public static DatastoreVersionUtil getInstance() {
        return DatastoreVersionUtil.Holder.INSTANCE;
    }

    public DatastoreVersionUtil(Class<TbDatastoreVersion> parameterClass) {
        super(parameterClass);
    }
}
