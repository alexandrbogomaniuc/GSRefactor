package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 03.08.18.
 */
public class RefreshBalance extends TInboundObject {
    private int clientAmmo;

    public RefreshBalance(long date, int rid, int clientAmmo) {
        super(date, rid);
        this.clientAmmo = clientAmmo;
    }

    public int getClientAmmo() {
        return clientAmmo;
    }

    public void setClientAmmo(int clientAmmo) {
        this.clientAmmo = clientAmmo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RefreshBalance [");
        sb.append("date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(", clientAmmo=").append(clientAmmo);
        sb.append(']');
        return sb.toString();
    }
}
