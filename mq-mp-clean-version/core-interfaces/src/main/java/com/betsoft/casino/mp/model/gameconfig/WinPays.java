package com.betsoft.casino.mp.model.gameconfig;

public class WinPays extends Pays{
    int payout;

    public WinPays(int payout) {
        this.payout = payout;
    }

    public int getPayout() {
        return payout;
    }

    public void setPayout(int payout) {
        this.payout = payout;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WinPays{");
        sb.append("payout=").append(payout);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int getRandomPay() {
        return payout;
    }
}
