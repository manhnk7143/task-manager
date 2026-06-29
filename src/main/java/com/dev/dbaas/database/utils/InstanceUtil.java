/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.InstanceDao;
import com.dev.dbaas.database.enties.TbInstance;

import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class InstanceUtil extends InstanceDao {

    static class Holder {
        private static final InstanceUtil INSTANCE = new InstanceUtil(TbInstance.class);
    }
    public static InstanceUtil getInstance() {
        return InstanceUtil.Holder.INSTANCE;
    }
    public InstanceUtil(Class<TbInstance> parameterClass) {
        super(parameterClass);
    }

}
