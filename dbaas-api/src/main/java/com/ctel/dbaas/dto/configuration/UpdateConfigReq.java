package com.ctel.dbaas.dto.configuration;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Data
@Builder
public class UpdateConfigReq {

    private String groupConfigId;

    private Map<String, String> configurations;

    public void validate() {
        if (StringUtils.isBlank(groupConfigId)) {
            throw new AppException(new ErrorResponse("Name of group configuration cannot be blank"));
        }
    }

}
