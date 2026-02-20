package com.dgphoenix.casino.common.cache.data.game;

import com.dgphoenix.casino.common.util.InheritFromTemplate;
import com.dgphoenix.casino.common.util.property.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * User: plastical
 * Date: 26.03.2010
 */
public class BaseGameConstants {

    public static final String TRUE = "TRUE";
    public static int MAX_PLAYER_COUNT = 10000;

    private static final Map<String, Field> baseGameInfoConstantFieldNames;

    static {
        baseGameInfoConstantFieldNames = new HashMap<String, Field>();
        for (Field field : BaseGameConstants.class.getDeclaredFields()) {
            if (isConstant(field.getModifiers())) {
                try {
                    baseGameInfoConstantFieldNames.put(String.valueOf(field.get(null)), field);
                } catch (IllegalAccessException ignore) {
                }
            }
        }
    }

    public static boolean containsProperty(String propertyName) {
        return baseGameInfoConstantFieldNames.containsKey(propertyName);
    }

    private static boolean isConstant(int modifiers) {
        return Modifier.isPublic(modifiers)
                && Modifier.isStatic(modifiers)
                && Modifier.isFinal(modifiers);
    }

    public static boolean isBooleanProperty(String propertyName) {
        Field f = baseGameInfoConstantFieldNames.get(propertyName);
        return f != null && f.isAnnotationPresent(BooleanProperty.class);
    }

    public static boolean isNumericProperty(String propertyName) {
        Field f = baseGameInfoConstantFieldNames.get(propertyName);
        return f != null && f.isAnnotationPresent(NumericProperty.class);
    }

    public static boolean isMandatoryProperty(String propertyName) {
        Field f = baseGameInfoConstantFieldNames.get(propertyName);
        return f != null && f.isAnnotationPresent(MandatoryProperty.class);
    }

    public static boolean isUrlProperty(String propertyName) {
        Field f = baseGameInfoConstantFieldNames.get(propertyName);
        return f != null && f.isAnnotationPresent(UrlProperty.class);
    }

    public static boolean isClassProperty(String propertyName) {
        Field f = baseGameInfoConstantFieldNames.get(propertyName);
        return f != null && f.isAnnotationPresent(JavaClassProperty.class);
    }

    public static boolean isInheritedFromTemplate(String propertyName) {
        Field f = baseGameInfoConstantFieldNames.get(propertyName);
        return f != null && f.isAnnotationPresent(InheritFromTemplate.class);
    }

    @NumericProperty
    @InheritFromTemplate
    public static final String KEY_FAKE_ID_FOR = "FAKE_ID_FOR";

    @BooleanProperty
    public static final String KEY_LGA_APPROVED = "LGA_APPROVED";

    @NumericProperty
    public static final String KEY_REDEFINED_JP_GAME_ID = "REDEFINED_JP_GAME_ID";

    @NumericProperty
    public static final String KEY_MAX_COIN_LIMIT_EUR = "MAX_COIN_LIMIT_EUR";

    @NumericProperty
    public static final String KEY_PAYOUT_PERCENT = "PAYOUT_PERCENT";

    @StringProperty
    public static final String KEY_CHIPVALUES = "CHIPVALUES";

    @BooleanProperty
    public static final String KEY_ISENABLED = "ISENABLED";

    @NumericProperty
    public static final String KEY_DEFAULT_COIN = "DEFCOIN";

    @NumericProperty
    public static final String KEY_DEFAULTBETPERLINE = "DEFAULTBETPERLINE";

    @NumericProperty
    public static final String KEY_DEFAULTNUMLINES = "DEFAULTNUMLINES";

    @NumericProperty
    public static final String KEY_MAX_BET_1 = "MAX_BET_1";

    @NumericProperty
    public static final String KEY_MAX_BET_2 = "MAX_BET_2";

    @NumericProperty
    public static final String KEY_MAX_BET_3 = "MAX_BET_3";

    @NumericProperty
    public static final String KEY_MAX_BET_4 = "MAX_BET_4";

    @NumericProperty
    public static final String KEY_MAX_BET_5 = "MAX_BET_5";

    @NumericProperty
    public static final String KEY_MAX_BET_6 = "MAX_BET_6";

    @NumericProperty
    public static final String KEY_MAX_BET_12 = "MAX_BET_12";

    @NumericProperty
    public static final String KEY_MAX_BET_18 = "MAX_BET_18";

