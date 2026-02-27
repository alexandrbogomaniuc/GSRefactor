package com.abs.casino.common.client;

import com.dgphoenix.casino.gs.managers.payment.wallet.ILoggableCWClient;
import com.dgphoenix.casino.gs.managers.payment.wallet.ILoggableContainer;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 21.02.18
 */
public abstract class AbstractLoggableClient implements ILoggableCWClient {

    private static final Joiner.MapJoiner PARAMS_JOINER = Joiner.on(";").withKeyValueSeparator((" : "));

    protected ILoggableContainer loggableContainer;

    @Override
    public void logUrl(String url) {
        log(() -> loggableContainer.logUrl(url));
    }

    @Override
    public void logRequest(Map<String, String> params) {
        log(() -> loggableContainer.logRequest(" request parameters:" + PARAMS_JOINER.join(params)));
    }

    @Override
    public void logResponse(String response) {
        log(() -> loggableContainer.logResponse(response));
    }

    @Override
    public String getUrl() {
        return retrieve(loggableContainer::getUrl);
    }

    @Override
    public String getRequest() {
        return retrieve(loggableContainer::getRequest);
    }

    @Override
    public String getResponse() {
        return retrieve(loggableContainer::getResponse);
    }

    @Override
    public void setLoggableContainer(ILoggableContainer loggableContainer) {
        this.loggableContainer = loggableContainer;
    }

    protected void log(Runnable logAction) {
        if (loggableContainer != null) {
            logAction.run();
        }
    }

    private String retrieve(Supplier<String> valueExtractor) {
        return loggableContainer != null ? valueExtractor.get() : StringUtils.EMPTY;
    }
}
