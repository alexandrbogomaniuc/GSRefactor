package com.dgphoenix.casino.common.util.logkit;

import org.apache.log4j.Logger;

public class GameLog implements IGameLogger.IContextProvider {
    private final static GameLog instance = new GameLog();

    private final InheritableThreadLocal<Long> accountIdContainer = new InheritableThreadLocal<Long>();
    private final InheritableThreadLocal<Boolean> accountGameMode = new InheritableThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    private IGameLogger delegate;

    public static GameLog getInstance() {
        return instance;
    }

    private GameLog() {
    }

    public void initialize(IGameLogger delegate) {
        this.delegate = delegate;
    }

    public void initUserLevelLog(long accountId) {
        accountIdContainer.set(accountId);
    }

    public void initUserGameModeLog(boolean isFreeMode) { accountGameMode.set(isFreeMode); }

    public void closeUserLevelLog() {
        accountIdContainer.set(null);
        accountGameMode.set(false);
    }

    @Override
    public String getContextInfo() {
        return accountIdContainer.get() != null ?
                " [ accountId: " + String.valueOf(accountIdContainer.get()) + "] " : "";
    }

    @Override
    public boolean isFreeMode() {
        return accountGameMode.get();
    }

    public Logger log() {
        return log(IGameLogger.DEFAULT_LOGGER);
    }

    public Logger log(String loggerName) {
        return log(loggerName, accountGameMode.get());
    }

    public Logger log(String loggerName, boolean isFreeMode) {
        return delegate.log(loggerName, isFreeMode);
    }

    public void debug(Logger logger, String message) {
        delegate.debug(logger, message, this);
    }

    public void debug(String message) {
        debug(IGameLogger.DEFAULT_LOGGER, message);
    }

    public void debug(String loggerName, String message) {
        delegate.debug(loggerName, message, this);
    }

    public void debugJackPot(String message) {
        debug(IGameLogger.JACKPOT_LOGGER, message);
    }

    public void info(String message) {
        info(IGameLogger.DEFAULT_LOGGER, message);
    }

    public void info(String logger, String message) {
        delegate.info(logger, message, this);
    }

    public void warn(Logger logger, String message) {
        delegate.warn(logger, message, this);
    }

    public void warn(String message) {
        warn(IGameLogger.DEFAULT_LOGGER, message);
    }

    public void warn(String logger, String message) {
        delegate.warn(logger, message, this);
    }

    public void error(Logger logger, String message) {
        delegate.error(logger, message, this);
    }

    public void error(Logger logger, String message, Throwable ex) {
        delegate.error(logger, message, this, ex);
    }

    public void error(String message) {
        error(IGameLogger.DEFAULT_ERROR_LOGGER, message);
    }

    public void error(String logger, String message) {
        delegate.error(logger, message, this);
    }

    public void error(String message, Throwable ex) {
        error((String) null, message, ex);
    }

    public void error(String logger, String message, Throwable ex) {
        delegate.error(logger, message, this, ex);
    }

    public void errorJackPot(String message, Throwable ex) {
        error(IGameLogger.JACKPOT_LOGGER, message, ex);
    }
    public void fatal(Logger logger, String message) {
        delegate.fatal(logger, message, this);
    }

    public void fatal(Logger logger, String message, Throwable ex) {
        delegate.fatal(logger, message, this, ex);
    }

    public void fatal(String message) {
        fatal((String) null, message);
    }

    public void fatal(String logger, String message) {
        delegate.fatal(logger, message, this);
    }

    public void fatal(String message, Throwable ex) {
        fatal((String) null, message, ex);
    }

    public void fatal(String logger, String message, Throwable ex) {
        delegate.fatal(logger, message, this, ex);
    }

}