package com.dgphoenix.casino.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class XssSanitizerRequestWrapper extends HttpServletRequestWrapper {
    private static final Logger LOG = LogManager.getLogger(XssSanitizerRequestWrapper.class);

    private final Pattern pattern0 = Pattern.compile("[\\p{C}\"]+");
    private final Pattern pattern1 = Pattern.compile("(?i)<.*?script.*?>(.*?</script.*?>)?");
    private final Pattern pattern2 = Pattern.compile("(?i)<.*?javascript:.*?>(.*?</.*?>)?");
    private final Pattern pattern3 = Pattern.compile("(?i)<.*?[\\s/]+on.*?>(.*?</.*?>)?");

    private final Map<String, String[]> sanitizedParams = new HashMap<>();

    XssSanitizerRequestWrapper(HttpServletRequest request) {
        super(request);
        initSanitizedParametersMap();
    }

    @Override
    public String getParameter(String name) {
        return sanitizedParams.containsKey(name) ? sanitizedParams.get(name)[0] : super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (sanitizedParams.isEmpty()) {
            return super.getParameterMap();
        } else {
            Map<String, String[]> params = new HashMap<>(super.getParameterMap());
            for (Map.Entry<String, String[]> param : sanitizedParams.entrySet()) {
                params.put(param.getKey(), param.getValue());
            }
            return params;
        }
    }

    @Override
    public String[] getParameterValues(String name) {
        return sanitizedParams.containsKey(name) ? sanitizedParams.get(name) : super.getParameterValues(name);
    }

    @Override
    public String getQueryString() {
        return sanitizedParams.isEmpty()
                ? super.getQueryString()
                : sanitize(super.getQueryString());
    }

    public Map<String, String[]> getRawParametersMap() {
        return super.getParameterMap();
    }

    private void initSanitizedParametersMap() {
        super.getParameterMap().forEach(this::sanitize);
    }

    private void sanitize(String key, String[] values) {
        String[] sanitizedValues = Stream.of(values)
                .map(this::sanitize)
                .toArray(String[]::new);

        if (!Arrays.equals(values, sanitizedValues)) {
            LOG.warn("Sanitized '{}' from {} to {}", key, values, sanitizedValues);
            sanitizedParams.put(key, sanitizedValues);
        }
    }

    public String sanitize(String string) {
        if (string == null) {
            return null;
        }
        String result = pattern0.matcher(string).replaceAll("");
        result = pattern1.matcher(result).replaceAll("");
        result = pattern2.matcher(result).replaceAll("");
        result = pattern3.matcher(result).replaceAll("");
        return result.equals(string) ? string : sanitize(result);
    }
}
