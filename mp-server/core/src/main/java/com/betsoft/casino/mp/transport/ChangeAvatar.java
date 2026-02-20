package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 16.07.18.
 */
public class ChangeAvatar extends TInboundObject {
    private int borderStyle;
    private int hero;
    private int background;

    public ChangeAvatar(long date, int rid, int borderStyle, int hero, int background) {
        super(date, rid);
        this.borderStyle = borderStyle;
        this.hero = hero;
        this.background = background;
    }

    public int getBorderStyle() {
        return borderStyle;
    }

    public void setBorderStyle(int borderStyle) {
        this.borderStyle = borderStyle;
    }

    public int getHero() {
        return hero;
    }

    public void setHero(int hero) {
        this.hero = hero;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeAvatar [");
        sb.append("borderStyle=").append(borderStyle);
        sb.append(", hero=").append(hero);
        sb.append(", background=").append(background);
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
