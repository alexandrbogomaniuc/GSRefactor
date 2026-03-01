package com.abs.casino.common.cache.data.game;

import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.abs.casino.common.cache.data.game.GameLanguageHelper;
import com.abs.casino.common.cache.data.game.RoundFinishedHelper;
import com.abs.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.abs.casino.common.cache.data.IDistributedConfigEntry;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.configuration.messages.MessageManager;
import com.abs.casino.common.util.property.PropertyUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.dgphoenix.casino.common.cache.data.bank.BankInfo.MAP_SPLITTER;
import static com.abs.casino.common.util.property.PropertyUtils.*;
import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

public class BaseGameInfoTemplate implements IDistributedConfigEntry, Identifiable, KryoSerializable, JsonSelfSerializable<BaseGameInfoTemplate> {
    private static final Logger LOG = LogManager.getLogger(BaseGameInfoTemplate.class);
    private static final int VERSION = 1;
    private static final String DEFAULT_TESTSTAND_ROWCOUNT = "4";

    private long gameId;
    private String gameName;
    private boolean jackpotGame;
    private Integer gameVariationIdBase;
    private BaseGameInfo defaultGameInfo;
    private String servlet;
    private String gameControllerClass;
    private String endRoundSignature;
    private RoundFinishedHelper roundFinishedHelper;
    private String title;
    private String swfLocation;
    private String additionalParams;
    private boolean oldTranslation;
    private boolean isFrbGame;

    private transient GameLanguageHelper gameLanguageHelper;

    public BaseGameInfoTemplate() {

    }

    public BaseGameInfoTemplate(long gameId, String gameName, Integer gameVariationId, BaseGameInfo defaultGameInfo,
                                boolean jackpotGame, String servlet) {
        this.gameId = gameId;
        this.gameName = gameName;
        gameVariationIdBase = gameVariationId;
        this.defaultGameInfo = defaultGameInfo;
        this.jackpotGame = jackpotGame;
        this.servlet = servlet;
    }

    @Override
    public long getId() {
        return gameId;
    }

    public Integer getGameVariationId(ClientType clientType) {
        if (isInRange(gameVariationIdBase, 4001, 4499)
                || isInRange(gameVariationIdBase, 4501, 4999)
                || isInRange(gameVariationIdBase, 5001, 5498)) {
            return gameVariationIdBase;
        } else {
            Integer gameVariationId = gameVariationIdBase;
            if (gameVariationId != null) {
                return clientType != ClientType.AIR ? gameVariationId : (gameVariationId + 100);
            }
            return null;
        }
    }

    private static boolean isInRange(Integer target, Integer a, Integer b) {
        return target != null && a != null && b != null && (a <= target) && (target < b);
    }

    public String getLocalizedGameName(String lang) {
        if (lang.equalsIgnoreCase("en")) {
            return MessageManager.getInstance().getApplicationMessage("game.name." + gameName);
        } else {
            return null;
        }
    }

    public String getLocalizedGameName(Locale locale) {
        if (locale.equals(Locale.ENGLISH) || locale.equals(Locale.US)) {
            return MessageManager.getInstance().getApplicationMessage("game.name." + gameName);
        } else {
            return null;
        }
    }

    public String getServlet() {
        return servlet;
    }

    public void setServlet(String servlet) {
        this.servlet = servlet;
    }

    public long getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setGameVariationIdBase(Integer gameVariationIdBase) {
        this.gameVariationIdBase = gameVariationIdBase;
    }

    public BaseGameInfo getDefaultGameInfo() {
        return defaultGameInfo;
    }

    public Integer getGameVariationIdBase() {
        return gameVariationIdBase;
    }

    public String getGameControllerClass() {
        return gameControllerClass;
    }

    public void setGameControllerClass(String gameControllerClass) {
        this.gameControllerClass = gameControllerClass;
    }

    public String getEndRoundSignature() {
        return endRoundSignature;
    }

    public void setEndRoundSignature(String endRoundSignature) {
        this.endRoundSignature = endRoundSignature;
    }

    public RoundFinishedHelper getRoundFinishedHelper() {
        return roundFinishedHelper;
    }

    public void setRoundFinishedHelper(RoundFinishedHelper roundFinishedHelper) {
        this.roundFinishedHelper = roundFinishedHelper;
    }

