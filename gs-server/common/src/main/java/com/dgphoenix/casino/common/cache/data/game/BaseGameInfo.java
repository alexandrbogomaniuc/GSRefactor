package com.dgphoenix.casino.common.cache.data.game;

import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;
import com.abs.casino.common.cache.data.account.PlayerDeviceType;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.currency.ICurrency;
import com.abs.casino.common.config.HostConfiguration;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.StreamUtils;
import com.abs.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

public class BaseGameInfo implements IDistributedConfigEntry, Identifiable, KryoSerializable, IBaseGameInfo<Coin, Limit>, JsonSelfSerializable<BaseGameInfo> {

    private static final Logger LOG = LogManager.getLogger(BaseGameInfo.class);

    private static final int VERSION = 0;
    private static final Set<String> templateDefaultProperty = ImmutableSet.of("KEY_PLAYER_DEVICE_TYPE",
            "ANDROID", "IOSMOBILE", "WINDOWSPHONE", "PC");

    private static final char SEPARATOR = '|';
    private static final String KEY_JACKPOT_WIN_LIMIT = "JACKPOT_WIN_LIMIT";

    private long id;
    private String externalId;
    private long bankId;
    private Currency currency;
    private String name;
    private GameType gameType;
    private GameGroup group;
    private GameVariableType variableType;

    private String rmClassName;
    private String gsClassName;

    private Limit limit;
    private List<Coin> coins;
    private List<String> languages = new ArrayList<>();
    protected Map<String, String> propertiesMap = new HashMap<>();

    private String servlet;
    private boolean isMobile;

    private long lastUpdateDate;
    @XStreamOmitField
    private transient BaseGameInfoTemplateCache templateCache;
    @XStreamOmitField
    private transient BankInfoCache bankInfoCache;
    @XStreamOmitField
    private transient HostConfiguration hostConfiguration;

    public BaseGameInfo() {
        super();
    }

    public BaseGameInfo(BaseGameInfoTemplateCache templateCache, BankInfoCache bankInfoCache) {
        this.templateCache = templateCache;
        this.bankInfoCache = bankInfoCache;
    }

    public BaseGameInfo(long id, long bankId, String name, GameType gameType, GameGroup group,
                        GameVariableType variableType, String rmClassName,
                        String gsClassName, List<Coin> coins,
                        Map<String, String> properties) {
        this.id = id;
        this.bankId = bankId;
        this.name = name;
        this.gameType = gameType;
        this.group = group;
        this.variableType = variableType;
        this.rmClassName = rmClassName;
        this.gsClassName = gsClassName;
        this.coins = coins;
        this.currency = getBankInfoCache().getBankInfo(bankId).getDefaultCurrency();
        if (properties != null) {
            propertiesMap.putAll(properties);
        }
    }

    //copy constructor
    public BaseGameInfo(long id, long bankId, String name, GameType gameType, GameGroup group,
                        GameVariableType variableType, String rmClassName,
                        String gsClassName, Limit limit, List<Coin> coins,
                        Map<String, String> properties, Currency currency, List<String> languages) {
        this.id = id;
        this.bankId = bankId;
        this.name = name;
        this.gameType = gameType;
        this.group = group;
        this.variableType = variableType;
        this.rmClassName = rmClassName;
        this.gsClassName = gsClassName;
        this.limit = limit;
        this.coins = coins;
        this.currency = currency;
        if (languages != null && !languages.isEmpty()) {
            this.languages = new ArrayList<>(languages);
        } else {
            this.languages = new ArrayList<>();
            this.languages.add("en");
        }
        if (properties != null) {
            propertiesMap.putAll(properties);
        }
    }

    //light copy constructor
    private BaseGameInfo(long id, long bankId, String name, GameType gameType, GameGroup group,
                         GameVariableType variableType, String rmClassName, String gsClassName,
                         Currency currency, List<String> languages, String servlet,
                         boolean isMobile, String extGameId) {
        this.id = id;
        this.bankId = bankId;
        this.name = name;
        this.gameType = gameType;
        this.group = group;
        this.variableType = variableType;
        this.rmClassName = rmClassName;
        this.gsClassName = gsClassName;
        this.currency = currency;
        if (languages != null && !languages.isEmpty()) {
            this.languages = new ArrayList<>(languages);
        } else {
            this.languages = new ArrayList<>();
            this.languages.add("en");
        }
        this.servlet = servlet;
        this.isMobile = isMobile;
        this.externalId = extGameId;
    }

