/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.GroupConfigurationDao;
import com.dev.dbaas.database.enties.TbGroupConfiguration;

/**
 *
 * @author hieutrinh
 */
public class GroupConfigurationUtil extends GroupConfigurationDao {

    static class Holder {
        private static final GroupConfigurationUtil INSTANCE = new GroupConfigurationUtil(TbGroupConfiguration.class);
    }
    public static GroupConfigurationUtil getInstance() {
        return GroupConfigurationUtil.Holder.INSTANCE;
    }
    public GroupConfigurationUtil(Class<TbGroupConfiguration> parameterClass) {
        super(parameterClass);
    }
}
