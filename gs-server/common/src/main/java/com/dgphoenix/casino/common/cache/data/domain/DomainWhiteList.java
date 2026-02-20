package com.dgphoenix.casino.common.cache.data.domain;


import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DomainWhiteList implements IDistributedConfigEntry, KryoSerializable {
    private static final byte VERSION = 0;
    private int gameId;
    private List<String> domainList = new ArrayList<String>();

    public DomainWhiteList() {

    }

    public DomainWhiteList(int gameId, List<String> whiteList) {
        this.gameId = gameId;
        this.domainList = whiteList;
    }

    public void addDomainIfAbsent(String domain) {
        if (domainList == null) {
            domainList = new ArrayList<String>();
        }
        if (!domainList.contains(domain)) {
            domainList.add(domain);
        }
    }

    public void removeDomain(String domain) {
        if (domainList != null) {
            domainList.remove(domain);
        }
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public List<String> getDomainList() {
        return domainList;
    }

    public void setDomainList(List<String> domainList) {
        if (this.domainList == null) {
            this.domainList = new ArrayList<String>();
        } else {
            this.domainList.clear();
        }
        this.domainList.addAll(domainList);
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        this.domainList = ((DomainWhiteList) entry).getDomainList();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DomainWhiteList");
        sb.append("[gameId=").append(gameId);
        sb.append(", white list domains=").append(Arrays.asList(domainList));
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(gameId);
        kryo.writeClassAndObject(output, domainList);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        gameId = input.readInt();
        domainList = (List<String>) kryo.readClassAndObject(input);
    }
}
