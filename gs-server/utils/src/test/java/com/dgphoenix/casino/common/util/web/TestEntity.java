package com.abs.casino.common.util.web;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;

/**
 * Created by mic on 22.11.14.
 */
public class TestEntity implements KryoSerializable {
    private String string;
    private Long aLong;
    private Boolean bool;
    private List list;


    public TestEntity() {
    }

    public TestEntity(String string, Long aLong, Boolean bool, List list) {
        this.string = string;
        this.aLong = aLong;
        this.bool = bool;
        this.list = list;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObjectOrNull(output, string, String.class);
        kryo.writeObjectOrNull(output, aLong, Long.class);
        kryo.writeObjectOrNull(output, bool, Boolean.class);
        kryo.writeClassAndObject(output, list);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        string = kryo.readObjectOrNull(input, String.class);
        aLong = kryo.readObjectOrNull(input, Long.class);
        bool = kryo.readObjectOrNull(input, Boolean.class);
        list = (List) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestEntity)) return false;

        TestEntity that = (TestEntity) o;

        if (aLong != null ? !aLong.equals(that.aLong) : that.aLong != null) return false;
        if (bool != null ? !bool.equals(that.bool) : that.bool != null) return false;
        if (list != null ? !list.equals(that.list) : that.list != null) return false;
        if (string != null ? !string.equals(that.string) : that.string != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = string != null ? string.hashCode() : 0;
        result = 31 * result + (aLong != null ? aLong.hashCode() : 0);
        result = 31 * result + (bool != null ? bool.hashCode() : 0);
        result = 31 * result + (list != null ? list.hashCode() : 0);
        return result;
    }
}
