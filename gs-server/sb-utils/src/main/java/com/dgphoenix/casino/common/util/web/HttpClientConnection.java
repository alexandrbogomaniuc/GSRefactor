package com.abs.casino.common.util.web;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.TransportException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.support.ExceptionInfo;
import com.abs.casino.common.util.support.Request;
import com.abs.casino.common.util.support.Response;
import com.abs.casino.common.web.statistics.IStatisticsGetter;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.Security;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.abs.casino.common.util.support.AdditionalInfoAttribute.REQUEST_TIMEOUT_IN_SECONDS;

public class HttpClientConnection implements IHttpClientConnection {

    private static final Logger LOG = Logger.getLogger(HttpClientConnection.class);

    protected static final int LONG_REQUEST_PERIOD = 10000;
    protected static final int DEFAULT_SOCKET_CONNECTION_TIMEOUT = 60000;
    protected static final int DEFAULT_REQUEST_TIMEOUT = 60000;
    protected static final int DEFAULT_MAX_RESPONSE_SIZE = 262144; // 256kB
    protected static final AtomicLong activeConnectionsCount = new AtomicLong(0);
    protected static final AtomicLong timeoutCount = new AtomicLong(0);
    protected static final AtomicLong connectionErrorsCount = new AtomicLong(0);
    protected static PoolingHttpClientConnectionManager connectionManager;
    private final HttpRequestContextHolder httpRequestContext;

    //HttpClient is threadSafe and recommended for multiple executions
    private static volatile CloseableHttpClient defaultHttpClient;

    private static final HttpClientBuilder builder = HttpClients.custom();
    protected static int defaultRequestTimeout = DEFAULT_REQUEST_TIMEOUT;
    protected static HttpHost proxy;
    protected static ConfigurableHostNameVerifier configurableHostNameVerifier;
    protected static String java8ProxyUrl;

    protected String url;
    protected long requestTimeOut = defaultRequestTimeout;
    protected int maxResponseSize = DEFAULT_MAX_RESPONSE_SIZE;
    protected boolean silentError = false;
    protected IHttpClientConnectionCallbackHandler callbackHandler;
    protected IHttpClientConnectionNotOkCallbackHandler notOkCallbackHandler;
    public static final String ORIGINAL_URL = "originalUrl";
    public static final String ENCODED_PARAMETERS = "encodedParameters";
    public static final int MAX_GET_REQUEST_BYTES_LENGTH = 512;
    public static final String ORIGINAL_METHOD = "originalMethod";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";


