package com.dev.dbaas.database.dao;

import com.dev.dbaas.database.enties.TbBackupStrategy;

public abstract class BackupStrategyDao extends GenericDao<TbBackupStrategy>{

    public BackupStrategyDao(Class<TbBackupStrategy> parameterClass) {
        super(parameterClass);
    }
}
