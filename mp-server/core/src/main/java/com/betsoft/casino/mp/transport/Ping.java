package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

public class Ping extends TObject {

    public Ping() {
        super(0, 0);
    }

    @Override
    public String toString() {
        return "Ping";
    }
}
