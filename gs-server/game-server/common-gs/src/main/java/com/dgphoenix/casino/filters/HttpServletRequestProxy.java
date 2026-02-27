package com.abs.casino.filters;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Grien
 * Date: 10.10.2014 16:32
 */
public class HttpServletRequestProxy implements HttpServletRequest {
    private static final Logger LOG = LogManager.getLogger(HttpServletRequestProxy.class);

    private static final String PROXY_SCHEME = "proxy_scheme";
    private static final String VERTICAL_DELIMITER = "|";
    private static final String SPACE_DELIMITER = " ";
    private static final List<String> MONEY_PARAMS = Arrays.asList(IGameController.PARAMBET, "MATCHBET", "SWEETBET", "BONUSBET", "BETS");
    private static final AtomicBoolean BAD_HOST_HEADER_WARNED = new AtomicBoolean(false);

    private String scheme;
    private String serverName;
    private String remoteIp;
    private HttpServletRequest request;
    private boolean needConvertMoneyValues;
    private final Map<String, String> replacedParameters;
    private final DecimalFormat decimalFormatter;

    public HttpServletRequestProxy(HttpServletRequest request) {
        this.request = request;
        scheme = request.getHeader(PROXY_SCHEME);
        remoteIp = getRemoteIp(request);
        replacedParameters = new HashMap<>();
        decimalFormatter = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        decimalFormatter.setMaximumFractionDigits(2);
    }

    private String getRemoteIp(HttpServletRequest request) {
        String forwardedHeader = request.getHeader("X-Forwarded-For");

        if (StringUtils.isTrimmedEmpty(forwardedHeader)) {
            return request.getRemoteAddr();
        }

        int pos = forwardedHeader.lastIndexOf(", ");
        if (pos != -1) {
            return forwardedHeader.substring(pos + 2);
        }

        return forwardedHeader;
    }

    @Override
    public String getAuthType() {
        return request.getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        return request.getCookies();
    }

    @Override
    public long getDateHeader(String s) {
        return request.getDateHeader(s);
    }

    @Override
    public String getHeader(String s) {
        String header = request.getHeader(s);
        //nginx/jetty bad host hack
        if ("Host".equalsIgnoreCase(s) && header != null && header.startsWith("local")) {
            String host = request.getHeader("X-Forwarded-Host");
            if (host == null) {
                host = request.getHeader("X-Forwarded-Server");
            }
            if (host == null) {
                if (BAD_HOST_HEADER_WARNED.compareAndSet(false, true)) {
                    LOG.warn("Bad header found, Host is: " + header +
                            ", but X-Forwarded-Host or X-Forwarded-Server undefined (further identical warnings suppressed)");
                } else {
                    LOG.debug("Host fallback without X-Forwarded-* headers, Host=" + header);
                }
                host = header;
            }
            return host;
        }
        return header;
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return request.getHeaders(s);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return request.getHeaderNames();
    }

    @Override
    public int getIntHeader(String s) {
        return request.getIntHeader(s);
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getPathInfo() {
        return request.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    @Override
    public String getContextPath() {
        return request.getContextPath();
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String s) {
        return request.isUserInRole(s);
    }

    @Override
    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return request.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return request.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return request.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean b) {
        return request.getSession(b);
    }

    @Override
    public HttpSession getSession() {
        return request.getSession();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return request.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return request.authenticate(httpServletResponse);
    }

    @Override
    public void login(String s, String s2) throws ServletException {
        request.login(s, s2);
    }

    @Override
    public void logout() throws ServletException {
        request.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return request.getParts();
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return request.getPart(s);
    }

    @Override
    public Object getAttribute(String s) {
        return request.getAttribute(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return request.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        request.setCharacterEncoding(s);
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public String getParameter(String name) {
        if (needConvertMoneyValues && MONEY_PARAMS.contains(name)) {
            return replacedParameters.get(name);
        } else {
            return request.getParameter(name);
        }
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return request.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String s) {
        return request.getParameterValues(s);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return request.getParameterMap();
    }

    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    @Override
    public String getScheme() {
        return StringUtils.isTrimmedEmpty(scheme) ? request.getScheme() : scheme;
    }

    @Override
    public String getServerName() {
        //LOG.debug("wrapped request: " + request);
        if (serverName != null) {
            return serverName;
        }
        serverName = request.getServerName();
        if (serverName.startsWith("local")) {
            serverName = getHeader("Host");
        }
        return serverName;
    }

    @Override
    public int getServerPort() {
        return request.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    @Override
    public String getRemoteAddr() {
        return StringUtils.isTrimmedEmpty(remoteIp) ? request.getRemoteAddr() : remoteIp;
    }

    @Override
    public String getRemoteHost() {
        return StringUtils.isTrimmedEmpty(remoteIp) ? request.getRemoteHost() : remoteIp;
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (name.equals("dbLink")) {
            //code removed
        } else {
            request.setAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String s) {
        request.removeAttribute(s);
    }

    @Override
    public Locale getLocale() {
        return request.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return request.getLocales();
    }

    @Override
    public boolean isSecure() {
        return "https".equalsIgnoreCase(scheme);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return request.getRequestDispatcher(s);
    }

    @Override
    public String getRealPath(String s) {
        return request.getRealPath(s);
    }

    @Override
    public int getRemotePort() {
        return request.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return request.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return request.getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return request.getLocalPort();
    }

    @Override
    public ServletContext getServletContext() {
        return request.getServletContext();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return request.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return request.startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return request.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return request.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return request.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return request.getDispatcherType();
    }

    @Override
    public String changeSessionId() {
        return request.changeSessionId();
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return request.upgrade(handlerClass);
    }

    @Override
    public long getContentLengthLong() {
        return request.getContentLengthLong();
    }
}
