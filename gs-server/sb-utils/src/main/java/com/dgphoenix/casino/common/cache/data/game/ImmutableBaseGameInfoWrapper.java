package com.abs.casino.common.cache.data.game;

import com.dgphoenix.casino.common.cache.data.account.PlayerDeviceType;
import com.dgphoenix.casino.common.cache.data.bank.ICoin;
import com.dgphoenix.casino.common.cache.data.game.GameGroup;
import com.dgphoenix.casino.common.cache.data.game.GameType;
import com.dgphoenix.casino.common.cache.data.game.GameVariableType;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.bank.ILimit;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 20.07.17.
 */
public class ImmutableBaseGameInfoWrapper implements IBaseGameInfo {

    private long id;
    private String name;
    private final IBaseGameInfo info;
    private final long bankId;
    private final boolean bankEnabled;
    private final Map<String, String> propertiesMap;
    private final List<String> languages;
    private final ILimit limit;
    private final List<ICoin> coins;

    public ImmutableBaseGameInfoWrapper(long id, String name, long bankId, IBaseGameInfo info, boolean bankEnabled,
                                        Map<String, String> properties, List<String> languages,
                                        ILimit limit, List<ICoin> coins) {
        this(bankId, info, bankEnabled, properties, languages, limit, coins);
        this.id = id;
        this.name = name;
    }

    public ImmutableBaseGameInfoWrapper(long bankId, IBaseGameInfo info, boolean bankEnabled,
                                        Map<String, String> properties, List<String> languages,
                                        ILimit limit, List<ICoin> coins) {
        this.id = info.getId();
        this.name = info.getName();
        this.bankId = bankId;
        this.info = info;
        this.propertiesMap = properties != null ? ImmutableMap.<String, String>builder()
                .putAll(Maps.filterValues(properties, Predicates.notNull())).build() : null;
        this.bankEnabled = bankEnabled;
        this.languages = languages;
        this.limit = limit;
        this.coins = coins;
    }

    private void methodNotSupported() {
        throw new RuntimeException("Unmodifiable object");
    }

