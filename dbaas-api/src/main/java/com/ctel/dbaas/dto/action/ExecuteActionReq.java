package com.ctel.dbaas.dto.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteActionReq {

    private String methodName;

    private Map<String, Object> dataReq = new HashMap<>();

}
