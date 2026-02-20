package com.dgphoenix.casino.common.util.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 20.12.2019
 */
public class HttpCallInfo implements KryoSerializable {

    private static final byte VERSION = 0;

    private String threadName;
    private int gameServerId;
    private ExceptionInfo exceptionInfo;
    private HttpMessage httpMessage;
    private Map<String, String> additionalInfo = Maps.newHashMap();

    public HttpCallInfo() {}

    public HttpCallInfo(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getGameServerId() {
        return gameServerId;
    }

    public void setGameServerId(int gameServerId) {
        this.gameServerId = gameServerId;
    }

    public ExceptionInfo getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(ExceptionInfo exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public HttpMessage getHttpMessage() {
        return httpMessage;
    }

    public void setRequest(String url, Request request) {
        httpMessage = new HttpMessage(url);
        httpMessage.setRequest(request);
    }

    public void setResponse(Response response) {
        if (httpMessage != null) {
            httpMessage.setResponse(response);
        }
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public HttpCallInfo addAdditionalInfo(String attribute, String value) {
        additionalInfo.put(attribute, value);
        return this;
    }

    public HttpCallInfo addAdditionalInfo(AdditionalInfoAttribute attribute, String value) {
        return addAdditionalInfo(attribute.getAttributeName(), value);
    }

    public HttpCallInfo addAdditionalInfo(AdditionalInfoAttribute attribute, long value) {
        return addAdditionalInfo(attribute, String.valueOf(value));
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(threadName);
        output.writeInt(gameServerId, true);
        kryo.writeObjectOrNull(output, exceptionInfo, ExceptionInfo.class);
        kryo.writeObjectOrNull(output, httpMessage, HttpMessage.class);
        kryo.writeClassAndObject(output, additionalInfo);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        threadName = input.readString();
        gameServerId = input.readInt(true);
        exceptionInfo = kryo.readObjectOrNull(input, ExceptionInfo.class);
        httpMessage = kryo.readObjectOrNull(input, HttpMessage.class);
        additionalInfo = (Map<String, String>) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "HttpCallInfo{" +
                "threadName='" + threadName + '\'' +
                ", gameServerId=" + gameServerId +
                ", exceptionInfo=" + exceptionInfo +
                ", httpMessage=" + httpMessage +
                ", additionalInfo=" + additionalInfo +
                '}';
    }
}
