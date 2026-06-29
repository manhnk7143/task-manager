package com.dev.dbaas.api.controller;

import com.dev.dbaas.packet.ResponsePacket;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
public class PingController {

    private static final Logger LOGGER = Logger.getLogger(PingController.class);

    private static final int WARNING_ASTERISK = -2;
    private static final int NOT_FOUND_ASTERISK = -1;
    private static final int SERVER_INTERNAL_ERROR = -100;
    private static final int SUCCESS = 1;


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity logInbounds() {

        ResponseEntity response = null;
        JSONObject data = new JSONObject();
        try {
            boolean isPass = true;
            int errorCode = SERVER_INTERNAL_ERROR;
            data.put("result", errorCode);
            response = ResponseEntity.status(HttpStatus.OK).body(ResponsePacket.responseObject(data));
        } catch (Exception e) {
            LOGGER.error(e, e);
            data.put("message", "Server external error");
            data.put("result", SERVER_INTERNAL_ERROR);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponsePacket.responseObject(data));
        }
        return response;
    }
}
