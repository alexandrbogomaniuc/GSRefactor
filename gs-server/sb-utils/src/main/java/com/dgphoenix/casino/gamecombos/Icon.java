package com.abs.casino.gamecombos;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * User: flsh
 * Date: 06.02.17.
 */
@XStreamAlias("icon")
public class Icon implements KryoSerializable {
    private String name;
    private int index;

    public Icon() {
    }

    public Icon(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(index);
        output.writeString(name);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        index = input.readInt();
        name = input.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Icon icon = (Icon) o;

        return name.equals(icon.name);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Icon[" +
                "name='" + name + '\'' +
                ", index=" + index +
                ']';
    }
}
