/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.packet;

/**
 *
 * @author hieutrinh
 */
public enum LogType {

    NONE(0), ADD_LOG_INBOUND(1), ADD_LOG_OUTBOUND(2), UPDATE_LOG_INBOUND(3);

    private int value;

    private LogType(int mValue){
        value = mValue;
    }
    
    public int getValue() {
        return value;
    }

    public static LogType parseValue(int value){

        LogType serviceType = LogType.NONE;
        for(LogType item : LogType.values()){
            if(item.getValue() == value){
                serviceType = item;
                break;
            }
        }
        return serviceType;
    }
}
