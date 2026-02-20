package com.dgphoenix.casino.gamecombos;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: flsh
 * Date: 06.02.17.
 */
public class ReelDetails implements KryoSerializable {
    private static final byte VERSION = 1;
    private Icon[][] reels;
    //may be second reels state (wild icon transfered to other icon), for example see Dr.Jekyll/Mr.Hyde
    private Icon[][] reels2;
    @Deprecated
    private List<String> bonuses;
    private List<ComboFeature> comboFeatures = new ArrayList<ComboFeature>();

    public ReelDetails() {
    }

    public ReelDetails(Icon[][] reels, List<ComboFeature> comboFeatures) {
        this.reels = reels;
        this.comboFeatures = comboFeatures;
    }

    public Icon[][] getReels() {
        return reels;
    }

    public void setReels(Icon[][] reels) {
        this.reels = reels;
    }

    public Icon[][] getReels2() {
        return reels2;
    }

    public void setReels2(Icon[][] reels2) {
        this.reels2 = reels2;
    }

    public List<String> getBonuses() {
        return bonuses;
    }

    public void setBonuses(List<String> bonuses) {
        this.bonuses = bonuses;
    }

    public List<ComboFeature> getComboFeatures() {
        return comboFeatures;
    }

    public void setComboFeatures(List<ComboFeature> comboFeatures) {
        this.comboFeatures = comboFeatures;
    }

    public int getIconsCount(Icon icon) {
        return getIconsCount(icon.getName(), reels) + getIconsCount(icon.getName(), reels2);
    }

    public int getIconsCount(String iconName) {
        return getIconsCount(iconName, reels) + getIconsCount(iconName, reels2);
    }

    private int getIconsCount(String iconName, Icon[][] r) {
        if(r == null || r.length == 0) {
            return 0;
        }
        int count = 0;
        for (Icon[] line : r) {
            if(line == null || line.length == 0) {
                continue;
            }
            for (Icon reelIcon : line) {
                if(iconName.equalsIgnoreCase(reelIcon.getName())) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean hasBonus(String bonus) {
        return bonus != null && bonuses.contains(bonus);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, reels);
        kryo.writeClassAndObject(output, reels2);
        kryo.writeClassAndObject(output, bonuses);
        kryo.writeClassAndObject(output, comboFeatures);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        reels = (Icon[][]) kryo.readClassAndObject(input);
        reels2 = (Icon[][]) kryo.readClassAndObject(input);
        bonuses = (List<String>) kryo.readClassAndObject(input);
        if (ver > 0) {
            comboFeatures = (List<ComboFeature>) kryo.readClassAndObject(input);
        }
    }

    @Override
    public String toString() {
        return "ReelDetails[" +
                "reels=" + Arrays.deepToString(reels) +
                ", reels2=" + Arrays.deepToString(reels2) +
                ", bonuses=" + bonuses +
                ", comboFeatures="+comboFeatures +
                ']';
    }
}
