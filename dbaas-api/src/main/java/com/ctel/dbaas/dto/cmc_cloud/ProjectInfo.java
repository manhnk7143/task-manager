package com.ctel.dbaas.dto.cmc_cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectInfo {

    private String id;

    private String name;

    @JsonProperty("domain_id")
    private String domainId;

    private String description;

    private boolean enabled;

    @JsonProperty("parent_id")
    private String parentId;

    @JsonProperty("is_domain")
    private boolean isDomain;

}
