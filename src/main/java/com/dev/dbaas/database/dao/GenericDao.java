/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.database.dao;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;

/**
 *
 * @author hieutrinh
 * @param <T>
 */
public class GenericDao<T extends Serializable> {

    private static final Logger LOGGER = Logger.getLogger(GenericDao.class);

    final Class<T> parameterClass;

    public GenericDao(Class<T> parameterClass) {
        this.parameterClass = parameterClass;
    }

    public T findOne(Session session, String id) {
        T record = session.get(parameterClass, id);
        return record;
    }

    public Integer create(Session session, final T entity) {
        Transaction transaction = session.getTransaction();
        if(!transaction.isActive()){
            transaction = session.beginTransaction();
        }
        Integer id = 0;
        try{
            id = (Integer) session.save(entity);
            transaction.commit();
        }
        catch (Exception e){
            LOGGER.error(e, e);
            transaction.rollback();
        }
        session.clear();
        return id;
    }

    public Long createAsLong(Session session, final T entity) {
        Transaction transaction = session.getTransaction();
        if(!transaction.isActive()){
            transaction = session.beginTransaction();
        }
        Long id = 0l;
        try{
            id = (Long) session.save(entity);
            transaction.commit();
        }
        catch (Exception e){
            LOGGER.error(e, e);
            transaction.rollback();
        }
        session.clear();
        return id;
    }

    public String createAsString(Session session, final T entity) {
        Transaction transaction = session.getTransaction();
        if(!transaction.isActive()){
            transaction = session.beginTransaction();
        }
        String id = "";
        try{
            session.save(entity);
            transaction.commit();
        }
        catch (Exception e){
            LOGGER.error(e, e);
            transaction.rollback();
        }
        session.clear();
        return id;
    }

    public T update(Session session, final T entity) {
        Transaction transaction = session.getTransaction();
        if(!transaction.isActive()){
            transaction = session.beginTransaction();
        }
        try{
            session.update(entity);
            transaction.commit();
        }
        catch (Exception e){
            LOGGER.error(e, e);
            transaction.rollback();
        }
        session.clear();
        return entity;
    }

    public void delete(Session session, final T entity) {
        Transaction transaction = session.getTransaction();
        if(!transaction.isActive()){
            transaction = session.beginTransaction();
        }
        try{
            session.delete(entity);
            transaction.commit();
        }
        catch (Exception e){
            LOGGER.error(e, e);
            transaction.rollback();
        }
        session.clear();
    }
}
