package com.abs.casino.common.util.string.mappers;

public interface FromStringMapper<R> {
    R parse(String raw);
}
