package com.dgphoenix.casino.common.util.compress;

import org.junit.Test;

import java.nio.ByteBuffer;

import static junit.framework.Assert.assertEquals;

/**
 * User: flsh
 * Date: 05.02.15.
 */
public class LZ4Test {

    @Test
    public void testCompressString() throws Exception {
        testCompressor("777777777777777777555555555511111111111111111111111177777777777777gggghcv");
    }

    @Test
    public void testCompressEmptyString() throws Exception {
        testCompressor("");
    }


    private void testCompressor(String testString) throws Exception {
        byte[] data = testString.getBytes("UTF-8");
        //System.out.println("data size=" + data.length);
        ByteBuffer sourceBuffer = ByteBuffer.allocateDirect(data.length);
        sourceBuffer.put(data);
        ByteBuffer destBuffer = com.dgphoenix.casino.common.util.LZ4Compressor.getInstance().compress(sourceBuffer);

        ByteBuffer uncompressedBuffer = com.dgphoenix.casino.common.util.LZ4Compressor.getInstance().
                uncompress(destBuffer);
        byte[] restored = new byte[uncompressedBuffer.remaining()];
        uncompressedBuffer.get(restored);
        //System.out.println("restored="+restored.length);

        String restoredString = new String(restored, "UTF-8");
        //System.out.println("restored=" + restoredString + "'");
        assertEquals(testString, restoredString);
    }
}
