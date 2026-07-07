package com.ctel.dbaas.datastore.kafka.model;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaCluster {

    private Integer brokersPerZone;

    private Set<String> zones;

    public void validate() {

        if (this.brokersPerZone == null || this.brokersPerZone < 1 || this.brokersPerZone > 5) {
            throw new AppException(new ErrorResponse("brokersPerZone must be between 1 and 5"));
        }

        if (this.zones == null || this.zones.size() < 2) {
            throw new AppException(new ErrorResponse("zones must contain at least 2 elements"));
        }
    }
}
