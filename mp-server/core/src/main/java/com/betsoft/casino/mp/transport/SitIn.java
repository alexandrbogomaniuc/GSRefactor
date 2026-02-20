package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ISitIn;
import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 03.06.17.
 */
public class SitIn extends TInboundObject implements ISitIn {
    private long stake;

    // TODO: remove lang from this request after implementation of player info storage
    private String lang;

    public SitIn(long date, int rid, String lang) {
        super(date, rid);
        this.lang = lang;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public long getStake() {
        return stake;
    }

    @Override
    public void setStake(long stake) {
        this.stake = stake;
    }

    @Override
    public String toString() {
        return "SitIn [" +
                ", stake=" + stake +
                ", date=" + date +
                ", rid=" + rid +
                ", lang=" + lang +
                ']';
    }
}
