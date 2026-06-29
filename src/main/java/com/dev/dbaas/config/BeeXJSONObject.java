package com.dev.dbaas.config;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class BeeXJSONObject extends JSONObject {

    private static final Logger LOGGER = Logger.getLogger(BeeXJSONObject.class);

    public BeeXJSONObject(String json) {
        super(json);
    }

    @Override
    public JSONObject putOnce(String key, Object value) throws JSONException {
        Object storedValue;
        if (key != null && value != null) {
            if ((storedValue = this.opt(key)) != null ) {
                return this;
            }
            this.put(key, value);
        }
        return this;
    }
}