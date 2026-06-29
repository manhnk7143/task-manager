/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.worker.job;

import com.dev.dbaas.common.JobBase;
import org.json.JSONObject;

/**
 *
 * @author hieutrinh
 */
public class CallbackJob extends JobBase{
    
    public static final int CALLBACK_JOB = 1;
    private int targetId;
    private JSONObject data;
    private String callbackUrl;
    
    public CallbackJob(){
        targetId = -1;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

   
}
