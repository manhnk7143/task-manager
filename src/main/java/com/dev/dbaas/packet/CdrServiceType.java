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
public enum CdrServiceType {

    NONE(0), CREATE_CALL(1), HANGUP_CALL(2) ;

    private int value;

    private CdrServiceType(int mValue){
        value = mValue;
    }
    
    public int getValue() {
        return value;
    }
}
