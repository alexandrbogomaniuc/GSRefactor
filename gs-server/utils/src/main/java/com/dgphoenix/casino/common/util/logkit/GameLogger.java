package com.dgphoenix.casino.common.util.logkit;

import com.abs.casino.common.util.logkit.log4j2specific.DynamicAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: Grien
 * Date: 21.11.2014 15:47
 */
public class GameLogger implements IGameLogger {
    private final static ConcurrentMap<String, org.apache.log4j.Logger> loggers = new ConcurrentHashMap<>(256);

    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(GameLogger.class);
    private final static String logger_context_key = "gameName";

    private GameLoggerFactory realGameLoggerFactory = new GameLoggerFactory("GAME_REAL");
    private GameLoggerFactory freeGameLoggerFactory = new GameLoggerFactory("GAME_FREE");

    public GameLogger() {
    }

    private void createLogger(String loggerName, String gameName, GameLoggerFactory factory) {
        synchronized (factory) {
            if (LogManager.exists(loggerName)) {
                return;
            }
            factory.createLogger(loggerName, gameName);
        }
    }

    private String getLoggerName(String name, boolean free) {
        return free ? "FREE_" + name : name;
    }

    public org.apache.logging.log4j.Logger getLogger(String gameName, boolean freeMode) {
        String loggerName = getLoggerName(gameName, freeMode);
        if (LogManager.exists(loggerName)) {
            return LogManager.getLogger(loggerName);
        }
        createLogger(loggerName, gameName, freeMode ? freeGameLoggerFactory : realGameLoggerFactory);
        return LogManager.getLogger(loggerName);
    }

    private org.apache.log4j.Logger getOldLogger(String gameName, boolean freeMode) {
        String loggerName = getLoggerName(gameName, freeMode);
        org.apache.log4j.Logger logger = loggers.get(loggerName);
        if (logger == null) {
            org.apache.log4j.Logger loggerExist = loggers.putIfAbsent(getLogger(gameName, freeMode).getName(),
                    logger = org.apache.log4j.Logger.getLogger(loggerName));
            if (loggerExist != null) {
                logger = loggerExist;
            }
        }
        return logger;
    }

    @Override
    public org.apache.log4j.Logger log(String loggerName, boolean isFreeMode) {
        return getOldLogger(loggerName, isFreeMode);
    }

    @Override
    public void info(String logName, String message, IContextProvider provider) {
        Logger logger = getLogger(logName, provider.isFreeMode());
        if (logger.isInfoEnabled()) {
            logger.info(getMessageWithContext(message, provider));
        }
    }

    @Override
    public void warn(org.apache.log4j.Logger logger, String message, IContextProvider provider) {
        if (logger.isWarnEnabled()) {
            logger.warn(getMessageWithContext(message, provider));
        }
    }

    @Override
    public void warn(String logName, String message, IContextProvider provider) {
        Logger logger = getLogger(logName, provider.isFreeMode());
        if (logger.isWarnEnabled()) {
            logger.warn(getMessageWithContext(message, provider));
        }
    }

    @Override
    public void debug(org.apache.log4j.Logger logger, String message, IContextProvider provider) {
        if (logger.isDebugEnabled()) {
            logger.debug(getMessageWithContext(message, provider));
        }
    }

    @Override
    public void debug(String loggerName, String message, IContextProvider provider) {
        Logger logger = getLogger(loggerName, provider.isFreeMode());
        if (logger.isDebugEnabled()) {
            logger.debug(getMessageWithContext(message, provider));
        }
    }

    @Override
    public void error(org.apache.log4j.Logger logger, String message, IContextProvider provider) {
        String messageWithContext = getMessageWithContext(message, provider);
        logger.error(messageWithContext);
        getLogger(DEFAULT_LOGGER, provider.isFreeMode()).error(messageWithContext);
    }

