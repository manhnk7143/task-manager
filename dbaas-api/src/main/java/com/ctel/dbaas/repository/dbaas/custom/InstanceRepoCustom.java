package com.ctel.dbaas.repository.dbaas.custom;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.repository.dbaas.projection.instance.InstanceDropdownRes;

import java.util.List;

public interface InstanceRepoCustom {

    List<InstanceDropdownRes> queryInstanceDropdown(String nameSearch, List<String> datastoreCodes, String status, RequestInfo reqCtx);

}
