/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.DatastoreDao;
import com.dev.dbaas.database.enties.TbDatastore;

/**
 *
 * @author hieutrinh
 */
public class DatastoreUtil extends DatastoreDao {

    static class Holder {
        private static final DatastoreUtil INSTANCE = new DatastoreUtil(TbDatastore.class);
    }
    public static DatastoreUtil getInstance() {
        return DatastoreUtil.Holder.INSTANCE;
    }

    public DatastoreUtil(Class<TbDatastore> parameterClass) {
        super(parameterClass);
    }
}
