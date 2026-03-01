package com.abs.casino.common.transport;

import com.abs.casino.common.transport.ITransportObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 03.06.17.
 */
public abstract class TObject implements ITransportObject, KryoSerializable, Serializable {
    public static final int SERVER_RID = -1;
    public static final int FREQUENCY_LIMIT = 500;
    protected long date;
    protected int rid;
    protected transient Byte version;

    public TObject() {}

    public TObject(long date, int rid) {
        this.date = date;
        this.rid = rid;
    }

    @Override
    public int getFrequencyLimit() {
        return FREQUENCY_LIMIT;
    }

    @Override
    public long getDate() {
        return date;
    }

    @Override
    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public int getRid() {
        return rid;
    }

    @Override
    public void setRid(int rid) {
        this.rid = rid;
    }

    protected abstract byte getVersion();

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(getVersion());
        output.writeLong(date, true);
        output.writeInt(rid);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        version = input.readByte();
        date = input.readLong(true);
        rid = input.readInt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TObject tObject = (TObject) o;

        if (date != tObject.date) return false;
        return rid == tObject.rid;

    }

    @Override
    public int hashCode() {
        int result = (int) (date ^ (date >>> 32));
        result = 31 * result + rid;
        return result;
    }

    //may be overrided for prevent class name collisions
    @Override
    @JsonProperty("class")
    public String getClassName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "TObject[" +
                "date=" + date +
                ", rid=" + rid +
                ']';
    }
}
