package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IKingOfHillChanged;
import com.betsoft.casino.utils.TObject;

import java.util.List;
import java.util.Objects;

public class KingOfHillChanged extends TObject implements IKingOfHillChanged {
    private List<Integer> newKings;

    public KingOfHillChanged(long date, int rid, List<Integer> newKings) {
        super(date, rid);
        this.newKings = newKings;
    }

    @Override
    public List<Integer> getNewKings() {
        return newKings;
    }

    @Override
    public String toString() {
        return "KingOfHillChanged{" +
                "date=" + date +
                ", rid=" + rid +
                ", newKing=" + newKings +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        KingOfHillChanged that = (KingOfHillChanged) o;
        return newKings.equals(that.newKings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), newKings);
    }
}