    public boolean isRoundFinished(String lasthand) {
        if (isTrimmedEmpty(lasthand)) {
            return true;
        }
        if (roundFinishedHelper != null) {
            return roundFinishedHelper.isRoundFinished(lasthand, endRoundSignature);
        }
        return false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSwfLocation() {
        return swfLocation;
    }

    public void setSwfLocation(String swfLocation) {
        this.swfLocation = swfLocation;
    }

    public String getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(String additionalParams) {
        this.additionalParams = additionalParams;
    }

    public boolean isOldTranslation() {
        return oldTranslation;
    }

    public void setOldTranslation(boolean oldTranslation) {
        this.oldTranslation = oldTranslation;
    }

    public boolean isDynamicLevelsSupported() {
        return getBooleanProperty(defaultGameInfo.propertiesMap, BaseGameConstants.KEY_GL_SUPPORTED);
    }

    public Long getMinBet() {
        return getLongProperty(defaultGameInfo.propertiesMap, BaseGameConstants.KEY_GL_MIN_BET_DEFAULT);
    }

    public Long getMaxBet() {
        return getLongProperty(defaultGameInfo.propertiesMap, BaseGameConstants.KEY_GL_MAX_BET_DEFAULT);
    }

    public String getVolatility() {
        return getStringProperty(defaultGameInfo.propertiesMap, BaseGameConstants.KEY_VOLATILITY);
    }

    public boolean isReadyForRelease() {
        return !isTrimmedEmpty(getVolatility())
                && !isTrimmedEmpty(defaultGameInfo.getProperty(BaseGameConstants.KEY_RELEASE_TIME))
                && !isTrimmedEmpty(defaultGameInfo.getProperty(BaseGameConstants.KEY_MAX_WIN));
    }

    public String getMultiplayerGameFolderName() {
        return getStringProperty(defaultGameInfo.propertiesMap, BaseGameConstants.KEY_MP_GAME_FOLDER_NAME);
    }

    public String getBuyInSelectUrl() {
        return getStringProperty(defaultGameInfo.propertiesMap, BaseGameConstants.KEY_BTG_BUY_IN_SELECT_URL);
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        BaseGameInfoTemplate from = (BaseGameInfoTemplate) entry;
        gameName = from.gameName;
        jackpotGame = from.jackpotGame;
        gameVariationIdBase = from.gameVariationIdBase;
        defaultGameInfo = from.defaultGameInfo;
        servlet = from.servlet;
        gameControllerClass = from.gameControllerClass;
        endRoundSignature = from.endRoundSignature;
        roundFinishedHelper = from.roundFinishedHelper;
        title = from.title;
        swfLocation = from.swfLocation;
        additionalParams = from.additionalParams;
        oldTranslation = from.oldTranslation;
        isFrbGame = from.isFrbGame;
    }

    @Override
    public String toString() {
        String str = "";
        str += "BaseGameInfoTemplate [";
        str += "gameId=" + gameId;
        str += ", gameName=" + gameName;
        str += ", gameVariationId=" + gameVariationIdBase;
        str += ", defaultGameInfo=" + defaultGameInfo;
        str += ", jackpotGame=" + jackpotGame;
        str += ", servlet=" + servlet;
        str += ", gameControllerClass=" + gameControllerClass;
        str += ", endRoundSignature=" + endRoundSignature;
        str += ", roundFinishedHelper=" + roundFinishedHelper;
        str += ", title=" + title;
        str += ", swfLocation=" + swfLocation;
        str += ", additionalParams=" + additionalParams;
        str += ", oldTranslation=" + oldTranslation;
        str += ", isFrbGame=" + isFrbGame;
        str += "]";
        return str;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION);
        output.writeLong(gameId);
        output.writeString(gameName);
        output.writeBoolean(jackpotGame);
        kryo.writeObjectOrNull(output, gameVariationIdBase, Integer.class);
        kryo.writeObject(output, defaultGameInfo);
        output.writeString(servlet);
        output.writeString(gameControllerClass);
        output.writeString(endRoundSignature);
        output.writeString(roundFinishedHelper == null ? null : roundFinishedHelper.name());
        output.writeString(title);
        output.writeString(swfLocation);
        output.writeString(additionalParams);
        output.writeBoolean(oldTranslation);
        output.writeBoolean(isFrbGame);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt();
        gameId = input.readLong();
        gameName = input.readString();
        jackpotGame = input.readBoolean();
        gameVariationIdBase = kryo.readObjectOrNull(input, Integer.class);
        defaultGameInfo = kryo.readObject(input, BaseGameInfo.class);
        servlet = input.readString();
        gameControllerClass = input.readString();
        endRoundSignature = input.readString();
        String s = input.readString();
        if (!isTrimmedEmpty(s)) {
            try {
                roundFinishedHelper = RoundFinishedHelper.valueOf(s.trim());
            } catch (IllegalArgumentException e) {
                LOG.error("Cannot read roundFinishedHelper, gameId={}, error={}", gameId, e.getMessage());
            }
        }
        title = input.readString();
        swfLocation = input.readString();
        try {
            additionalParams = input.readString();
        } catch (KryoException e) {
            LOG.error("Cannot read additionalParams, gameId={}, error={}. Please fix immediate",
                    gameId, e.getMessage());
            return;
        }
        oldTranslation = input.readBoolean();
        if (ver > 0) {
            isFrbGame = input.readBoolean();
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("gameId", gameId);
        gen.writeStringField("gameName", gameName);
        gen.writeBooleanField("jackpotGame", jackpotGame);
        serializeNumberOrNull(gen, "gameVariationIdBase", gameVariationIdBase);
        gen.writeObjectField("defaultGameInfo", defaultGameInfo);
        gen.writeStringField("servlet", servlet);
        gen.writeStringField("gameControllerClass", gameControllerClass);
        gen.writeStringField("endRoundSignature", endRoundSignature);
        gen.writeStringField("roundFinishedHelper", roundFinishedHelper == null ? null : roundFinishedHelper.name());
        gen.writeStringField("title", title);
        gen.writeStringField("swfLocation", swfLocation);
        gen.writeStringField("additionalParams", additionalParams);
        gen.writeBooleanField("oldTranslation", oldTranslation);
        gen.writeBooleanField("isFrbGame", isFrbGame);
    }

    @Override
    public BaseGameInfoTemplate deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        gameId = node.get("gameId").longValue();
        gameName = node.get("gameName").textValue();
        jackpotGame = node.get("jackpotGame").booleanValue();
        gameVariationIdBase = deserializeOrNull(om, node.get("gameVariationIdBase"), Integer.class);
        defaultGameInfo = om.convertValue(node.get("defaultGameInfo"), BaseGameInfo.class);
        servlet = node.get("servlet").textValue();
        gameControllerClass = node.get("gameControllerClass").textValue();
        endRoundSignature = node.get("endRoundSignature").textValue();
        String s = node.get("roundFinishedHelper").textValue();
        if (!isTrimmedEmpty(s)) {
            try {
                roundFinishedHelper = RoundFinishedHelper.valueOf(s.trim());
            } catch (IllegalArgumentException e) {
                LOG.error("Cannot read roundFinishedHelper, gameId={}, error={}", gameId, e.getMessage());
            }
        }
        title = node.get("title").textValue();
        swfLocation = node.get("swfLocation").textValue();
        additionalParams = readNullableText(node, "additionalParams");
        oldTranslation = node.get("oldTranslation").booleanValue();
        isFrbGame = node.get("isFrbGame").booleanValue();

        return this;
    }

