package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<ResourceEntity, String> {

    ResourceEntity findFirstByOrgIdAndRegionIdAndProjectId(String orgId, String regionId, String projectId);

}