    static {
        // java.security.Security
        // .addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        // System.setProperty(
        // "java.protocol.handler.pkgs",
        // "com.sun.net.ssl.internal.www.protocol");
        Security.addProvider(new BouncyCastleProvider());
        StatisticsManager.getInstance().registerStatisticsGetter("HttpClientConnection", new IStatisticsGetter() {
            @Override
            public String getStatistics() {
                return "activeConnectionsCount=" + activeConnectionsCount.get() +
                        ", connectionManager.totalStats=" + connectionManager.getTotalStats() +
                        ", maxTotal=" + connectionManager.getMaxTotal() +
                        ", timeoutCount=" + timeoutCount.get() +
                        ", connectionErrorsCount=" + connectionErrorsCount.get() +
                        ", configurableHostNameVerifier=" + configurableHostNameVerifier.toString() +
                        ", proxy=" + proxy;
            }
        });
        System.out.println("HttpClientConnection: BouncyCastleProvider registered");

        configurableHostNameVerifier = new ConfigurableHostNameVerifier(
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER, new HashSet<String>());
        SSLContext sslcontext = SSLContexts.createSystemDefault();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext, configurableHostNameVerifier))
                .build();
        connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        setupDefaultConnectionParams();
    }

    private static void setupDefaultConnectionParams() {
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoReuseAddress(true)
                .setSoTimeout(DEFAULT_SOCKET_CONNECTION_TIMEOUT)
                .build();

        connectionManager.setMaxTotal(5000);
        connectionManager.setDefaultMaxPerRoute(5000);
        connectionManager.setDefaultSocketConfig(socketConfig);

        builder.setConnectionManager(connectionManager);
        RequestConfig defaultRequestConfig = getDefaultRequestConfigBuilder().build();
        builder.setDefaultRequestConfig(defaultRequestConfig);
        builder.setDefaultSocketConfig(socketConfig);
        builder.setRedirectStrategy(new EverythingRedirectStrategy());

        builder.disableConnectionState();
        builder.disableAutomaticRetries();
        builder.disableContentCompression();
    }

    private static RequestConfig.Builder getDefaultRequestConfigBuilder() {
        return RequestConfig.custom()
                .setSocketTimeout(DEFAULT_SOCKET_CONNECTION_TIMEOUT)
                .setConnectTimeout(defaultRequestTimeout)
                .setConnectionRequestTimeout(10000)
                .setStaleConnectionCheckEnabled(false)
                .setCookieSpec(CookieSpecs.BEST_MATCH)
                .setMaxRedirects(2);
    }

    private RequestConfig.Builder getRequestConfigBuilder() {
        int timeOut = requestTimeOut <= 0 || requestTimeOut > Integer.MAX_VALUE ?
                Integer.MAX_VALUE : (int) requestTimeOut;
        //getLog().debug("getRequestConfigBuilder: timeOut=" + timeOut);
        return RequestConfig.custom()
                .setSocketTimeout(timeOut)
                .setConnectTimeout(timeOut)
                .setConnectionRequestTimeout(10000)
                .setStaleConnectionCheckEnabled(false)
                .setCookieSpec(CookieSpecs.BEST_MATCH)
                .setMaxRedirects(2);
    }

    public static void setPoolMaxSize(int max) {
        connectionManager.setMaxTotal(max);
        connectionManager.setDefaultMaxPerRoute(max);
    }

    public static void setupTrustedHostnameVerifier(Set<String> trustedHosts) {
        addTrustedHosts(trustedHosts);
        HttpsURLConnection.setDefaultHostnameVerifier(configurableHostNameVerifier);
        getLog().info("setupTrustedHostnameVerifier=" + HttpsURLConnection.getDefaultHostnameVerifier());
    }

    public static void addTrustedHosts(Set<String> trustedHosts) {
        if (configurableHostNameVerifier != null) {
            configurableHostNameVerifier.addTrustedHosts(trustedHosts);
            getLog().info("addTrustedHosts: trustedHosts=" +
                    (trustedHosts == null ? "null" : Arrays.toString(trustedHosts.toArray())));
        }
    }

    public static void setupJava8ProxyUrl(String url) {
        if (!StringUtils.isTrimmedEmpty(url)) {
            java8ProxyUrl = url;
            LOG.debug("proxy url set up : " + java8ProxyUrl);
        } else {
            LOG.debug("proxy url is empty");
        }
    }


    public static String getTrustedHostsInfo() {
        return configurableHostNameVerifier == null ? "null" : configurableHostNameVerifier.toString();
    }

    public static void setProperties(long timeout, String proxyHost, int proxyPort, boolean trustAllSsl) {
        defaultRequestTimeout = (int) timeout;
        setupDefaultConnectionParams();
        try {
            if (!StringUtils.isTrimmedEmpty(proxyHost) && !proxyHost.equalsIgnoreCase("null")) {
                proxy = new HttpHost(proxyHost, proxyPort);
            }
        } catch (Throwable e) {
            getLog().error("setup HttpProxyHost error:", e);
        }
        try {
            if (trustAllSsl) {
                configurableHostNameVerifier.setTrustAll(true);
                getLog().warn("SSL check disabled, trust all");
                System.out.println("HttpClientConnection: SSL check disabled, trust all");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            getLog().error("Cannot setup SSL SocketFactory:", e);
        }

    }

    protected static Logger getLog() {
        return LOG;
    }

    public static int getMaxTotal() {
        return connectionManager.getMaxTotal();
    }

    public static int getConnectionsInPool() {
        return connectionManager.getTotalStats().getLeased();
    }

    public static HttpClientConnection newInstance() {
        return new HttpClientConnection();
    }

    public static HttpClientConnection newInstanceSilent() {
        final HttpClientConnection connection = new HttpClientConnection();
        connection.silentError = true;
        return connection;
    }


    public static HttpClientConnection newInstance(long timeOut) {
        return new HttpClientConnection(timeOut);
    }

    public static HttpClientConnection newInstance(long timeOut, int maxResponseSize) {
        return new HttpClientConnection(timeOut, maxResponseSize);
    }

    public static void shutdown() {
        getLog().info("shutdown started");
        connectionManager.shutdown();
        getLog().info("shutdown completed");
    }

    public static HttpClientConnection newInstance(IHttpClientConnectionCallbackHandler callbackHandler) {
        return new HttpClientConnection(callbackHandler);
    }

    public static HttpClientConnection newInstance(IHttpClientConnectionCallbackHandler callbackHandler,
                                                   IHttpClientConnectionNotOkCallbackHandler notOkcallbackHandler) {
        return new HttpClientConnection(callbackHandler, notOkcallbackHandler);
    }

    public static HttpClientConnection newInstance(IHttpClientConnectionCallbackHandler callbackHandler,
                                                   IHttpClientConnectionNotOkCallbackHandler notOkcallbackHandler,
                                                   long timeout) {
        return new HttpClientConnection(callbackHandler, notOkcallbackHandler, timeout);
    }

    public static HttpClientConnection newInstance(IHttpClientConnectionCallbackHandler callbackHandler,
                                                   IHttpClientConnectionNotOkCallbackHandler notOkcallbackHandler,
                                                   long timeout,
                                                   int maxResponseSize) {
        return new HttpClientConnection(callbackHandler, notOkcallbackHandler, timeout, maxResponseSize);
    }

    public static HttpClientConnection newInstance(IHttpClientConnectionCallbackHandler callbackHandler, long timeOut) {
        return new HttpClientConnection(callbackHandler, timeOut);
    }

    public static HttpClientConnection newInstance(IHttpClientConnectionCallbackHandler callbackHandler, long timeOut, int maxResponseSize) {
        return new HttpClientConnection(callbackHandler, timeOut, maxResponseSize);
    }

    protected HttpClientConnection() {
        httpRequestContext = HttpRequestContextHolder.getRequestContext();
        httpRequestContext.create();
        this.requestTimeOut = defaultRequestTimeout;
    }

    protected HttpClientConnection(long requestTimeOut) {
        httpRequestContext = HttpRequestContextHolder.getRequestContext();
        httpRequestContext.create();
        this.requestTimeOut = requestTimeOut > 0 ? requestTimeOut : defaultRequestTimeout;
    }

    protected HttpClientConnection(long requestTimeOut, int maxResponseSize) {
        this(requestTimeOut);
        this.maxResponseSize = maxResponseSize <= 0 ? Integer.MAX_VALUE : maxResponseSize;
    }

    protected HttpClientConnection(IHttpClientConnectionCallbackHandler callbackHandler) {
        this();
        this.callbackHandler = callbackHandler;
    }

    public HttpClientConnection(IHttpClientConnectionCallbackHandler callbackHandler,
                                IHttpClientConnectionNotOkCallbackHandler notOkcallbackHandler) {
        this();
        this.callbackHandler = callbackHandler;
        this.notOkCallbackHandler = notOkcallbackHandler;
    }

    public HttpClientConnection(IHttpClientConnectionCallbackHandler callbackHandler,
                                IHttpClientConnectionNotOkCallbackHandler notOkcallbackHandler,
                                long timeout) {
        this(timeout);
        this.callbackHandler = callbackHandler;
        this.notOkCallbackHandler = notOkcallbackHandler;
    }

    public HttpClientConnection(IHttpClientConnectionCallbackHandler callbackHandler,
                                IHttpClientConnectionNotOkCallbackHandler notOkcallbackHandler,
                                long timeout,
                                int maxResponseSize) {
        this(timeout, maxResponseSize);
        this.callbackHandler = callbackHandler;
        this.notOkCallbackHandler = notOkcallbackHandler;
    }

    protected HttpClientConnection(IHttpClientConnectionCallbackHandler callbackHandler, long timeOut) {
        this(timeOut);
        this.callbackHandler = callbackHandler;
    }

    protected HttpClientConnection(IHttpClientConnectionCallbackHandler callbackHandler, long timeOut, int maxResponseSize) {
        this(timeOut, maxResponseSize);
        this.callbackHandler = callbackHandler;
    }

    public HashMap<String, String> request(String url, HashMap<String, String> params) throws TransportException {
        return request(url, params, true, false);
    }

    public static void uploadFile(String url, String file, boolean checkResponseBody) throws CommonException {
        HttpEntity entity = null;
        CloseableHttpClient client = getHttpClient();
        CloseableHttpResponse httpResponse = null;
        try {
            File f = new File(file);
            HttpPost mPost = new HttpPost(url);
            FileEntity reqEntity = new FileEntity(f, ContentType.APPLICATION_FORM_URLENCODED);
            mPost.setEntity(reqEntity);
            httpResponse = client.execute(mPost, HttpClientContext.create());
            final int returnCode = httpResponse.getStatusLine().getStatusCode();

            entity = httpResponse.getEntity();
            String response = EntityUtils.toString(entity);
            getLog().debug("uploadFile: code=" + returnCode + ", response=" + response);
            if (returnCode != HttpServletResponse.SC_OK || (checkResponseBody && !response.startsWith("success"))) {
                throw new IOException("Bad response: code=" + returnCode + ", response=" + response);
            }
        } catch (Exception e) {
            getLog().error("Cannot upload file", e);
            throw new CommonException("File upload error, url=" + url + ", file=" + file, e);
        } finally {
            try {
                EntityUtils.consume(entity);
            } catch (Exception e) {
                //nop
            }
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    getLog().error("Cannot close connection", e);
                }
            }
        }
    }

    public HashMap<String, String> request(String url, HashMap<String, String> params, boolean post, boolean useProxy)
            throws TransportException {
        getLog().debug("Request to url '" + url + "' with params: " + params +
                ", isPost: " + post + ", useProxy: " + useProxy);
        try {
            String response = getResponse(params, url, post, null, useProxy);
            if (response != null && !response.isEmpty()) {
                HashMap<String, String> result = new HashMap<String, String>();
                interpreteResponse(response, result);
                return result;
            } else {
                if (callbackHandler != null) {
                    callbackHandler.emptyResponse(this.url);
                }
                throw new TransportException("request :: Response is null or empty");
            }
        } catch (TransportException te) {
            throw te;
        } catch (Exception e) {
            if (callbackHandler != null && !(e instanceof InterruptedException)) {
                callbackHandler.unclassifiedError(this.url);
            }
            throw new TransportException("request :: Failed to perform request", e);
        }
    }

    @Override
    public String doRequest(String url, Map<String, String> params) throws TransportException {
        return doRequest(url, params, true);
    }


    @Override
    public StringBuilder doRequest(String url, String params, boolean post) throws TransportException {
        return doRequest(url, params, post, false);
    }

    @Override
    public StringBuilder doRequest(String url, String params, boolean post, boolean plain) throws TransportException {
        return doRequest(url, params, post, plain, true);
    }

    //version with proxy flag
    public StringBuilder doRequest(boolean java8Proxy, String url, String params, boolean post, boolean plain)
            throws TransportException {
        return doRequest(java8Proxy, url, params, post, plain, true);
    }

    @Override
    public StringBuilder doRequest(String url, String params, boolean post, boolean plain, boolean useProxy)
            throws TransportException {
        return doRequest(url, params, post, plain, null, useProxy);
    }

    @Override
    public StringBuilder doRequest(String url, String params, boolean post, boolean plain,
                                   Map<String, String> requestHeaders, boolean useProxy) throws TransportException {
        getLog().debug("Request to url '" + url + "' with params: " + params + " and headers: " + requestHeaders
                + ", isPost: " + post + ", useProxy: " + useProxy + ", plain: " + plain);
        try {
            String response = getResponse(params, url, post, plain, requestHeaders, useProxy);
            if (response != null && !response.isEmpty()) {
                return new StringBuilder(response);
            } else {
                if (callbackHandler != null) {
                    callbackHandler.emptyResponse(this.url);
                }
                throw new TransportException("doRequest:: Response is null or empty");
            }
        } catch (TransportException te) {
            throw te;
        } catch (Exception e) {
            if (callbackHandler != null && !(e instanceof InterruptedException)) {
                callbackHandler.unclassifiedError(this.url);
            }
            throw new TransportException("doRequest :: Failed to read response", e);
        }
    }

    //version with proxy flag
    public StringBuilder doRequest(boolean java8Proxy, String url, String params, boolean post,
                                   boolean plain, boolean useProxy) throws TransportException {
        if (java8Proxy && !StringUtils.isTrimmedEmpty(java8ProxyUrl)) {
            try {
                LOG.info("send request : url=" + url + " parameters="
                        + params + " through proxy url=" + java8ProxyUrl);
                Map<String, String> requestParams = new HashMap<String, String>();
                requestParams.put(ORIGINAL_URL, url);
                requestParams.put(ORIGINAL_METHOD, post ? METHOD_POST : METHOD_GET);
                String encodedParams = params == null ? null : Base64.encodeBase64String(params.getBytes());
                if (encodedParams != null) {
                    requestParams.put(ENCODED_PARAMETERS, encodedParams);
                }
                //if parameters have small length go to proxy by GET even if original method is POST
                boolean isProxyPost = post && encodedParams != null &&
                        encodedParams.length() > MAX_GET_REQUEST_BYTES_LENGTH;
                return doRequest(java8ProxyUrl, WebTools.encodeHashTableToURL(requestParams), isProxyPost, plain,
                        useProxy);
            } catch (Exception e) {
                LOG.error("Proxying failed : " + e.getMessage() + " originalUrl=" + url + " parameters=" + params
                        + " method=" + (post ? "POST" : "GET") + " java8ProxyUrl=" + java8ProxyUrl, e);
                if (e instanceof TransportException) {
                    throw (TransportException) e;
                } else {
                    throw new TransportException(e);
                }
            }
        }
        return doRequest(url, params, post, plain, useProxy);
    }


    @Override
    public String doRequest(String url, Map<String, String> params, boolean post, boolean useProxy)
            throws TransportException {
        return doRequest(url, params, post, null, useProxy);
    }

    //version with proxy flag
    public String doRequest(boolean java8Proxy, String url, Map<String, String> params,
                            boolean post, boolean useProxy) throws TransportException {
        return doRequest(java8Proxy, url, params, post, null, useProxy);
    }


    @Override
    public String doRequest(String url, Map<String, String> params, boolean post) throws TransportException {
        return doRequest(url, params, post, null, true);
    }

    //version with proxy flag
    public String doRequest(boolean java8Proxy, String url, Map<String, String> params, boolean post)
            throws TransportException {
        return doRequest(java8Proxy, url, params, post, null, true);
    }

    @Override
    public String doRequest(String url, Map<String, String> params, boolean post, Map<String, String> requestHeaders,
                            boolean useProxy)
            throws TransportException {
        getLog().debug("Request to url '" + url + "' with params: " + params + " and headers: " + requestHeaders
                + ", isPost: " + post + ", useProxy: " + useProxy);
        try {
            String response = getResponse(params, url, post, requestHeaders, useProxy);
            if (response != null && !response.isEmpty()) {
                return response;
            } else {
                if (callbackHandler != null) {
                    callbackHandler.emptyResponse(this.url);
                }
                throw new TransportException("doRequest :: Response is null or empty");
            }
        } catch (TransportException te) {
            throw te;
        } catch (Exception e) {
            if (callbackHandler != null && !(e instanceof InterruptedException)) {
                callbackHandler.unclassifiedError(this.url);
            }
            throw new TransportException("doRequest ::Failed to read response", e);
        }
    }


    // version with proxy flag
    public String doRequest(boolean java8Proxy, String url, Map<String, String> params, boolean post,
                            Map<String, String> requestHeaders,
                            boolean useProxy) throws TransportException {
        if (java8Proxy && !StringUtils.isTrimmedEmpty(java8ProxyUrl)) {
            try {
                LOG.info("send request : url=" + url + " parameters="
                        + params + " through proxy url=" + java8ProxyUrl);
                Map<String, String> requestParams = new HashMap<String, String>();
                requestParams.put(ORIGINAL_URL, url);
                requestParams.put(ORIGINAL_METHOD, post ? METHOD_POST : METHOD_GET);
                String encodedParamsMap = params == null ? null : WebTools.encodeHashTableToURL(params);
                if (encodedParamsMap != null) {
                    String encodedParams = Base64.encodeBase64String(encodedParamsMap.getBytes());
                    requestParams.put(ENCODED_PARAMETERS, encodedParams);
                }
                //if parameters have small length go to proxy by GET even if original method is POST
                boolean isProxyPost = post && encodedParamsMap != null &&
                        encodedParamsMap.length() > MAX_GET_REQUEST_BYTES_LENGTH;
                return doRequest(java8ProxyUrl, requestParams, isProxyPost, requestHeaders, useProxy);
            } catch (Exception e) {
                LOG.error("Proxy request failed : " + e.getMessage() + " originalUrl=" + url + " parameters=" + params
                        + " method=" + (post ? "POST" : "GET") + " java8ProxyUrl=" + java8ProxyUrl, e);
                if (e instanceof TransportException) {
                    throw (TransportException) e;
                } else {
                    throw new TransportException(e);
                }
            }
        }
        return doRequest(url, params, post, requestHeaders, useProxy);
    }

    @Override
    public HttpResponseWrapper doRequest(String url, Map<String, String> params, boolean post, String cookies)
            throws TransportException {
        return doRequest(url, params, post, false, cookies);
    }

    //version with proxy flag
    public HttpResponseWrapper doRequest(boolean java8Proxy, String url, Map<String, String> params,
                                         boolean post, String cookies) throws TransportException {
        return doRequest(java8Proxy, url, params, post, false, cookies);
    }

    //version with proxy flag
    public HttpResponseWrapper doRequest(boolean java8Proxy, String url, Map<String, String> params, boolean post,
                                         boolean allowedEmptyResponse, String cookies) throws TransportException {
        if (java8Proxy && !StringUtils.isTrimmedEmpty(java8ProxyUrl)) {
            try {
                LOG.info("send request : url=" + url + " parameters="
                        + params + " through proxy url=" + java8ProxyUrl);
                Map<String, String> requestParams = new HashMap<String, String>();
                requestParams.put(ORIGINAL_URL, url);
                requestParams.put(ORIGINAL_METHOD, post ? METHOD_POST : METHOD_GET);
                String encodedParamsMap = params == null ? null : WebTools.encodeHashTableToURL(params);
                if (encodedParamsMap != null) {
                    String encodedParams = Base64.encodeBase64String(encodedParamsMap.getBytes());
                    requestParams.put(ENCODED_PARAMETERS, encodedParams);
                }
                //if parameters have small length go to proxy by GET even if original method is POST
                boolean isProxyPost = post && encodedParamsMap != null &&
                        encodedParamsMap.length() > MAX_GET_REQUEST_BYTES_LENGTH;
                return doRequest(java8ProxyUrl, requestParams, isProxyPost, allowedEmptyResponse, cookies);
            } catch (Exception e) {
                LOG.error("Proxy request failed : " + e.getMessage() + " originalUrl=" + url + " parameters=" + params
                                + " method=" + (post ? "POST" : "GET") + " cookies=" + cookies + " java8ProxyUrl=" + java8ProxyUrl,
                        e);
                if (e instanceof TransportException) {
                    throw (TransportException) e;
                } else {
                    throw new TransportException(e);
                }
            }
        }
        return doRequest(url, params, post, allowedEmptyResponse, cookies);
    }


    @Override
    public HttpResponseWrapper doRequest(String url, Map<String, String> params, boolean post,
                                         boolean allowedEmptyResponse, String cookies)
            throws TransportException {
        getLog().debug("Request to url '" + url + "' with params: " + params
                + " and cookies: " + cookies + ", isPost: " + post);
        HttpResponseWrapper response;
        try {
            response = getResponse(params, url, post, cookies);
            if (response != null && !response.isEmpty()) {
                return response;
            } else {
                if (callbackHandler != null) {
                    callbackHandler.emptyResponse(this.url);
                }
                if (!allowedEmptyResponse) {
                    throw new TransportException("doRequest :: Response is null or empty");
                }
            }
        } catch (TransportException te) {
            throw te;
        } catch (Exception e) {
            if (callbackHandler != null && !(e instanceof InterruptedException)) {
                callbackHandler.unclassifiedError(this.url);
            }
            throw new TransportException("doRequest ::Failed to read response", e);
        }
        return response;
    }

    private String getResponse(String request, String url, boolean post, boolean plain,
                               Map<String, String> requestHeaders, boolean useProxy) throws TransportException,
            InterruptedException {
        long now = System.currentTimeMillis();
        initUrl(url, request, post);
        String response = getResponse(request, post, plain, requestHeaders, useProxy);
        StatisticsManager.getInstance().updateRequestStatistics("HttpClientConnection: getResponse",
                System.currentTimeMillis() - now);
        return response;
    }

    private String getResponse(Map<String, String> params, String url, boolean post, Map<String, String> requestHeaders,
                               boolean useProxy) throws TransportException, InterruptedException {
        long now = System.currentTimeMillis();
        String request;
        try {
            request = WebTools.encodeHashTableToURL(params);
        } catch (UnsupportedEncodingException e) {
            throw new TransportException("encoding exception", e);
        }
        initUrl(url, request, post);
        String response = getResponse(request, post, false, requestHeaders, useProxy);
        StatisticsManager.getInstance().updateRequestStatistics("HttpClientConnection: getResponse",
                System.currentTimeMillis() - now);
        return response;
    }


    private HttpResponseWrapper getResponse(Map<String, String> params, String url, boolean post, String cookie)
            throws TransportException,
            InterruptedException {
        long now = System.currentTimeMillis();
        String request;
        try {
            request = WebTools.encodeHashTableToURL(params);
        } catch (UnsupportedEncodingException e) {
            throw new TransportException("encoding exception", e);
        }
        initUrl(url, request, post);
        HttpResponseWrapper response = getResponse(request, post, cookie);
        StatisticsManager.getInstance().updateRequestStatistics("HttpClientConnection: getResponse",
                System.currentTimeMillis() - now);
        return response;
    }

    private void initUrl(String url, String request, boolean post) throws TransportException {
        this.url = url + (post ? "" : (url.contains("?") ? "&" : "?") + request);
    }

    private void interpreteResponse(String answer, HashMap<String, String> htRequestResults) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(answer.getBytes());
        Properties properties = new Properties();
        properties.load(stream);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            try {
                htRequestResults.put((String) entry.getKey(), (String) entry.getValue());
            } catch (Throwable e) {
                getLog().error("HttpClientConnection :: interprete response exception", e);
            }
        }
    }

    private static CloseableHttpClient getHttpClient() {
        if (defaultHttpClient == null) {
            synchronized (HttpClientConnection.class) {
                if (defaultHttpClient == null) {
                    final String s = System.getProperty("http.keepAlive");
                    if ("true".equalsIgnoreCase(s)) {
                        LOG.info("getHttpClient: keepAlive is enabled to default behaviour");
                    } else {
                        LOG.info("getHttpClient: keepAlive is disabled");
                        builder.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
                            @Override
                            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                                return -1; //don't keep
                            }
                        });
                        builder.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
                    }
                    defaultHttpClient = builder.build();
                }
            }
        }
        return defaultHttpClient;
    }

    public static CloseableHttpClient createHttpClient() {
        return builder.build();
    }

    private boolean isHttps(HttpUriRequest method) {
        return "https".equalsIgnoreCase(method.getURI().getScheme());
    }

    private HttpHost getProxyHost(HttpUriRequest method) {
        return proxy;
    }

    private boolean isProxyAvailable() {
        return proxy != null;
    }


    private String getUrlRequestSubString(String url) {
        if (!url.contains("?")) {
            return null;
        }
        return url.split("\\?")[1];
    }

    private String getBaseUrl(String url) {
        if (!url.contains("?")) {
            return url;
        }
        return url.split("\\?")[0];
    }


    private String getResponse(boolean java8Proxy, String request, boolean post, boolean plain,
                               Map<String, String> requestHeaders, boolean useProxy)
            throws TransportException, InterruptedException {
        if (java8Proxy && !StringUtils.isTrimmedEmpty(java8ProxyUrl)) {
            try {
                Map<String, String> requestParams = new HashMap<String, String>();
                requestParams.put(ORIGINAL_URL, getBaseUrl(url));
                requestParams.put(ORIGINAL_METHOD, post ? METHOD_POST : METHOD_GET);
                String requestSubstring = getUrlRequestSubString(url);
                String encodedParams;
                if (requestSubstring != null) {
                    encodedParams = Base64.encodeBase64String(requestSubstring.getBytes());
                } else {
                    encodedParams = Base64.encodeBase64String(request.getBytes());
                }
                requestParams.put(ENCODED_PARAMETERS, encodedParams);
                if (post) {
                    url = java8ProxyUrl;
                    request = WebTools.encodeHashTableToURL(requestParams);
                } else {
                    url = java8ProxyUrl + "?" + WebTools.encodeHashTableToURL(requestParams);
                }
            } catch (Exception e) {
                LOG.error("Proxying failed : " + e.getMessage() + " originalUrl=" + url + " request=" + request
                                + " method=" + (post ? "POST" : "GET") + " request headers=" + requestHeaders + " java8ProxyUrl=" + java8ProxyUrl,
                        e);
                throw new TransportException(e);
            }
        }
        return getResponse(request, post, plain, requestHeaders, useProxy);
    }

    private String getResponse(String request, boolean post, boolean plain, Map<String, String> requestHeaders,
                               boolean useProxy)
            throws InterruptedException, TransportException {
        long now = System.currentTimeMillis();
        CloseableHttpClient client = getHttpClient();

        HttpRequestBase method = post ? new HttpPost(url) : new HttpGet(url);

        final String keepAlive = System.getProperty("http.keepAlive");
        method.setHeader(HTTP.CONN_DIRECTIVE,
                "true".equalsIgnoreCase(keepAlive) ? HTTP.CONN_KEEP_ALIVE : HTTP.CONN_CLOSE);

        String response = null;
        HttpEntity responseEntity = null;
        CloseableHttpResponse httpResponse = null;
        httpRequestContext.setRequest(url, new Request(request, requestHeaders, post, now));
        try {
            activeConnectionsCount.incrementAndGet();
            if (post) {
                final HttpPost postMethod = (HttpPost) method;
                ContentType contentType = ContentType.create(plain ? ContentType.TEXT_PLAIN.getMimeType() :
                        ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), Consts.UTF_8);
                StringEntity ent = new StringEntity(request, contentType);
                postMethod.setEntity(ent);
            }
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                    method.setHeader(entry.getKey(), entry.getValue());
                }
            }
            RequestConfig.Builder configBuilder = getRequestConfigBuilder();
            if (useProxy && isProxyAvailable()) {
                configBuilder.setProxy(getProxyHost(method));
            }
            method.setConfig(configBuilder.build());
            httpResponse = client.execute(method, HttpClientContext.create());
            if (httpResponse == null || httpResponse.getStatusLine() == null) {
                getLog().debug("Illegal response = " + httpResponse);
                throw new CommonException("Illegal response");
            }
            final int returnCode = httpResponse.getStatusLine().getStatusCode();
            if (returnCode != HttpStatus.SC_OK) {
                processNotOkResponse(returnCode, httpResponse);
            }
            responseEntity = httpResponse.getEntity();
            response = toString(responseEntity, maxResponseSize);
            if (callbackHandler != null) {
                callbackHandler.success(this.url);
                if (System.currentTimeMillis() - now > LONG_REQUEST_PERIOD) {
                    callbackHandler.longRequest(this.url);
                }
            }
            httpRequestContext.setResponse(new Response(response, returnCode, System.currentTimeMillis()));
        } catch (SocketTimeoutException e) {
            handleSocketTimeoutException(e, method, now);
        } catch (Exception e) {
            handleException(e, method, request);
        } finally {
            handleFinallyBlock(responseEntity, httpResponse);
        }
        return response == null ? "" : response;
    }

    private HttpResponseWrapper getResponse(String request, boolean post, String serverCookieValue)
            throws InterruptedException, TransportException {
        long now = System.currentTimeMillis();

        CloseableHttpClient client = getHttpClient();
        HttpRequestBase method = post ? new HttpPost(url) : new HttpGet(url);
        HttpEntity responseEntity = null;
        CloseableHttpResponse httpResponse = null;
        String response = null;
        StringBuilder responseCookies = new StringBuilder();
        CookieStore cookieStore = new BasicCookieStore();
        try {
            activeConnectionsCount.incrementAndGet();
            if (post) {
                final HttpPost postMethod = (HttpPost) method;
                ContentType contentType = ContentType.create(
                        ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), Consts.UTF_8);
                StringEntity ent = new StringEntity(request, contentType);
                postMethod.setEntity(ent);
            }
            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

            Map<String, String> requestHeaders = null;
            if (serverCookieValue != null) {
                method.addHeader("Cookie", serverCookieValue);
                requestHeaders = ImmutableMap.of("Cookie", serverCookieValue);
            }
            httpRequestContext.setRequest(url, new Request(request, requestHeaders, post, now));
            RequestConfig.Builder configBuilder = getRequestConfigBuilder();
            if (isProxyAvailable()) {
                configBuilder.setProxy(getProxyHost(method));
            }
            method.setConfig(configBuilder.build());
            httpResponse = client.execute(method, localContext);
            final int returnCode = httpResponse.getStatusLine().getStatusCode();
            if (returnCode != HttpStatus.SC_OK) {
                processNotOkResponse(returnCode, httpResponse);
            }
            StringBuilder cookiesBuff = new StringBuilder();
            responseEntity = httpResponse.getEntity();
            final Header[] cookieHeaders = httpResponse.getHeaders("Set-Cookie");
            if (cookieHeaders != null && cookieHeaders.length > 0) {
                for (Header cookieHeader : cookieHeaders) {
                    cookiesBuff.append(cookieHeader.getValue()).append(";");
                }
            }
            if (cookiesBuff.length() > 0) {
                StringTokenizer st = new StringTokenizer(cookiesBuff.toString(), ";");
                while (st.hasMoreElements()) {
                    String token = st.nextToken().trim();
                    if (!token.toLowerCase().startsWith("path")) {
                        responseCookies.append(token).append(";");
                    }
                }
            }
            final List<Cookie> cookieList = cookieStore.getCookies();
            for (Cookie cookie : cookieList) {
                responseCookies.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
            }
            response = toString(responseEntity, maxResponseSize);
            if (callbackHandler != null) {
                callbackHandler.success(this.url);
                if (System.currentTimeMillis() - now > LONG_REQUEST_PERIOD) {
                    callbackHandler.longRequest(this.url);
                }
            }
            httpRequestContext.setResponse(new Response(response, returnCode, System.currentTimeMillis()));
        } catch (SocketTimeoutException e) {
            handleSocketTimeoutException(e, method, now);
        } catch (Exception e) {
            handleException(e, method, request);
        } finally {
            handleFinallyBlock(responseEntity, httpResponse);
        }
        return new HttpResponseWrapper(response == null ? "" : response, responseCookies.toString(), cookieStore);
    }

    private String getResponseForRequestWithDefinedHttpEntity(HttpEntity entity, Map<String, String> requestHeaders,
                                                              boolean useProxy)
            throws InterruptedException, TransportException {
        long now = System.currentTimeMillis();

        CloseableHttpClient client = getHttpClient();
        HttpRequestBase method = new HttpPost(url);

        String response = null;
        HttpEntity responseEntity = null;
        CloseableHttpResponse httpResponse = null;
        String request = null;
        try {
            request = toString(entity, maxResponseSize);
            httpRequestContext.setRequest(url, new Request(request, requestHeaders, true, now));
            activeConnectionsCount.incrementAndGet();
            final HttpPost postMethod = (HttpPost) method;
            postMethod.setEntity(entity);
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                    method.setHeader(entry.getKey(), entry.getValue());
                }
            }
            RequestConfig.Builder configBuilder = getRequestConfigBuilder();
            if (useProxy && isProxyAvailable()) {
                configBuilder.setProxy(getProxyHost(method));
            }
            method.setConfig(configBuilder.build());
            //HttpClientParams.setRedirecting(params, true);
            httpResponse = client.execute(method, HttpClientContext.create());
            final int returnCode = httpResponse.getStatusLine().getStatusCode();
            if (returnCode != HttpStatus.SC_OK) {
                processNotOkResponse(returnCode, httpResponse);
            }
            responseEntity = httpResponse.getEntity();
            response = toString(responseEntity, maxResponseSize);
            if (callbackHandler != null) {
                callbackHandler.success(this.url);
                if (System.currentTimeMillis() - now > LONG_REQUEST_PERIOD) {
                    callbackHandler.longRequest(this.url);
                }
            }
            httpRequestContext.setResponse(new Response(response, returnCode, System.currentTimeMillis()));
        } catch (SocketTimeoutException e) {
            handleSocketTimeoutException(e, method, now);
        } catch (Exception e) {
            handleException(e, method, request);
        } finally {
            handleFinallyBlock(responseEntity, httpResponse);
        }
        return response == null ? "" : response;
    }

    private void addToStatistic(Exception e) {
        if (callbackHandler != null && notOkCallbackHandler == null) {
            boolean errorWritten = false;
            if (e instanceof TransportException) {
                TransportException transportException = (TransportException) e;
                if (transportException.getStatusCode() == 500) {
                    callbackHandler.httpError500(this.url, null);
                    errorWritten = true;
                } else if (transportException.getStatusCode() == 503) {
                    callbackHandler.httpError503(this.url, null);
                    errorWritten = true;
                }
            }
            if (!errorWritten) {
                IOException ioException = null;
                if (e instanceof IOException) {
                    ioException = (IOException) e;
                }
                callbackHandler.httpErrorUnclassified(this.url, ioException);
            }
        }
    }

    @Override
    public String doPostRequest(String url, HttpEntity entity) throws TransportException {
        return doPostRequest(url, entity, null, false);
    }

    @Override
    public String doPostRequest(String url, HttpEntity entity, Map<String, String> requestHeaders, boolean useProxy)
            throws TransportException {
        getLog().debug("Request to url " + url + ", httpEntity: " + entity + ", headers: " + requestHeaders + ", useProxy: " + useProxy);
        try {
            initUrl(url, null, true);
            String response = getResponseForRequestWithDefinedHttpEntity(entity, requestHeaders, useProxy);
            if (response != null && !response.isEmpty()) {
                return response;
            } else {
                if (callbackHandler != null) {
                    callbackHandler.emptyResponse(this.url);
                }
                throw new TransportException("doRequest :: Response is null or empty");
            }

        } catch (TransportException te) {
            throw te;
        } catch (Exception e) {
            if (callbackHandler != null && !(e instanceof InterruptedException)) {
                callbackHandler.unclassifiedError(this.url);
            }
            throw new TransportException("doRequest ::Failed to read response", e);
        }
    }

    public HttpClientConnection setNotOkCallbackHandler(
            IHttpClientConnectionNotOkCallbackHandler notOkCallbackHandler) {
        this.notOkCallbackHandler = notOkCallbackHandler;
        return this;
    }

    protected void processNotOkResponse(int returnCode, HttpResponse httpResponse) throws TransportException {
        String response = null;
        try {
            final HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
                response = EntityUtils.toString(responseEntity);
            }
        } catch (Throwable t) {
            //nop
        }
        httpRequestContext.setResponse(new Response(response, returnCode, System.currentTimeMillis()));
        if (notOkCallbackHandler != null) {
            notOkCallbackHandler.onNotOkResult(this.url, returnCode, response);
        }
        final TransportException exception = new TransportException("Invalid response code: " +
                returnCode + ", " + httpResponse.getStatusLine().getReasonPhrase());
        exception.setStatusCode(returnCode);
        exception.setReasonPhrase(httpResponse.getStatusLine().getReasonPhrase());
        exception.setMessageBody(response);
        throw exception;
    }

    private String toString(final HttpEntity entity, final int maxStreamSize) throws IOException {
        final InputStream instream = entity.getContent();
        if (instream == null) {
            return null;
        }
        try {
            if (entity.getContentLength() > maxStreamSize) {
                getLog().error("Content-Length is very big. " + entity.getContentLength() + " > " + maxStreamSize);
                throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
            }
            int i = (int)entity.getContentLength();
            if (i < 0) {
                i = 4096;
            }
            ContentType contentType = ContentType.get(entity);
            Charset charset = null;
            if (contentType != null) {
                charset = contentType.getCharset();
                if (charset == null) {
                    final ContentType defaultContentType = ContentType.getByMimeType(contentType.getMimeType());
                    charset = defaultContentType != null ? defaultContentType.getCharset() : null;
                }
            }
            if (charset == null) {
                charset = HTTP.DEF_CONTENT_CHARSET;
            }
            final Reader reader = new InputStreamReader(instream, charset);
            final CharArrayBuffer buffer = new CharArrayBuffer(i);
            final char[] tmp = new char[1024];
            int l;
            long length = 0;
            while((l = reader.read(tmp)) != -1) {
                if ((length + l) > maxStreamSize) {
                    getLog().error("HTTP entity too large to be buffered in memory. Read: " + (length + l)
                            + "'" + buffer.toString() + "'" + ". Limit: " + maxStreamSize +
                            ". Content-Length header value: " + entity.getContentLength());
                    throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
                }
                buffer.append(tmp, 0, l);
                length += l;
            }
            return buffer.toString();
        } finally {
            instream.close();
        }
    }

    private void handleSocketTimeoutException(SocketTimeoutException e, HttpRequestBase method, long now)
            throws TransportException {
        timeoutCount.incrementAndGet();
        if (callbackHandler != null) {
            callbackHandler.timeout(this.url);
        }
        method.abort();
        long secondsPast = System.currentTimeMillis() - now;
        httpRequestContext.addAdditionalInfo(REQUEST_TIMEOUT_IN_SECONDS, String.valueOf(secondsPast));
        httpRequestContext.setExceptionInfo(new ExceptionInfo(e, System.currentTimeMillis()));
        throw new TransportException("Request timeout", e);
    }

    private void handleException(Exception e, HttpRequestBase method, String request) throws TransportException {
        connectionErrorsCount.incrementAndGet();
        String errorMessage = "Failed to perform request: " + request + " to URL:" + url;
        if (!silentError) {
            getLog().error(errorMessage, e);
        }
        method.abort();
        addToStatistic(e);
        httpRequestContext.setExceptionInfo(new ExceptionInfo(e, System.currentTimeMillis()));
        if(e instanceof TransportException) {
            throw (TransportException) e;
        }
        throw new TransportException("Failed to perform http request", e);
    }

    private void handleFinallyBlock(HttpEntity responseEntity, CloseableHttpResponse httpResponse) {
        try {
            EntityUtils.consume(responseEntity);
        } catch (Exception e) {
            //nop
        }
        if (httpResponse != null) {
            try {
                httpResponse.close();
            } catch (IOException e) {
                getLog().error("Cannot close connection", e);
            }
        }
        activeConnectionsCount.decrementAndGet();
    }
}
