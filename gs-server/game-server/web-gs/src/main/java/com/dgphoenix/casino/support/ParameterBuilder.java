package com.dgphoenix.casino.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ParameterBuilder {
    private static final Logger LOG = LogManager.getLogger(ParameterBuilder.class);
    private static final String PARAMETER_DELIMITER = "&";
    private static final String VALUE_DELIMITER = "=";
    private String pathToJsp;

    public void setPathToJsp(String pathToJsp) {
        this.pathToJsp = pathToJsp;
    }

    private final List<Pair<String, String>> parameters = new ArrayList<>();


    public ParameterBuilder addParameter(String parameter, Object value) {
        parameters.add(Pair.of(parameter, Objects.toString(value)));
        return this;
    }

    public ParameterBuilder addParameterMap(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            parameters.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return this;
    }

    public ParameterBuilder addIfParameter(boolean condition, String parameter, Object value) {
        if (condition) {
            parameters.add(Pair.of(parameter, Objects.toString(value)));
        }
        return this;
    }

    public ParameterBuilder addIfParameter(boolean condition, String parameter, Supplier<Object> valueSupplier) {
        if (condition) {
            Object value = valueSupplier.get();
            parameters.add(Pair.of(parameter, Objects.toString(value)));
        }
        return this;
    }

    public ParameterBuilder addBooleanParameter(String parameter, boolean condition) {
        if (condition) {
            parameters.add(Pair.of(parameter, "1"));
        } else {
            parameters.add(Pair.of(parameter, "0"));
        }
        return this;
    }

    public void validate(String originalString) {
        String parameterBuilderString = build();
        if (!parameterBuilderString.equals(originalString)) {
            LOG.error("{}\nStringBuilder={}\nParameterBuilder={}\nDifference={}", pathToJsp, originalString, parameterBuilderString,
                    StringUtils.difference(originalString, parameterBuilderString));
        }
    }

    public String build() {
        return parameters.stream()
                .map(entry -> entry.getKey() + VALUE_DELIMITER + entry.getValue())
                .collect(Collectors.joining(PARAMETER_DELIMITER));
    }
}