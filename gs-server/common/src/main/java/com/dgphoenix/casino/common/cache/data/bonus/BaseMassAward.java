package com.abs.casino.common.cache.data.bonus;

import com.abs.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.abs.casino.common.cache.VersionedDistributedCacheEntry;
import com.abs.casino.common.cache.data.IDistributedConfigEntry;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * User: flsh
 * Date: 11.07.13
 */
public class BaseMassAward<T extends MassAwardBonusTemplate> extends VersionedDistributedCacheEntry
        implements IDistributedConfigEntry, Identifiable, KryoSerializable, JsonSelfSerializable<BaseMassAward> {
    private static final byte VERSION = 3;
    private long id;
    private String bankIds;
    private String accountIds;

    //newAccounts - list of pairs bankId/extUserId for new/nonexisting players: 1/zzz,2/aaa,3/bbb
    private String newAccounts;
    private MassAwardType type;

    private T template;
    private Double maxWinMultiplier;
    private Long maxWinLimit;

    private BonusStatus status;

    public BaseMassAward() {}

    public BaseMassAward(long id, MassAwardType type, T template, Double maxWinMultiplier) {
        this.id = id;
        this.type = type;
        this.template = template;
        this.maxWinMultiplier = maxWinMultiplier;

        status = BonusStatus.ACTIVE;
    }

    public long getId() {
        return id;
    }

    public T getTemplate() {
        return template;
    }

    public MassAwardType getType() {
        return type;
    }

    public List<Long> getBankIds() {
        return CollectionUtils.stringToListOfLongs(bankIds);
    }

    public void setBankIds(List<Long> idsList) {
        this.bankIds = CollectionUtils.listOfLongsToString(idsList);
    }

    public String getBankIdsAsString() {
        return bankIds;
    }

    public BonusStatus getStatus() {
        if (status == null) return BonusStatus.ACTIVE;

        return status;
    }

    public void setStatus(BonusStatus status) {
        this.status = status;
    }

    public List<Long> getAccountIds() {
        return CollectionUtils.stringToListOfLongs(accountIds);
    }

    public void setAccountIds(List<Long> idsList) {
        this.accountIds = CollectionUtils.listOfLongsToString(idsList);
    }

    public String getAccountIdsAsString() {
        return accountIds;
    }

    public boolean isContains(Long accountId) {
        if (accountId == null || accountIds == null) {
            return false;
        }
        String s = Long.toString(accountId);
        return accountIds.contains(s + ",") || accountIds.endsWith(s);
    }

    public boolean isNewAccountContains(int bankId, String extUserId) {
        if (extUserId == null || newAccounts == null) {
            return false;
        }
        String pair = String.valueOf(bankId) + "/" + extUserId;
        return newAccounts.contains(pair + ",") || newAccounts.endsWith(pair);
    }

    public BaseBonus getBonusByAccountId(AccountInfo account, long bonusId) {
        return getTemplate().createBonus(bonusId, account.getId(), account.getBankId(), id, null, maxWinMultiplier);
    }

    public boolean isExpired() {
        return getTemplate().getExpirationDate() != null &&
                getTemplate().getExpirationDate() < System.currentTimeMillis();
    }

    public boolean isContainAnyAccount(AccountInfo accountInfo) {
        return isContains(accountInfo.getId()) ||
                isNewAccountContains(accountInfo.getBankId(), accountInfo.getExternalId());
    }

    public boolean isPlayerSuitable(AccountInfo accountInfo) {

        boolean countryValid = getTemplate().getCountryCode() == null || (getTemplate().getCountryCode() != null &&
                getTemplate().getCountryCode().equalsIgnoreCase(accountInfo.getCountryCode()));
        return getStatus().equals(BonusStatus.ACTIVE) && countryValid &&
                (((type.equals(MassAwardType.CSV) || type.equals(MassAwardType.SELECTED)) &&
                        isContainAnyAccount(accountInfo)) ||
                        (type.equals(MassAwardType.ALL) &&
                                getTemplate().getTimeAwarded() > accountInfo.getRegisterTime()) ||
                        (type.equals(MassAwardType.NEWPLAYERS) &&
                                !accountInfo.isGuest() &&
                                getTemplate().getRegisteredFrom() <= accountInfo.getRegisterTime()));
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setBankIds(String bankIds) {
        this.bankIds = bankIds;
    }

    public void setAccountIds(String accountIds) {
        this.accountIds = accountIds;
    }

    public String getNewAccounts() {
        return newAccounts;
    }

    public void setNewAccounts(String newAccounts) {
        this.newAccounts = newAccounts;
    }

    public void setNewAccounts(Long bankId, Collection<String> accounts) {
        StringBuilder sb = new StringBuilder();
        for (String account : accounts) {
            sb.append(bankId).append("/").append(account).append(",");
        }
        this.newAccounts = sb.toString();
    }

    public void setType(MassAwardType type) {
        this.type = type;
    }

    public void setTemplate(T template) {
        this.template = template;
    }

    public Double getMaxWinMultiplier() {
        return maxWinMultiplier;
    }

    public void setMaxWinMultiplier(Double maxWinMultiplier) {
        this.maxWinMultiplier = maxWinMultiplier;
    }

    public void setMaxWinLimit(Long maxWinLimit) {
        this.maxWinLimit = maxWinLimit;
    }

    public Long getMaxWinLimit() {
        return maxWinLimit;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeString(bankIds);
        output.writeString(accountIds);
        output.writeString(newAccounts);
        output.writeString(type == null ? null : type.name());
        output.writeString(status == null ? null : status.name());
        kryo.writeClassAndObject(output, template);
        output.writeLong(getVersion(), true);
        kryo.writeObjectOrNull(output, maxWinMultiplier, Double.class);
        kryo.writeObjectOrNull(output, maxWinLimit, Long.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        bankIds = input.readString();
        accountIds = input.readString();
        newAccounts = input.readString();
        String s = input.readString();
        type = StringUtils.isTrimmedEmpty(s) ? null : MassAwardType.valueOf(s);
        s = input.readString();
        status = StringUtils.isTrimmedEmpty(s) ? null : BonusStatus.valueOf(s);
        template = (T) kryo.readClassAndObject(input);
        if (ver >= 1) {
            setVersion(input.readLong(true));
        }
        if (ver >= 2) {
            maxWinMultiplier = kryo.readObjectOrNull(input, Double.class);
        }
        if (ver >= 3) {
            maxWinLimit = kryo.readObjectOrNull(input, Long.class);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("id", id);
        gen.writeStringField("bankIds", bankIds);
        gen.writeStringField("accountIds", accountIds);
        gen.writeStringField("newAccounts", newAccounts);
        gen.writeStringField("type", type == null ? null : type.name());
        gen.writeStringField("status", status == null ? null : status.name());
        gen.writeObjectField("template", template);
        gen.writeNumberField("version", getVersion());
        serializeNumberOrNull(gen, "maxWinMultiplier", maxWinMultiplier);
        serializeNumberOrNull(gen, "maxWinLimit", maxWinLimit);
    }

    @Override
    public BaseMassAward deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode n = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        id = n.get("id").longValue();
        bankIds = n.get("bankIds").textValue();
        accountIds = n.get("accountIds").textValue();
        newAccounts = n.get("newAccounts").textValue();
        String typeStr =  n.get("type").textValue();
        type = StringUtils.isTrimmedEmpty(typeStr) ? null : MassAwardType.valueOf(typeStr);
        String statusStr = n.get("status").textValue();
        status = StringUtils.isTrimmedEmpty(statusStr) ? null : BonusStatus.valueOf(statusStr);
        template = (T) om.convertValue(n.get("template"), MassAwardBonusTemplate.class);
        setVersion(n.get("version").longValue());
        maxWinMultiplier = deserializeOrNull(om, n.get("maxWinMultiplier"), Double.class);
        maxWinLimit = deserializeOrNull(om, n.get("maxWinLimit"), Long.class);
        return this;
    }

    @Override
    public String toString() {
        return "BaseMassAward [" +
                "id=" + id +
                ", bankIds='" + bankIds + '\'' +
                ", accountIds='" + accountIds + '\'' +
                ", newAccounts='" + newAccounts + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", template=" + template +
                ", maxWinMultiplier=" + maxWinMultiplier +
                ", maxWinLimit=" + maxWinLimit +
                ']';
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        BaseMassAward fromCopy = (BaseMassAward) entry;
        this.bankIds = fromCopy.getBankIdsAsString();
        this.accountIds = fromCopy.getAccountIdsAsString();
        this.newAccounts = fromCopy.getNewAccounts();
        this.type = fromCopy.getType();
        this.status = fromCopy.getStatus();
        this.template = (T) fromCopy.getTemplate();
        this.maxWinMultiplier = ((BaseMassAward) entry).getMaxWinMultiplier();
        this.maxWinLimit = ((BaseMassAward) entry).getMaxWinLimit();
    }
}
