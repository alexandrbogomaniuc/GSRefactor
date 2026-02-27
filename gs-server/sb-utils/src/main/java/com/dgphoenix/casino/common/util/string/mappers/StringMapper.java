package com.abs.casino.common.util.string.mappers;

public class StringMapper implements FromStringMapper<String> {

    @Override
    public String parse(String raw) {
        return raw;
    }
}
