package com.betsoft.casino.mp.model.movement;

import com.abs.casino.common.util.RNG;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PossiblePoints implements KryoSerializable, Serializable {
    List<List<Point>> listPoints;

    public PossiblePoints() {}

    public PossiblePoints(List<List<Point>> listPoints) {
        this.listPoints = listPoints;
    }

    public List<List<Point>> getListPoints() {
        return listPoints;
    }

    public void setListPoints(List<List<Point>> listPoints) {
        this.listPoints = listPoints;
    }

    public List<Point> getRandomPossiblePoints(){
        return  listPoints == null ? null : listPoints.get(RNG.nextInt(listPoints.size()));
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output, listPoints);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        listPoints = kryo.readObject(input, ArrayList.class);
    }
}
