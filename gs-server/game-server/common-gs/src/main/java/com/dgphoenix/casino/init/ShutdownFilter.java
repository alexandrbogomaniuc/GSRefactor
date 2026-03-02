package com.abs.casino.init;

import com.abs.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.GameServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: flsh
 * Date: 7/23/12
 */
public class ShutdownFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger(ShutdownFilter.class);
    private static final long SHUTDOWN_TIMEOUT = TimeUnit.SECONDS.toMillis(60);
    private static final String FILTER_APPLIED = "ShutdownFilter_APPLIED";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    static {
        StatisticsManager.getInstance().registerStatisticsGetter("ShutdownFilter", ()
                -> "initialized=" + initialized + ", activeThreadsCount=" + getActiveThreadsCount());
    }

    public static Integer getActiveThreadsCount() {
        Integer threads = null;
        Integer idleThreads = null;
        try {
            final ObjectName objectNameQuery = new ObjectName("*:type=queuedthreadpool,id=0");
            MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);
            Set<ObjectName> objectNames = server.queryNames(objectNameQuery, null);
            if (!objectNames.isEmpty()) {
                for (ObjectName name : objectNames) {
                    threads = (Integer) server.getAttribute(name, "threads");
                    idleThreads = (Integer) server.getAttribute(name, "idleThreads");
                    if (threads != null && idleThreads != null) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("getActiveThreadsCount: error", e);
        }
        if (threads == null || idleThreads == null) {
            LOG.warn("getActiveThreadsCount: load error, attribute not found: threads={}, idleThreads={}", threads, idleThreads);
        } else {
            return threads - idleThreads;
        }
        return null;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        initialized.set(true);
        GameServer.getInstance().registerShutdownFilter(this);
        LOG.info("init completed");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean isRequest = request.getAttribute(FILTER_APPLIED) == null;

        if (!initialized.get() && isRequest) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            LOG.error("Not initialized or shutdown in progress, return error. initialized={}", initialized.get());
            return;
        }
        if (isRequest) {
            request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        initialized.set(false);
        LOG.warn("destroy completed");
    }

    public void markDown() {
        initialized.set(false);
        long now = System.currentTimeMillis();
        while (now + SHUTDOWN_TIMEOUT > System.currentTimeMillis()) {
            Integer activeThreadsCount = getActiveThreadsCount();
            LOG.info("markDown: activeThreadsCount={}", activeThreadsCount);
            if (activeThreadsCount == null) {
                LOG.error("markDown: cannot load activeThreadsCount, shutdown immediate");
                break;
            }
            //8 is magic jetty number, always active threads
            if (activeThreadsCount - 8 > 0) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOG.error("Interrupted", e);
                    break;
                }
            } else {
                break;
            }
        }
        LOG.error("shutdown");
    }

    public boolean isMarkedDown() {
        return !initialized.get();
    }
}
