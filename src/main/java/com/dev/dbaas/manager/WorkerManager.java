package com.dev.dbaas.manager;

import com.dev.dbaas.common.WorkerType;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.worker.*;
import com.dev.dbaas.worker.job.*;
import com.dev.dbaas.worker.AppWorker;
import com.dev.dbaas.worker.AuthAppWorker;
import com.dev.dbaas.worker.CallbackWorker;
import com.dev.dbaas.worker.ControlWorker;
import com.dev.dbaas.worker.job.AppJob;
import com.dev.dbaas.worker.job.CallbackJob;
import com.dev.dbaas.worker.job.ControlJob;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.apache.log4j.Logger;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class WorkerManager {

    private static final Logger LOGGER = Logger.getLogger(WorkerManager.class);

    private static final Timer TIMER = new HashedWheelTimer();

    public static final ExecutorService CALLBACK_WORKER_SERVICE = Executors.newCachedThreadPool();
    public static final ExecutorService AUTH_APP_WORKER_SERVICE = Executors.newCachedThreadPool();
    public static final ExecutorService APP_WORKER_SERVICE = Executors.newCachedThreadPool();
    public static final ExecutorService CONTROL_WORKER_SERVICE = Executors.newCachedThreadPool();

    private static final ConcurrentMap<Integer, AppWorker> MAP_APP_WORKERS = new ConcurrentHashMap<Integer, AppWorker>();
    private static final ConcurrentMap<Integer, AuthAppWorker> MAP_AUTH_APP_WORKERS = new ConcurrentHashMap<Integer, AuthAppWorker>();
    private static final ConcurrentMap<Integer, ControlWorker> MAP_CONTROL_WORKERS = new ConcurrentHashMap<Integer, ControlWorker>();
    private static final ConcurrentMap<Integer, CallbackWorker> MAP_CALLBACK_WORKER = new ConcurrentHashMap<Integer, CallbackWorker>();

    private static final SecureRandom RAND = new SecureRandom();

    public static void addCallbackWorker(int index, CallbackWorker worker) {

        if (worker != null) {

            if (worker.getTypeWorker() == WorkerType.CALLBACK_WORKER) {
                MAP_CALLBACK_WORKER.put(index, worker);
                CALLBACK_WORKER_SERVICE.execute(worker);
            } else {
                LOGGER.info("cannot find type worker to create");
            }
        }
    }

    public static void addAuthAppWorker(int index, AuthAppWorker worker) {

        if (worker != null) {

            if (worker.getTypeWorker() == WorkerType.AUTH_APP_WORKER) {
                MAP_AUTH_APP_WORKERS.put(index, worker);
                AUTH_APP_WORKER_SERVICE.execute(worker);
            } else {
                LOGGER.info("cannot find type worker to create");
            }
        }
    }

    public static void addAppWorker(int index, AppWorker worker) {

        if (worker != null) {

            if (worker.getTypeWorker() == WorkerType.APP_WORKER) {
                MAP_APP_WORKERS.put(index, worker);
                APP_WORKER_SERVICE.execute(worker);
            } else {
                LOGGER.info("cannot find type worker to create");
            }
        }
    }

    public static void addControlWorker(int index, ControlWorker worker) {

        if (worker != null) {

            if (worker.getTypeWorker() == WorkerType.CONTROL_WORKER) {
                MAP_CONTROL_WORKERS.put(index, worker);
                CONTROL_WORKER_SERVICE.execute(worker);
            } else {
                LOGGER.info("cannot find type worker to create");
            }
        }
    }

    public static void putCallbackJob(CallbackJob job) {

        LOGGER.info("Put callback job, " + job.toString());
        int indexWorker = new Random().nextInt(100) % Config.number_callback_worker;
        CallbackWorker callbackWorker = MAP_CALLBACK_WORKER.get(indexWorker);
        callbackWorker.receiveJob(job);
    }

    public static void putAuthAppJob(AppJob job) {

//        LOGGER.info("[4CMS] - AuthAppJob - " + job.getId());
        int indexWorker = RAND.nextInt(Config.number_auth_app_worker);
//        LOGGER.info("indexWorker " + indexWorker);
        job.setId(System.currentTimeMillis());

        AuthAppWorker worker = MAP_AUTH_APP_WORKERS.get(indexWorker);
        worker.receiveJob(job);
    }

    public static void putAppJob(AppJob job) {

//        LOGGER.info("[4CMS] - AppJob - " + job.getId());
        int indexWorker = Math.abs(job.getAccount().getAgentId().hashCode())%Config.number_app_worker;
//        LOGGER.info("indexWorker " + indexWorker);
        job.setId(System.currentTimeMillis());

        AppWorker worker = MAP_APP_WORKERS.get(indexWorker);
        worker.receiveJob(job);
    }

    public static void putControlJob(ControlJob job) {

//        LOGGER.info("[4CMS] - ControlJob - " + job.getId());
        int indexWorker = RAND.nextInt(Config.number_control_worker);
//        LOGGER.info("indexWorker " + indexWorker);
        job.setId(System.currentTimeMillis());

        ControlWorker worker = MAP_CONTROL_WORKERS.get(indexWorker);
        worker.receiveJob(job);
    }

    public static void init() {

//        LOGGER.info("start check workers");
        TIMER.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {

                List<CallbackWorker> pendingCallbackWorkers = new ArrayList<>();
                MAP_CALLBACK_WORKER.forEach((key, worker) -> {

                    long processTime = System.currentTimeMillis() - worker.getProcessStartTime();
                    if (processTime > 5000 && worker.getProcessStartTime() != 0) {
                        LOGGER.info("[CRITICAL] worker is pending " + worker.getNameWorker());
                        CallbackJob callbackJob = worker.getCurrentJob();
                        LOGGER.info("Data pending .. " + callbackJob.getCallbackUrl() + ", " + callbackJob.getData());
                        pendingCallbackWorkers.add(worker);
                    }
                });

                for (CallbackWorker worker : pendingCallbackWorkers) {
                    int index = worker.getIndex();
                    LOGGER.info("Stop callback worker " + index + ", job size: " + worker.getJobQueues().size());
                    worker.setRunning(false);
                    CallbackWorker newWorker = new CallbackWorker();
                    newWorker.setNameThread("CallBackWorker " + index);
                    newWorker.setTypeWorker(WorkerType.CALLBACK_WORKER);
                    newWorker.setBatchJobSize(1);
                    newWorker.setIndex(index);
                    newWorker.setJobQueues(worker.getJobQueues());
                    LOGGER.info("renew callback worker " + index + ", job size: " + newWorker.getJobQueues().size());
                    addCallbackWorker(index, newWorker);
                }
                init();
            }
        }, 10, TimeUnit.SECONDS);
    }

    public static void stop() {
        CALLBACK_WORKER_SERVICE.shutdownNow();
        AUTH_APP_WORKER_SERVICE.shutdownNow();
        APP_WORKER_SERVICE.shutdownNow();
        CONTROL_WORKER_SERVICE.shutdownNow();
    }
}
