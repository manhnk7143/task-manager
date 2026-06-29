/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.api.transaction;

import java.util.Stack;
import org.apache.log4j.Logger;

/**
 *
 * @author hieutrinh
 */
public class Runner {

    private static final Logger LOGGER = Logger.getLogger(Runner.class);
    private Stack<Task> STACK_OF_TASK = new Stack<>();
    public String errorMessage;

    public boolean executeTask(Task originalTask) throws Exception {

        if (originalTask == null) {
            throw new Exception("Cannot execute runner because original task is null");
        }
        LOGGER.info("Execute task : " + originalTask.getName());

        boolean isSuccess = originalTask.commit();
        if (isSuccess) {
            if (originalTask.getNextTask() != null) {
                STACK_OF_TASK.push(originalTask);
                return executeTask(originalTask.getNextTask());
            } else {
                STACK_OF_TASK = null;
            }
            return true;
        } else {
            errorMessage = originalTask.getErrorMessage();
            rollbackTask();
            return false;
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void rollbackTask() {

        while (!STACK_OF_TASK.isEmpty()) {

            Task task = STACK_OF_TASK.peek();
            boolean isSuccess = task.rollback();
            if (isSuccess) {
                STACK_OF_TASK.pop();
            } else {
                LOGGER.error("Cannot rollback task, log all to database");
            }
        }
    }

}
