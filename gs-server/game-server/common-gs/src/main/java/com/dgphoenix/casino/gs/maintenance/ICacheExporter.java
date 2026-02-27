package com.abs.casino.gs.maintenance;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * User: flsh
 * Date: 20.02.13
 */
public interface ICacheExporter {
    void export(ObjectOutputStream outStream) throws IOException;
}
