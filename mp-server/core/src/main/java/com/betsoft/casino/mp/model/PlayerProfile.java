package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Set;

/**
 * User: flsh
 * Date: 12.07.18.
 */
public class PlayerProfile implements IPlayerProfile {
    private static final byte VERSION = 0;

    //current
    private Integer border;
    private Integer hero;
    private Integer background;

    //purchased borders, heroes, backgrounds
    private Set<Integer> borders;
    private Set<Integer> heroes;
    private Set<Integer> backgrounds;

    private boolean disableTooltips;

    public PlayerProfile() {}

    public PlayerProfile(Set<Integer> borders, Set<Integer> heroes, Set<Integer> backgrounds, Integer border,
                         Integer hero, Integer background, boolean disableTooltips) {
        this.borders = borders;
        this.heroes = heroes;
        this.backgrounds = backgrounds;
        this.border = border;
        this.hero = hero;
        this.background = background;
        this.disableTooltips = disableTooltips;
    }

    @Override
    public Set<Integer> getBorders() {
        return borders;
    }

    @Override
    public void setBorders(Set<Integer> borders) {
        this.borders = borders;
    }

    @Override
    public Set<Integer> getHeroes() {
        return heroes;
    }

    @Override
    public void setHeroes(Set<Integer> heroes) {
        this.heroes = heroes;
    }

    @Override
    public Set<Integer> getBackgrounds() {
        return backgrounds;
    }

    @Override
    public void setBackgrounds(Set<Integer> backgrounds) {
        this.backgrounds = backgrounds;
    }

    @Override
    public Integer getBorder() {
        return border;
    }

    @Override
    public void setBorder(Integer border) {
        this.border = border;
    }

    @Override
    public Integer getHero() {
        return hero;
    }

    @Override
    public void setHero(Integer hero) {
        this.hero = hero;
    }

    @Override
    public Integer getBackground() {
        return background;
    }

    @Override
    public void setBackground(Integer background) {
        this.background = background;
    }

    @Override
    public boolean isDisableTooltips() {
        return disableTooltips;
    }

    @Override
    public void setDisableTooltips(boolean disableTooltips) {
        this.disableTooltips = disableTooltips;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(border, true);
        output.writeInt(hero, true);
        output.writeInt(background, true);
        kryo.writeClassAndObject(output, borders);
        kryo.writeClassAndObject(output, heroes);
        kryo.writeClassAndObject(output, backgrounds);
        output.writeBoolean(disableTooltips);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        border = input.readInt(true);
        hero = input.readInt(true);
        background = input.readInt(true);
        borders = (Set<Integer>) kryo.readClassAndObject(input);
        heroes = (Set<Integer>) kryo.readClassAndObject(input);
        backgrounds = (Set<Integer>) kryo.readClassAndObject(input);
        disableTooltips = input.readBoolean();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlayerProfile [");
        sb.append("borders=").append(borders);
        sb.append(", heroes=").append(heroes);
        sb.append(", backgrounds=").append(backgrounds);
        sb.append(", border=").append(border);
        sb.append(", hero=").append(hero);
        sb.append(", background=").append(background);
        sb.append(", disableTooltips=").append(disableTooltips);
        sb.append(']');
        return sb.toString();
    }
}
