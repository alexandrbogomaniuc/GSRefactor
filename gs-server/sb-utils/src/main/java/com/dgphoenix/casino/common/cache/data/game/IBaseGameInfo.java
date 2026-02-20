package com.dgphoenix.casino.common.cache.data.game;

import com.dgphoenix.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.cache.data.account.PlayerDeviceType;
import com.dgphoenix.casino.common.cache.data.bank.ICoin;
import com.dgphoenix.casino.common.cache.data.bank.ILimit;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.exception.CommonException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 18.07.17.
 */
public interface IBaseGameInfo<COIN extends ICoin, LIMIT extends ILimit> extends Identifiable {

    void setId(long id);

    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    Map<String, String> getPropertiesMap();

    String getProperty(String propertyName);

    void setProperty(String key, String value);

    void removeProperty(String key);

    String getExternalId();

    void setExternalId(String externalId);

    String getServlet();

    void setServlet(String servlet);

    void setMobile(boolean isMobile);

    boolean isMobile();

    PlayerDeviceType getPlayerDeviceType();

    long getBankId();

    void setBankId(long bankId);

    String getName();

    void setName(String name);

    ICurrency getCurrency();

    void setCurrency(ICurrency currency);

    GameType getGameType();

    void setGameType(GameType gameType);

    GameGroup getGroup();

    void setGroup(GameGroup group);

    String getRmClassName();

    void setRmClassName(String rmClassName);

    GameVariableType getVariableType();

    void setVariableType(GameVariableType variableType);

    String getGsClassName();

    void setGsClassName(String gsClassName);

    boolean isEnabled();

    boolean isBankEnabled();

    void setEnabled(boolean flag);

    double getPayoutPercent();

    void setPayoutPrecent(double percent);

    double getRtp();

    double getRtp(boolean isCashBonusSession);

    double getRtp(Long bankId, boolean isCashBonusSession);

    Double getRtpMin(boolean isCashBonusSession);

    Double getRtpMin(Long bankId, boolean isCashBonusSession);

    long getAdmCode() throws CommonException;

    void setAdmCode(long admCode);

    void setDefaultCoin(int coinPosition);

    Integer getDefaultCoin();

    void setWJP(int wjp);

    Integer getWJP();

    String getCDNUrl();

    String getThirdPartyGameId();

    boolean isThirdPartyGame();

    String getThirdPartyProviderName();

    String getChipValues();

    void setChipValues(String chipValues);

    List<COIN> getCoins();

    void setCoins(List<COIN> coins);

    LIMIT getLimit();

    void setLimit(LIMIT limit);

    int getCoinsSize();

    Set<Long> getCoinsWithDisabledJPWon();

    Double getJackpotWinLimit();

    IBaseGameInfo copy();

    IBaseGameInfo lightCopy();

    boolean hasLimit();

    boolean hasCoins();

    boolean isExist(String language);

    List<String> getLanguages();

    void setLanguages(List<String> languages);

    void removeAllLanguages();

    void addLanguage(String language);

    boolean isAutoplayGame();

    boolean isHasAchievements();

    Html5PcVersionMode getHtml5PcVersionMode();

    String getRepositoryFile();

    boolean isDevelopmentVersion();

    String getGameEventProcessorClass();

    String getProfileId();

    long getLastUpdateDate();

    void setLastUpdateDate(long lastUpdateDate);

    String getComboDetectorName();

    boolean isMaintenanceMode();

    void marshal(HierarchicalStreamWriter writer, MarshallingContext context);

    String getLocalizedName(Locale curLocale);

    boolean isExclusive();

    Double getMQLeaderboardContributionPercent();

    String getHelpUrl();

    String getMaxWinProbability();

    ClientGeneration getClientGeneration();

    String getDefaultRtp();

    boolean isNeedClearLasthandOnCloseGameIfRoundFinished();
}
