package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IBuyInConfirmedSeats;
import com.betsoft.casino.utils.TObject;

import java.util.List;

public class BuyInConfirmedSeats extends TObject implements IBuyInConfirmedSeats {
    private List<Integer> confirmedSeatsId;

    public BuyInConfirmedSeats(long date, int rid, List<Integer> confirmedSeatsId) {
        super(date, rid);
        this.confirmedSeatsId = confirmedSeatsId;
    }

    @Override
    public List<Integer> getConfirmedSeatsId() {
        return confirmedSeatsId;
    }

    @Override
    public String toString() {
        return "BuyInConfirmedSeats{" +
                "confirmedSeatsId=" + confirmedSeatsId +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
