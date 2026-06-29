package com.dev.dbaas.protocol;

import com.dev.dbaas.packet.AppServiceType;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ResponsePacket {

    private static final Logger LOGGER = Logger.getLogger(ResponsePacket.class);
    @Getter
    @Setter
    private AppServiceType serviceId;
    @Getter
    @Setter
    private int result;
    @Getter
    @Setter
    private String messageId;
    @Getter
    @Setter
    private String message;
    @Getter
    @Setter
    private String data;
    @Getter
    @Setter
    private long time;

    public ResponsePacket() {

    }

    public ResponsePacket(AppServiceType serviceId, String data, int result, String messageId, String message) {
        this.message = message;
        this.data = data;
        this.result = result;
        this.messageId = messageId;
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {

        JSONObject response = new JSONObject();
        try {
            response.put("serviceId", serviceId.getValue());
            response.put("result", result);
            response.put("messageId", messageId);
            response.put("message", message);
            response.put("data", data);
            response.put("time", time);
        } catch (JSONException e) {
            LOGGER.error(e, e);
        }
        return response.toString();
    }
}
