package com.betsoft.casino.mp.bgdragonstone.model.math.slot;

import com.betsoft.casino.mp.model.ISpinResult;

import java.util.List;

public class SpinResult implements ISpinResult {
    private final List<Integer> positions;
    private final int payment;

    public SpinResult(List<Integer> positions, int payment) {
        this.positions = positions;
        this.payment = payment;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public int getPayment() {
        return payment;
    }

    @Override
    public String toString() {
        return "SpinResult{" +
                "positions=" + positions +
                ", payment=" + payment +
                '}';
    }
}
