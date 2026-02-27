package com.abs.casino.common.cache;

import com.thoughtworks.xstream.XStream;

import java.io.IOException;

/**
 * User: flsh
 * Date: 02.09.14.
 */
public interface MultiThreadedExportableCache {
    void exportEntries(final XStream xStream, String outFile) throws IOException;
}
