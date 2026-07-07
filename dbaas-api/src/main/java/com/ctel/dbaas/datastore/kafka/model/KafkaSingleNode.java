package com.ctel.dbaas.datastore.kafka.model;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaSingleNode {

    private String zone;

    public void validate() {
        if (StringUtils.isBlank(this.zone)) {
            throw new AppException(new ErrorResponse("zone is required"));
        }
    }

}
