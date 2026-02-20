package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 03.06.17.
 */
public class Ok extends TObject {

    public Ok(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "Ok[" +
                "date=" + date +
                ", rid=" + rid +
                ']';
    }
}
