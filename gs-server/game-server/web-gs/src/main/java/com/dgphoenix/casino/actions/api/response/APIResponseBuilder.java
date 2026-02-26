package com.dgphoenix.casino.actions.api.response;

import com.abs.casino.actions.api.response.ErrorResponse;
import com.abs.casino.actions.api.response.SuccessResponse;
import com.abs.casino.actions.api.response.UpperCaseNameCoder;
import com.dgphoenix.casino.common.exception.XmlWriterException;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class APIResponseBuilder {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String API_RESPONSE_CONTENT_TYPE = "text/xml;charset=iso-8859-1";
    private static final String REQUEST_TAG = "REQUEST";
    private static final String TIME_TAG = "TIME";

    private final XStream xstream;
    private String rootTag;
    private LocalDateTime time;
    private final Map<String, String> requestParams = new HashMap<>();
    private Response response;

    public APIResponseBuilder(@Nonnull XStream xstream, String rootTag) {
        checkNotNull(xstream);
        checkNotNull(rootTag);
        this.xstream = xstream;
        this.rootTag = rootTag;
    }

    public static APIResponseBuilder create(@Nonnull XStream xstream, @Nonnull String rootTag) {
        return new APIResponseBuilder(xstream, rootTag);
    }

    public static XStream createXStream() {
        XStream xStream = new XStream(null, new XppDriver(new UpperCaseNameCoder()));
        XStream.setupDefaultSecurity(xStream);
        xStream.addPermission(AnyTypePermission.ANY);
        xStream.processAnnotations(Response.class);
        return xStream;
    }

    public APIResponseBuilder setTime(@Nonnull LocalDateTime time) {
        this.time = time;
        return this;
    }

    public APIResponseBuilder addRequestParam(String name, String value) {
        requestParams.put(name, value);
        return this;
    }

    public APIResponseBuilder addRequestParams(Map<String, String> params) {
        requestParams.putAll(params);
        return this;
    }

    public APIResponseBuilder addRequestParamsWithMultipleValues(Map<String, String[]> params) {
        params.forEach((key, values) -> {
            String value = values.length > 0 ? values[0] : null;
            requestParams.put(key, value);
        });
        return this;
    }

    public APIResponseBuilder setSuccessResponse() {
        response = new SuccessResponse();
        return this;
    }

    public APIResponseBuilder setSuccessResponse(SuccessResponse response) {
        this.response = response;
        return this;
    }

    public APIResponseBuilder setErrorResponse(int code, String description) {
        response = new ErrorResponse(code, description);
        return this;
    }

    public VerifiedResponseBuilder verify() {
        checkNotNull(rootTag);
        checkNotNull(time);
        checkNotNull(response);
        return new VerifiedResponseBuilder();
    }

    public class VerifiedResponseBuilder {

        public void buildAndWrite(HttpServletResponse response) throws XmlWriterException, IOException {
            response.setContentType(API_RESPONSE_CONTENT_TYPE);

            PrintWriter writer = response.getWriter();
            XmlWriter xmlWriter = new XmlWriter(writer);

            xmlWriter.startDocument(rootTag);

            xmlWriter.startNode(REQUEST_TAG);
            for (Map.Entry<String, String> paramEntry : requestParams.entrySet()) {
                String key = paramEntry.getKey();
                String value = paramEntry.getValue();
                xmlWriter.node(key, value);
            }
            xmlWriter.endNode(REQUEST_TAG);

            xmlWriter.node(TIME_TAG, dateTimeFormatter.format(time));

            xmlWriter.println(xstream.toXML(APIResponseBuilder.this.response));

            xmlWriter.endDocument(rootTag);
        }
    }
}
