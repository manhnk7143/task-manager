package com.dev.dbaas.worker;

import com.dev.dbaas.worker.job.ScheduleJob;
import com.dev.dbaas.common.WorkerBase;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.apache.log4j.Logger;
import java.util.List;

public class ScheduleWorker extends WorkerBase<ScheduleJob> {

    private static final Logger LOGGER = Logger.getLogger(ScheduleWorker.class);
    private static final Timer TIMER = new HashedWheelTimer();

    @Override
    protected void process(ScheduleJob job) {

        TIMER.newTimeout(job.getTask(), job.getDuration(), job.getTimeUnit());
    }

    @Override
    protected void process(List<ScheduleJob> jobs) {

    }
}
