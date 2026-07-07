package com.ctel.dbaas.dto.configuration;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
public class UpdateGroupConfigReq {

    private String groupConfigId;

    private String name;

    private String description;

    public void validate() {
        if (StringUtils.isBlank(this.name) || this.name.length() > 255) {
            throw new AppException(new ErrorResponse("Name cannot be blank and length cannot be greater than 255"));
        }

        if (StringUtils.isNotBlank(this.description) && this.description.length() > 255) {
            throw new AppException(new ErrorResponse("Description cannot be blank and length cannot be greater than 255"));
        }
    }

}
