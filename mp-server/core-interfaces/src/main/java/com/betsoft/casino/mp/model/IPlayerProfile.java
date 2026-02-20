package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.KryoSerializable;

import java.io.Serializable;
import java.util.Set;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IPlayerProfile extends KryoSerializable, Serializable {
    Set<Integer> getBorders();

    void setBorders(Set<Integer> borders);

    Set<Integer> getHeroes();

    void setHeroes(Set<Integer> heroes);

    Set<Integer> getBackgrounds();

    void setBackgrounds(Set<Integer> backgrounds);

    Integer getBorder();

    void setBorder(Integer border);

    Integer getHero();

    void setHero(Integer hero);

    Integer getBackground();

    void setBackground(Integer background);

    boolean isDisableTooltips();

    void setDisableTooltips(boolean disableTooltips);
}
