/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.packet;

import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author hieutrinh
 */
public class ResponsePacket {
    
    public static String SERVER_INTERNAL_ERROR = "SERVER INTERNAL ERROR";
    
    public static String fetchSuccess(List items){
        
        JSONObject response = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("count", items.size());
        data.put("items", items);
        response.put("data", data);
        response.put("message", "FETCH SUCCESS");
        return response.toString();
    }
    
    public static String responseObject(JSONObject item){
        return item.toString();
    }
    
    public static String createSuccess(Object item){
        
        JSONObject response = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("item", item);
        response.put("data", data);
        response.put("message", "CREATE SUCCESS");
        return response.toString();
    }
    
    public static String updateSuccess(Object item){
        
        JSONObject response = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("item", item);
        response.put("data", data);
        response.put("message", "UPDATE SUCCESS");
        return response.toString();
    }
    
    public static String deleteSuccess(){
        
        JSONObject response = new JSONObject();
        response.put("message", "DELETE SUCCESS");
        return response.toString();
    }
}
