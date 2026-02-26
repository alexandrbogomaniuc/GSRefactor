package com.abs.casino.common.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;

public class AddableHttpRequest extends HttpServletRequestWrapper {

    private HashMap<String, String> params = new HashMap<>();

    public AddableHttpRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String res = params.get(name);
        if (res == null) {
            HttpServletRequest req = (HttpServletRequest) super.getRequest();
            req.getParameter(name);
        }
        return res;
    }

    public void addParameter( String name, String value ) {
        params.put(name, value);
    }
}
