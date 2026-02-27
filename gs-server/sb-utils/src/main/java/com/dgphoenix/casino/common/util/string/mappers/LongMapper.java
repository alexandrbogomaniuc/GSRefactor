package com.abs.casino.common.util.string.mappers;

public class LongMapper implements FromStringMapper<Long> {

    @Override
    public Long parse(String raw) {
        return Long.parseLong(raw);
    }
}
