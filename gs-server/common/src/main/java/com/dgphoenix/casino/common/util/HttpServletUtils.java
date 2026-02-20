package com.dgphoenix.casino.common.util;

import com.google.gson.Gson;
import org.springframework.http.MediaType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class HttpServletUtils {

    private HttpServletUtils() {
    }

    public static <T> T parseRequestBodyJSON(HttpServletRequest request, Class<T> bodyClass, Gson gson) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), request.getCharacterEncoding()));
        String body = reader.lines().collect(Collectors.joining());
        return gson.fromJson(body, bodyClass);
    }

    public static void writeResponseBodyJSON(Object obj, HttpServletResponse response, Gson gson) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8.toString());
        ServletOutputStream out = response.getOutputStream();
        out.write(gson.toJson(obj).getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }
}
