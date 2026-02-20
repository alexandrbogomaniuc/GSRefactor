package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IGetFullGameInfo;
import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class GetFullGameInfo extends TInboundObject implements IGetFullGameInfo {

    public GetFullGameInfo(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "GetFullGameInfo[" +
                "date=" + date +
                ", rid=" + rid +
                ']';
    }
}
