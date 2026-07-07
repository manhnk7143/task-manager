package com.ctel.dbaas.repository.dbaas;
 
import com.ctel.dbaas.entity.dbaas.ComputeEntity;
import com.ctel.dbaas.repository.dbaas.projection.FlavorIdCompute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComputeRepository extends JpaRepository<ComputeEntity, String> {

    ComputeEntity findFirstByNovaInstanceId(String novaInstanceId);

    ComputeEntity findFirstByMonitorResourceIdAndProjectIdAndOrgIdAndRegionId(String resourceId, String projectId, String orgId, String regionId);

    ComputeEntity findFirstByInstanceIdAndRole(String instanceId, String role); // query master

    List<ComputeEntity> findAllByInstanceIdAndRole(String instanceId, String role);

    List<ComputeEntity> findAllByInstanceIdAndProjectIdAndOrgId(String instanceId, String projectId, String orgId);

    List<ComputeEntity> findAllByInstanceId(String instanceId);

    @Query(value = "SELECT compute.id AS computeId, compute.flavorId AS flavorId FROM ComputeEntity compute WHERE compute.instanceId = :instanceId")
    List<FlavorIdCompute> getFlavorIds(@Param("instanceId") String instanceId);

    int countByInstanceId(String instanceId);
}