    private BaseGameInfoTemplateCache getTemplateCache() {
        if (templateCache == null) {
            templateCache = BaseGameInfoTemplateCache.getInstance();
        }
        return templateCache;
    }

    public BankInfoCache getBankInfoCache() {
        if (bankInfoCache == null) {
            bankInfoCache = BankInfoCache.getInstance();
        }
        return bankInfoCache;
    }

    private HostConfiguration getHostConfiguration() {
        if (hostConfiguration == null) {
            hostConfiguration = ApplicationContextHelper.getBean(HostConfiguration.class);
        }
        return hostConfiguration;
    }

    @Override
    public String getExternalId() {
        return externalId;
    }

    @Override
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public String getServlet() {
        return servlet;
    }

    @Override
    public void setServlet(String servlet) {
        this.servlet = servlet;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setMobile(boolean isMobile) {
        this.isMobile = isMobile;
    }

    @Override
    public boolean isMobile() {
        return isMobile || getPlayerDeviceType() != null;
    }

    @Override
    public PlayerDeviceType getPlayerDeviceType() {
        String playerDeviceType = getProperty("KEY_PLAYER_DEVICE_TYPE");
        return (!isTrimmedEmpty(playerDeviceType)) ?
                PlayerDeviceType.valueOf(playerDeviceType.trim().toUpperCase()) : null;
    }

    @Override
    public long getAdmCode() throws CommonException {
        String property = getProperty(BaseGameConstants.KEY_EURO_BET_ADM_CODE);
        if (property == null) {
            throw new CommonException("ADM code not found");
        }
        return Long.parseLong(property);
    }

    @Override
    public void setAdmCode(long admCode) {
        setProperty(BaseGameConstants.KEY_EURO_BET_ADM_CODE, String.valueOf(admCode));
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ICurrency getCurrency() {
        if (currency == null) {
            currency = getBankInfoCache().getBankInfo(bankId).getDefaultCurrency();
        }
        return currency;
    }

    @Override
    public void setCurrency(ICurrency currency) {
        this.currency = (Currency) currency;
    }

    @Override
    public GameType getGameType() {
        return gameType;
    }

    @Override
    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    @Override
    public GameGroup getGroup() {
        return group;
    }

    @Override
    public void setGroup(GameGroup group) {
        this.group = group;
    }

    @Override
    public String getRmClassName() {
        return rmClassName;
    }

    @Override
    public GameVariableType getVariableType() {
        return variableType;
    }

    @Override
    public void setVariableType(GameVariableType variableType) {
        this.variableType = variableType;
    }

    @Override
    public void setRmClassName(String rmClassName) {
        this.rmClassName = rmClassName;
    }

    @Override
    public String getGsClassName() {
        return gsClassName;
    }

    @Override
    public void setGsClassName(String gsClassName) {
        this.gsClassName = gsClassName;
    }

    @Override
    public void removeProperty(String key) {
        if (propertiesMap != null) {
            propertiesMap.remove(key);
        }
    }

    @Override
    public String getProperty(String propertyName) {
        return propertiesMap.get(propertyName);
    }

    @Override
    public void setProperty(String key, String value) {
        if (!isTrimmedEmpty(key) && value != null) {
            propertiesMap.put(key, value);
            if (BaseGameConstants.isInheritedFromTemplate(key)) {
                LOG.warn("Manual change property: '{}' to value: '{}' for game: '{}' bank: '{}'", key, value, id, bankId);
            }
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return propertiesMap;
    }

    @Override
    public Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    @Override
    public boolean isBankEnabled() {
        return getBankInfoCache().getBankInfo(getBankId()).isEnabled();
    }

    @Override
    public boolean isEnabled() {
        BankInfo bankInfo = getBankInfoCache().getBankInfo(getBankId());
        return BaseGameConstants.TRUE.equalsIgnoreCase(getProperty(BaseGameConstants.KEY_ISENABLED))
                && bankInfo != null
                && bankInfo.isEnabled();
    }

    @Override
    public void setEnabled(boolean flag) {
        setProperty(BaseGameConstants.KEY_ISENABLED, String.valueOf(flag).toUpperCase());
    }

    @Override
    public double getRtp() {
        return getRtp(getBankId(), false);
    }

    @Override
    public double getRtp(boolean isCashBonusSession) {
        return getRtp(getBankId(), isCashBonusSession);
    }

    @Override
    public double getRtp(Long bankId, boolean isCashBonusSession) {
        String strRTP = getProperty(BaseGameConstants.KEY_RTP);
        if (isTrimmedEmpty(strRTP)) {
            throw new IllegalStateException("RTP is not defined");
        }
        try {
            return Double.parseDouble(strRTP);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid RTP: " + strRTP);
        }
    }

    @Override
    public Double getRtpMin(boolean isCashBonusSession) {
        return getRtpMin(getBankId(), isCashBonusSession);
    }

    @Override
    public Double getRtpMin(Long bankId, boolean isCashBonusSession) {
        String rtp = getProperty(BaseGameConstants.KEY_RTP_MIN_NMI);
        return !StringUtils.isTrimmedEmpty(rtp) ? Double.parseDouble(rtp) : null;
    }

    @Override
    public double getPayoutPercent() {
        String strPercent = this.getProperty(BaseGameConstants.KEY_PAYOUT_PERCENT);
        if (isTrimmedEmpty(strPercent) || getGameType().equals(GameType.MP))
            return getRtp() / 100;
        try {
            return Double.parseDouble(strPercent);
        } catch (NumberFormatException e) {
            ThreadLog.error("Invalid payout percent: " + strPercent + ". BaseGameInfo=" + this);
            throw new IllegalStateException("Invalid payout percent: " + strPercent);
        }
    }

    @Override
    public void setPayoutPrecent(double percent) {
        setProperty(BaseGameConstants.KEY_PAYOUT_PERCENT, String.valueOf(percent));
    }

    @Override
    public void setDefaultCoin(int coinPosition) {
        setProperty(BaseGameConstants.KEY_DEFAULT_COIN, String.valueOf(coinPosition));
    }

    @Override
    public Integer getDefaultCoin() {
        String defCoin = getProperty(BaseGameConstants.KEY_DEFAULT_COIN);
        return isTrimmedEmpty(defCoin) ? null : Integer.parseInt(defCoin);
    }


    @Override
    public void setWJP(int wjp) {
        setProperty(BaseGameConstants.KEY_WJP, String.valueOf(wjp));
    }

    @Override
    public Integer getWJP() {
        String wjp = getProperty(BaseGameConstants.KEY_WJP);
        return isTrimmedEmpty(wjp) ? null : Integer.parseInt(wjp);
    }

    @Override
    public String getCDNUrl() {
        String cdnUrl = getProperty(BaseGameConstants.KEY_CDN_URL);
        if (isTrimmedEmpty(cdnUrl)) {
            return null;
        }
        return cdnUrl;
    }

    @Override
    public String getThirdPartyGameId() {
        return getProperty(BaseGameConstants.KEY_THIRD_PARTY_GAME_ID);
    }

    @Override
    public boolean isThirdPartyGame() {
        return !isTrimmedEmpty(getThirdPartyGameId());
    }

    //values see in ExternalGameProvider
    @Override
    public String getThirdPartyProviderName() {
        return getProperty(BaseGameConstants.KEY_THIRD_PARTY_PROVIDER_NAME);
    }

    @Override
    public String getChipValues() {
        return getProperty(BaseGameConstants.KEY_CHIPVALUES);
    }

    @Override
    public void setChipValues(String chipValues) {
        setProperty(BaseGameConstants.KEY_CHIPVALUES, chipValues);
    }

    @Override
    public List<Coin> getCoins() {
        return coins;
    }

    @Override
    public void setCoins(List<Coin> coins) {
        this.coins = coins;
    }

    @Override
    public Limit getLimit() {
        return limit;
    }

    @Override
    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    @Override
    public int getCoinsSize() {
        return coins == null ? 0 : coins.size();
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.propertiesMap = properties;
    }

    @Override
    public Set<Long> getCoinsWithDisabledJPWon() {
        String stringCoins = getProperty(BaseGameConstants.KEY_COINS_WITH_DISABLED_JP_WON);
        if (isTrimmedEmpty(stringCoins)) {
            return Collections.emptySet();
        }
        return StreamUtils.asStream(Splitter.on(SEPARATOR).split(stringCoins))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    @Override
    public BaseGameInfo copy() {
        List<Coin> coinsCopy = Coin.copyCoins(this.coins);

        Limit limitCopy = null;
        if (limit != null) {
            limitCopy = limit;
        }

        BaseGameInfo copy = new BaseGameInfo(id, bankId, name, gameType, group, variableType, rmClassName, gsClassName,
                limitCopy, coinsCopy, CollectionUtils.copyProperties(propertiesMap),
                currency, languages);
        copy.externalId = this.externalId;
        copy.lastUpdateDate = this.lastUpdateDate;

        return copy;
    }

    @Override
    public boolean hasLimit() {
        return limit != null;
    }

    @Override
    public boolean hasCoins() {
        return !CollectionUtils.isEmpty(coins);
    }

    @Override
    public boolean isExist(String language) {
        return (language != null) && (languages != null) && languages.contains(language);
    }

    @Override
    public List<String> getLanguages() {
        return languages;
    }

    @Override
    public void setLanguages(List<String> languages) {
        if (languages != null) {
            this.languages = new ArrayList<>(languages);
        }
    }

    @Override
    public void removeAllLanguages() {
        if (languages != null) {
            languages.clear();
        }
    }

    @Override
    public void addLanguage(String language) {
        if (!languages.contains(language)) {
            languages.add(language);
        }
    }

    @Override
    public boolean isAutoplayGame() {
        return BaseGameConstants.TRUE.equalsIgnoreCase(this.getProperty(BaseGameConstants.KEY_IS_AUTOPLAY_GAME));
    }

    @Override
    public boolean isHasAchievements() {
        String property = getProperty(BaseGameConstants.KEY_HAS_ACHIEVEMENTS);
        return property != null && BaseGameConstants.TRUE.equalsIgnoreCase(property);
    }

    @Override
    public com.abs.casino.common.cache.data.game.Html5PcVersionMode getHtml5PcVersionMode() {
        String mode = getProperty(BaseGameConstants.KEY_HTML5PC_VERSION_MODE);
        if (isTrimmedEmpty(mode)) {
            return com.abs.casino.common.cache.data.game.Html5PcVersionMode.NOT_AVAILABLE;
        } else {
            return com.abs.casino.common.cache.data.game.Html5PcVersionMode.valueOf(mode);
        }
    }

    @Override
    public String getRepositoryFile() {
        return getProperty(BaseGameConstants.KEY_REPOSITORY_FILE);
    }

    @Override
    public boolean isDevelopmentVersion() {
        return BaseGameConstants.TRUE.equalsIgnoreCase(this.getProperty(BaseGameConstants.KEY_DEVELOPMENT_VERSION));
    }

    @Override
    public String getGameEventProcessorClass() {
        return getProperty(BaseGameConstants.KEY_GAME_EVENT_PROCESSOR_CLASS);
    }

    @Override
    public String getProfileId() {
        return getProperty(BaseGameConstants.KEY_PROFILE_ID);
    }

    @Override
    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        BaseGameInfo from = (BaseGameInfo) entry;
        this.externalId = from.externalId;
        this.bankId = from.bankId;
        this.currency = (Currency) from.getCurrency();
        this.name = from.name;
        this.gameType = from.gameType;
        this.group = from.group;
        this.variableType = from.variableType;
        this.rmClassName = from.rmClassName;
        this.gsClassName = from.gsClassName;
        this.limit = from.limit;
        this.coins = from.coins;
        this.languages = from.getLanguages();
        this.servlet = from.servlet;
        this.isMobile = from.isMobile;
        this.propertiesMap = from.propertiesMap;
        this.lastUpdateDate = from.lastUpdateDate;
    }

    @Override
    public BaseGameInfo lightCopy() {
        return new BaseGameInfo(id, bankId, name, gameType, group, variableType, rmClassName, gsClassName,
                currency, languages, servlet, isMobile, externalId);
    }

    private synchronized String getCoinsAsString() {
        String coinsString;
        List<Coin> tmpCoins = coins;
        if (CollectionUtils.isEmpty(tmpCoins)) {
            coinsString = "";
        } else {
            List<Long> ids = new ArrayList<>(tmpCoins.size());
            for (Coin coin : tmpCoins) {
                if (!ids.contains(coin.getId())) {
                    ids.add(coin.getId());
                }
            }
            coinsString = CollectionUtils.listOfLongsToString(ids);
        }
        return coinsString;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BaseGameInfo");
        sb.append("[id=").append(id);
        sb.append(", bankId=").append(bankId);
        sb.append(", currency=").append(currency);
        sb.append(", name='").append(name).append('\'');
        sb.append(", gameType=").append(gameType);
        sb.append(", group=").append(group);
        sb.append(", variableType=").append(variableType);
        sb.append(", rmClassName='").append(rmClassName).append('\'');
        sb.append(", gsClassName='").append(gsClassName).append('\'');
        sb.append(", lastUpdateDate=").append(lastUpdateDate);
        sb.append(", limit=").append(limit);
        sb.append(", properties=").append(StringUtils.printProperties(getPropertiesMap()));
        sb.append(", coins=").append(coins);
        sb.append(", languages=").append(Arrays.asList(getLanguages()));
        sb.append(", servlet=").append(servlet);
        sb.append(", isMobile=").append(isMobile);
        sb.append(", isMobile()=").append(isMobile());
        sb.append(", external id=").append(externalId);
        sb.append(", repositoryFile=").append(getRepositoryFile());
        sb.append(", isDevelopmentVersion=").append(isDevelopmentVersion());
        sb.append(", CDN_URL=").append(getCDNUrl());
        sb.append(", enabled=").append(isEnabled());
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        output.writeLong(id, true);
        output.writeString(externalId);
        output.writeLong(bankId, true);
        kryo.writeObjectOrNull(output, currency, Currency.class);
        output.writeString(name);
        kryo.writeClassAndObject(output, gameType);
        kryo.writeClassAndObject(output, group);
        kryo.writeClassAndObject(output, variableType);
        output.writeString(rmClassName);
        output.writeString(gsClassName);
        kryo.writeObjectOrNull(output, limit, Limit.class);
        kryo.writeClassAndObject(output, coins);
        kryo.writeClassAndObject(output, languages);
        kryo.writeClassAndObject(output, propertiesMap);
        output.writeString(servlet);
        output.writeBoolean(isMobile);
        output.writeLong(lastUpdateDate, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int version = input.readInt(true);
        id = input.readLong(true);
        externalId = input.readString();
        bankId = input.readLong(true);
        currency = kryo.readObjectOrNull(input, Currency.class, Currency.SERIALIZER);
        name = input.readString();
        gameType = (GameType) kryo.readClassAndObject(input);
        group = (GameGroup) kryo.readClassAndObject(input);
        variableType = (GameVariableType) kryo.readClassAndObject(input);
        rmClassName = input.readString();
        gsClassName = input.readString();
        limit = kryo.readObjectOrNull(input, Limit.class);
        coins = (List<Coin>) kryo.readClassAndObject(input);
        languages = (List<String>) kryo.readClassAndObject(input);
        propertiesMap = (Map<String, String>) kryo.readClassAndObject(input);
        servlet = input.readString();
        isMobile = input.readBoolean();
        lastUpdateDate = input.readLong(true);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("id", id);
        gen.writeStringField("externalId", externalId);
        gen.writeNumberField("bankId", bankId);
        gen.writeObjectField("currency", currency);
        gen.writeStringField("name", name);
        gen.writeNumberField("gameTypeId", gameType.ordinal());
        gen.writeNumberField("groupId", group.ordinal());
        gen.writeNumberField("variableTypeId", variableType.ordinal());
        gen.writeStringField("rmClassName", rmClassName);
        gen.writeStringField("gsClassName", gsClassName);
        gen.writeObjectField("limit", limit);
        serializeListField(gen, "coins", coins, new TypeReference<List<Coin>>() {});
        serializeListField(gen, "languages", languages, new TypeReference<List<String>>() {});
        serializeMapField(gen, "propertiesMap", propertiesMap, new TypeReference<Map<String,String>>() {});
        gen.writeStringField("servlet", servlet);
        gen.writeBooleanField("isMobile", isMobile);
        gen.writeNumberField("lastUpdateDate", lastUpdateDate);
    }

    @Override
    public BaseGameInfo deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        id = node.get("id").longValue();
        externalId = node.get("externalId").textValue();
        bankId = node.get("bankId").longValue();
        currency = om.convertValue(node.get("currency"), Currency.class);
        name = readNullableText(node, "name");
        gameType = GameType.values()[node.get("gameTypeId").intValue()];
        group = GameGroup.values()[node.get("groupId").intValue()];
        variableType = GameVariableType.values()[node.get("variableTypeId").intValue()];
        rmClassName = node.get("rmClassName").textValue();
        gsClassName = node.get("gsClassName").textValue();
        limit = om.convertValue(node.get("limit"), Limit.class);
        coins = om.convertValue(node.get("coins"), new TypeReference<List<Coin>>() {});
        languages = om.convertValue(node.get("languages"), new TypeReference<List<String>>() {});
        propertiesMap = om.convertValue(node.get("propertiesMap"), new TypeReference<Map<String, String>>() {});
        servlet = node.get("servlet").textValue();
        isMobile = node.get("isMobile").booleanValue();
        lastUpdateDate = node.get("lastUpdateDate").longValue();

        return this;
    }

    @Override
    public void marshal(HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.startNode("id");
        writer.setValue(String.valueOf(id));
        writer.endNode();

        if (externalId != null) {
            writer.startNode("externalId");
            writer.setValue(externalId);
            writer.endNode();
        }

        writer.startNode("bankId");
        writer.setValue(String.valueOf(bankId));
        writer.endNode();

        if (currency != null) {
            writer.startNode("currency");
            context.convertAnother(currency);
            writer.endNode();
        }

        if (name != null) {
            writer.startNode("name");
            writer.setValue(name);
            writer.endNode();
        }

        if (gameType != null) {
            writer.startNode("gameType");
            writer.setValue(gameType.name());
            writer.endNode();
        }

        if (group != null) {
            writer.startNode("group");
            writer.setValue(group.name());
            writer.endNode();
        }

        if (variableType != null) {
            writer.startNode("variableType");
            writer.setValue(variableType.name());
            writer.endNode();
        }

        if (rmClassName != null) {
            writer.startNode("rmClassName");
            writer.setValue(rmClassName);
            writer.endNode();
        }

        if (gsClassName != null) {
            writer.startNode("gsClassName");
            writer.setValue(gsClassName);
            writer.endNode();
        }

        if (limit != null) {
            writer.startNode("limit");
            context.convertAnother(limit);
            writer.endNode();
        }

        String coinsString = getCoinsAsString();
        if (coinsString != null) {
            writer.startNode("coinsString");
            writer.setValue(coinsString);
            writer.endNode();
        }

        String langsString = CollectionUtils.listOfStringsToString(languages);
        writer.startNode("langsString");
        writer.setValue(langsString);
        writer.endNode();

        String propertiesString = CollectionUtils.mapToString(propertiesMap);
        writer.startNode("propertiesString");
        writer.setValue(propertiesString);
        writer.endNode();

        if (servlet != null) {
            writer.startNode("servlet");
            writer.setValue(servlet);
            writer.endNode();
        }

        writer.startNode("isMobile");
        writer.setValue(String.valueOf(isMobile));
        writer.endNode();


        writer.startNode("lastUpdateDate");
        writer.setValue(String.valueOf(lastUpdateDate));
        writer.endNode();
    }

    @Override
    public Double getJackpotWinLimit() {
        String strLimit = getProperty(KEY_JACKPOT_WIN_LIMIT);
        if (!isTrimmedEmpty(strLimit)) {
            try {
                return Double.valueOf(strLimit);
            } catch (NumberFormatException ignored) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Whether the game is in Test Mode or hasn't been released yet
     */
    @Override
    public boolean isMaintenanceMode() {
        return BaseGameConstants.TRUE.equalsIgnoreCase(getProperty(BaseGameConstants.KEY_GAME_TESTING)) || !isReleased();
    }

    public boolean isReserveBalance() {
        return BaseGameConstants.TRUE.equalsIgnoreCase(getProperty(BaseGameConstants.KEY_RESERVE_BALANCE));
    }

    private boolean isReleased() {
        if (!getHostConfiguration().isProductionCluster()) {
            return true;
        }
        String releaseTimeString = getProperty(BaseGameConstants.KEY_RELEASE_TIME);
        if (StringUtils.isTrimmedEmpty(releaseTimeString)) {
            return true;
        } else {
            long releaseTime;
            try {
                releaseTime = Long.parseLong(releaseTimeString);
            } catch (NumberFormatException e) {
                return true;
            }
            return System.currentTimeMillis() / 1000 >= releaseTime;
        }
    }

    @Override
    public String getComboDetectorName() {
        String property = getProperty(BaseGameConstants.KEY_GAME_COMBO_DETECTOR_NAME);
        if (isTrimmedEmpty(property)) {
            return null;
        }
        return property;
    }

    @Override
    public String getLocalizedName(Locale curLocale) {
        String localizedName = MessageManager.getInstance().getApplicationMessage("game.name." + getName());
        return isTrimmedEmpty(localizedName) ? getName() : localizedName;
    }

    @Override
    public boolean isExclusive() {
        return "TRUE".equalsIgnoreCase(getProperty("EXCLUSIVE"));
    }

    @Override
    public Double getMQLeaderboardContributionPercent() {
        String value = getProperty(BaseGameConstants.KEY_MQ_LB_CONTRIBUTION);
        if (!isTrimmedEmpty(value)) {
            return Double.valueOf(value);
        }
        return null;
    }

    private Double getMiddleRTPValue(List<Double> possibleModels, Double defaultRtp) {
        List<Double> trimmed = trimExtremeRTPValues(possibleModels, defaultRtp);
        if (trimmed.size() == 3 || trimmed.size() == 2) {
            return trimmed.get(1);
        }
        Double mean = (trimmed.get(0) + trimmed.get(trimmed.size() - 1)) / 2;
        return getNearestRTPValue(trimmed, mean);
    }

    private Double getNearestRTPValue(List<Double> models, Double mark) {
        return models.stream()
                .min(Comparator.comparingDouble(m -> Math.abs(m - mark)))
                .orElse(null);
    }

    private List<Double> trimExtremeRTPValues(List<Double> possibleModels, Double defaultRtp) {
        possibleModels.removeIf(rtp -> possibleModels.size() > 3 && rtp > defaultRtp);
        possibleModels.removeIf(rtp -> possibleModels.size() > 3 && rtp < 90);
        possibleModels.sort(Comparator.naturalOrder());
        return possibleModels;
    }

    @Override
    public String getHelpUrl() {
        return getProperty(BaseGameConstants.KEY_HELP_URL);
    }

    @Override
    public String getMaxWinProbability() {
        return getProperty(BaseGameConstants.KEY_MAX_WIN_PROBABILITY);
    }

    @Override
    public com.abs.casino.common.cache.data.game.ClientGeneration getClientGeneration() {
        com.abs.casino.common.cache.data.game.ClientGeneration result =
                com.abs.casino.common.cache.data.game.ClientGeneration.UNDEFINED;
        String generation = getProperty(BaseGameConstants.KEY_CLIENT_GENERATION);
        if (!StringUtils.isTrimmedEmpty(generation)) {
            try {
                result = com.abs.casino.common.cache.data.game.ClientGeneration.valueOf(generation);
            } catch (IllegalArgumentException e) {
                LOG.error("Illegal CLIENT_GENERATION: {}, id={}", generation, id);
            }
        }
        return result;
    }

    @Override
    public String getDefaultRtp() {
        BaseGameInfo defaultGameInfo = getTemplateCache().getDefaultGameInfo(getId());
        String defaultModel = defaultGameInfo.getProperty(BaseGameConstants.KEY_CURRENT_MODEL);
        if (isTrimmedEmpty(defaultModel)) {
            return null;
        }
        return defaultModel;
    }

    @Override
    public boolean isNeedClearLasthandOnCloseGameIfRoundFinished() {
        return BaseGameConstants.TRUE.equalsIgnoreCase(getProperty(BaseGameConstants.KEY_CLEAR_LASTHAND_ON_CLOSE_GAME_IF_ROUND_FINISHED));
    }

    /**
     * Return property value separated by {@link #SEPARATOR} as list
     */
    private List<String> getPropertyAsList(String propertyName) {
        String values = getProperty(propertyName);
        if (values != null) {
            return Splitter.on(SEPARATOR).splitToList(values);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get value from list by index. If index doesn't exist in list, return null.
     * @param values list with values
     * @param index index of target value
     * @return values[index] or null
     * @param <T> values type
     */
    private <T> T getByIndexOrNull(List<T> values, Integer index) {
        T result= null;
        if (index != null && index < values.size() && index >= 0) {
            result = values.get(index);
        }
        return result;
    }

    /**
     * Calculates index of current value for properties with multi values allowed: RTP_MIN, RTP_WITHOUT_BF, RTP_MIN_WITHOUT_BF
     * For multi-model games multiValueIndex is the same with currentModelIndex,
     * for single-model games (empty possible models and currentModelIndex is null) index is 0 by default
     * @param currentModelIndex index of current model for game
     * @param singleModel game is single-model
     * @return index of current value or null
     */
    private Integer getMultiValueIndex(Integer currentModelIndex, boolean singleModel) {
        if (currentModelIndex != null) {
            return currentModelIndex;
        }
        return singleModel ? 0 : null;
    }
}
