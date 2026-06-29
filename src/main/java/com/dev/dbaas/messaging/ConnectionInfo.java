package com.dev.dbaas.messaging;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class ConnectionInfo {

    @Getter
    @Setter
    private String exchange;
    @Getter
    @Setter
    private String queue;

    public String toString(){
        return exchange+queue;
    }
}
