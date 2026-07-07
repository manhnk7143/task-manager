package com.ctel.dbaas.config.interceptor;

import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.config.EnvConfig;
import io.grpc.*;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Component
public class InterceptorGrpc implements ServerInterceptor {

    private static final Metadata.Key<String> LOCALE_KEY = Metadata.Key.of("lang", Metadata.ASCII_STRING_MARSHALLER);
    private static final List<String> LANGUAGE_SUPPORT = List.of("vi", "en");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> next) {
        try {
            String localeStr = metadata.get(LOCALE_KEY);
            if (localeStr == null || localeStr.isEmpty()) {
                localeStr = LANGUAGE_SUPPORT.get(0);
            }

            Function<String, String> getValue = key -> metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
            String apiKey = getValue.apply("apiKey");
            if (!StringUtils.isNotEmpty(apiKey)) {
                RequestInfo req = RequestInfo.builder()
                        .orgId(getValue.apply("orgId"))
                        .regionId(getValue.apply("regionId"))
                        .projectId(getValue.apply("projectId"))
                        .userId(getValue.apply("userId"))
                        .token(getValue.apply("token"))
                        .locale(new Locale(localeStr))
                        .build();
                req.validate();
                Context context = Context.current().withValue(GrpcCtx.REQUEST_INFO_KEY, req);
                return Contexts.interceptCall(context, call, metadata, next);
            }
        } catch (Exception e) {
            Status status = Status.UNAVAILABLE.withDescription(e.getMessage());
            call.close(status, metadata);
        }
        return next.startCall(call, metadata);
    }

}
