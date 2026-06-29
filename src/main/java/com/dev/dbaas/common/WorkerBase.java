package com.dev.dbaas.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public abstract class WorkerBase<T extends JobBase> implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("WorkerBase");
    private BlockingQueue<T> jobQueues = new LinkedBlockingQueue<T>();
    private T currentJob;
    private long processStartTime;
    private String nameThread;
    private Thread workerThread;
    private int batchJobSize = 1;
    private boolean isRunning = false;
    private WorkerType typeWorker;
    private int index;

    public WorkerBase() {

    }

    protected abstract void process(T job);

    protected abstract void process(List<T> jobs);

    @Override
    public void run() {
        // TODO Auto-generated method stub

        if (isRunning) {
            return;
        }

        isRunning = true;
        workerThread = Thread.currentThread();
        workerThread.setName(nameThread);

        while (isRunning) {

            try {
//                LOGGER.info(getNameWorker()+", Size : " + jobQueues.size());
                T job = jobQueues.take();
                currentJob = job;
                processStartTime = System.currentTimeMillis();

                if (batchJobSize == 1) {
                    process(job);
                } else if (batchJobSize > 1) {

                    List<T> jobs = new ArrayList<T>();
                    jobs.add(job);
                    for (int i = 0; i < batchJobSize; i++) {

                        T tempJob = jobQueues.poll();
                        if (tempJob == null) {
                            break;
                        }
                        jobs.add(tempJob);
                    }
                    process(jobs);
                }
                currentJob = null;
                processStartTime = 0;
            } catch (InterruptedException e) {
                LOGGER.error("error process job, " + e);
            }
        }

//        LOGGER.info(getNameWorker() + " has been stopped!");

    }

    public void receiveJob(T job) {
        if (jobQueues == null) {
            jobQueues = new LinkedBlockingQueue<>();
        }
//        LOGGER.info(nameThread+" size queue : "+jobQueues.size());
        jobQueues.add(job);
    }

    public void stopWorker() {
        setRunning(false);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public T getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(T currentJob) {
        this.currentJob = currentJob;
    }

    public long getProcessStartTime() {
        return processStartTime;
    }

    public void setProcessStartTime(long processStartTime) {
        this.processStartTime = processStartTime;
    }

    public String getNameWorker() {
        return nameThread;
    }

    public void setNameThread(String nameThread) {
        this.nameThread = nameThread;
    }

    public Thread getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(Thread workerThread) {
        this.workerThread = workerThread;
    }

    public int getBatchJobSize() {
        return batchJobSize;
    }

    public void setBatchJobSize(int batchJobSize) {
        this.batchJobSize = batchJobSize;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public WorkerType getTypeWorker() {
        return typeWorker;
    }

    public void setTypeWorker(WorkerType typeWorker) {
        this.typeWorker = typeWorker;
    }

    public void setJobQueues(BlockingQueue queue){
        jobQueues = queue;
    }

    public BlockingQueue getJobQueues(){
        return jobQueues;
    }
}
