package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.SocketInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SocketInfoRepository extends JpaRepository<SocketInfoEntity, String> {

    SocketInfoEntity findFirstBySocketClientId(String socketClientId);

    List<SocketInfoEntity> findAllByUserIdAndOrgIdAndRegionId(String userId, String orgId, String regionId);

    @Transactional
    void deleteBySocketClientId(String socketClientId);

}