    @Override
    public void setId(long id) {
        methodNotSupported();
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
    public String getProperty(String propertyName) {
        return propertiesMap.get(propertyName);
    }

    @Override
    public void setProperty(String key, String value) {
        methodNotSupported();
    }

    @Override
    public void removeProperty(String key) {
        methodNotSupported();
    }

    @Override
    public String getExternalId() {
        return info.getExternalId();
    }

    @Override
    public long getAdmCode() throws CommonException{
        return info.getAdmCode();
    }

    @Override
    public void setAdmCode(long admCode) {

    }

    @Override
    public void setExternalId(String externalId) {
        methodNotSupported();
    }

    @Override
    public String getServlet() {
        return info.getServlet();
    }

    @Override
    public void setServlet(String servlet) {
        methodNotSupported();
    }

    @Override
    public void setMobile(boolean isMobile) {
        methodNotSupported();
    }

    @Override
    public boolean isMobile() {
        return info.isMobile();
    }

    @Override
    public PlayerDeviceType getPlayerDeviceType() {
        return info.getPlayerDeviceType();
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public void setBankId(long bankId) {
        methodNotSupported();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        methodNotSupported();
    }

    @Override
    public ICurrency getCurrency() {
        return info.getCurrency();
    }

    @Override
    public void setCurrency(ICurrency currency) {
        methodNotSupported();
    }

    @Override
    public GameType getGameType() {
        return info.getGameType();
    }

    @Override
    public void setGameType(GameType gameType) {
        methodNotSupported();
    }

    @Override
    public GameGroup getGroup() {
        return info.getGroup();
    }

    @Override
    public void setGroup(GameGroup group) {
        methodNotSupported();
    }

    @Override
    public String getRmClassName() {
        return info.getRmClassName();
    }

    @Override
    public void setRmClassName(String rmClassName) {
        methodNotSupported();
    }

    @Override
    public GameVariableType getVariableType() {
        return info.getVariableType();
    }

    @Override
    public void setVariableType(GameVariableType variableType) {
        methodNotSupported();
    }

    @Override
    public String getGsClassName() {
        return info.getGsClassName();
    }

    @Override
    public void setGsClassName(String gsClassName) {
        methodNotSupported();
    }

    @Override
    public boolean isEnabled() {
        return info.isEnabled();
    }

    @Override
    public boolean isBankEnabled() {
        return bankEnabled;
    }

    @Override
    public void setEnabled(boolean flag) {
        methodNotSupported();
    }

    @Override
    public double getPayoutPercent() {
        return info.getPayoutPercent();
    }

    @Override
    public void setPayoutPrecent(double percent) {
        methodNotSupported();
    }

    @Override
    public double getRtp() {
        return info.getRtp(getBankId(), false);
    }

    @Override
    public double getRtp(boolean isCashBonusSession) {
        return info.getRtp(getBankId(), isCashBonusSession);
    }

    @Override
    public double getRtp(Long bankId, boolean isCashBonusSession) {
        return info.getRtp(bankId, isCashBonusSession);
    }

    public Double getRtpMin(boolean isCashBonusSession) {
        return info.getRtpMin(getBankId(), isCashBonusSession);
    }

    @Override
    public Double getRtpMin(Long bankId, boolean isCashBonusSession) {
        return info.getRtpMin(bankId, isCashBonusSession);
    }

    @Override
    public void setDefaultCoin(int coinPosition) {
        methodNotSupported();
    }

    @Override
    public Integer getDefaultCoin() {
        return info.getDefaultCoin();
    }

    @Override
    public void setWJP(int wjp) {
        methodNotSupported();
    }

    @Override
    public Integer getWJP() {
        return info.getWJP();
    }

    @Override
    public String getCDNUrl() {
        return info.getCDNUrl();
    }

    @Override
    public String getThirdPartyGameId() {
        return info.getThirdPartyGameId();
    }

    @Override
    public boolean isThirdPartyGame() {
        return info.isThirdPartyGame();
    }

    @Override
    public String getThirdPartyProviderName() {
        return info.getThirdPartyProviderName();
    }

    @Override
    public String getChipValues() {
        return info.getChipValues();
    }

    @Override
    public void setChipValues(String chipValues) {
        methodNotSupported();
    }

    @Override
    public List<ICoin> getCoins() {
        return coins;
    }

    @Override
    public void setCoins(List list) {
        methodNotSupported();
    }

    @Override
    public ILimit getLimit() {
        return limit;
    }

    @Override
    public void setLimit(ILimit limit) {
        methodNotSupported();
    }

    @Override
    public int getCoinsSize() {
        List<ICoin> coins = getCoins();
        return coins == null ? 0 : coins.size();
    }

    @Override
    public Set<Long> getCoinsWithDisabledJPWon() {
        return info.getCoinsWithDisabledJPWon();
    }

    @Override
    public Double getJackpotWinLimit() {
        return info.getJackpotWinLimit();
    }

    @Override
    public IBaseGameInfo copy() {
        return new ImmutableBaseGameInfoWrapper(bankId, info, bankEnabled, propertiesMap, languages, limit, coins);
    }

    @Override
    public IBaseGameInfo lightCopy() {
        return copy();
    }

    @Override
    public boolean hasLimit() {
        return limit != null;
    }

    @Override
    public boolean hasCoins() {
        return !CollectionUtils.isEmpty(getCoins());
    }

    @Override
    public boolean isExist(String language) {
        List<String> languages = getLanguages();
        return (language != null) && (languages != null) && languages.contains(language);
    }

    @Override
    public List<String> getLanguages() {
        return languages;
    }

    @Override
    public void removeAllLanguages() {
        methodNotSupported();
    }

    @Override
    public void addLanguage(String language) {
        methodNotSupported();
    }

    @Override
    public boolean isAutoplayGame() {
        return info.isAutoplayGame();
    }

    @Override
    public boolean isHasAchievements() {
        return info.isHasAchievements();
    }

    @Override
    public Html5PcVersionMode getHtml5PcVersionMode() {
        return info.getHtml5PcVersionMode();
    }

    @Override
    public String getRepositoryFile() {
        return info.getRepositoryFile();
    }

    @Override
    public boolean isDevelopmentVersion() {
        return info.isDevelopmentVersion();
    }

    @Override
    public String getGameEventProcessorClass() {
        return info.getGameEventProcessorClass();
    }

    @Override
    public String getProfileId() {
        return info.getProfileId();
    }

    @Override
    public long getLastUpdateDate() {
        return info.getLastUpdateDate();
    }

    @Override
    public void setLastUpdateDate(long lastUpdateDate) {
        methodNotSupported();
    }

    @Override
    public String getComboDetectorName() {
        return info.getComboDetectorName();
    }

    @Override
    public boolean isMaintenanceMode() {
        return info.isMaintenanceMode();
    }

    @Override
    public void marshal(HierarchicalStreamWriter writer, MarshallingContext context) {
        info.marshal(writer, context);
    }

    @Override
    public void setLanguages(List languages) {
        methodNotSupported();
    }

    @Override
    public void setProperties(Map properties) {
        methodNotSupported();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getLocalizedName(Locale curLocale) {
        return info.getLocalizedName(curLocale);
    }

    @Override
    public boolean isExclusive() {
        return info.isExclusive();
    }

    @Override
    public Double getMQLeaderboardContributionPercent() {
        return info.getMQLeaderboardContributionPercent();
    }

    @Override
    public String getHelpUrl() {
        return info.getHelpUrl();
    }

    @Override
    public String toString() {
        return "ImmutableBaseGameInfoWrapper[" +
                "id=" + id +
                ", name=" + name +
                ", bankId=" + bankId +
                ", bankEnabled=" + bankEnabled +
                ", info=" + info +
                ", propertiesMap=" + StringUtils.printProperties(getPropertiesMap()) +
                ']';
    }

    @Override
    public String getMaxWinProbability() {
        return info.getMaxWinProbability();
    }

    @Override
    public ClientGeneration getClientGeneration() {
        return info.getClientGeneration();
    }

    @Override
    public String getDefaultRtp() {
        return info.getDefaultRtp();
    }

    @Override
    public boolean isNeedClearLasthandOnCloseGameIfRoundFinished() {
        return info.isNeedClearLasthandOnCloseGameIfRoundFinished();
    }
}
