package com.dgphoenix.casino.filters;

import com.abs.casino.common.util.web.HttpRequestContextHolder;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 20.01.2020
 */
public class HttpCallInfoFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpRequestContextHolder.getRequestContext().clear();
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
