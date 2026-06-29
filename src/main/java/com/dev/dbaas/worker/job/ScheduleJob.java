package com.dev.dbaas.worker.job;


import com.dev.dbaas.common.JobBase;
import com.dev.dbaas.packet.ScheduleServiceType;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

public class ScheduleJob extends JobBase{

    private TimeUnit timeUnit;
    private int duration;
    private TimerTask task;
    private int accountId;
    private ScheduleServiceType serviceType;


    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public TimerTask getTask() {
        return task;
    }

    public void setTask(TimerTask task) {
        this.task = task;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

}
