package com.abs.casino.cassandra.persist.engine.configuration;


/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 25.05.16
 */
public enum Compression {

    NONE(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE)),
    CLIENT(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE)),
    DEFLATE(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.DEFLATE)),
    LZ4(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.LZ4)),
    SNAPPY(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.SNAPPY));

    private final com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions compressionOptions;

    Compression(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions compressionOptions) {
        this.compressionOptions = compressionOptions;
    }

    public com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions getCompressionOptions() {
        return compressionOptions;
    }
}
