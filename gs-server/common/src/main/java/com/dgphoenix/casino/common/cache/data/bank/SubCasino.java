package com.abs.casino.common.cache.data.bank;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.data.IDistributedConfigEntry;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

/**
 * User: flsh
 * Date: 18.12.12
 */
public class SubCasino implements IDistributedConfigEntry, Identifiable, KryoSerializable {
    private static final int VERSION = 0;
    private long id;
    private String name;
    private long defaultBank;
    private List<Long> bankIds = new ArrayList<Long>();
    private List<String> domainNames = new ArrayList<String>();

    public SubCasino() {
    }

    public SubCasino(long id) {
        this.id = id;
    }

    public SubCasino(long id, String name, long defaultBank, List<Long> bankIds, List<String> domainNames) {
        this.id = id;
        this.name = name;
        this.defaultBank = defaultBank;
        this.bankIds = bankIds;
        this.domainNames = domainNames;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDefaultBank() {
        return defaultBank;
    }

    public void setDefaultBank(long defaultBank) {
        this.defaultBank = defaultBank;
    }

    public List<Long> getBankIds() {
        return bankIds;
    }

    public void setBankIds(List<Long> bankIds) {
        if (!(bankIds instanceof ArrayList)) {
            this.bankIds = new ArrayList<>(bankIds);
        } else {
            this.bankIds = bankIds;
        }
    }

    public void addBankId(long bankId) {
        if (!bankIds.contains(bankId)) {
            bankIds.add(bankId);
        }
    }

    public List<String> getDomainNames() {
        return domainNames;
    }

    public void setDomainNames(List<String> domainNames) {
        this.domainNames = domainNames;
    }

    public String getDomainNamesAsString() {
        if (domainNames == null) {
            domainNames = new ArrayList<>();
        }
        StringBuilder sb = new StringBuilder();
        for (String domainName : domainNames) {
            sb.append(domainName).append(";");
        }
        return sb.toString();
    }

    public synchronized void addDomainName(String domainName) {
        if (domainNames == null) {
            domainNames = new ArrayList<>();
        }
        if (!domainNames.contains(domainName)) {
            domainNames.add(domainName);
        }
    }

    public void removeDomainName(String domainName) {
        if (domainNames == null) {
            domainNames = new ArrayList<>();
        } else {
            domainNames.remove(domainName);
        }
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        SubCasino fromCopy = (SubCasino) entry;
        this.id = fromCopy.id;
        this.name = fromCopy.name;
        this.defaultBank = fromCopy.defaultBank;
        this.bankIds = fromCopy.bankIds == null || fromCopy.bankIds.isEmpty() ? new ArrayList<Long>() :
                new ArrayList(fromCopy.bankIds);
        this.domainNames = fromCopy.domainNames == null || fromCopy.domainNames.isEmpty() ? new ArrayList<String>() :
                new ArrayList(fromCopy.domainNames);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        output.writeLong(id, true);
        output.writeString(name == null ? "" : name);
        output.writeLong(defaultBank, true);
        kryo.writeClassAndObject(output, bankIds);
        kryo.writeClassAndObject(output, domainNames);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt(true);
        id = input.readLong(true);
        name = input.readString();
        defaultBank = input.readLong(true);
        bankIds = (List<Long>) kryo.readClassAndObject(input);
        domainNames = (List<String>) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubCasino subCasino = (SubCasino) o;
        return id == subCasino.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SubCasino");
        sb.append("[id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", domainNames=").append(getDomainNamesAsString());
        sb.append(", defaultBank=").append(defaultBank);
        sb.append(", bankIds=").append(bankIds);
        sb.append(']');
        return sb.toString();
    }

    public String getStaticDirectoryName() {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(defaultBank);
        return bankInfo.getStaticDirectoryName();
    }
}
