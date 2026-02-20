package com.dgphoenix.casino.common.geoip;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Set;

public class CountryRestrictionList implements KryoSerializable {
    private static final byte VERSION = 0;
    private boolean white;
    private Set<String> countries;

    public CountryRestrictionList() {}

    public CountryRestrictionList(boolean white, Set<String> countries) {
        this.white = white;
        this.countries = countries;
    }

    public boolean isAllowed(String country) {
        if (countries == null || countries.isEmpty()) {
            return true;
        }
        return white ? countries.contains(country) : !countries.contains(country);
    }

    public boolean isWhite() {
        return white;
    }

    public void setWhite(boolean white) {
        this.white = white;
    }

    public Set<String> getCountries() {
        return countries;
    }

    public void setCountries(Set<String> countries) {
        this.countries = countries;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeBoolean(white);
        kryo.writeClassAndObject(output, countries);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        white = input.readBoolean();
        countries = (Set<String>)kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "CountryRestrictionList{" +
            "white=" + white +
            ", countries=" + countries +
            '}';
    }
}
