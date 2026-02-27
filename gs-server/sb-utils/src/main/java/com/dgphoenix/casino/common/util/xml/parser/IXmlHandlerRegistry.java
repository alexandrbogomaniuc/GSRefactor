package com.abs.casino.common.util.xml.parser;

import com.dgphoenix.casino.common.exception.ObjectNotFoundException;

public interface IXmlHandlerRegistry {

    IXmlHandler getXmlHandler(String name) throws ObjectNotFoundException;
    void register(String name, IXmlHandler handler);
}
