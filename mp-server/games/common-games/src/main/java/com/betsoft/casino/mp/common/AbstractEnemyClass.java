package com.betsoft.casino.mp.common;

import java.io.IOException;

import com.betsoft.casino.mp.model.IEnemyClass;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 08.02.19.
 */
public abstract class AbstractEnemyClass<EC extends IEnemyClass> implements IEnemyClass<EC> {
    private static final byte VERSION = 0;
    protected long id;
    private short width;
    private short height;
    private String name;
    private double energy;
    private float speed;

    public AbstractEnemyClass() {}

    public AbstractEnemyClass(long id, short width, short height, String name, double energy, float speed) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.name = name;
        this.energy = energy;
        this.speed = speed;
    }

    @Override
    public long getId() {
        return id;
    }

    public short getWidth() {
        return width;
    }

    public short getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractEnemyClass that = (AbstractEnemyClass) o;

        if (id != that.id) return false;
        if (width != that.width) return false;
        if (height != that.height) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) width;
        result = 31 * result + (int) height;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("");
        sb.append(", id=").append(id);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", name='").append(name).append('\'');
        sb.append(", energy=").append(energy);
        sb.append(", speed=").append(speed);
        return sb.toString();
    }

    protected void writeInheritorFields(Kryo kryo, Output output) {
        //nop by default.
    }

    protected void redInheritorFields(byte version, Kryo kryo, Input input) {
        //nop by default.
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeShort(width);
        output.writeShort(height);
        output.writeString(name);
        output.writeDouble(energy);
        output.writeFloat(speed);
        writeInheritorFields(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        id = input.readLong(true);
        width = input.readShort();
        height = input.readShort();
        name = input.readString();
        energy = input.readDouble();
        speed = input.readFloat();
        redInheritorFields(version, kryo, input);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeNumberField("id", id);
        gen.writeNumberField("width", width);
        gen.writeNumberField("height", height);
        gen.writeStringField("name", name);
        gen.writeNumberField("energy", energy);
        gen.writeNumberField("speed", speed);
        serializeInheritorFields(gen, serializers);


    }

    protected abstract void serializeInheritorFields(JsonGenerator gen,
                                                     SerializerProvider serializers) throws IOException;

    @Override
    public EC deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        id = node.get("id").longValue();
        width = node.get("width").shortValue();
        height = node.get("height").shortValue();
        name = node.get("name").textValue();
        energy = node.get("energy").doubleValue();
        speed = node.get("speed").floatValue();
        deserializeInheritorFields(p, node, ctxt);

        return getDeserialized();
    }

    protected abstract EC getDeserialized();

    protected abstract void deserializeInheritorFields(JsonParser p,
                                                       JsonNode node,
                                                       DeserializationContext ctxt);

}
