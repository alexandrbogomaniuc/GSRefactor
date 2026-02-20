package com.dgphoenix.casino.websocket;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.util.QuoteUtil;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Created by vladislav on 11/16/16.
 */
public class WebSocketServletImpl extends WebSocketServlet {
    private static final Logger LOG = LogManager.getLogger(WebSocketServletImpl.class);

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.register(WebSocketImpl.class);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Enumeration<String> requestHeaderNames = request.getHeaderNames();
        while (requestHeaderNames.hasMoreElements()) {
            String headerName = requestHeaderNames.nextElement();
            String headerValue = request.getHeader(headerName);
            LOG.debug("Request header: {}:{}", headerName, headerValue);
        }

        super.service(request, response);

        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);
            LOG.debug("Response header: {}:{}", headerName, headerValue);
        }
        LOG.debug("Response status: {}", response.getStatus());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                String headerValue = req.getHeader(header);
                LOG.debug("Header: {}:{}", header, headerValue);
            }
            Preconditions.checkArgument(isUpgrade(req), "Request is not upgrade");
        } catch (Throwable e) {
            LOG.error("doGet: error", e);
        }
    }

    private boolean isUpgrade(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            // not a "GET" request (not a websocket upgrade)
            LOG.debug("Not get");
            return false;
        }

        String connection = request.getHeader("connection");
        if (connection == null) {
            // no "Connection: upgrade" header present.
            LOG.debug("No connection");
            return false;
        }

        // Test for "Upgrade" token
        boolean foundUpgradeToken = false;
        Iterator<String> iter = QuoteUtil.splitAt(connection, ",");
        while (iter.hasNext()) {
            String token = iter.next();
            if ("upgrade".equalsIgnoreCase(token)) {
                foundUpgradeToken = true;
                break;
            }
        }

        if (!foundUpgradeToken) {
            LOG.debug("Not found upgrade");
            return false;
        }

        String upgrade = request.getHeader("Upgrade");
        if (upgrade == null) {
            // no "Upgrade: websocket" header present.
            LOG.debug("No found upgrade header");
            return false;
        }

        if (!"websocket".equalsIgnoreCase(upgrade)) {
            LOG.debug("Not a 'Upgrade: WebSocket' (was [Upgrade: " + upgrade + "])");
            return false;
        }

        if (!"HTTP/1.1".equals(request.getProtocol())) {
            LOG.debug("Not a 'HTTP/1.1' request (was [" + request.getProtocol() + "])");
            return false;
        }

        return true;
    }
}
