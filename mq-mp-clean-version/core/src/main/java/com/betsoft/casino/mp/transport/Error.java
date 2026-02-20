package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IError;
import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 03.06.17.
 */
public class Error extends TObject implements IError {
    private int code;
    private String msg;

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

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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
}
