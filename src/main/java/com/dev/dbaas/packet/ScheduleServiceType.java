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
public enum ScheduleServiceType {
    
    NONE(0);
    private int value;

    private ScheduleServiceType(int mValue){
        value = mValue;
    }
    
    public int getValue() {
        return value;
    }
}
