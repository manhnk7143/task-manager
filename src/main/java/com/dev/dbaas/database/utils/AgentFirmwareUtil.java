/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.utils;

import com.dev.dbaas.database.dao.AgentFirmwareDao;
import com.dev.dbaas.database.enties.TbAgentFirmware;

import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class AgentFirmwareUtil extends AgentFirmwareDao {

    static class Holder {
        private static final AgentFirmwareUtil INSTANCE = new AgentFirmwareUtil(TbAgentFirmware.class);
    }
    public static AgentFirmwareUtil getInstance() {
        return AgentFirmwareUtil.Holder.INSTANCE;
    }

    public AgentFirmwareUtil(Class<TbAgentFirmware> parameterClass) {
        super(parameterClass);
    }

}
