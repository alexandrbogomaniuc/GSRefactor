package com.dgphoenix.casino.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import java.io.IOException;

/**
 * Filter for logging unhandled exceptions appeared in view
 */
public class ViewErrorLogFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(ViewErrorLogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //nop
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Throwable t) {
            LOG.error("Unexpected error", t);
            throw t;
        }
    }

    @Override
    public void destroy() {
        //nop
    }
}
