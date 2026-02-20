package com.dgphoenix.casino.filters;

import org.apache.logging.log4j.ThreadContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Propagates correlation ids across request attributes, response headers and log context.
 */
public class CorrelationContextFilter implements Filter {

    private static final String HDR_TRACE_ID = "X-Trace-Id";
    private static final String HDR_SESSION_ID = "X-Session-Id";
    private static final String HDR_BANK_ID = "X-Bank-Id";
    private static final String HDR_GAME_ID = "X-Game-Id";
    private static final String HDR_OPERATION_ID = "X-Operation-Id";
    private static final String HDR_CONFIG_VERSION = "X-Config-Version";

    private static final String ATTR_TRACE_ID = "traceId";
    private static final String ATTR_SESSION_ID = "sessionId";
    private static final String ATTR_BANK_ID = "bankId";
    private static final String ATTR_GAME_ID = "gameId";
    private static final String ATTR_OPERATION_ID = "operationId";
    private static final String ATTR_CONFIG_VERSION = "configVersion";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nop
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String traceId = coalesce(
                normalize(httpRequest.getHeader(HDR_TRACE_ID)),
                normalize(httpRequest.getParameter(ATTR_TRACE_ID)),
                normalize((String) httpRequest.getAttribute(ATTR_TRACE_ID))
        );
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        String sessionId = coalesce(
                normalize(httpRequest.getHeader(HDR_SESSION_ID)),
                normalize(httpRequest.getParameter(ATTR_SESSION_ID)),
                normalize(httpRequest.getParameter("SID"))
        );

        String bankId = coalesce(
                normalize(httpRequest.getHeader(HDR_BANK_ID)),
                normalize(httpRequest.getParameter(ATTR_BANK_ID))
        );

        String gameId = coalesce(
                normalize(httpRequest.getHeader(HDR_GAME_ID)),
                normalize(httpRequest.getParameter(ATTR_GAME_ID))
        );

        String operationId = coalesce(
                normalize(httpRequest.getHeader(HDR_OPERATION_ID)),
                normalize(httpRequest.getParameter(ATTR_OPERATION_ID)),
                normalize(httpRequest.getParameter("transactionId"))
        );

        String configVersion = coalesce(
                normalize(httpRequest.getHeader(HDR_CONFIG_VERSION)),
                normalize(httpRequest.getParameter(ATTR_CONFIG_VERSION))
        );

        httpRequest.setAttribute(ATTR_TRACE_ID, traceId);
        setAttrIfPresent(httpRequest, ATTR_SESSION_ID, sessionId);
        setAttrIfPresent(httpRequest, ATTR_BANK_ID, bankId);
        setAttrIfPresent(httpRequest, ATTR_GAME_ID, gameId);
        setAttrIfPresent(httpRequest, ATTR_OPERATION_ID, operationId);
        setAttrIfPresent(httpRequest, ATTR_CONFIG_VERSION, configVersion);

        httpResponse.setHeader(HDR_TRACE_ID, traceId);
        setHeaderIfPresent(httpResponse, HDR_SESSION_ID, sessionId);
        setHeaderIfPresent(httpResponse, HDR_OPERATION_ID, operationId);
        setHeaderIfPresent(httpResponse, HDR_CONFIG_VERSION, configVersion);

        String prevTraceId = ThreadContext.get(ATTR_TRACE_ID);
        String prevSessionId = ThreadContext.get(ATTR_SESSION_ID);
        String prevBankId = ThreadContext.get(ATTR_BANK_ID);
        String prevGameId = ThreadContext.get(ATTR_GAME_ID);
        String prevOperationId = ThreadContext.get(ATTR_OPERATION_ID);
        String prevConfigVersion = ThreadContext.get(ATTR_CONFIG_VERSION);

        ThreadContext.put(ATTR_TRACE_ID, traceId);
        putOrRemove(ATTR_SESSION_ID, sessionId);
        putOrRemove(ATTR_BANK_ID, bankId);
        putOrRemove(ATTR_GAME_ID, gameId);
        putOrRemove(ATTR_OPERATION_ID, operationId);
        putOrRemove(ATTR_CONFIG_VERSION, configVersion);

        try {
            chain.doFilter(request, response);
        } finally {
            restore(ATTR_TRACE_ID, prevTraceId);
            restore(ATTR_SESSION_ID, prevSessionId);
            restore(ATTR_BANK_ID, prevBankId);
            restore(ATTR_GAME_ID, prevGameId);
            restore(ATTR_OPERATION_ID, prevOperationId);
            restore(ATTR_CONFIG_VERSION, prevConfigVersion);
        }
    }

    @Override
    public void destroy() {
        // nop
    }

    private static void setAttrIfPresent(HttpServletRequest request, String name, String value) {
        if (value != null) {
            request.setAttribute(name, value);
        }
    }

    private static void setHeaderIfPresent(HttpServletResponse response, String header, String value) {
        if (value != null) {
            response.setHeader(header, value);
        }
    }

    private static void putOrRemove(String key, String value) {
        if (value == null) {
            ThreadContext.remove(key);
        } else {
            ThreadContext.put(key, value);
        }
    }

    private static void restore(String key, String value) {
        if (value == null) {
            ThreadContext.remove(key);
        } else {
            ThreadContext.put(key, value);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String coalesce(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
