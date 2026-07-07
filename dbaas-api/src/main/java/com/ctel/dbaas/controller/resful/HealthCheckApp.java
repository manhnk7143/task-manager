package com.ctel.dbaas.controller.resful;

import com.ctel.dbaas.dto.common.ResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health_check")
public class HealthCheckApp {

    @GetMapping
    public ResponseDto<?> healthCheck () {
        return new ResponseDto<>("ok");
    }

}
