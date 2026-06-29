package com.dev.dbaas.messaging;


import org.apache.log4j.Logger;

import java.util.concurrent.Flow;

public abstract class MessagingSubscriber implements Flow.Subscriber<Message> {

    private static final Logger LOGGER = Logger.getLogger(MessagingSubscriber.class);

    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

        LOGGER.info("onSubscribe");
        this.subscription = subscription;
        this.subscription.request(1);
        LOGGER.info("onSubscribe request +1");
    }

    @Override
    public void onNext(Message message) {
        //LOGGER.info("onNext");
        try {
            onProcess(message);
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.info("onError");
        LOGGER.error(throwable, throwable);
    }

    @Override
    public void onComplete() {
        LOGGER.info("onComplete");
    }

    public abstract void onProcess(Message message);

    public void cancel() {
        this.subscription.cancel();
    }
}
