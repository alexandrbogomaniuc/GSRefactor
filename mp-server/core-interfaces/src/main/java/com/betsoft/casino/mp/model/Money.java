package com.betsoft.casino.mp.model;

import com.abs.casino.common.cache.JsonSelfSerializable;
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
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public class Money implements KryoSerializable, Serializable, Comparable<Money>, JsonSelfSerializable<Money> {
    private static final byte VERSION = 0;
    private static final long SCALE = 1_000_000L;

    public static final Money ZERO = new Money(0);
    public static final Money INVALID = new Money(-1);
    public static final Money BG_STAKE = Money.fromCents(100);

    private long value;

    public Money() {}

    public Money(long value) {
        this.value = value;
    }

    public Money add(Money other) {
        return new Money(this.value + other.value);
    }

    public Money subtract(Money other) {
        return new Money(this.value - other.value);
    }

    public Money multiply(Money other) {
        return new Money(this.value * other.toCents());
    }

    public Money multiply(long multiplier) {
        return new Money(multiplier * value);
    }

    public Money multiply(double multiplier) {
        return new Money((long) (this.value * multiplier));
    }

    public Money divideBy(long divider) {
        return new Money(this.value / divider);
    }

    public long divideBy(Money other) {
        return this.value / other.value;
    }

    public Money floor() {
        return new Money(value - value % SCALE);
    }

    public long getValue() {
        return value;
    }

    public long toCents() {
        return value / SCALE;
    }
    // need recheck and confirm

    public long toCentsExact() {
        long copy = value;
        if (value % SCALE != 0) {
            copy = value - value % SCALE + SCALE;
        }
        return copy / SCALE;
    }

    @Deprecated
    public float toFloatCents() {
        return (new BigDecimal(value, MathContext.DECIMAL32)).
                divide(new BigDecimal(SCALE, MathContext.DECIMAL32)).floatValue();
    }

    public double toDoubleCents() {
        return (new BigDecimal(value, MathContext.DECIMAL32)).
                divide(new BigDecimal(SCALE, MathContext.DECIMAL32)).doubleValue();
    }



    public boolean greaterThan(Money other) {
        return this.value > other.value;
    }

    public boolean greaterOrEqualsTo(Money other) {
        return this.value >= other.value;
    }

    public boolean lessOrEqualsTo(Money other) {
        return this.value <= other.value;
    }

    public boolean smallerThan(Money other) {
        return this.value < other.value;
    }

    public boolean smallerOrEqualsTo(Money other) {
        return this.value <= other.value;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(value, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        value = input.readLong(true);
    }

    @Override
    public String toString() {
        return "Money[value=" + value + ']';
    }

    public static Money fromFloatCents(float cents) {
        return new Money((long) (cents * SCALE));
    }

    public Money getWithRate(double rate) {
        return new Money((long) (this.value * rate));
    }

    public Money getWithMultiplier(double multiplier) {
        return new Money((long) (new Money((long) (this.value * multiplier)).toDoubleCents()) * SCALE);
    }

    public static Money fromCents(long cents) {
        return new Money(cents * SCALE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return value == money.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(Money o) {
        return Long.compare(value, o.value);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("value", value);
    }

    @Override
    public Money deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        value = node.get("value").longValue();
        return this;
    }
}
