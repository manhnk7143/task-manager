package com.ctel.dbaas.dto.datastore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatastoreView {

    private String id;

    private String name;

    private String code;

    private List<VersionInfo> versionInfos;

    @Builder
    @Data
    public static class VersionInfo {

        private String id;

        private String versionName;

        private List<ModeInfo> models;

    }

    @Builder
    @Data
    public static class ModeInfo {

        private String id;

        private String name;

        private String code;

    }

}
