package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_datastore_version",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"datastore_id", "version"})})
@Getter
@Setter
public class DatastoreVersionEntity extends BaseEntity {

    @Column(name = "repo_information", length = 300, nullable = false)
    private String repoInformation = "{}";

    @Column(name = "provider", length = 50, nullable = false)
    private String provider = "CMC";

    @Column(name = "platform_supports", length = 100, nullable = false)
    private String platformSupports = "docker";

    @Column(name = "version", length = 50, nullable = false)
    private String version;

    @Column(name = "glance_image_tags", nullable = false)
    private String glanceImageTags;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "tag", length = 50)
    private String tag;

    @Column(name = "datastore_id", length = 36, nullable = false)
    private String datastoreId;

}
