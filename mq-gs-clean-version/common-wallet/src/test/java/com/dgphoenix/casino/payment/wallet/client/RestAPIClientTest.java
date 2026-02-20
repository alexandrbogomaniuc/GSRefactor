package com.dgphoenix.casino.payment.wallet.client;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.util.AssertionErrors;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.ResponseCreator;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public abstract class RestAPIClientTest {

    private final AtomicLong requestIdSequence = new AtomicLong(1);

    protected abstract String getBasePath();

    protected abstract MediaType getMediaType();

    protected RequestMatcher withBody(String filename) throws IOException {
        String json = String.format(readFromFile("request/" + filename), requestIdSequence.getAndIncrement());
        return content().string(json);
    }

    protected RequestMatcher withBody(String filename, Long time) throws IOException {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time).truncatedTo(ChronoUnit.SECONDS), ZoneId.systemDefault());
        String transactionTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime);
        String json = String.format(readFromFile("request/" + filename), transactionTime, transactionTime, requestIdSequence.getAndIncrement());
        return content().string(json);
    }

    protected ResponseCreator withSuccessResponse(String filename) throws IOException {
        return withSuccess(withResponseBody(filename), getMediaType());
    }

    protected ResponseCreator withBadRequestError(String filename) throws IOException {
        return withBadRequest()
                .body(withResponseBody(filename))
                .contentType(getMediaType());
    }

    protected ResponseCreator withError(String filename) throws IOException {
        return withServerError()
                .body(withResponseBody(filename))
                .contentType(getMediaType());
    }

    protected String withResponseBody(@Nonnull String filename) throws IOException {
        return readFromFile("response/" + filename);
    }

    protected RequestMatcher withMultipleBody(Map<String, String> mapping) {
        return request -> {
            MockClientHttpRequest mockRequest = (MockClientHttpRequest) request;
            String path = request.getURI().getPath();
            String filename = mapping.get(path);
            if (filename == null) {
                AssertionErrors.assertTrue("Request content", false);
            }
            String expectedContent = String.format(readFromFile("request/" + filename), requestIdSequence.getAndIncrement());
            AssertionErrors.assertEquals("Request content", expectedContent, mockRequest.getBodyAsString());
        };
    }

    protected ResponseCreator withMultipleBodyResponse(Map<String, String> mapping) {
        return request -> {
            String path = request.getURI().getPath();
            String filename = mapping.get(path);
            String content = filename != null ? withResponseBody(filename) : "";
            MockClientHttpResponse response = new MockClientHttpResponse(content.getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType());
            response.getHeaders().putAll(headers);
            return response;
        };
    }

    protected String readFromFile(String filename) throws IOException {
        URL resource = getClass().getClassLoader().getResource(getBasePath() + filename);
        if (resource == null) {
            throw new IOException("Unable get resource with name: " + filename);
        }
        File file = new File(resource.getFile());
        return FileUtils.readFileToString(file);
    }
}
