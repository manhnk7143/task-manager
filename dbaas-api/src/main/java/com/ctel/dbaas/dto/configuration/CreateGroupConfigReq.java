package com.ctel.dbaas.dto.configuration;

import com.ctel.dbaas.common.context.I18nContext;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Data
@Builder
public class CreateGroupConfigReq {

    private String datastoreModeId;

    private String name;

    private String description;

    private Map<String, String> overridesConfig;

    public void validate() {
        if (StringUtils.isBlank(this.name) || this.name.length() > 255) {
            throw new AppException(new ErrorResponse(I18nContext.getMessage("configuration.name_length_exceeds",
                    new Object[]{255})));
        }

        if (this.overridesConfig == null || this.overridesConfig.isEmpty()) {
            throw new AppException(new ErrorResponse(I18nContext.getMessage("configuration.overrides_config_empty")));
        }
    }

}
