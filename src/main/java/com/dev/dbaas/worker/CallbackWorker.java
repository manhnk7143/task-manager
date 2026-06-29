/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.worker;

import com.dev.dbaas.worker.job.CallbackJob;
import com.dev.dbaas.worker.processor.callback.CallbackCallLogProcessor;
import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.common.WorkerBase;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;

/**
 *
 * @author hieutrinh
 */
public class CallbackWorker extends WorkerBase<CallbackJob> {

    private static final Logger LOGGER = Logger.getLogger(CallbackWorker.class);
    private static final ConcurrentMap<Integer, ProcessorBase> MAP_PROCESSORS = new ConcurrentHashMap();

    static{
        MAP_PROCESSORS.put(CallbackJob.CALLBACK_JOB, new CallbackCallLogProcessor());
    }
    
    @Override
    protected void process(CallbackJob job) {
        LOGGER.info("Process sipJob ");
    }

    @Override
    protected void process(List<CallbackJob> jobs) {

    }
}