    public List<String> getGameLanguagesPaths(String languageCode) {
        GameLanguageHelper gameLanguageHelper = getGameLanguageHelper();
        if (gameLanguageHelper != null) {
            return gameLanguageHelper.getGameLanguagesPaths(languageCode);
        }
        return null;
    }

    public GameLanguageHelper getGameLanguageHelper() {
        if (gameLanguageHelper == null) {
            synchronized (this) {
                if (gameLanguageHelper == null) {
                    initGameLanguageHelper();
                }
            }
        }
        return gameLanguageHelper;
    }

    private void initGameLanguageHelper() {
        if (gameId == 146) {
            gameLanguageHelper = GameLanguageHelper.TENSBETTER;
        }
    }

    public boolean isFrbGame() {
        return isFrbGame;
    }

    public void setFrbGame(boolean isFrbGame) {
        this.isFrbGame = isFrbGame;
    }

    public boolean isSingleGameIdForAllPlatforms() {
        return PropertyUtils.getBooleanProperty(defaultGameInfo.getProperties(),
                BaseGameConstants.KEY_SINGLE_GAME_ID_FOR_ALL_PLATFORMS);
    }

    public GameType getGameType() {
        return defaultGameInfo.getGameType();
    }

    public boolean isMultiplayerGame() {
        return getGameType() == GameType.MP;
    }

