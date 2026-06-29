/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.messaging;

/**
 * @author hieutrinh
 */
public enum EngineType {

    RABBIT_MQ(1);
    private Integer value;

    private EngineType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
