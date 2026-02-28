package com.abs.casino.common.transport;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface ITransportObject extends IdentifiableRequest {
    int getFrequencyLimit();

    long getDate();

    void setDate(long date);

    String getClassName();
}
