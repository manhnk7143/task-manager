package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.NetworkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetworkRepository extends JpaRepository<NetworkEntity, String> {

    NetworkEntity findFirstByComputeIdAndMode(String computeId, String mode);

}
