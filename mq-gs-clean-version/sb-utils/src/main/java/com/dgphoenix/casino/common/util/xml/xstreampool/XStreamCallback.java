package com.dgphoenix.casino.common.util.xml.xstreampool;

import com.thoughtworks.xstream.XStream;

/**
 * User: flsh
 * Date: 06.12.14.
 */
public interface XStreamCallback<T> {
    T execute(XStream xstream);
}
