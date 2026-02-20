package com.dgphoenix.casino.gs.web.messages;

import org.apache.struts.util.MessageResources;

import java.util.Locale;

public class GsMessageManager {
    public static final String APPLICATION_MESSAGE_BUNDLE = "com.dgphoenix.casino.gs.web.messages.ApplicationMessage";
    private static final GsMessageManager instance = new GsMessageManager();

    public static GsMessageManager getInstance() {
        return instance;
    }

    private GsMessageManager() {}

    private MessageResources getApplicationResourceBundle() {
        return MessageResources.getMessageResources(APPLICATION_MESSAGE_BUNDLE);
    }

    public String getApplicationMessage(Locale locale, String key, Object... args) {
        return getApplicationResourceBundle().getMessage(locale, key, args);
    }
}
