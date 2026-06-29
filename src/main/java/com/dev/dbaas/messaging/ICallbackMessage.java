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
public interface ICallbackMessage {
    public void onMessage(JSONObject json);
}
