package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IRoundFinishSoon;
import com.betsoft.casino.utils.TObject;

public class RoundFinishSoon extends TObject implements IRoundFinishSoon {

    public RoundFinishSoon(long date) {
        super(date, SERVER_RID);
    }
}
