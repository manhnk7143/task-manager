/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.BackupDao;
import com.dev.dbaas.database.enties.TbBackup;

/**
 *
 * @author hieutrinh
 */
public class BackupUtil extends BackupDao {

    static class Holder {
        private static final BackupUtil INSTANCE = new BackupUtil(TbBackup.class);
    }
    public static BackupUtil getInstance() {
        return BackupUtil.Holder.INSTANCE;
    }
    public BackupUtil(Class<TbBackup> parameterClass) {
        super(parameterClass);
    }
}
