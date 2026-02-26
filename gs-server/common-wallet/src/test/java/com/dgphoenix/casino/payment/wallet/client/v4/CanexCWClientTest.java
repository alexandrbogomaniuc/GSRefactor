package com.dgphoenix.casino.payment.wallet.client.v4;

import com.dgphoenix.casino.common.client.LoggableWithResponseCodeClient;
import com.abs.casino.common.client.canex.request.RequestType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.rest.CustomRestTemplate;
import com.dgphoenix.casino.payment.wallet.client.RestAPIClientTest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;

@Ignore
public class CanexCWClientTest extends RestAPIClientTest {

    private static final String BASE_PATH = "canex/";

    private CanexCWClient client;

    @Test
    public void testAuthRequest() throws CommonException, IOException {
        CustomRestTemplate restTemplate = new CustomRestTemplate();
        restTemplate.setLoggableClient(new LoggableWithResponseCodeClient());
        Gson serializer = new GsonBuilder().setPrettyPrinting().create();
        restTemplate.setGsonSerializer(serializer);
        restTemplate.setContentType(getMediaType());
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        ResponseActions responseActions = mockServer.expect(manyTimes(), requestTo(startsWith("https://test.canex.com")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(getMediaType()));

        responseActions
                .andExpect(withBody("auth.json"))
                .andRespond(withSuccessResponse("auth.json"));

        client = new CanexCWClient(123L, restTemplate);
        Map<String, String> params = new HashMap<>();
        params.put("token", "12345");
        params.put("bankId", "123");
        params.put("gameId", "777");
        params.put("hash", "some-hash");
        params.put(CanexCWClient.REQUEST_TYPE, RequestType.AUTH.name());
        client.doRequest(params, "https://test.canex.com/auth", 123L, 10);

    }

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Override
    protected MediaType getMediaType() {
        return MediaType.APPLICATION_JSON;
    }
}
