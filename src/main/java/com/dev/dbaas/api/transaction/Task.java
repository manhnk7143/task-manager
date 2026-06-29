/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.api.transaction;

import com.dev.dbaas.api.transaction.command.CommitCommand;
import com.dev.dbaas.api.transaction.command.RollbackCommand;

/**
 *
 * @author hieutrinh
 */
public class Task <T>{

    private String name;
    private CommitCommand<T> commitCommand;
    private RollbackCommand rollbackCommand;
    private Task nextTask;

    private Task(String name, CommitCommand<T> commitCommand, RollbackCommand rollbackCommand) {
        this.commitCommand = commitCommand;
        this.rollbackCommand = rollbackCommand;
        this.name = name;
        this.nextTask = null;
    }

    public static class Builder<T>   {

        private String name;
        private CommitCommand<T>  commitCommand;
        private RollbackCommand rollbackCommand;

        public Builder setCommitCommand(CommitCommand<T>  commitCommand) {

            this.commitCommand = commitCommand;
            return this;
        }

        public Builder setRolbackCommand(RollbackCommand rollbackCommand) {

            this.rollbackCommand = rollbackCommand;
            return this;
        }

        public Builder setName(String name) {

            this.name = name;
            return this;
        }

        public Task build() throws Exception {

            if (commitCommand == null) {
                throw new Exception("CommitCommand is required");
            }
            return new Task(name, commitCommand, rollbackCommand);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Task getNextTask() {
        return nextTask;
    }

    public void setNextTask(Task nextTask) {
        this.nextTask = nextTask;
    }

    public boolean commit() {
        return commitCommand.executeCommit();
    }

    public boolean rollback() {
        
        if(rollbackCommand != null){
            return rollbackCommand.executeRollback();
        }
        return true;
    }
    
    public T getResult(){
        return commitCommand.getResult();
    }
    
    public String getErrorMessage(){
        return commitCommand.getErrorMessage();
    }

}
