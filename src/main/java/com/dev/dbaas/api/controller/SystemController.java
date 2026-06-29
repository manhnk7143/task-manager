
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.api.controller;

import com.dev.dbaas.packet.ResponsePacket;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hieutrinh
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    private static final Logger LOGGER = Logger.getLogger(SystemController.class);
    private static final String MISSING_PARAMETERS = "MISSING PARAMETERS : ";

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public ResponseEntity version() {

        JSONObject data = new JSONObject();
        data.put("version", "v1.0.0");
        return ResponseEntity.ok(ResponsePacket.responseObject(data));
    }

}
