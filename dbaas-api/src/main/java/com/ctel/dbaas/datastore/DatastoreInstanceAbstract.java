package com.ctel.dbaas.datastore;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.dto.instance.InstanceCreateReq;
import com.ctel.dbaas.dto.instance.InstanceDetail;
import com.ctel.dbaas.dto.instance.InstanceInfo;

public interface DatastoreInstanceAbstract {

    void createInstance(InstanceCreateReq req, RequestInfo requestCtx);

    InstanceDetail getInstanceDetail(InstanceInfo instanceInfo, RequestInfo requestCtx);

}
