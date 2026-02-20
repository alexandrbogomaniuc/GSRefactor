package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IAvatar;

import java.io.Serializable;
import java.util.Objects;

public class Avatar implements IAvatar, Serializable {
    private int borderStyle;
    private int hero;
    private int background;

    public Avatar() {
    }

    public Avatar(int borderStyle, int hero, int background) {
        this.borderStyle = borderStyle;
        this.hero = hero;
        this.background = background;
    }

    @Override
    public int getBorderStyle() {
        return borderStyle;
    }

    @Override
    public void setBorderStyle(int borderStyle) {
        this.borderStyle = borderStyle;
    }

    @Override
    public int getHero() {
        return hero;
    }

    @Override
    public void setHero(int hero) {
        this.hero = hero;
    }

    @Override
    public int getBackground() {
        return background;
    }

    @Override
    public void setBackground(int background) {
        this.background = background;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Avatar avatar = (Avatar) o;
        return borderStyle == avatar.borderStyle &&
                hero == avatar.hero &&
                background == avatar.background;
    }

    @Override
    public int hashCode() {
        return Objects.hash(borderStyle, hero, background);
    }

    @Override
    public String toString() {
        return "Avatar[" +
                "borderStyle=" + borderStyle +
                ", hero=" + hero +
                ", background=" + background +
                ']';
    }
}
