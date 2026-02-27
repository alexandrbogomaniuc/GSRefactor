package com.abs.casino.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * User: Grien
 * Date: 10.10.2014 16:26
 */
public class RequestSchemeFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger(RequestSchemeFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //nop
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        //LOG.debug("doFilter: " + request.getRequestURI());
        chain.doFilter(new HttpServletRequestProxy(request), response);
    }

    @Override
    public void destroy() {
        //nop
    }
}
