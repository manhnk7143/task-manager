/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.DatastoreModeDao;
import com.dev.dbaas.database.enties.TbDatastoreMode;

/**
 *
 * @author hieutrinh
 */
public class DatastoreModeUtil extends DatastoreModeDao {

    static class Holder {
        private static final DatastoreModeUtil INSTANCE = new DatastoreModeUtil(TbDatastoreMode.class);
    }
    public static DatastoreModeUtil getInstance() {
        return DatastoreModeUtil.Holder.INSTANCE;
    }

    public DatastoreModeUtil(Class<TbDatastoreMode> parameterClass) {
        super(parameterClass);
    }
}
