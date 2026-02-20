package com.betsoft.casino.mp.model;

import java.util.List;

public interface IShotResult {
    boolean isLastResult();

    int getServerAmmo();

    void setServerAmmo(int serverAmmo);

    Integer getFragmentId();
    void setFragmentId(Integer fragmentId);

    List<String> getEffects();
    void setEffects(List<String> effects);

    int getBossNumberShots();

    void setBossNumberShots(int bossNumberShots);
}
