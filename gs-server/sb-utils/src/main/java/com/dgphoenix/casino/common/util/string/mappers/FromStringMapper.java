package com.dgphoenix.casino.common.util.string.mappers;

public interface FromStringMapper<R> {
    R parse(String raw);
}