    @StringProperty
    public static final String KEY_GAME_IMAGE_URL = "GAME_IMAGE_URL";

    @BooleanProperty
    public static final String KEY_GAME_TESTING = "GAME_TESTING";

    @BooleanProperty
    public static final String KEY_RESERVE_BALANCE = "RESERVE_BALANCE";

    @Deprecated
    @UrlProperty
    public static final String KEY_CDN_URL = "CDN_URL"; // http(s)://server.domain

    @BooleanProperty
    public static final String KEY_CDN_SUPPORT = "CDN_SUPPORT";

    @StringProperty
    @InheritFromTemplate
    public static final String KEY_PLAYER_DEVICE_TYPE = "KEY_PLAYER_DEVICE_TYPE";

    @NumericProperty
    public static final String KEY_FREEBALANCE = "FREEBALANCE";

    @NumericProperty
    public static final String KEY_FREEBALANCE_MULTIPLIER = "FREEBALANCE_MULTIPLIER";

    @NumericProperty
    public static final String KEY_FRB_COIN = "FRB_COIN";

    @NumericProperty
    public static final String KEY_FRB_DEFAULTBETPERLINE = "FRB_BPL";

    @NumericProperty
    public static final String KEY_FRB_DEFAULTNUMLINES = "FRB_NUMLINES";

    @BooleanProperty
    public static final String KEY_IS_AUTOPLAY_GAME = "IS_AUTOPLAY_GAME";

    @NumericProperty
    public static final String KEY_WJP = "WJP";

    @BooleanProperty
    public static final String KEY_HAS_ACHIEVEMENTS = "HAS_ACHIEVEMENTS";

    @NumericProperty
    public static final String KEY_LINES_COUNT = "LINES_COUNT";

    @NumericProperty
    public static final String KEY_MAX_WIN = "MAX_WIN";

    @BooleanProperty
    public static final String KEY_GAMBLE_ALLOWED = "GAMBLE_ALLOWED";

    @StringProperty
    public static final String KEY_THIRD_PARTY_PROVIDER_NAME = "THIRD_PARTY_PROVIDER_NAME";

    @StringProperty
    public static final String KEY_THIRD_PARTY_GAME_ID = "THIRD_PARTY_GAME_ID";

    @NumericProperty
    public static final String KEY_RTP = "RTP";

    public static final String KEY_RTP_WITHOUT_BF = "RTP_WITHOUT_BF";

    @BooleanProperty
    public static final String KEY_EXCLUSIVE = "EXCLUSIVE";

    @StringProperty
    public static final String KEY_GAME_CLIENT_VERSION = "CLIENTVERSION";

    @StringProperty
    public static final String KEY_GAME_SERVER_VERSION_LASTHAND = "GAMEVERSION";

    // unused
    public static final String KEY_LIST_AVAILABLE_GAME_VERSIONS = "LIST_AVAILABLE_GAME_VERSIONS";


    //properties for dynamic reload game classes from repository (WEB-INF/rlib)
    //if this property not found game will be loaded from standart classpath; must be in form: arrival.jar
    @StringProperty
    public static final String KEY_REPOSITORY_FILE = "REPOSITORY_FILE";

    @BooleanProperty
    public static final String KEY_DEVELOPMENT_VERSION = "DEVELOPMENT_VERSION";

    @BooleanProperty
    public static final String KEY_FRB_TWO_PARAMS = "FRB_TWO_PARAMS";

    @JavaClassProperty
    public static final String KEY_GAME_EVENT_PROCESSOR_CLASS = "GAME_EVENT_PROCESSOR";

    @StringProperty
    public static final String KEY_PROFILE_ID = "PROFILE_ID";

    @StringProperty
    public static final String KEY_COINS_WITH_DISABLED_JP_WON = "COINS_WITH_DISABLED_JP_WON";

    @BooleanProperty
    @InheritFromTemplate
    public static final String KEY_IS_GGBG_GAME = "IS_GGBG_GAME";

    @NumericProperty
    public static final String KEY_MAX_BET_IN_CREDITS = "MAX_BET_IN_CREDITS";

    @StringProperty
    public static final String KEY_POSSIBLE_LINES = "POSSIBLE_LINES";

    @StringProperty
    public static final String KEY_POSSIBLE_BETPERLINES = "POSSIBLE_BETPERLINES";

