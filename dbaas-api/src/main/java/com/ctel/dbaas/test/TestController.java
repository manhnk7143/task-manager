package com.ctel.dbaas.test;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.utils.S3Utils;
import com.ctel.dbaas.dto.backup.S3StorageConfigDto;
import com.ctel.dbaas.dto.common.ResponseDto;
import com.ctel.dbaas.repository.dbaas.BackupRepository;
import com.ctel.dbaas.repository.dbaas.BackupStrategyRepository;
import com.ctel.dbaas.repository.dbaas.InstanceRepository;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BackupRepository backupRepository;

    @Autowired
    private BackupStrategyRepository backupStrategyRepository;

    @SneakyThrows
    @GetMapping("/download-backup")
    public ResponseDto<?> test(@RequestParam("backupId") String backupId) {

//        RequestInfo req = RequestInfo.builder()
//                .orgId("v5xwnfab2hgt")
//                .regionId("hn-1")
//                .projectId("c3d7f4a51ad04d989defbb69a6ab1762")
//                .userId("v5xwnfab2hgt_devrnd")
//                .token("xxxxx")
//                .locale(new Locale("en"))
//                .build();
//        req.validate();
//        RequestCtx.setRequestInfo(req);
//
//        BackupEntity backup = backupRepository.findFirstByIdAndOrgId(backupId, RequestCtx.get().getOrgId())
//                .orElseThrow(() -> new AppException(new ErrorResponse("Backup not found")));
//
//        BackupStrategyEntity backupStrategy = backupStrategyRepository.findById(backup.getBackupStrategyId())
//                .orElseThrow(() -> new AppException(new ErrorResponse("Backup strategy not found")));
//
//        String configJsonDecode = CryptoUtils.decrypt(backupStrategy.getConfiguration(), System.getenv("KEY_DECRYPT_CONFIG_BACKUP"));
//        S3StorageConfigDto storageConfigDto = CommonUtils.convert(new JSONObject(configJsonDecode), S3StorageConfigDto.class);
//
//        String objectName = RequestCtx.get().getOrgId() + "/" + backup.getFileName();
//        String urlGet = this.genUrls3(storageConfigDto, objectName, HttpMethod.GET);
//
//        RequestCtx.clear();
//
//        return new ResponseDto<>(urlGet);
        return new ResponseDto<>();
    }

    @Autowired
    private InstanceRepository instanceRepository;

    @PostMapping("/test-dynamic-builder")
    public ResponseDto<?> testDynamicBuilder(@RequestBody DynamicBuilder req) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setOrgId(req.getOrgId());
        requestInfo.setProjectId(req.getProjectId());
        return new ResponseDto<>(instanceRepository
                .queryInstanceDropdown(req.getInstanceName(),req.getDatastores(), req.getStatus(), requestInfo));
    }

    @Data
    public static class DynamicBuilder {
        private String instanceName;
        private List<String> datastores;
        private String status;
        private String projectId;
        private String orgId;
    }

    private String genUrls3(S3StorageConfigDto storageConfigDto, String objectName, HttpMethod method) {
        AmazonS3 amazonS3 = S3Utils.buildAmazonS3(storageConfigDto);
        return S3Utils.genPresignedUrl(amazonS3, method, objectName);
    }

}
