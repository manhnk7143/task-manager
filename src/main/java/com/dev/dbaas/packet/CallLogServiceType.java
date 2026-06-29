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
public enum CallLogServiceType {
    
    NONE(0), CHANNEL_CREATE(1), CHANNEL_BRIDGE(2), CHANNEL_OPEN_APP(3), CHANNEL_HANGUP(4), CHANNEL_SET_VAR(5), CHANNEL_DIAL_BEGIN(6),
    ADD_POSTMAN_LOG(7),
    UPDATE_POSTMAN_LOG(8);

    private int value;

    private CallLogServiceType(int mValue){
        value = mValue;
    }
    
    public int getValue() {
        return value;
    }

    public static CallLogServiceType parseValue(int value){

        CallLogServiceType serviceType = CallLogServiceType.NONE;
        for(CallLogServiceType item : CallLogServiceType.values()){
            if(item.getValue() == value){
                serviceType = item;
                break;
            }
        }
        return serviceType;
    }
}
