package com.ctel.dbaas.repository.dbaas.projection;

import java.time.LocalDateTime;

public interface QueryConfiguration {

    String getId();

    String getDatastoreConfigId();

    String getParamName();

    String getParamValue();

    String getDefaultValue();

    String getValueRange();

    String getValueType();

    String getDescription();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

}
