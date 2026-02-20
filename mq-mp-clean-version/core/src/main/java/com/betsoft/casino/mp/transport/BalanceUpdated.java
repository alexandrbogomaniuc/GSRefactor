package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IBalanceUpdated;
import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 12.07.18.
 */
public class BalanceUpdated extends TInboundObject implements IBalanceUpdated {
    private long balance;
    private int serverAmmo;

    public BalanceUpdated(long date, long balance, int serverAmmo) {
        super(date, SERVER_RID);
        this.balance = balance;
        this.serverAmmo = serverAmmo;
    }

    public BalanceUpdated(long date, int rid, long balance, int serverAmmo) {
        super(date, rid);
        this.balance = balance;
        this.serverAmmo = serverAmmo;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public int getServerAmmo() {
        return serverAmmo;
    }

    public void setServerAmmo(int serverAmmo) {
        this.serverAmmo = serverAmmo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BalanceUpdated [");
        sb.append("balance=").append(balance);
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(", serverAmmo=").append(serverAmmo);
        sb.append(']');
        return sb.toString();
    }
}
