/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.messaging;

import org.json.JSONObject;

/**
 * @author hieutrinh
 */
public abstract class IMessageConnection {

    private boolean isConnected;
    private String tag;

    public abstract boolean registerQueueExchange(String queue, String exchange);

    public abstract boolean emmitQueueDurable(String exchange, String queue, JSONObject data);

    public abstract void addMessagingSubscriber(MessagingSubscriber subscriber);

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public abstract void disconnect();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
