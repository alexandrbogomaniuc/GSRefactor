package com.betsoft.casino.mp.model.gameconfig;

import java.util.List;

public class Boss {
    double probability_to_trigger;
    int pays;
    List<BossSetting> settings;

    public double getProbability_to_trigger() {
        return probability_to_trigger;
    }

    public void setProbability_to_trigger(double probability_to_trigger) {
        this.probability_to_trigger = probability_to_trigger;
    }

    public int getPays() {
        return pays;
    }

    public void setPays(int pays) {
        this.pays = pays;
    }

    public List<BossSetting> getSettings() {
        return settings;
    }

    public void setSettings(List<BossSetting> settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Boss{");
        sb.append("probability_to_trigger=").append(probability_to_trigger);
        sb.append(", pays=").append(pays);
        sb.append(", settings=").append(settings);
        sb.append('}');
        return sb.toString();
    }
}
