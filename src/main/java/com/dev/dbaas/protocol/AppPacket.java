package com.dev.dbaas.protocol;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class AppPacket {

    private final static Logger LOGGER = Logger.getLogger("AppPacket");

    @Getter @Setter
    private String serviceId;
    @Getter @Setter
    private String data;
    @Getter @Setter
    private String messageId;
    @Getter @Setter
    private long time;
    @Getter
    private JSONObject jsonData;

    public AppPacket(String serviceId, String messageId, String data, long time) {

        super();
        this.serviceId = serviceId;
        this.messageId = messageId;
        this.data = data;
        this.time = time;
    }

    public AppPacket() {

    }

    public AppPacket(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setJsonData(JSONObject jsonData) {
        this.jsonData = jsonData;
        this.data = jsonData.toString();
    }

    public void decodePacket() throws Exception {

        if (data == null) {
            data = "{}";
        }
        jsonData = new JSONObject(data);
    }

    public int optIntField(String field, int defaultValue) {
        return jsonData.optInt(field, defaultValue);
    }

    public long optLongField(String field, long defaultValue) {
        return jsonData.optLong(field, defaultValue);
    }

    public double optDoubleField(String field, double defaultValue) {
        return jsonData.optDouble(field, defaultValue);
    }

    public String optStringField(String field, String defaultValue) {
        return jsonData.optString(field, defaultValue);
    }

    public void show() {
        LOGGER.info("serviceId = " + serviceId + ", data = " + data + ", messageId = " + messageId);
    }
}