    @StringProperty
    public static final String KEY_PDF_RULES_NAME = "PDF_RULES_NAME";

    @StringProperty
    public static final String KEY_HTML5PC_VERSION_MODE = "HTML5PC_VERSION_MODE";

    @StringProperty
    public static final String KEY_UNIFIED_LOCATION = "UNIFIED_LOCATION";

    // unused
    public static final String KEY_GAME_COMBOS_DISABLED = "GAME_COMBOS_DISABLED";

    @StringProperty
    public static final String KEY_GAME_COMBO_DETECTOR_NAME = "GAME_COMBO_DETECTOR_NAME";

    // unused
    public static final String KEY_RTP_NMI = "RTP_NMI";

    // unused
    public static final String KEY_RTP_MIN_NMI = "RTP_MIN_NMI";

    // unused
    public static final String KEY_RTP_MIN = "RTP_MIN";

    // unused
    public static final String KEY_RTP_MIN_WITHOUT_BF = "RTP_MIN_WITHOUT_BF";

    @NumericProperty
    public static final String KEY_ROLLOVER_PERCENT = "ROLLOVER_PERCENT";

    @BooleanProperty
    @InheritFromTemplate
    public static final String KEY_SINGLE_GAME_ID_FOR_ALL_PLATFORMS = "KEY_SINGLE_GAME_ID_FOR_ALL_PLATFORMS";

    @NumericProperty
    public static final String KEY_MQ_STAKES_RESERVE = "MQ_STAKES_RESERVE";

    @NumericProperty
    public static final String KEY_MQ_STAKES_LIMIT = "MQ_STAKES_LIMIT";

    @NumericProperty(description = "Percent of regular bets that should be added to Leaderboards in MaxQuest")
    public static final String KEY_MQ_LB_CONTRIBUTION = "MQ_LB_CONTRIBUTION";

    @BooleanProperty(description = "If enabled start player bonus should be awarded")
    public static final String KEY_MQ_AWARD_PLAYER_START_BONUS = "MQ_AWARD_PLAYER_START_BONUS";

    @StringProperty(description = "List of possible game models")
    @InheritFromTemplate
    public static final String KEY_POSSIBLE_MODELS = "POSSIBLE_MODELS";

    @StringProperty(description = "Current active game model index")
    public static final String KEY_CURRENT_MODEL = "CURRENT_MODEL";

    @StringProperty(description = "Custom url that should be opened when user clicks on help button")
    public static final String KEY_HELP_URL = "HELP_URL";

    @StringProperty
    public static final String KEY_DEMO_MODE_PARAMS = "DEMO_MODE_PARAMS";

    @BooleanProperty(description = "Is game has background image")
    @InheritFromTemplate
    public static final String KEY_HAS_BACKGROUND = "HAS_BACKGROUND";

    @StringProperty(description = "Additional properties that should be passed to game")
    @InheritFromTemplate
    public static final String KEY_ADDITIONAL_FLASHVARS = "ADDITIONAL_FLASHVARS";

    @BooleanProperty(description = "Games Levels: Whether the game supports dynamic Games Levels")
    @InheritFromTemplate
    public static final String KEY_GL_SUPPORTED = "GL_SUPPORTED";

    @NumericProperty(description = "Games Levels: Default min bet value from Product Sheet")
    @InheritFromTemplate
    public static final String KEY_GL_MIN_BET_DEFAULT = "GL_MIN_BET_DEFAULT";

    @NumericProperty(description = "Games Levels: Default max bet value from Product Sheet")
    @InheritFromTemplate
    public static final String KEY_GL_MAX_BET_DEFAULT = "GL_MAX_BET_DEFAULT";

    @NumericProperty(description = "Games Levels: Min bet value")
    public static final String KEY_GL_MIN_BET = "GL_MIN_BET";

    @NumericProperty(description = "Games Levels: Max bet value")
    public static final String KEY_GL_MAX_BET = "GL_MAX_BET";

    @NumericProperty(description = "Games Levels: Number of coins in output set")
    public static final String KEY_GL_NUMBER_OF_COINS = "GL_NUMBER_OF_COINS";

    @NumericProperty(description = "Games Levels: Max exposure")
    public static final String KEY_GL_MAX_EXPOSURE = "GL_MAX_EXPOSURE";

    @NumericProperty(description = "Games Levels: Default bet value")
    public static final String KEY_GL_DEFAULT_BET = "GL_DEFAULT_BET";

