package com.ctel.dbaas.internal.report;

import com.ctel.dbaas.common.enums.InstanceStatus;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.compute.FlavorInfo;
import com.ctel.dbaas.entity.dbaas.ComputeEntity;
import com.ctel.dbaas.entity.dbaas.VolumeEntity;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.ComputeRepository;
import com.ctel.dbaas.repository.dbaas.InstanceRepository;
import com.ctel.dbaas.repository.dbaas.VolumeRepository;
import com.ctel.dbaas.repository.dbaas.projection.instance.InstanceInfoProjection;
import com.ctel.dbaas.service.CmcCloudService;
import com.ctel.dbaas.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/daily-report")
public class DailyReportController {

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private ComputeRepository computeRepository;

    private static final List<String> ORG_INTERNAL = List.of("v5xwnfab2hgt", "s3t4xa5r2gwn");
    @Autowired
    private CmcCloudService cmcCloudService;
    @Autowired
    private VolumeRepository volumeRepository;

    @PostMapping
    public Object getDailyReport(@RequestBody DailyReportRequest request) {
        LocalDateTime fromDate = DateUtils.toLocalDateTime("2000-01-01", false);
        LocalDateTime toDate = DateUtils.toLocalDateTime(request.getToDate(), true);

        if (fromDate == null || toDate == null) {
            throw new AppException(new ErrorResponse("date invalid fromDate[%s] - toDate[%s]", fromDate, toDate));
        }

        long countCustomerRedis = instanceRepository.countCustomersByDatastore("redis", ORG_INTERNAL, fromDate, toDate);
        long countCustomerMongodb = instanceRepository.countCustomersByDatastore("mongodb", ORG_INTERNAL, fromDate, toDate);

        List<InstanceInfoProjection> reportRedis = instanceRepository.getReportInstances("redis", ORG_INTERNAL, fromDate, toDate);
        List<InstanceInfoProjection> reportMongodb = instanceRepository.getReportInstances("mongodb", ORG_INTERNAL, fromDate, toDate);

        int redisCpu = 0;
        int redisRam = 0;
        int redisDisk = 0;
        int redisVolume = 0;
        for (InstanceInfoProjection redisRp : reportRedis) {
            if (InstanceStatus.RUNNING.getStatus().equals(redisRp.getStatus())) {
                List<ComputeEntity> listCompute = computeRepository.findAllByInstanceId(redisRp.getId());
                for (ComputeEntity computeEntity : listCompute) {
                    FlavorInfo flavorInfo = cmcCloudService.getFlavor(computeEntity.getFlavorId(), request.getTokenPortalV2(), redisRp.getRegionId());
                    if (flavorInfo != null) {
                        redisCpu = redisCpu + flavorInfo.getVCpus();
                        redisRam = redisRam + flavorInfo.getRam();
                        redisDisk = redisDisk + flavorInfo.getDisk();
                    }
                    VolumeEntity volumeEntity = volumeRepository.findFirstByComputeId(computeEntity.getId());
                    if (volumeEntity != null) {
                        redisVolume = redisVolume + volumeEntity.getSize();
                    }
                }
            }
        }

        int mongoCpu = 0;
        int mongoRam = 0;
        int mongoDisk = 0;
        int mongoVolume = 0;
        for (InstanceInfoProjection mongoRp : reportMongodb) {
            if (InstanceStatus.RUNNING.getStatus().equals(mongoRp.getStatus())) {
                List<ComputeEntity> listCompute = computeRepository.findAllByInstanceId(mongoRp.getId());
                for (ComputeEntity computeEntity : listCompute) {
                    FlavorInfo flavorInfo = cmcCloudService.getFlavor(computeEntity.getFlavorId(), request.getTokenPortalV2(), mongoRp.getRegionId());
                    if (flavorInfo.getFlavorId() != null) {
                        mongoCpu = mongoCpu + flavorInfo.getVCpus();
                        mongoRam = mongoRam + flavorInfo.getRam();
                        mongoDisk = mongoDisk + flavorInfo.getDisk();
                    }
                    VolumeEntity volumeEntity = volumeRepository.findFirstByComputeId(computeEntity.getId());
                    if (volumeEntity != null) {
                        mongoVolume = mongoVolume + volumeEntity.getSize();
                    }
                }
            }
        }

        log.info("Report {}", toDate);
        log.info("Redis => Số lượng KH: {} - instance được tạo: {} - CPU: {} - RAM: {} - DISK: {} - VOLUME: {}", countCustomerRedis, reportRedis.size(), redisCpu, redisRam, redisDisk, redisVolume);
        log.info("Mongodb => Số lượng KH: {} - instance được tạo: {} - CPU: {} - RAM: {} - DISK: {} - VOLUME: {}", countCustomerMongodb, reportMongodb.size(), mongoCpu, mongoRam, mongoDisk, mongoVolume);
        log.info("TỔNG SỐ KH: {} - TỔNG SỐ INSTANCE: {}", countCustomerRedis + countCustomerMongodb, reportRedis.size() + reportMongodb.size());

        return null;
    }

}
