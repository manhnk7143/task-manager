/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.DatastoreConfigurationDao;
import com.dev.dbaas.database.enties.TbDatastoreConfiguration;

/**
 *
 * @author hieutrinh
 */
public class DatastoreConfigurationUtil extends DatastoreConfigurationDao {

    static class Holder {
        private static final DatastoreConfigurationUtil INSTANCE = new DatastoreConfigurationUtil(TbDatastoreConfiguration.class);
    }
    public static DatastoreConfigurationUtil getInstance() {
        return DatastoreConfigurationUtil.Holder.INSTANCE;
    }
    public DatastoreConfigurationUtil(Class<TbDatastoreConfiguration> parameterClass) {
        super(parameterClass);
    }
}
