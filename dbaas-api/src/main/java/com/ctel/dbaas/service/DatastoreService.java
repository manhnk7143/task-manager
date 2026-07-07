package com.ctel.dbaas.service;

import com.ctel.dbaas.common.enums.Status;
import com.ctel.dbaas.dto.datastore.DatastoreView;
import com.ctel.dbaas.entity.dbaas.DatastoreEntity;
import com.ctel.dbaas.entity.dbaas.DatastoreModeEntity;
import com.ctel.dbaas.entity.dbaas.DatastoreVersionEntity;
import com.ctel.dbaas.repository.dbaas.DatastoreModeRepository;
import com.ctel.dbaas.repository.dbaas.DatastoreRepository;
import com.ctel.dbaas.repository.dbaas.DatastoreVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatastoreService {

    @Autowired
    private DatastoreRepository datastoreRepository;

    @Autowired
    private DatastoreVersionRepository datastoreVersionRepository;

    @Autowired
    private DatastoreModeRepository datastoreModeRepository;

    public List<DatastoreView> datastoreInformation(List<String> datastoreCodes, String tag) {
        List<DatastoreView> result = new ArrayList<>();
        List<DatastoreEntity> datastoreEntities;
        if (datastoreCodes.isEmpty()) {
            datastoreEntities = datastoreRepository.findAllByStatus(Status.ACTIVE.getStatus());
        } else {
            datastoreEntities = datastoreRepository.findAllByCodeInAndStatus(datastoreCodes, Status.ACTIVE.getStatus());
        }

        if ("production".equals(tag)) {
            datastoreEntities = datastoreEntities.stream().filter(d -> d.getTag().equals("production")).toList();
        }

        for (DatastoreEntity datastore : datastoreEntities) {
            DatastoreView datastoreView = new DatastoreView();
            datastoreView.setId(datastore.getId());
            datastoreView.setName(datastore.getName());
            datastoreView.setCode(datastore.getCode());

            List<DatastoreView.VersionInfo> versionInfos = new ArrayList<>();
            List<DatastoreVersionEntity> lstDatastoreVersion = datastoreVersionRepository
                    .findAllByDatastoreIdAndStatus(datastore.getId(), Status.ACTIVE.getStatus());

            if ("production".equals(tag)) {
                lstDatastoreVersion = lstDatastoreVersion.stream().filter(d -> "production".equals(d.getTag())).toList();
            }

            for (DatastoreVersionEntity datastoreVersion : lstDatastoreVersion) {
                List<DatastoreView.ModeInfo> models = new ArrayList<>();
                List<DatastoreModeEntity> datastoreModes = datastoreModeRepository.findAllByDatastoreVersionIdAndStatus(
                        datastoreVersion.getId(), Status.ACTIVE.getStatus());

                if ("production".equals(tag)) {
                    datastoreModes = datastoreModes.stream().filter(d -> "production".equals(d.getTag())).toList();
                }

                for (DatastoreModeEntity datastoreMode : datastoreModes) {
                    models.add(DatastoreView.ModeInfo.builder()
                            .id(datastoreMode.getId())
                            .code(datastoreMode.getCode())
                            .name(datastoreMode.getName())
                            .build());
                }

                versionInfos.add(DatastoreView.VersionInfo.builder()
                        .id(datastoreVersion.getId())
                        .versionName(datastoreVersion.getVersion())
                        .models(models)
                        .build());

            }
            datastoreView.setVersionInfos(versionInfos);
            result.add(datastoreView);
        }

        return result;
    }

}
