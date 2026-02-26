package com.abs.casino.common.client.canex.response;

import com.dgphoenix.casino.common.client.canex.request.CanexRequest;
import com.dgphoenix.casino.common.util.xml.XmlRequestResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

/**
 * Common response body for all responses from Canex
 */
public class CanexJsonResponse {
    private static final Logger LOG = LogManager.getLogger(CanexJsonResponse.class);

    @JsonProperty("EXTSYSTEM")
    @SerializedName("EXTSYSTEM")
    private ExtSystem extSystem;

    public CanexJsonResponse() {
    }

    public CanexJsonResponse(CanexRequest request, CanexResponse response, String time) {
        extSystem = new ExtSystem();
        extSystem.setRequest(request);
        extSystem.setResponse(response);
        extSystem.setTime(time);
    }

    public ExtSystem getExtSystem() {
        return extSystem;
    }

    public void setExtSystem(ExtSystem extSystem) {
        this.extSystem = extSystem;
    }

    public XmlRequestResult toXmlResult() {
        XmlRequestResult result = new XmlRequestResult();
        result.setStatus(extSystem.getResponse().getResult());
        convertParameters(extSystem.getRequest(), result::putRequestParameter);
        convertParameters(extSystem.getResponse(), result::putResponseParameter);
        return result;
    }

    private void convertParameters(Object object, BiConsumer<String, String> consumer) {
        for (Field field : object.getClass().getDeclaredFields()) {
            Object value = getFieldValue(object, field);
            if (value != null) {
                String name = field.getAnnotation(SerializedName.class).value();
                consumer.accept(name, value.toString());
            }
        }
    }

    private Object getFieldValue(Object object, Field field) {
        Object value = null;
        try {
            field.setAccessible(true);
            value = field.get(object);
        } catch (IllegalAccessException e) {
            LOG.warn("Unable access to field");

        }
        return value;
    }
}
