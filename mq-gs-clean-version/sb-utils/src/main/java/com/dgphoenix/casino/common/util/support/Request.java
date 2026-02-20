package com.dgphoenix.casino.common.util.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 25.12.2019
 */
public class Request implements KryoSerializable {

    private static final byte VERSION = 0;

    private String request;
    private Map<String, String> headers;
    private boolean isPost;
    private long time;

    public Request() {}

    public Request(String request, Map<String, String> headers, boolean isPost, long time) {
        this.request = request;
        this.headers = headers;
        this.isPost = isPost;
        this.time = time;
    }

    public String getRequest() {
        return request;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isPost() {
        return isPost;
    }

    public long getTime() {
        return time;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(request);
        kryo.writeClassAndObject(output, headers);
        output.writeBoolean(isPost);
        output.writeLong(time, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        request = input.readString();
        headers = (Map<String, String>) kryo.readClassAndObject(input);
        isPost = input.readBoolean();
        time = input.readLong(true);
    }

    @Override
    public String toString() {
        return "Request{" +
                "request='" + request + '\'' +
                ", headers=" + headers +
                ", isPost=" + isPost +
                ", time=" + new Date(time) +
                '}';
    }
}
