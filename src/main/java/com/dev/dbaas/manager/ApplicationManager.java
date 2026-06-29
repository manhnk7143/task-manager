package com.dev.dbaas.manager;

import com.dev.dbaas.common.WorkerType;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.listener.AppSocketIOListenerTls;
import com.dev.dbaas.worker.*;
import com.dev.dbaas.worker.AppWorker;
import com.dev.dbaas.worker.AuthAppWorker;
import com.dev.dbaas.worker.CallbackWorker;
import com.dev.dbaas.worker.ControlWorker;
import org.apache.log4j.Logger;
import java.io.IOException;

public class ApplicationManager {

    private static final Logger LOGGER = Logger.getLogger(ApplicationManager.class);
    private static AppSocketIOListenerTls appSocketIoListenerTls;
    private static Thread appSocketIoThreadTls;
    //public static String ipAddress;

    public static void start() throws IOException, InterruptedException {

        /*
        try{
            ipAddress = NetworkUtil.getCurrentPublicIp("https://core2.beex.vn:8089/checkIp");
        }
        catch (Exception e){
            LOGGER.error(e, e);
            try {
                ipAddress = NetworkUtil.getCurrentPublicIp("http://checkip.amazonaws.com/");
            } catch (Exception ex) {
                LOGGER.error(ex, ex);
            }
        }

        if(ipAddress == null || ipAddress.isEmpty()){
            LOGGER.error("Cannot define ipAddress ....");
            throw new IOException();
        }

         */

        // start all callback worker 
        for(int i = 0; i < Config.number_callback_worker; i++){
            CallbackWorker worker = new CallbackWorker();
            worker.setNameThread("CallbackWorker "+i);
            worker.setTypeWorker(WorkerType.CALLBACK_WORKER);
            worker.setBatchJobSize(1);
            worker.setIndex(i);
            WorkerManager.addCallbackWorker(i, worker);
            LOGGER.info("Start CallbackWorker "+i);
        }

        // start auth app worker
        for(int i = 0; i < Config.number_app_worker; i++){
            AuthAppWorker worker = new AuthAppWorker();
            worker.setNameThread("AuthAppWorker "+i);
            worker.setTypeWorker(WorkerType.AUTH_APP_WORKER);
            worker.setBatchJobSize(1);
            worker.setIndex(i);
            WorkerManager.addAuthAppWorker(i, worker);
            LOGGER.info("Start AuthAppWorker "+i);
        }

        // start app worker
        for(int i = 0; i < Config.number_app_worker; i++){
            AppWorker worker = new AppWorker();
            worker.setNameThread("AppWorker "+i);
            worker.setTypeWorker(WorkerType.APP_WORKER);
            worker.setBatchJobSize(1);
            worker.setIndex(i);
            WorkerManager.addAppWorker(i, worker);
            LOGGER.info("Start AppWorker "+i);
        }

        // start control worker
        for(int i = 0; i < Config.number_control_worker; i++){
            ControlWorker worker = new ControlWorker();
            worker.setNameThread("ControlWorker "+i);
            worker.setTypeWorker(WorkerType.CONTROL_WORKER);
            worker.setBatchJobSize(1);
            worker.setIndex(i);
            WorkerManager.addControlWorker(i, worker);
            LOGGER.info("Start ControlWorker "+i);
        }

        appSocketIoListenerTls = new AppSocketIOListenerTls(Config.app_listener_port);
        appSocketIoListenerTls.setCerFile(Config.file_certificate);
        appSocketIoListenerTls.setCerPass(Config.pass_certificate);
        appSocketIoThreadTls = new Thread(appSocketIoListenerTls);
        appSocketIoThreadTls.start();

        ClusterManager.listenTopic(Config.rabbit_connection, Config.rabbit_port, Config.default_exchange_config, Config.default_queue_config, Config.rabbit_username, Config.rabbit_password);
        WorkerManager.init();
    }

    public static void stop(){
        appSocketIoListenerTls.stop();
        appSocketIoThreadTls.interrupt();
        WorkerManager.stop();
    }
}
