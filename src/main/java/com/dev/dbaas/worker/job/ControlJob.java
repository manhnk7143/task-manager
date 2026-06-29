package com.dev.dbaas.worker.job;

import com.dev.dbaas.common.JobBase;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.json.JSONObject;


public class ControlJob extends JobBase{

    private final static Logger LOGGER = Logger.getLogger("ControlJob");

    @Getter
    @Setter
    private String data;
    @Getter
    @Setter
    private long time;
    @Getter
    @Setter
    private String serviceId;
    @Getter
    @Setter
    private JSONObject jsonData;


    public ControlJob() {
        super();
    }


    public void decodePacket() throws Exception {

        if (data == null) {
            data = "{}";
        }
        jsonData = new JSONObject(data);
    }

    public void show() {
        //LOGGER.warn("[show] account: " + account.getAgentId() + ", service: " + packet.getServiceId() + ", data: " + packet.getData());
    }
}