    @Override
    public void error(String loggerName, String message, IContextProvider provider) {
        String messageWithContext = getMessageWithContext(message, provider);
        boolean freeMode = provider.isFreeMode();
        getLogger(DEFAULT_LOGGER, freeMode).error(messageWithContext);
        if (loggerName != null) {
            getLogger(loggerName, freeMode).error(messageWithContext);
        }
    }

    @Override
    public void error(org.apache.log4j.Logger logger, String message, IContextProvider provider, Throwable ex) {
        if (ex instanceof Error) {
            fatal(logger, message, provider, ex);
        } else {
            String messageWithContext = getMessageWithContext(message, provider);
            getLogger(DEFAULT_ERROR_LOGGER, provider.isFreeMode()).error(messageWithContext, ex);
            logger.error(messageWithContext, ex);
        }
    }

    @Override
    public void error(String loggerName, String message, IContextProvider provider, Throwable ex) {
        if (ex instanceof Error) {
            fatal(loggerName, message, provider, ex);
        } else {
            String messageWithContext = getMessageWithContext(message, provider);
            boolean freeMode = provider.isFreeMode();
            getLogger(DEFAULT_ERROR_LOGGER, freeMode).error(messageWithContext, ex);
            if (loggerName != null) {
                getLogger(loggerName, freeMode).error(messageWithContext, ex);
            }
        }
    }

    @Override
    public void fatal(org.apache.log4j.Logger logger, String message, IContextProvider provider) {
        String messageWithContext = getMessageWithContext(message, provider);
        getLogger(DEFAULT_ERROR_LOGGER, provider.isFreeMode()).fatal(messageWithContext);
        logger.fatal(messageWithContext);
    }

    @Override
    public void fatal(String loggerName, String message, IContextProvider provider) {
        String messageWithContext = getMessageWithContext(message, provider);
        boolean freeMode = provider.isFreeMode();
        getLogger(DEFAULT_ERROR_LOGGER, freeMode).fatal(messageWithContext);
        if (loggerName != null) {
            getLogger(loggerName, freeMode).fatal(messageWithContext);
        }
    }

    @Override
    public void fatal(org.apache.log4j.Logger logger, String message, IContextProvider provider, Throwable ex) {
        String messageWithContext = getMessageWithContext(message, provider);
        getLogger(DEFAULT_ERROR_LOGGER, provider.isFreeMode()).fatal(messageWithContext, ex);
        logger.fatal(messageWithContext, ex);
    }

    @Override
    public void fatal(String loggerName, String message, IContextProvider provider, Throwable ex) {
        String messageWithContext = getMessageWithContext(message, provider);
        boolean freeMode = provider.isFreeMode();
        getLogger(DEFAULT_ERROR_LOGGER, freeMode).fatal(messageWithContext, ex);
        if (loggerName != null) {
            getLogger(loggerName, freeMode).fatal(messageWithContext, ex);
        }
    }

    protected String getMessageWithContext(String message, IContextProvider provider) {
        return provider.getContextInfo() + message;
    }

    private class GameLoggerFactory {
        private class GameLoggerConfiguration {
            private Property[] properties;
            private List<AppenderRef> notDynamicAppenderRefs;
            private String includeLocation;
            private String additivity;
            private Level level;
            private Filter filter;
            private DynamicAppender dynamicAppender;
            private AppenderRef dynamicAppenderRef;
        }

        private String loggerName;
        private GameLoggerConfiguration configuration = null;

        private GameLoggerFactory(String loggerName) {
            this.loggerName = loggerName;
        }

