package com.betsoft.casino.mp.model.gameconfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class WeaponPays extends Pays{
    Map<String, Map<String, String>> pays;

    public WeaponPays(Map<String, Map<String, String>> pays) {
        this.pays = pays;
    }

    public Map<String, Map<String, String>> getPays() {
        return pays;
    }

    public void setPays(Map<String, Map<String, String>> pays) {
        this.pays = pays;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeaponPays{");
        sb.append("pays=").append(pays);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int getRandomPay() {
        HashMap<Integer, Double> params = new HashMap<>();
        Optional<String> first = pays.keySet().stream().findFirst();
        Set<Map.Entry<String, String>> entries = pays.get(first.get()).entrySet();
        for (Map.Entry<String, String> entry : entries) {
            int key = Integer.parseInt(entry.getKey());
            double value = Double.parseDouble(entry.getValue());
            params.put(key, value);
        }
        return GameTools.getRandomNumberKeyFromMap(params);
    }
}

