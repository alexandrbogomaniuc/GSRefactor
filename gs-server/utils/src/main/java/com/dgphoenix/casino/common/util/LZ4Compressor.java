package com.abs.casino.common.util;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * User: flsh
 * Date: 06.02.15.
 */
public class LZ4Compressor {
    private static final Logger LOG = LogManager.getLogger(LZ4Compressor.class);
    private static final LZ4Compressor instance = new LZ4Compressor();

    //limit is 100mb
    private final static int SIZE_LIMIT = 100 * 1024 * 1024;
    private LZ4Factory lz4Factory;

    private LZ4Compressor() {
        lz4Factory = LZ4Factory.nativeInstance();
        LOG.info("Using: " + lz4Factory.toString());
        if (!"LZ4Factory:JNI".equals(lz4Factory.toString())) {
            LOG.warn("Not using native implementation, may be reason for performance degradation");
        }
    }

    public static LZ4Compressor getInstance() {
        return instance;
    }

    //first 4 bytes in result ByteBuffer is uncompressed size (this need for fast uncompress)
    public ByteBuffer compress(ByteBuffer source) {
        assert source != null : "source is null";
        assert source.limit() < SIZE_LIMIT : ("too large buffer: " + source.limit());
        source.rewind();
        net.jpountz.lz4.LZ4Compressor compressor = lz4Factory.highCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(source.limit() + 4);
        ByteBuffer destBuffer = ByteBuffer.allocateDirect(maxCompressedLength);
        destBuffer.putInt(source.limit());
        compressor.compress(source, destBuffer);
        destBuffer.rewind();
        return destBuffer;
    }

    //first 4 bytes in source must be uncompressed length
    public ByteBuffer uncompress(ByteBuffer source) {
        assert source != null : "source is null";
        assert source.limit() >= 4;
        source.rewind();
        int uncompressedSize = source.getInt();
        assert uncompressedSize >= 0 : ("uncompressed size must be positive: " + uncompressedSize);
        assert uncompressedSize < SIZE_LIMIT : ("too large uncompressed size: " + uncompressedSize);
        LZ4FastDecompressor decompressor = lz4Factory.fastDecompressor();
        ByteBuffer destBuffer = ByteBuffer.allocateDirect(uncompressedSize);
        decompressor.decompress(source, destBuffer);
        destBuffer.rewind();
        source.rewind();
        return destBuffer;
    }
}
