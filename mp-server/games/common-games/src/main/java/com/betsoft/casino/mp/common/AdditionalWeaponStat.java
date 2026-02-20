package com.betsoft.casino.mp.common;

import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Objects;

public class AdditionalWeaponStat implements KryoSerializable, 
        JsonSelfSerializable<AdditionalWeaponStat> {
    private static final byte VERSION = 0;
    private int numberOfRealShots;
    private int numberOfHits;
    private int numberOfMiss;
    private int numberOfKilledMiss;
    private int numberOfCompensateHits;
    private int numberOfMathHits;

    public AdditionalWeaponStat() {}

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(numberOfRealShots, true);
        output.writeInt(numberOfHits, true);
        output.writeInt(numberOfMiss, true);
        output.writeInt(numberOfKilledMiss, true);
        output.writeInt(numberOfCompensateHits, true);
        output.writeInt(numberOfMathHits, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        numberOfRealShots = input.readInt(true);
        numberOfHits = input.readInt(true);
        numberOfMiss = input.readInt(true);
        numberOfKilledMiss = input.readInt(true);
        numberOfCompensateHits = input.readInt(true);
        numberOfMathHits = input.readInt(true);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeNumberField("numberOfRealShots", numberOfRealShots);
        gen.writeNumberField("numberOfHits", numberOfHits);
        gen.writeNumberField("numberOfMiss", numberOfMiss);
        gen.writeNumberField("numberOfKilledMiss", numberOfKilledMiss);
        gen.writeNumberField("numberOfCompensateHits", numberOfCompensateHits);
        gen.writeNumberField("numberOfMathHits", numberOfMathHits);


    }

    @Override
    public AdditionalWeaponStat deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode n = p.getCodec().readTree(p);

        numberOfRealShots = n.get("numberOfRealShots").intValue();
        numberOfHits = n.get("numberOfHits").asInt();
        numberOfMiss = n.get("numberOfMiss").asInt();
        numberOfKilledMiss = n.get("numberOfKilledMiss").asInt();
        numberOfCompensateHits = n.get("numberOfCompensateHits").asInt();
        numberOfMathHits = n.get("numberOfMathHits").asInt();

        return this;
    }

    public void addValues(int numberOfRealShots, int numberOfHits, int numberOfMiss, int numberOfKilledMiss,
                          int numberOfCompensateHits, int numberOfMathHits) {
        this.numberOfRealShots += numberOfRealShots;
        this.numberOfHits += numberOfHits;
        this.numberOfMiss += numberOfMiss;
        this.numberOfKilledMiss += numberOfKilledMiss;
        this.numberOfCompensateHits += numberOfCompensateHits;
        this.numberOfMathHits += numberOfMathHits;
    }

    public int getNumberOfMathHits() {
        return numberOfMathHits;
    }

    public void setNumberOfMathHits(int numberOfMathHits) {
        this.numberOfMathHits = numberOfMathHits;
    }

    public int getNumberOfCompensateHits() {
        return numberOfCompensateHits;
    }

    public void setNumberOfCompensateHits(int numberOfCompensateHits) {
        this.numberOfCompensateHits = numberOfCompensateHits;
    }

    public void addNumberOfCompensateHits(int cnt) {
        numberOfCompensateHits += cnt;
    }

    public void addNumberOfRealShots(int cnt) {
        numberOfRealShots += cnt;
    }

    public void addNumberOfHits(int cnt) {
        numberOfHits += cnt;
    }

    public void addNumberOfMathHits(int cnt) {
        numberOfMathHits += cnt;
    }

    public void addNumberOfMiss(int cnt) {
        numberOfMiss += cnt;
    }

    public void addNumberOfKilledMiss(int cnt) {
        numberOfKilledMiss += cnt;
    }

    public int getNumberOfRealShots() {
        return numberOfRealShots;
    }

    public void setNumberOfRealShots(int numberOfRealShots) {
        this.numberOfRealShots = numberOfRealShots;
    }

    public int getNumberOfHits() {
        return numberOfHits;
    }

    public void setNumberOfHits(int numberOfHits) {
        this.numberOfHits = numberOfHits;
    }

    public int getNumberOfMiss() {
        return numberOfMiss;
    }

    public void setNumberOfMiss(int numberOfMiss) {
        this.numberOfMiss = numberOfMiss;
    }

    public int getNumberOfKilledMiss() {
        return numberOfKilledMiss;
    }

    public void setNumberOfKilledMiss(int numberOfKilledMiss) {
        this.numberOfKilledMiss = numberOfKilledMiss;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdditionalWeaponStat that = (AdditionalWeaponStat) o;
        return numberOfRealShots == that.numberOfRealShots &&
                numberOfHits == that.numberOfHits &&
                numberOfMiss == that.numberOfMiss &&
                numberOfKilledMiss == that.numberOfKilledMiss;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfRealShots, numberOfHits, numberOfMiss, numberOfKilledMiss);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AdditionalWeaponStat{");
        sb.append("numberOfRealShots=").append(numberOfRealShots);
        sb.append(", numberOfHits=").append(numberOfHits);
        sb.append(", numberOfMiss=").append(numberOfMiss);
        sb.append(", numberOfKilledMiss=").append(numberOfKilledMiss);
        sb.append(", numberOfCompensateHits=").append(numberOfCompensateHits);
        sb.append(", numberOfMathHits=").append(numberOfMathHits);
        sb.append('}');
        return sb.toString();
    }

}
