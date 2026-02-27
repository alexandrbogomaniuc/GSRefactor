package com.abs.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Error extends TObject {
    private static final byte VERSION = 0;

    private int code;
    private String msg;

    public Error() {}

    public Error(int code, String msg, long date) {
        super(date, -1);
        this.code = code;
        this.msg = msg;
    }

    public Error(int code, String msg, long date, int rid) {
        super(date, rid);
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeInt(code, true);
        output.writeString(msg);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        code = input.readInt(true);
        msg = input.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Error error = (Error) o;

        if (code != error.code) return false;
        if (date != error.date) return false;
        return msg.equals(error.msg);

    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + (int) (date ^ (date >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Error[" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }
}
