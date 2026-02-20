package com.dgphoenix.casino.common.util.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.Date;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 27.12.2019
 */
public class ExceptionInfo implements KryoSerializable {

    private static final byte VERSION = 0;

    private String className;
    private String message;
    private String stackTrace;
    private long time;

    public ExceptionInfo() {}

    public ExceptionInfo(Exception e, long time) {
        this.className = e.getClass().getName();
        this.message = e.getMessage();
        this.stackTrace = ExceptionUtils.getStackTrace(e);
        this.time = time;
    }

    public String getClassName() {
        return className;
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public long getTime() {
        return time;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(className);
        output.writeString(message);
        output.writeString(stackTrace);
        output.writeLong(time, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        className = input.readString();
        message = input.readString();
        stackTrace = input.readString();
        time = input.readLong(true);
    }

    @Override
    public String toString() {
        return "ExceptionInfo{" +
                "className='" + className + '\'' +
                ", message='" + message + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", time=" + new Date(time) +
                '}';
    }
}
