package com.ctel.dbaas.common.context;

import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Arrays;
import java.util.List;

public class I18nContext {

    public static final List<String> LANGUAGE_SUPPORT = Arrays.asList("vi", "en");

    public static String getMessage(String messageCode, Object[] args, String defaultMessage) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/message");
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource.getMessage(messageCode, args, defaultMessage, GrpcCtx.getReqCtx().getLocale());
    }

    public static String getMessage(String messageCode, Object[] args) {
        return getMessage(messageCode, args, null);
    }

    public static String getMessage(String messageCode) {
        return getMessage(messageCode, null, null);
    }

}