    @NumericProperty(description = "Release time, unix timestamp, seconds")
    public static final String KEY_RELEASE_TIME = "RELEASE_TIME";

    @StringProperty(description = "Game volatility as stated in the official Product Sheet")
    @InheritFromTemplate
    public static final String KEY_VOLATILITY = "VOLATILITY";

    @NumericProperty(description = "Specifies jackpot limit amount in euro cents")
    public static final String KEY_JACKPOT_LIMIT_AMOUNT = "JACKPOT_LIMIT_AMOUNT";

    @BooleanProperty(description = "Has game Buy Free Spins price decrement")
    @InheritFromTemplate
    public static final String KEY_HAS_BUY_FEATURE_PRICE_DECREMENT = "HAS_BUY_FEATURE_PRICE_DECREMENT";

    @BooleanProperty(description = "Is POV Action game")
    public static final String KEY_POV_MULTIPLAYER_ACTION_GAME = "POV_MULTIPLAYER_ACTION_GAME";

    @StringProperty(description = "Player alias for MQ tournament")
    public static final String KEY_TOURNAMENT_PLAYER_ALIAS = "TOURNAMENT_PLAYER_ALIAS";

    @NumericProperty(description = "Adm code for Eurobet integration")
    public static final String KEY_EURO_BET_ADM_CODE = "EURO_BET_ADM_CODE";

    @StringProperty(description = "Max win probability in format \"1:N\"")
    public static final String KEY_MAX_WIN_PROBABILITY = "MAX_WIN_PROBABILITY";

    @StringProperty(description = "Generation of client-side UE implementation")
    public static final String KEY_CLIENT_GENERATION = "CLIENT_GENERATION";

    @NumericProperty(description = "Multiplayer game max players limit")
    public static final String KEY_MULTIPLAYER_MAX_ROOM_PLAYERS = "MULTIPLAYER_MAX_ROOM_PLAYERS";

    @NumericProperty(description = "CrashGame max multiplier in cents (for multiplier 500.25 enter 50025)")
    public static final String KEY_CRASHGAME_MAX_MULTIPLIER = "CRASHGAME_MAX_MULTIPLIER";

    @NumericProperty(description = "CrashGame max player round profit")
    public static final String KEY_CRASHGAME_MAX_PLAYER_PROFIT = "CRASHGAME_MAX_PLAYER_PROFIT";

    @NumericProperty(description = "CrashGame max all players round profit")
    public static final String KEY_CRASHGAME_MAX_ALL_PLAYERS_PROFIT = "CRASHGAME_MAX_ALL_PLAYERS_PROFIT";

    @BooleanProperty(description = "Show TripleMaxBlast decorations (asteroids) for CrashGame")
    public static final String KEY_CRASHGAME_IS_TRIPLE_MAX_BLAST = "CRASHGAME_IS_TRIPLE_MAX_BLAST";

    @BooleanProperty(description = "Is needed clear lasthand at game session and round finished")
    @InheritFromTemplate
    public static final String KEY_CLEAR_LASTHAND_ON_CLOSE_GAME_IF_ROUND_FINISHED = "CLEAR_LASTHAND_ON_CLOSE_GAME_IF_ROUND_FINISHED";

    @NumericProperty(description = "ID of game with linked JP4 bank")
    @InheritFromTemplate
    public static final String KEY_HANDLE_UNJ_LINKED_GAMEID = "HANDLE_UNJ_LINKED_GAMEID";

    @StringProperty(description = "Contains the name of the folder from which the static will be loaded")
    public static final String KEY_MP_GAME_FOLDER_NAME = "MP_GAME_FOLDER_NAME";

    @StringProperty(description = "Custom url that should be opened when user clicks on Change BuyIn in MQ BTG game")
    public static final String KEY_BTG_BUY_IN_SELECT_URL = "BTG_BUY_IN_SELECT_URL";

    @BooleanProperty(description = "Whether Buy Feature is disabled")
    public static final String KEY_BUY_FEATURE_DISABLED = "BUY_FEATURE_DISABLED";

    @BooleanProperty(description = "Whether Buy Feature is disabled for Cash Bonus")
    public static final String KEY_BUY_FEATURE_DISABLED_FOR_CASH_BONUS = "BUY_FEATURE_DISABLED_FOR_CASH_BONUS";
}
