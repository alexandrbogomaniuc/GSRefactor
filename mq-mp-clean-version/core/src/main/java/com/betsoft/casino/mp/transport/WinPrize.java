package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IWinPrize;

import java.io.Serializable;
import java.util.Objects;

public class WinPrize implements IWinPrize, Serializable {
    private int id;
    private String value;

    public WinPrize(int id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WinPrize winPrize = (WinPrize) o;
        return id == winPrize.id &&
                Objects.equals(value, winPrize.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WinPrize{");
        sb.append("id=").append(id);
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
