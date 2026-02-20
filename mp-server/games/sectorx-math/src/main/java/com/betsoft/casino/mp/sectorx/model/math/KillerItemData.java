package com.betsoft.casino.mp.sectorx.model.math;

public class KillerItemData {
    private EnemyType enemy;
    private int count;
    private int extraPayout;

    public KillerItemData(EnemyType enemy, int count, int extraPayout) {
        this.enemy = enemy;
        this.count = count;
        this.extraPayout = extraPayout;
    }

    public EnemyType getEnemy() {
        return enemy;
    }

    public void setEnemy(EnemyType enemy) {
        this.enemy = enemy;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getExtraPayout() {
        return extraPayout;
    }

    public void setExtraPayout(int extraPayout) {
        this.extraPayout = extraPayout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KillerItemData that = (KillerItemData) o;

        if (count != that.count) return false;
        if (extraPayout != that.extraPayout) return false;
        return enemy == that.enemy;
    }

    @Override
    public int hashCode() {
        int result = enemy != null ? enemy.hashCode() : 0;
        result = 31 * result + count;
        result = 31 * result + extraPayout;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("KillerItemData{");
        sb.append("enemy=").append(enemy.name());
        sb.append(", count=").append(count);
        sb.append(", extraPayout=").append(extraPayout);
        sb.append('}');
        return sb.toString();
    }
}
