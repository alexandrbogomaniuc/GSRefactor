package com.dgphoenix.casino.common.cache.data.account;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by ANGeL Date: Sep 17, 2008 Time: 6:03:59 PM
 */
public class AccountInfo implements IDistributedCacheEntry, IAccountInfo, KryoSerializable, JsonSelfSerializable<AccountInfo> {
    private static final Logger LOG = LogManager.getLogger(AccountInfo.class);
    public static final String BIRTH_DATE = "BIRTH_DATE";
    public static final String GENDER = "GENDER";
    /**
     * VERSION 1 changes:
     * -add field 'testUser'
     */
    private static final byte VERSION = 3;

    private long id;
    private String nickName;
    private String externalId;
    private int bankId;
    private short subCasinoId;
    private volatile long balance;
    private long freeBalance;
    private boolean guest;
    private boolean locked;
    private long registerTime;
    private long lastLoginTime;
    private Currency currency;
    private Currency currencyFraction;

    private long currentGameServer = -1;

    private String finsoftSessionId;
    private String smartLiveOperator;
    private Long accountUseId;
    private String agentId;
    private String email;
    private String firstName;
    private String lastName;
    private String password;

    //external sessionId -> SessionInfo.externalSessionId
    private String sessionKey;
    private PlayerGameSettings gameSettings;

    private String frbMassAwardIdsList;
    private String bonusMassAwardIdsList;
    private String countryCode;
    private boolean sendNewsletters;
    private long lastActivityTime = System.currentTimeMillis();
    private boolean testUser;
    private double unjContributionFractionalPart;

    public AccountInfo() {
        super();
    }

    public AccountInfo(long id, String externalId, int bankId, short subCasinoId, long registerTime, boolean guest,
                       boolean locked, Currency currency, String countryCode) {
        super();
        checkNotNull(currency, "currency must not be null");
        this.id = id;
        this.externalId = externalId;
        this.bankId = bankId;
        this.subCasinoId = subCasinoId;
        this.guest = guest;
        this.locked = locked;
        this.registerTime = registerTime;
        this.currency = currency;
        this.countryCode = countryCode;
    }

    public AccountInfo(long id, String externalId, int bankId, short subCasinoId, long registerTime, boolean guest,
                       boolean locked, Currency currency, String countryCode, String nickName) {
        super();
        checkNotNull(currency, "currency must not be null");
        this.id = id;
        this.externalId = externalId;
        this.bankId = bankId;
        this.subCasinoId = subCasinoId;
        this.guest = guest;
        this.locked = locked;
        this.registerTime = registerTime;
        this.currency = currency;
        this.countryCode = countryCode;
        this.nickName = nickName;
    }

    //copy constructor
    private AccountInfo(long id, String nickName, String externalId, int bankId, short subcasinoId,
                        long balance, long freeBalance, boolean guest, boolean locked,
                        long registerTime, long lastLoginTime, Currency currency, boolean testUser) {
        super(/*properties*/);
        checkNotNull(currency, "currency must not be null");
        this.id = id;
        this.nickName = nickName;
        this.externalId = externalId;
        this.bankId = bankId;
        this.subCasinoId = subcasinoId;
        this.balance = balance;
        this.freeBalance = freeBalance;
        this.guest = guest;
        this.locked = locked;
        this.registerTime = registerTime;
        this.lastLoginTime = lastLoginTime;
        this.currency = currency;
        this.testUser = testUser;
    }

    public AccountInfo copy() {
        AccountInfo copy = new AccountInfo(id, nickName, externalId, bankId, subCasinoId, balance,
                freeBalance, guest, locked, registerTime, lastLoginTime, currency, testUser);
        copy.setFinsoftSessionId(finsoftSessionId);
        copy.setSmartLiveOperator(smartLiveOperator);
        copy.setAccountUseId(accountUseId);
        copy.setAgentId(agentId);
        copy.setEmail(email);
        copy.setFirstName(firstName);
        copy.setLastName(lastName);
        copy.setPassword(password);
        copy.setSessionKey(sessionKey);
        copy.frbMassAwardIdsList = frbMassAwardIdsList;
        copy.bonusMassAwardIdsList = bonusMassAwardIdsList;
        copy.testUser = testUser;
        copy.unjContributionFractionalPart = unjContributionFractionalPart;
        return copy;
    }

