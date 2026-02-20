package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

public class CloseRoundResults extends TObject {
    public CloseRoundResults(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "CloseRoundResults[" +
                "date=" + date +
                ", rid=" + rid +
                ']';
    }
}
