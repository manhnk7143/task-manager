package com.dev.dbaas.cache;

public interface ICacheClient {

    void set(String key, int timeout, String data) throws Exception;
    String get(String key) throws Exception;
}
