package com.dev.dbaas.common;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public abstract class ScheduleWorkerBase implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ScheduleWorkerBase.class);
    private int interval = 2;
    private Timer timer = new HashedWheelTimer();

    @Override
    public void run() {
        checkAndInit();
    }

    private void checkAndInit(){

        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                process();
                checkAndInit();
            }
        }, interval, TimeUnit.SECONDS);
    }
    protected abstract void process();

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
