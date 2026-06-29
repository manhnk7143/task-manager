package com.dev.dbaas.worker;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.common.WorkerBase;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.AppJob;
import com.dev.dbaas.worker.processor.app.RequireLoginProcessor;
import com.dev.dbaas.worker.processor.auth.AuthenticationProcessor;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AuthAppWorker extends WorkerBase<AppJob> {

    private static final Logger LOGGER = Logger.getLogger(AuthAppWorker.class);
    private static final ConcurrentMap<String, ProcessorBase> MAP_PROCESSORS = new ConcurrentHashMap<>();

    static {
        MAP_PROCESSORS.put(AppServiceType.AUTHENTICATION.getValue(), new AuthenticationProcessor());
        MAP_PROCESSORS.put(AppServiceType.REQUIRE_LOGIN.getValue(), new RequireLoginProcessor());
    }

    @Override
    protected void process(AppJob job) {

        LOGGER.info("Starting AuthAppJob  " + getNameWorker() + " ....");
        long currentTime = System.currentTimeMillis();
        long delay = currentTime - job.getEventTimestamp();
        ProcessorBase processor = MAP_PROCESSORS.get(job.getPacket().getServiceId());
        if (processor == null) {
            processor = MAP_PROCESSORS.get(AppServiceType.NOT_FOUND.getValue());
        }
        try {
            processor.process(job);
        } catch (Exception e) {
            LOGGER.error(e, e);
            ResponsePacket responsePacket = new ResponsePacket();
            responsePacket.setServiceId(AppServiceType.getAppServiceType(job.getPacket().getServiceId()));
            responsePacket.setResult(-100);
            responsePacket.setMessage("Something when wrong");
            responsePacket.setMessageId(job.getPacket().getMessageId());
            responsePacket.setTime(System.currentTimeMillis());
            Account.receivePacketWithChannelDetail(job.getClient(), responsePacket);
        }

//        long duration = System.currentTimeMillis() - currentTime;
//
//        if (duration > 100 || delay > 500) {
//            LOGGER.warn("[4CMS] " + job.getPacket().getServiceId() + " - " + job.getId() + " - Time processing AuthAppWorker: " + duration + ", delay : " + delay);
//        } else {
//            LOGGER.info("[4CMS] " + job.getPacket().getServiceId() + " - " + job.getId() + " - Time processing AuthAppWorker: " + duration + ", delay : " + delay);
//        }
    }

    @Override
    protected void process(List<AppJob> jobs) {

    }
}
