/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.api.transaction.command;

/**
 *
 * @author hieutrinh
 */
public abstract class RollbackCommand{

    public abstract boolean executeRollback();

}
