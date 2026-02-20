package com.betsoft.casino.mp.bgdragonstone;

import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType;

public class TestParam {
    private long cnt;
    private boolean canBuyWeapon;
    private boolean canShotFromSpecialWeapon;
    private EnemyType enemyType;
    private double probBuySpecialWeapon;

    public TestParam(long cnt, boolean canBuyWeapon, boolean canShotFromSpecialWeapon, EnemyType enemyType) {
        this.cnt = cnt;
        this.canBuyWeapon = canBuyWeapon;
        this.canShotFromSpecialWeapon = canShotFromSpecialWeapon;
        this.enemyType = enemyType;
        this.probBuySpecialWeapon = -1;
    }



    public TestParam(long cnt, boolean canBuyWeapon, boolean canShotFromSpecialWeapon, EnemyType enemyType,
                     double probBuySpecialWeapon) {
        this.cnt = cnt;
        this.canBuyWeapon = canBuyWeapon;
        this.canShotFromSpecialWeapon = canShotFromSpecialWeapon;
        this.enemyType = enemyType;
        this.probBuySpecialWeapon = probBuySpecialWeapon;
    }

    public double getProbBuySpecialWeapon() {
        return probBuySpecialWeapon;
    }

    public long getCnt() {
        return cnt;
    }

    public boolean isCanBuyWeapon() {
        return canBuyWeapon;
    }

    public boolean isCanShotFromSpecialWeapon() {
        return canShotFromSpecialWeapon;
    }

    public EnemyType getEnemyType() {
        return enemyType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestParam{");
        sb.append("cnt=").append(cnt);
        sb.append(", canBuyWeapon=").append(canBuyWeapon);
        sb.append(", canShotFromSpecialWeapon=").append(canShotFromSpecialWeapon);
        sb.append(", probBuySpecialWeapon=").append(probBuySpecialWeapon);
        sb.append(", enemyType=").append(enemyType.getName());
        sb.append('}');
        return sb.toString();
    }
}
