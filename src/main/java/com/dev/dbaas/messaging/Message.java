package com.dev.dbaas.messaging;

import org.json.JSONObject;

public class Message {

    private JSONObject data;
    private String transactionId;

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