        public void createLogger(String loggerName, String gameName) {
            lazyLoadConfiguration();
            GameLoggerConfiguration glc = this.configuration;
            if (glc == null) {
                LOG.error("Can't crate logger name= " + loggerName);
                return;
            }
            Configuration config = getLoggerContext().getConfiguration();
            Property[] gameProperties;
            if (glc.properties == null) {
                gameProperties = new Property[]{Property.createProperty(logger_context_key, gameName)};
            } else {
                int gamePropertiesLength = glc.properties.length;
                gameProperties = new Property[gamePropertiesLength + 1];
                System.arraycopy(glc.properties, 0, gameProperties, 0, gamePropertiesLength);
                gameProperties[gamePropertiesLength] = Property.createProperty(logger_context_key, gameName);
            }
            Appender appender = glc.dynamicAppender.getAppender(gameProperties);
            AppenderRef[] gameAppenderRefs = new AppenderRef[glc.notDynamicAppenderRefs.size() + 1];
            glc.notDynamicAppenderRefs.toArray(gameAppenderRefs);
            AppenderRef dar = glc.dynamicAppenderRef;
            gameAppenderRefs[gameAppenderRefs.length - 1] = AppenderRef.createAppenderRef(appender.getName(),
                    dar.getLevel(), dar.getFilter());
            LoggerConfig logger = LoggerConfig.createLogger(glc.additivity, glc.level, loggerName,
                    glc.includeLocation, gameAppenderRefs, gameProperties, config, glc.filter);
            Map<String, Appender> appenders = config.getAppenders();
            for (AppenderRef ref : gameAppenderRefs) {
                final Appender app = appenders.get(ref.getRef());
                if (app != null) {
                    logger.addAppender(app, ref.getLevel(), ref.getFilter());
                }
            }
            if (!logger.getAppenders().containsKey(appender.getName())) {
                logger.addAppender(appender, dar.getLevel(), dar.getFilter());
            }
            config.addLogger(loggerName, logger);
        }

        void lazyLoadConfiguration() {
            if (configuration == null) {
                synchronized (this) {
                    if (configuration == null) {
                        Configuration configuration = getLoggerContext().getConfiguration();
                        GameLoggerConfiguration glc = new GameLoggerConfiguration();
                        LoggerConfig gameLogger = configuration.getLoggerConfig(loggerName);
                        Map<String, Appender> appenders = gameLogger.getAppenders();
                        for (Appender appender : appenders.values()) {
                            if (DynamicAppender.class.isInstance(appender)) {
                                glc.dynamicAppender = (DynamicAppender) appender;
                                break;
                            }
                        }
                        if (glc.dynamicAppender == null) {
                            LOG.error("Not configured DynamicAppender for appender '" + loggerName + "'");
                            return;
                        }
                        List<AppenderRef> tmpAppenderRefs = gameLogger.getAppenderRefs();
                        glc.notDynamicAppenderRefs = new ArrayList<>(tmpAppenderRefs.size());
                        for (AppenderRef ref : tmpAppenderRefs) {
                            if (!ref.getRef().equals(glc.dynamicAppender.getName())) {
                                glc.notDynamicAppenderRefs.add(ref);
                            } else {
                                glc.dynamicAppenderRef = ref;
                            }
                        }
                        if (glc.dynamicAppenderRef == null) {
                            LOG.error("Not configured Dynamic AppenderRef 2 DynamicAppender with name '" +
                                    glc.dynamicAppender.getName() + "'");
                            return;
                        }
                        Map<Property, Boolean> baseProperties = gameLogger.getProperties();
                        if (baseProperties == null) {
                            glc.properties = null;
                        } else {
                            List<Property> properties = new ArrayList<Property>(baseProperties.size());
                            for (Property property : baseProperties.keySet()) {
                                if (!property.getName().equals(logger_context_key)) {
                                    properties.add(property);
                                }
                            }
                            if (properties.size() == 0) {
                                glc.properties = null;
                            } else {
                                glc.properties = properties.toArray(new Property[properties.size()]);
                            }
                        }
                        glc.additivity = String.valueOf(gameLogger.isAdditive());
                        glc.level = gameLogger.getLevel();
                        glc.includeLocation = String.valueOf(gameLogger.isIncludeLocation());
                        glc.filter = gameLogger.getFilter();
                        this.configuration = glc;
                    }
                }
            }
        }

        private LoggerContext getLoggerContext() {
            return ((org.apache.logging.log4j.core.Logger) LOG).getContext();
        }
    }
}
