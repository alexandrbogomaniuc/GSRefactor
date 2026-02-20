package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IWeaponLootBox;
import com.betsoft.casino.utils.TObject;

public class StubWeaponLootBox extends TObject implements IWeaponLootBox {
    private int weaponId;
    private int shots;
    private long balance;
    private float currentWin;
    private int usedAmmoAmount;

    public StubWeaponLootBox(long date, int rid, int weaponId, int shots, long balance, float currentWin, int usedAmmoAmount) {
        super(date, rid);
        this.weaponId = weaponId;
        this.shots = shots;
        this.balance = balance;
        this.currentWin = currentWin;
        this.usedAmmoAmount = usedAmmoAmount;
    }

    @Override
    public int getWeaponId() {
        return weaponId;
    }

    @Override
    public int getShots() {
        return shots;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    @Override
    public float getCurrentWin() {
        return currentWin;
    }

    @Override
    public String toString() {
        return "WeaponLootBox[" +
                "weaponId=" + weaponId +
                ", shots=" + shots +
                ", balance=" + balance +
                ", currentWin=" + currentWin +
                ", usedAmmoAmount=" + usedAmmoAmount +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
