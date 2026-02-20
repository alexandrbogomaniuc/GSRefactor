package com.dgphoenix.casino.common.util.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 25.12.2019
 */
public class Response implements KryoSerializable {

    private static final byte VERSION = 0;
    private static final int MAX_RESPONSE_BODY_SIZE = 1024;

    private String responseBody;
    private int statusCode;
    private long time;
    private ExceptionInfo exceptionInfo;

    public Response() {}

    public Response(String responseBody, int statusCode, long time) {
        this.responseBody = StringUtils.substring(responseBody, 0, MAX_RESPONSE_BODY_SIZE);;
        this.statusCode = statusCode;
        this.time = time;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getTime() {
        return time;
    }

    public ExceptionInfo getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(ExceptionInfo exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(responseBody);
        output.writeInt(statusCode, true);
        output.writeLong(time, true);
        kryo.writeObjectOrNull(output, exceptionInfo, ExceptionInfo.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        responseBody = input.readString();
        statusCode = input.readInt(true);
        time = input.readLong(true);
        exceptionInfo = kryo.readObjectOrNull(input, ExceptionInfo.class);
    }

    @Override
    public String toString() {
        return "Response{" +
                "responseBody='" + responseBody + '\'' +
                ", statusCode=" + statusCode +
                ", time=" + new Date(time) +
                ", exceptionInfo=" + exceptionInfo +
                '}';
    }
}
