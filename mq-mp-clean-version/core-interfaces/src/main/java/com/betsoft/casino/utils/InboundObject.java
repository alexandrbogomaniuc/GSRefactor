package com.betsoft.casino.utils;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface InboundObject extends ITransportObject {
    long getInboundDate();

    void setInboundDate(long inboundDate);
}
