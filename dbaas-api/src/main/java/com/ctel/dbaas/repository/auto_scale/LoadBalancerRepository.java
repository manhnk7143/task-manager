package com.ctel.dbaas.repository.auto_scale;

import com.ctel.dbaas.entity.auto_scale.LoadBalancerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoadBalancerRepository extends JpaRepository<LoadBalancerEntity, String> {

    List<LoadBalancerEntity> findAllByOrgIdAndRegionIdAndProjectIdAndDeletedAtIsNull(String orgId, String regionId, String projectId);

    LoadBalancerEntity findFirstByLoadBalancerIdAndOrgIdAndRegionIdAndProjectId(String LoadBalancerId, String orgId, String regionId, String projectId);

    int countAllByOrgIdAndRegionIdAndProjectIdAndDeletedAtIsNull(String orgId, String regionId, String projectId);

    List<LoadBalancerEntity> findAllByOriginLoadBalancerIdAndOrgIdAndRegionIdAndProjectId(String originLoadBalancerId, String orgId, String regionId, String projectId);

}
