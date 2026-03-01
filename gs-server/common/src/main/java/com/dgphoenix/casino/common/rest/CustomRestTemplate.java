package com.abs.casino.common.rest;

import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.support.ExceptionInfo;
import com.abs.casino.common.util.support.Request;
import com.abs.casino.common.util.support.Response;
import com.abs.casino.common.util.web.HttpRequestContextHolder;
import com.abs.casino.gs.managers.payment.wallet.ILoggableCWClient;
import com.abs.casino.gs.managers.payment.wallet.ILoggableResponseCode;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.*;

import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static com.abs.casino.common.util.support.AdditionalInfoAttribute.REQUEST_TIMEOUT_IN_SECONDS;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.time.DurationFormatUtils.formatDurationHMS;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 20.06.2020
 */
public class CustomRestTemplate extends RestTemplate {
    private static final Logger LOG = LogManager.getLogger(CustomRestTemplate.class);
    private static final String HASH_HEADER = "Hash";

    protected BankInfo bankInfo;
    private ILoggableCWClient loggableClient;
    private String baseUrl;
    protected HttpHeaders httpHeaders = new HttpHeaders();

    private Gson gsonSerializer;
    private XStream xStreamSerializer;
    private HmacUtils hashEncoder;

    private final HttpRequestContextHolder httpRequestContext = HttpRequestContextHolder.getRequestContext();

    public CustomRestTemplate() {
        this(Collections.singletonList(new StringHttpMessageConverter(StandardCharsets.UTF_8)));
    }

    public CustomRestTemplate(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
        setErrorHandler(new CustomResponseErrorHandler());
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(10000);
        httpRequestFactory.setConnectTimeout(30000);
        httpRequestFactory.setReadTimeout(50000);
        setRequestFactory(httpRequestFactory);
    }

    public CustomRestTemplate(BankInfo bankInfo) {
        this();
        this.bankInfo = bankInfo;
        this.baseUrl = bankInfo.getCWWSUrl();
        checkArgument(isNotBlank(baseUrl), "Base URL must be specified");
    }

    public void setup(ILoggableCWClient loggableClient, String baseUrl, MediaType mediaType) {
        this.loggableClient = loggableClient;
        checkArgument(isNotBlank(baseUrl), "Base URL must be specified");
        this.baseUrl = baseUrl;
        httpHeaders.setContentType(mediaType);
    }

    public void setBaseUrl(String baseUrl) {
        checkArgument(isNotBlank(baseUrl), "Base URL must be specified");
        this.baseUrl = baseUrl;
    }

    public void setContentType(MediaType mediaType) {
        httpHeaders.setContentType(mediaType);
    }

    public void setupHashCalculation(HmacAlgorithms algorithm, String secretKey) {
        hashEncoder = new HmacUtils(algorithm, secretKey);
    }

    public void setLoggableClient(ILoggableCWClient loggableClient) {
        this.loggableClient = loggableClient;
    }

    public void setGsonSerializer(Gson gsonSerializer) {
        this.gsonSerializer = gsonSerializer;
    }

    public void setXStreamSerializer(XStream xStreamSerializer) {
        this.xStreamSerializer = xStreamSerializer;
    }

    public BankInfo getBankInfo() {
        return bankInfo;
    }

    public <T> String serialize(T request) throws CommonException {
        if (gsonSerializer != null) {
            return gsonSerializer.toJson(request);
        } else if (xStreamSerializer != null) {
            return xStreamSerializer.toXML(request);
        } else {
            throw new CommonException("Serializer is not set up");
        }
    }

    public <T> T deserialize(String responseBody, Type responseClass) throws CommonException {
        if (gsonSerializer != null) {
            return gsonSerializer.fromJson(responseBody, responseClass);
        } else if (xStreamSerializer != null) {
            return (T) xStreamSerializer.fromXML(responseBody);
        } else {
            throw new CommonException("Serializer is not set up");
        }
    }

    public <T, N> N sendRequest(String uri, T request, Class<N> responseClass) throws CommonException {
        String url = baseUrl == null ? uri : baseUrl + uri;
        loggableClient.logUrl(url);

        String clientName = loggableClient.getClass().getSimpleName();
        String requestBody = serialize(request);
        loggableClient.logRequest(ImmutableMap.of("body", requestBody));
        LOG.info("{}: request to url: {} with body: {}", clientName, url, requestBody);

        if (needCalculateHash()) {
            calculateHash(requestBody);
        }
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
        ResponseEntity<String> responseEntity = null;
        String responseBody = "";
        long start = System.currentTimeMillis();
        try {
            responseEntity = postForEntity(url, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            responseBody = e.getResponseBodyAsString();
            if (loggableClient instanceof ILoggableResponseCode) {
                ((ILoggableResponseCode)loggableClient).logResponseHTTPCode(e.getStatusCode().value());
            }
            loggableClient.logResponse(responseBody);
            LOG.error("error while trying to send request", e);
            throw e;
        } catch (Exception e) {
            LOG.error("error while trying to send request", e);
            throw e;
        } finally {
            if (responseEntity != null) {
                if (loggableClient instanceof ILoggableResponseCode) {
                    ((ILoggableResponseCode)loggableClient).logResponseHTTPCode(responseEntity.getStatusCodeValue());
                }
                responseBody = responseEntity.getBody();
            }
            String duration = formatDurationHMS(System.currentTimeMillis() - start);
            LOG.debug("{}: response from url: {} is: {}, executed in: {}", clientName, url, responseBody, duration);
        }
        MediaType responseContentType = responseEntity.getHeaders().getContentType();
        LOG.debug("{}: response content type: {}", clientName, responseContentType);
        loggableClient.logResponse(responseBody);
        return deserialize(responseBody, responseClass);
    }

    private boolean needCalculateHash() {
        return hashEncoder != null;
    }

    private void calculateHash(String requestBody) {
        String hash = hashEncoder.hmacHex(requestBody);
        httpHeaders.set(HASH_HEADER, hash);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
        HttpEntity<String> requestEntity = (HttpEntity<String>) request;
        Request req = new Request(requestEntity.getBody(), requestEntity.getHeaders().toSingleValueMap(), true, System.currentTimeMillis());
        httpRequestContext.create();
        httpRequestContext.setRequest(url, req);
        return super.postForEntity(url, request, responseType, uriVariables);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) {
        long startTime = System.currentTimeMillis();
        try {
            T result = super.doExecute(url, method, requestCallback, responseExtractor);
            ResponseEntity<String> responseEntity = (ResponseEntity<String>) result;
            Response response = new Response(responseEntity.getBody(), responseEntity.getStatusCodeValue(), System.currentTimeMillis());
            httpRequestContext.setResponse(response);
            return result;
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                long timeout = System.currentTimeMillis() - startTime;
                httpRequestContext.addAdditionalInfo(REQUEST_TIMEOUT_IN_SECONDS, String.valueOf(timeout / 1000.0));
            }
            httpRequestContext.setExceptionInfo(new ExceptionInfo(e, System.currentTimeMillis()));
            throw e;
        }
    }
}
