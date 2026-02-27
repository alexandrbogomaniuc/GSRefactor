package com.abs.casino.common.util.string.mappers;

public class DoubleMapper implements FromStringMapper<Double> {

    @Override
    public Double parse(String raw) {
        return Double.parseDouble(raw);
    }
}
