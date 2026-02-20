package com.dgphoenix.casino.common.web;

import java.io.Serializable;

/**
 * User: flsh
 * Date: Dec 4, 2009
 */
public class IdValueBean implements Serializable, Comparable {
    private long id;
    private String value;

    public IdValueBean(long id, String value) {
        this.id = id;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("IdValueBean");
        sb.append("[id=").append(id);
        sb.append(", value='").append(value).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        IdValueBean other = (IdValueBean) o;
        return value.compareTo(other.getValue());
    }
}
