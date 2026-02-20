package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.InboundObject;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ISitIn extends InboundObject {
    String getLang();

    void setLang(String lang);

    long getStake();

    void setStake(long stake);
}
