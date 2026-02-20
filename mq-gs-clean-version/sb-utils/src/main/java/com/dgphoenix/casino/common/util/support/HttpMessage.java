package com.dgphoenix.casino.common.util.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 25.12.2019
 */
public class HttpMessage implements KryoSerializable {

    private static final byte VERSION = 0;

    private String url;
    private Request request;
    private Response response;

    public HttpMessage() {}

    public HttpMessage(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(url);
        kryo.writeObjectOrNull(output, request, Request.class);
        kryo.writeObjectOrNull(output, response, Response.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        url = input.readString();
        request = kryo.readObjectOrNull(input, Request.class);
        response = kryo.readObjectOrNull(input, Response.class);
    }

    @Override
    public String toString() {
        return "HttpMessage{" +
                "url='" + url + '\'' +
                ", request=" + request +
                ", response=" + response +
                '}';
    }
}
