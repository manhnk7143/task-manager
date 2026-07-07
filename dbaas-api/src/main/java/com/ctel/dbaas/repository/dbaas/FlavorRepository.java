package com.ctel.dbaas.repository.dbaas;

import com.ctel.dbaas.entity.dbaas.FlavorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlavorRepository extends JpaRepository<FlavorEntity, String> {

    Optional<FlavorEntity> findFirstByOsFlavorId(String osFlavorId);

}
