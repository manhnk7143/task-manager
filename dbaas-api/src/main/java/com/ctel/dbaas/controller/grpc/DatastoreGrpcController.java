package com.ctel.dbaas.controller.grpc;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.datastore.DatastoreView;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.service.DatastoreService;
import grpc.model.dbaas_service.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@GrpcService
@Log4j2
public class DatastoreGrpcController extends DatastoreServiceGrpc.DatastoreServiceImplBase {

    @Autowired
    private DatastoreService datastoreService;

    @Override
    public void getAllDatastoreVersion(GetAllDatastoreVersionRequest request, StreamObserver<GetAllDatastoreVersionResponse> responseObserver) {
        try {
            log.info("getAllDatastoreVersion params => [{}]", request);

            String tag = request.getTag();
            if (StringUtils.isBlank(tag)) {
                tag = "production";
            }

            if (!List.of("production", "all").contains(tag)) {
                throw new AppException(new ErrorResponse("tag invalid"));
            }

            List<String> datastoreCodes = DatastoreSupport.validateDatastoreCodes(request.getDatastoreCode());
            List<DatastoreView> lstDatastore = datastoreService.datastoreInformation(datastoreCodes, tag);

            List<DatastoreVersion> result = lstDatastore.stream().map(datastore -> {
                List<VersionInfo> versionInfos = datastore.getVersionInfos().stream().map(datastoreVersion -> {
                            List<ModeInfo> models = datastoreVersion.getModels().stream().map(mode -> ModeInfo.newBuilder()
                                    .setId(mode.getId())
                                    .setName(mode.getName())
                                    .setCode(mode.getCode())
                                    .build()).toList();

                            return VersionInfo.newBuilder()
                                    .setId(datastoreVersion.getId())
                                    .setVersionName(datastoreVersion.getVersionName())
                                    .addAllModeInfo(models)
                                    .build();
                        }

                ).toList();
                return DatastoreVersion.newBuilder()
                        .setId(datastore.getId())
                        .setName(datastore.getName())
                        .setCode(datastore.getCode())
                        .addAllVersionInfos(versionInfos)
                        .build();
            }).toList();

            responseObserver.onNext(GetAllDatastoreVersionResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ListDatastoreVersion.newBuilder()
                            .addAllDocs(result)
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetAllDatastoreVersionResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ListDatastoreVersion.newBuilder()
                            .addAllDocs(new ArrayList<>())
                            .build())
                    .build());
        } finally {
//            RequestCtx.clear();
            responseObserver.onCompleted();
        }
    }
}
