package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IEnemyResultPrize;
import com.betsoft.casino.mp.model.ITransportWeapon;
import com.betsoft.casino.mp.model.IWeapon;
import com.betsoft.casino.mp.model.Money;

import java.util.List;
import java.util.Objects;

public class EnemyResultPrize implements IEnemyResultPrize {
    private Long enemyId;
    private Money cachPrize;
    private List<ITransportWeapon> awardedWeapons;

    public EnemyResultPrize(Long enemyId, Money cachPrize, List<ITransportWeapon> awardedWeapons) {
        this.enemyId = enemyId;
        this.cachPrize = cachPrize;
        this.awardedWeapons = awardedWeapons;
    }

    @Override
    public Long getEnemyId() {
        return enemyId;
    }

    @Override
    public void setEnemyId(Long enemyId) {
        this.enemyId = enemyId;
    }

    @Override
    public Money getCachPrize() {
        return cachPrize;
    }

    @Override
    public void setCachPrize(Money cachPrize) {
        this.cachPrize = cachPrize;
    }

    @Override
    public List<ITransportWeapon> getAwardedWeapons() {
        return awardedWeapons;
    }

    @Override
    public void setAwardedWeapons(List<ITransportWeapon> awardedWeapons) {
        this.awardedWeapons = awardedWeapons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnemyResultPrize that = (EnemyResultPrize) o;
        return Objects.equals(enemyId, that.enemyId) &&
                Objects.equals(cachPrize, that.cachPrize) &&
                Objects.equals(awardedWeapons, that.awardedWeapons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enemyId, cachPrize, awardedWeapons);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyResultPrize{");
        sb.append("enemyId=").append(enemyId);
        sb.append(", cachPrize=").append(cachPrize);
        sb.append(", awardedWeapons=").append(awardedWeapons);
        sb.append('}');
        return sb.toString();
    }
}
