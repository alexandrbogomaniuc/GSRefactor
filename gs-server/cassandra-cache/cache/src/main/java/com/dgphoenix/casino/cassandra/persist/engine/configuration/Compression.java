package com.dgphoenix.casino.cassandra.persist.engine.configuration;

import com.datastax.driver.core.schemabuilder.CompressionOptions3;
import com.datastax.driver.core.schemabuilder.TableOptions;
import com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 25.05.16
 */
public enum Compression {

    NONE(new CompressionOptions3(TableOptions.CompressionOptions.Algorithm.NONE)),
    CLIENT(new CompressionOptions3(TableOptions.CompressionOptions.Algorithm.NONE)),
    DEFLATE(new CompressionOptions3(TableOptions.CompressionOptions.Algorithm.DEFLATE)),
    LZ4(new CompressionOptions3(TableOptions.CompressionOptions.Algorithm.LZ4)),
    SNAPPY(new CompressionOptions3(TableOptions.CompressionOptions.Algorithm.SNAPPY));

    private final CompressionOptions compressionOptions;

    Compression(CompressionOptions compressionOptions) {
        this.compressionOptions = compressionOptions;
    }

    public CompressionOptions getCompressionOptions() {
        return compressionOptions;
    }
}