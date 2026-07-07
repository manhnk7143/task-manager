package com.ctel.dbaas.controller.resful;

import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.dto.agent_firmware.CreateAgentFirmwareReq;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.common.ResponseDto;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.service.AgentFirmwareService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Log4j2
@RestController
@RequestMapping("/agent-firmware")
public class AgentFirmwareController {

    @Autowired
    private AgentFirmwareService agentFirmwareService;

    @PostMapping
    public ResponseDto<?> addFirmware(@RequestBody CreateAgentFirmwareReq req) {
        log.info("addFirmware params => {}", req);
        req.validate();
        if (!Objects.equals(EnvConfig.APIKEY_ADD_AGENT_FIRMWARE, req.getApiKey())) {
            throw new AppException(new ErrorResponse("apiKey invalid", HttpStatus.UNAUTHORIZED));
        }
        agentFirmwareService.addAgentFirmware(req);
        return new ResponseDto<>("Add firmware success");
    }

}
