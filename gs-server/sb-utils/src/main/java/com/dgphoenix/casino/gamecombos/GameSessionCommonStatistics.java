package com.dgphoenix.casino.gamecombos;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;

public class GameSessionCommonStatistics implements KryoSerializable {
    private static final Logger LOG = LogManager.getLogger(GameSessionCommonStatistics.class);

    public static final byte VERSION = 0;
    private String sdKey;
    private Map<ComboFeature, Integer> comboFeatureStatistics = new EnumMap<ComboFeature, Integer>(ComboFeature.class);

    public GameSessionCommonStatistics() {}

    public GameSessionCommonStatistics(String sdKey) {
        this.sdKey = sdKey;
    }

    public GameSessionCommonStatistics(String sdKey, Map<ComboFeature, Integer> comboFeatureStatistics) {
        this.comboFeatureStatistics = comboFeatureStatistics;
        this.sdKey = sdKey;
    }

    public Map<ComboFeature, Integer> getComboFeatureStatistics() {
        return comboFeatureStatistics;
    }

    public String getSdKey() {
        return sdKey;
    }

    public void incrementComboFeatureStatistics(ComboFeature comboFeature, int hitCounts) {
        Integer oldHitCounts = comboFeatureStatistics.get(comboFeature);
        if (oldHitCounts == null) {
            oldHitCounts = 0;
        }
        comboFeatureStatistics.put(comboFeature, oldHitCounts + hitCounts);
    }

    @Override
    public String toString() {
        return "GameSessionCommonStatistics{" +
            "comboFeatureStatistics=" + comboFeatureStatistics +
            ", sdKey='" + sdKey + '\'' +
            '}';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(sdKey);
        kryo.writeObjectOrNull(output, comboFeatureStatistics, EnumMap.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        sdKey = input.readString();
        try {
            comboFeatureStatistics = kryo.readObjectOrNull(input, EnumMap.class);
        } catch (Exception e) {
            comboFeatureStatistics = null;
            LOG.warn("Previously serialized statistics", e);
        }
    }
}
