package com.ctel.dbaas.repository.dbaas.projection.instance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceDropdownRes {

//    String getId();
//
//    String getName();
//
//    String getDatastoreName();

    private String id;

    private String name;

    private String datastoreName;

}
