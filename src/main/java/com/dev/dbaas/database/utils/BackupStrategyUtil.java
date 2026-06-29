/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.BackupStrategyDao;
import com.dev.dbaas.database.enties.TbBackupStrategy;

/**
 *
 * @author hieutrinh
 */
public class BackupStrategyUtil extends BackupStrategyDao {

    static class Holder {
        private static final BackupStrategyUtil INSTANCE = new BackupStrategyUtil(TbBackupStrategy.class);
    }
    public static BackupStrategyUtil getInstance() {
        return BackupStrategyUtil.Holder.INSTANCE;
    }

    public BackupStrategyUtil(Class<TbBackupStrategy> parameterClass) {
        super(parameterClass);
    }
}
