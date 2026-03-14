package com.abs.casino.cassandra.persist.engine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractCassandraPersisterTest {

    @Test
    public void normalizeCompressionOptionsRewritesLegacyCompressionClass() {
        String query = "CREATE TABLE BetCF (SID bigint PRIMARY KEY) WITH compression = {'sstable_compression' : 'DeflateCompressor'}";

        String normalized = AbstractCassandraPersister.normalizeCompressionOptions(query);

        assertEquals(
                "CREATE TABLE BetCF (SID bigint PRIMARY KEY) WITH compression = {'class' : 'org.apache.cassandra.io.compress.DeflateCompressor'}",
                normalized
        );
    }

    @Test
    public void normalizeCompressionOptionsRenamesLegacyChunkLengthField() {
        String query = "CREATE TABLE BetCF (SID bigint PRIMARY KEY) WITH compression = {'sstable_compression' : 'LZ4Compressor', 'chunk_length_kb' : '64'}";

        String normalized = AbstractCassandraPersister.normalizeCompressionOptions(query);

        assertEquals(
                "CREATE TABLE BetCF (SID bigint PRIMARY KEY) WITH compression = {'class' : 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb' : '64'}",
                normalized
        );
    }
}
