package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ISeatWinForQuest;
import com.betsoft.casino.utils.TObject;

public class SeatWinForQuest extends TObject implements ISeatWinForQuest {
    private long seatId;
    private long enemyId;
    private long winAmount;
    private int awardedWeaponId;

    public SeatWinForQuest(long date, int rid, long seatId, long enemyId, long winAmount, int awardedWeaponId) {
        super(date, rid);
        this.seatId = seatId;
        this.enemyId = enemyId;
        this.winAmount = winAmount;
        this.awardedWeaponId = awardedWeaponId;
    }

    @Override
    public long getSeatId() {
        return seatId;
    }

    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }

    @Override
    public long getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(long enemyId) {
        this.enemyId = enemyId;
    }

    @Override
    public long getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(long winAmount) {
        this.winAmount = winAmount;
    }

    @Override
    public int getAwardedWeaponId() {
        return awardedWeaponId;
    }

    public void setAwardedWeaponId(int awardedWeaponId) {
        this.awardedWeaponId = awardedWeaponId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SeatWinForQuest{");
        sb.append("seatId=").append(seatId);
        sb.append(", enemyId=").append(enemyId);
        sb.append(", winAmount=").append(winAmount);
        sb.append(", awardedWeaponId=").append(awardedWeaponId);
        sb.append('}');
        return sb.toString();
    }
}
