package com.ctel.dbaas.datastore.mongodb.model;

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
public class MongodbReplicaSet {

    private Set<String> zones;

    private Integer quantityOfSecondary;

    public void validate() {
        if (this.quantityOfSecondary == null || this.quantityOfSecondary < 1 || this.quantityOfSecondary > 5) {
            throw new AppException(new ErrorResponse("quantity of secondaries must be greater than equal 1 and less than equal 5"));
        }

        if (this.zones == null || this.zones.isEmpty()) {
            throw new AppException(new ErrorResponse("zones is required"));
        }
    }

}
