package com.abs.casino.filters.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Log4j2 filter. Can be used in log4j2.xml configuration.
 * Checks the name of exception being logged and its cause.
 */
@Plugin(name = ExceptionNameFilter.FILTER_NAME,
        category = Node.CATEGORY,
        elementType = Filter.ELEMENT_TYPE,
        printObject = true)
public class ExceptionNameFilter extends AbstractFilter {
    static final String FILTER_NAME = "ExceptionNameFilter";

    /** Exceptions list as it passed to the filter */
    private final String loggedExceptionsRaw;

    /** Set of target exceptions names */
    private final Set<String> loggedExceptions;

    private ExceptionNameFilter(
            String loggedExceptionsRaw, Result onMatch, Result onMismatch)
    {
        super(onMatch, onMismatch);
        this.loggedExceptionsRaw = loggedExceptionsRaw;
        loggedExceptions = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(loggedExceptionsRaw.split(","))));
    }

    @Override
    public Result filter(Logger logger, Level level,
                         Marker marker, Object msg,
                         Throwable t) {
        return doFilter(t);
    }

    @Override
    public Result filter(Logger logger, Level level,
                         Marker marker, Message msg,
                         Throwable t) {
        return doFilter(t);
    }

    @Override
    public Result filter(LogEvent event) {
        return doFilter(event.getThrown());
    }

    private Result doFilter(Throwable t) {
        while (t != null) {
            //checking in depth through the causes
            if (loggedExceptions.contains(t.getClass().getSimpleName())) {
                return onMatch;
            }
            t = t.getCause();
        }
        return onMismatch;
    }

    @Override
    public String toString() {
        return String.format("exceptions=%s", loggedExceptionsRaw);
    }

    /**
     * @param exceptions exceptions names separated by comma
     * @param match result on match
     * @param mismatch result on mismatch
     */
    @PluginFactory
    public static ExceptionNameFilter createFilter(
            @PluginAttribute("exceptions") String exceptions,
            @PluginAttribute("onMatch") Result match,
            @PluginAttribute("onMismatch") Result mismatch)
    {
        if (exceptions == null) {
            LOGGER.error("At least one exception name must be provided for "
                    + FILTER_NAME);
            return null;
        }

        return new ExceptionNameFilter(exceptions, match, mismatch);
    }
}