    public String getLockId() {
        return StringIdGenerator.getAccountHash(getBankId(), getExternalId());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public int getBankId() {
        return bankId;
    }

    @Override
    public long getSystemId() {
        return bankId;
    }

    public long getBalance() {
        return balance;
    }

    public long getFreeBalance() {
        return freeBalance;
    }

    public void setFreeBalance(long freeBalance) {
        this.freeBalance = freeBalance;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public void setBalance(long balance) throws CommonException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setBalance accountId:" + id + " oldBalance:" + this.balance +
                    " newBalance:" + balance + " thread:" + Thread.currentThread().getId());
        }
        if (balance < 0) {
            throw new CommonException("Balance cannot be negative, current=" + this.balance + ", new=" + balance);
        }
        this.balance = balance;
    }

    public void incrementBalance(long delta, boolean silently) throws CommonException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("incrementBalance accountId:" + id + " currentBalance:" +
                    this.balance + " delta:" + delta + " thread:" + Thread.currentThread().getId());
        }
        if (!silently && balance + delta < 0) {
            throw new CommonException("Balance cannot be negative, current=" + balance + ", delta=" + delta);
        }
        this.balance += delta;
    }

    public void incrementBalance(long bet, long win, boolean silently) throws CommonException {
        //bet is negative
        if (LOG.isDebugEnabled()) {
            LOG.debug("incrementBalance accountId:" + id + " currentBalance:" +
                    this.balance + " bet:" + bet + ", win:" + win + " thread:" + Thread.currentThread().getId());
        }
        if (bet != 0) {
            if (!silently && balance + bet < 0) {
                throw new CommonException("Balance cannot be negative, current=" + balance + ", bet=" + bet);
            }
            this.balance += bet;
        }
        if (win != 0) {
            if (!silently && balance + win < 0) {
                throw new CommonException("Balance cannot be negative, current=" + balance + ", win=" + win);
            }
            this.balance += win;
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean lock) {
        this.locked = lock;
    }

    public short getSubCasinoId() {
        return subCasinoId;
    }

    public void setSubcasinoId(short subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    public boolean isGuest() {
        return guest;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setSubCasinoId(short subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    public void setFrbMassAwardIdsList(String frbMassAwardIdsList) {
        this.frbMassAwardIdsList = frbMassAwardIdsList;
    }

    public void setBonusMassAwardIdsList(String bonusMassAwardIdsList) {
        this.bonusMassAwardIdsList = bonusMassAwardIdsList;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Long getAccountUseId() {
        return accountUseId;
    }

    public void setAccountUseId(Long accountUseId) {
        this.accountUseId = accountUseId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void update(String password, String sessionKey, boolean freeMode, boolean updateLastLoginTime) {
        setPassword(password);
        setSessionKey(sessionKey);
        this.guest = freeMode;
        if (updateLastLoginTime) {
            this.lastLoginTime = System.currentTimeMillis();
        }
    }

    public void update(String password, boolean freeMode, boolean updateLastLoginTime, long currentGameServer) {
        setPassword(password);
        this.guest = freeMode;
        if (updateLastLoginTime) {
            this.lastLoginTime = System.currentTimeMillis();
        }
        this.currentGameServer = currentGameServer;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrencyFraction() {
        return currencyFraction;
    }

    public void setCurrencyFraction(Currency currencyFraction) {
        this.currencyFraction = currencyFraction;
    }

    public long getCurrentGameServer() {
        return currentGameServer;
    }

    public void setCurrentGameServer(long currentGameServer) {
        this.currentGameServer = currentGameServer;
    }

    public String getFinsoftSessionId() {
        return finsoftSessionId;
    }

    public void setFinsoftSessionId(String finsoftSessionId) {
        this.finsoftSessionId = finsoftSessionId;
    }

    public String getSmartLiveOperator() {
        return smartLiveOperator;
    }

    public void setSmartLiveOperator(String smartLiveOperator) {
        this.smartLiveOperator = smartLiveOperator;
    }

    public List<Long> getFrbMassAwardIdsList() {
        return CollectionUtils.stringToListOfLongs(frbMassAwardIdsList);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getFrbMassAwardIds() {
        return frbMassAwardIdsList;
    }

    public void setFrbMassAwardIdsList(List<Long> idsList) {
        this.frbMassAwardIdsList = CollectionUtils.listOfLongsToString(idsList);
    }

    public List<Long> getBonusMassAwardIdsList() {
        return CollectionUtils.stringToListOfLongs(bonusMassAwardIdsList);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getBonusMassAwardIds() {
        return bonusMassAwardIdsList;
    }

    public void setBonusMassAwardIdsList(List<Long> idsList) {
        this.bonusMassAwardIdsList = CollectionUtils.listOfLongsToString(idsList);
    }

    public PlayerGameSettings getGameSettings() {
        return gameSettings;
    }

    public void setGameSettings(PlayerGameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isSendNewsletters() {
        return sendNewsletters;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setSendNewsletters(boolean sendNewsletters) {
        this.sendNewsletters = sendNewsletters;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setLastActivityTime(long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public boolean isTestUser() {
        return testUser;
    }

    public void setTestUser(boolean testUser) {
        this.testUser = testUser;
    }

    public double getUnjContributionFractionalPart() {
        return unjContributionFractionalPart;
    }

    public void setUnjContributionFractionalPart(double unjContributionFractionalPart) {
        this.unjContributionFractionalPart = unjContributionFractionalPart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountInfo that = (AccountInfo) o;
        if (id != that.id) return false;
        if (bankId != that.bankId) return false;
        if (externalId != null ? !externalId.equals(that.externalId) : that.externalId != null) return false;
        if (nickName != null ? !nickName.equals(that.nickName) : that.nickName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AccountInfo");
        sb.append("[id=").append(id);
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append(", externalId='").append(externalId).append('\'');
        sb.append(", bankId=").append(bankId);
        sb.append(", subCasinoId=").append(subCasinoId);
        sb.append(", balance=").append(balance);
        sb.append(", freeBalance=").append(freeBalance);
        sb.append(", guest=").append(guest);
        sb.append(", locked=").append(locked);
        sb.append(", currentGameServer=").append(currentGameServer);
        sb.append(", registerTime=").append(new Date(registerTime));
        sb.append(", lastLoginTime=").append(new Date(lastLoginTime));
        sb.append(", currency=").append(currency == null ? "null" : currency.getCode());
        sb.append(", currencyFraction=").append(currencyFraction == null ? "null" : currencyFraction.getCode());
        sb.append(", smartLiveOperator=").append(smartLiveOperator);
        sb.append(", finsoftSessionId=").append(finsoftSessionId);
        sb.append(", accountUseId=").append(accountUseId);
        sb.append(", agentId=").append(agentId);
        sb.append(", email=").append(email);
        sb.append(", firstName=").append(firstName);
        sb.append(", lastName=").append(lastName);
        sb.append(", password=").append(password);
        sb.append(", sessionKey='").append(sessionKey).append("'");
        sb.append(", frbMassAwardIdsList=").append(frbMassAwardIdsList);
        sb.append(", bonusMassAwardIdsList=").append(bonusMassAwardIdsList);
        sb.append(", countryCode=").append(countryCode);
        sb.append(", sendNewsletters=").append(sendNewsletters);
        sb.append(", lastActivityTime=").append(new Date(lastActivityTime));
        sb.append(", gameSettings=").append(gameSettings);
        sb.append(", testUser=").append(testUser);
        sb.append(", unjContributionFractionalPart=").append(unjContributionFractionalPart);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeString(nickName);
        output.writeString(externalId);
        output.writeInt(bankId, true);
        output.writeShort(subCasinoId);
        output.writeLong(balance);
        output.writeLong(freeBalance);
        output.writeBoolean(guest);
        output.writeBoolean(locked);
        output.writeLong(registerTime, true);
        output.writeLong(lastLoginTime, true);
        kryo.writeObjectOrNull(output, currency, Currency.class);
        output.writeLong(currentGameServer, true);
        output.writeString(finsoftSessionId);
        output.writeString(smartLiveOperator);
        kryo.writeObjectOrNull(output, accountUseId, Long.class);
        output.writeString(agentId);
        output.writeString(email);
        output.writeString(firstName);
        output.writeString(lastName);
        output.writeString(password);
        output.writeString(sessionKey);
        kryo.writeObjectOrNull(output, gameSettings, PlayerGameSettings.class);
        kryo.writeObjectOrNull(output, frbMassAwardIdsList, String.class);
        kryo.writeObjectOrNull(output, bonusMassAwardIdsList, String.class);
        kryo.writeObjectOrNull(output, countryCode, String.class);
        output.writeBoolean(sendNewsletters);
        output.writeLong(lastActivityTime, true);
        output.writeBoolean(testUser);
        output.writeDouble(unjContributionFractionalPart);
        kryo.writeObjectOrNull(output, currencyFraction, Currency.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        nickName = input.readString();
        externalId = input.readString();
        bankId = input.readInt(true);
        subCasinoId = input.readShort();
        balance = input.readLong();
        freeBalance = input.readLong();
        guest = input.readBoolean();
        locked = input.readBoolean();
        registerTime = input.readLong(true);
        lastLoginTime = input.readLong(true);
        currency = kryo.readObjectOrNull(input, Currency.class, Currency.SERIALIZER);
        currentGameServer = input.readLong(true);
        finsoftSessionId = input.readString();
        smartLiveOperator = input.readString();
        accountUseId = kryo.readObjectOrNull(input, Long.class);
        agentId = input.readString();
        email = input.readString();
        firstName = input.readString();
        lastName = input.readString();
        password = input.readString();
        sessionKey = input.readString();
        gameSettings = kryo.readObjectOrNull(input, PlayerGameSettings.class);
        frbMassAwardIdsList = kryo.readObjectOrNull(input, String.class);
        bonusMassAwardIdsList = kryo.readObjectOrNull(input, String.class);
        countryCode = kryo.readObjectOrNull(input, String.class);
        sendNewsletters = input.readBoolean();
        lastActivityTime = input.readLong(true);
        if (ver >= 1) {
            testUser = input.readBoolean();
        }
        if (ver >= 2) {
            unjContributionFractionalPart = input.readDouble();
        }
        if (ver >= 3) {
            currencyFraction = kryo.readObjectOrNull(input, Currency.class, Currency.SERIALIZER);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("id", id);
        gen.writeStringField("nickName", nickName);
        gen.writeStringField("externalId", externalId);
        gen.writeNumberField("bankId", bankId);
        gen.writeNumberField("subCasinoId", subCasinoId);
        gen.writeNumberField("balance", balance);
        gen.writeNumberField("freeBalance", freeBalance);
        gen.writeBooleanField("guest", guest);
        gen.writeBooleanField("locked", locked);
        gen.writeNumberField("registerTime", registerTime);
        gen.writeNumberField("lastLoginTime", lastLoginTime);
        gen.writeObjectField("currency", currency);
        gen.writeNumberField("currentGameServer", currentGameServer);
        gen.writeStringField("finsoftSessionId", finsoftSessionId);
        gen.writeStringField("smartLiveOperator", smartLiveOperator);
        serializeNumberOrNull(gen, "accountUseId", accountUseId);
        gen.writeStringField("agentId", agentId);
        gen.writeStringField("email", email);
        gen.writeStringField("firstName", firstName);
        gen.writeStringField("lastName", lastName);
        gen.writeStringField("password", password);
        gen.writeStringField("sessionKey", sessionKey);
        gen.writeObjectField("gameSettings", gameSettings);
        gen.writeStringField("frbMassAwardIdsList", frbMassAwardIdsList);
        gen.writeStringField("bonusMassAwardIdsList", bonusMassAwardIdsList);
        gen.writeStringField("countryCode", countryCode);
        gen.writeBooleanField("sendNewsletters", sendNewsletters);
        gen.writeNumberField("lastActivityTime", lastActivityTime);
        gen.writeBooleanField("testUser", testUser);
        gen.writeNumberField("unjContributionFractionalPart", unjContributionFractionalPart);
        gen.writeObjectField("currencyFraction", currencyFraction);
    }

    @Override
    public AccountInfo deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode t = p.getCodec().readTree(p);

        ObjectMapper om = (ObjectMapper) p.getCodec();

        id = t.get("id").longValue();
        nickName = readNullableText(t, "nickName");
        externalId = readNullableText(t,"externalId");
        bankId = t.get("bankId").intValue();
        subCasinoId = t.get("subCasinoId").shortValue();
        balance = t.get("balance").asLong();
        freeBalance = t.get("freeBalance").asLong();
        guest = t.get("guest").asBoolean();
        locked = t.get("locked").asBoolean();
        registerTime = t.get("registerTime").asLong();
        lastLoginTime = t.get("lastLoginTime").asLong();
        currency = om.convertValue(t.get("currency"), Currency.class);
        currentGameServer = t.get("currentGameServer").asLong();
        finsoftSessionId = readNullableText(t,"finsoftSessionId");
        smartLiveOperator = readNullableText(t,"smartLiveOperator");
        accountUseId = deserializeOrNull(om, t.get("accountUseId"), Long.class);
        agentId = readNullableText(t,"agentId");
        email = readNullableText(t,"email");
        firstName = readNullableText(t,"firstName");
        lastName = readNullableText(t,"lastName");
        password = readNullableText(t,"password");
        sessionKey = readNullableText(t,"sessionKey");
        gameSettings = om.convertValue(t.get("gameSettings"), PlayerGameSettings.class);
        frbMassAwardIdsList = readNullableText(t,"frbMassAwardIdsList");
        bonusMassAwardIdsList = readNullableText(t,"bonusMassAwardIdsList");
        countryCode = readNullableText(t,"countryCode");
        sendNewsletters = t.get("sendNewsletters").booleanValue();
        lastActivityTime = t.get("lastActivityTime").longValue();
        testUser = t.get("testUser").booleanValue();
        unjContributionFractionalPart = t.get("unjContributionFractionalPart").doubleValue();
        currencyFraction = om.convertValue(t.get("currencyFraction"), Currency.class);

        return this;
    }
}