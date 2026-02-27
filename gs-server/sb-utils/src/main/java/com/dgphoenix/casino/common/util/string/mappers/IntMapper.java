package com.abs.casino.common.util.string.mappers;

public class IntMapper implements FromStringMapper<Integer> {

    @Override
    public Integer parse(String raw) {
        return Integer.parseInt(raw);
    }
}
