package com.ctel.dbaas.controller.message_queue.handler;

public interface HandlerBase<T> {
    void handle(T t);
}