    public boolean isPovMultiplayerGame() {
        return PropertyUtils.getBooleanProperty(defaultGameInfo.getProperties(),
                BaseGameConstants.KEY_POV_MULTIPLAYER_ACTION_GAME);
    }

    public boolean isBattleGroundsMultiplayerGame() {
        return getGameType() == GameType.MP && getGameName().startsWith("BG_");
    }

    public int getMaxCredits() {
        if (!defaultGameInfo.getVariableType().equals(GameVariableType.COIN)) return 0;
        int maxCredits = 0;
        String strAvailableBetPerLines = defaultGameInfo.getProperty(BaseGameConstants.KEY_POSSIBLE_BETPERLINES);
        String strAvailableLines = defaultGameInfo.getProperty(BaseGameConstants.KEY_POSSIBLE_LINES);
        if (!StringUtils.isTrimmedEmpty(strAvailableBetPerLines) &&
                !StringUtils.isTrimmedEmpty(strAvailableLines)) {
            String[] arrayStrBetsPerLine = strAvailableBetPerLines.split("\\|");
            String[] arrayStrLines = strAvailableLines.split("\\|");
            int maxBetPerLine = Integer.parseInt(arrayStrBetsPerLine[arrayStrBetsPerLine.length - 1]);
            int maxLines = Integer.parseInt(arrayStrLines[arrayStrLines.length - 1]);
            maxCredits = maxBetPerLine * maxLines;
        }
        return maxCredits;
    }

    public int getMinCredits() {
        if (!defaultGameInfo.getVariableType().equals(GameVariableType.COIN)) return 0;
        int minCredits = 0;
        String strAvailableBetPerLines = defaultGameInfo.getProperty(BaseGameConstants.KEY_POSSIBLE_BETPERLINES);
        String strAvailableLines = defaultGameInfo.getProperty(BaseGameConstants.KEY_POSSIBLE_LINES);
        if (!StringUtils.isTrimmedEmpty(strAvailableBetPerLines) &&
                !StringUtils.isTrimmedEmpty(strAvailableLines)) {
            String[] arrayStrBetsPerLine = strAvailableBetPerLines.split("\\|");
            String[] arrayStrLines = strAvailableLines.split("\\|");
            int minBetPerLine = Integer.parseInt(arrayStrBetsPerLine[0]);
            int minLines = Integer.parseInt(arrayStrLines[0]);
            minCredits = minBetPerLine * minLines;
        }
        return minCredits;
    }

    public boolean isReplaceableFlashGame() {
        return isPcVersionFlashOnly() && isHasToGoClient();
    }

    public boolean isPureFlashGame() {
        return isPcVersionFlashOnly() && !isHasToGoClient();
    }

    private boolean isPcVersionFlashOnly() {
        return defaultGameInfo.getPlayerDeviceType() == null
                && defaultGameInfo.getHtml5PcVersionMode()
                == com.abs.casino.common.cache.data.game.Html5PcVersionMode.NOT_AVAILABLE;
    }

    private boolean isHasToGoClient() {
        return !StringUtils.isTrimmedEmpty(defaultGameInfo.getProperty("WINDOWSPHONE")) &&
                !StringUtils.isTrimmedEmpty(defaultGameInfo.getProperty("IOSMOBILE")) &&
                !StringUtils.isTrimmedEmpty(defaultGameInfo.getProperty("ANDROID"));
    }

    public Map<String, String> getAdditionalFlashVars() {
        String additionalFlashVarsAsString = getStringProperty(defaultGameInfo.getProperties(), BaseGameConstants.KEY_ADDITIONAL_FLASHVARS);
        if (isTrimmedEmpty(additionalFlashVarsAsString)) {
            return Maps.newHashMap();
        }
        return MAP_SPLITTER.split(additionalFlashVarsAsString);
    }

    public String getMaxWinProbability() {
        return defaultGameInfo != null ? defaultGameInfo.getMaxWinProbability() : null;
    }
}
