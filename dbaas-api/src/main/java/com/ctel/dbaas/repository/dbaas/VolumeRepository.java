package com.ctel.dbaas.repository.dbaas;
 
import com.ctel.dbaas.entity.dbaas.VolumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolumeRepository extends JpaRepository<VolumeEntity, String> {

    VolumeEntity findFirstByComputeId(String computeId);

    VolumeEntity findFirstByCinderVolumeId(String cinderVolumeId);

}
