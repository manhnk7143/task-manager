package com.ctel.dbaas.common.context;

import com.ctel.dbaas.common.RequestInfo;
import io.grpc.Context;

public class GrpcCtx {

    public static final Context.Key<RequestInfo> REQUEST_INFO_KEY = Context.key("requestInfo");

    public static RequestInfo getReqCtx() {
        return GrpcCtx.REQUEST_INFO_KEY.get();
    }

}
