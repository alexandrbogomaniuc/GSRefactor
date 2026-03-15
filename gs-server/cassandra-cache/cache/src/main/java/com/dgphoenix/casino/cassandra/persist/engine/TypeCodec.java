package com.abs.casino.cassandra.persist.engine;

import java.nio.ByteBuffer;

public final class TypeCodec {
    private static final com.datastax.driver.core.ProtocolVersion SERIALIZE_PROTOCOL_VERSION =
            com.datastax.driver.core.ProtocolVersion.NEWEST_SUPPORTED;

    private TypeCodec() {
    }

    public static ByteBuffer serializeAscii(String value) {
        return com.datastax.driver.core.TypeCodec.ascii().serialize(value, SERIALIZE_PROTOCOL_VERSION);
    }
}
