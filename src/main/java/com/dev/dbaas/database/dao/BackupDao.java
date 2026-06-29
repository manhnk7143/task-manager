package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbBackup;

public abstract class BackupDao extends GenericDao<TbBackup>{

    public BackupDao(Class<TbBackup> parameterClass) {
        super(parameterClass);
    }
}
