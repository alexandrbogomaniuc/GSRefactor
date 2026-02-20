package com.betsoft.casino.mp.model.gameconfig;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 19.07.19.
 */
public class BuySpecialWeapons {
    Map<String, List<Integer>> bullets;
    int limit;

    public BuySpecialWeapons(Map<String, List<Integer>> bullets, int limit) {
        this.bullets = bullets;
        this.limit = limit;
    }

    public Map<String, List<Integer>> getBullets() {
        return bullets;
    }

    public void setBullets(Map<String, List<Integer>> bullets) {
        this.bullets = bullets;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BuySpecialWeapons [");
        sb.append("bullets=").append(bullets);
        sb.append(", limit=").append(limit);
        sb.append(']');
        return sb.toString();
    }
}
