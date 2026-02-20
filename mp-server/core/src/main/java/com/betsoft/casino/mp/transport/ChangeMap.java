package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IChangeMap;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;

/**
 * User: flsh
 * Date: 02.04.18.
 */
public class ChangeMap extends TObject implements IChangeMap {
    private int mapId;
    private String subround;

    public ChangeMap(long date, int mapId, String subround) {
        super(date, SERVER_RID);
        this.mapId = mapId;
        this.subround = subround;
    }

    @Override
    public int getMapId() {
        return mapId;
    }

    @Override
    public String getSubround() {
        return subround;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChangeMap changeMap = (ChangeMap) o;
        return mapId == changeMap.mapId &&
                Objects.equals(subround, changeMap.subround);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mapId, subround);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeMap [");
        sb.append("date=").append(date);
        sb.append(", rid=").append(getRid());
        sb.append(", mapId=").append(mapId);
        sb.append(", subround='").append(subround).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
