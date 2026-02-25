package com.dgphoenix.casino.common.cache.data.bank;

import com.dgphoenix.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.util.BidirectionalMultivalueMap;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.LogoutActionType;
import com.dgphoenix.casino.common.util.RefererDomains;
import com.dgphoenix.casino.common.util.property.*;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.Registration;
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
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.dgphoenix.casino.common.util.property.PropertyUtils.getBooleanProperty;
import static com.dgphoenix.casino.common.util.property.PropertyUtils.getLongProperty;
import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class BankInfo implements IDistributedConfigEntry, Identifiable, 
        KryoSerializable, JsonSelfSerializable<BankInfo> {

    private static final Logger LOG = LogManager.getLogger(BankInfo.class);
    private static final int VERSION = 2;
    public static final Splitter.MapSplitter MAP_SPLITTER = Splitter.on(";").omitEmptyStrings().withKeyValueSeparator("=");

    @BooleanProperty(description = "If enabled, enables bank")
    public static final String KEY_ISENABLED = "ISENABLED";

    //WalletConfiguration.properties
    @StringProperty(description = "Sets common wallet manager class")
    public static final String KEY_WPM_CLASS = "WPM_CLASS";
    @StringProperty(description = "Sets type of common wallet manager")
    public static final String KEY_CWM_TYPE = "CWM_TYPE";
    @BooleanProperty(description = "If enabled, turns on supporting of refund bets")
    public static final String KEY_CW_REFUND_SUPPORTED = "CW_REFUND_SUPPORTED";
    @BooleanProperty(description = "If enabled, turns on supporting bonus parts for CW4: Specifies what part of " +
            "the bet/win was done/applied on/to bonus money")
    public static final String KEY_CW4_BONUS_PARTS_SUPPORTED = "CW4_BONUS_PARTS_SUPPORTED";
    @BooleanProperty(description = "If enabled, common wallet manager sends bet amount in dollars for credit/debit operations")
    public static final String KEY_CW_SEND_AMOUNT_IN_DOLLARS = "CW_SEND_AMOUNT_IN_DOLLARS";
    @BooleanProperty(description = "If enabled, doesn't add negative bet amount to win amount")
    public static final String KEY_CW_NOT_ADD_NEGATIVE_BET_TO_WIN = "CW_NOT_ADD_NEGATIVE_BET_TO_WIN";

    @BooleanProperty(description = "If enabled, turns on stub mode")
    public static final String KEY_STUB_MODE = "STUB_MODE";

    @BooleanProperty(description = "If enabled, turns on token mode: sends token in request")
    public static final String KEY_ADD_TOKEN_MODE = "ADD_TOKEN_MODE";

    @BooleanProperty(description = "If enabled, turns on saving and sending token")
    public static final String KEY_SAVE_AND_SEND_TOKEN_IN_GAME_WALLET_MODE = "SAVE_AND_SEND_TOKEN_IN_GAME_WALLET_MODE";

    @NumericProperty(description = "Sets Master bank id")
    public static final String KEY_MASTER_BANK_ID = "MASTER_BANK_ID";
    @BooleanProperty(description = "If enabled, turns on sending bankId to external api")
    public static final String KEY_SEND_BANKID_TO_EXT_API = "SEND_BANKID_TO_EXT_API";
    @BooleanProperty(description = "If enabled, allow update players status in private room over BattlegroundPrivateRoomController")
    public static final String KEY_ALLOW_UPDATE_PLAYERS_STATUS_IN_PRIVATE_ROOM = "ALLOW_UPDATE_PLAYERS_STATUS_IN_PRIVATE_ROOM";
    @StringProperty(description = "Update player status in private room url")
    public static final String KEY_UPDATE_PLAYER_STATUS_IN_PRIVATE_ROOM_URL = "UPDATE_PLAYER_STATUS_IN_PRIVATE_ROOM_URL";
    @StringProperty(description = "Update players rooms number url")
    public static final String KEY_UPDATE_PLAYERS_ROOMS_NUMBER_URL = "UPDATE_PLAYERS_ROOMS_NUMBER_URL";
    @StringProperty(description = "Get friends url")
    public static final String KEY_GET_FRIENDS_URL = "GET_FRIENDS_URL";
    @StringProperty(description = "Invite Players to Private Room url")
    public static final String KEY_INVITE_PLAYERS_TO_PRIVATE_ROOM_URL = "INVATE_PLAYERS_TO_PRIVATE_ROOM_URL";
    @StringProperty(description = "Get Players online Status")
    public static final String KEY_GET_PLAYERS_ONLINE_STATUS_URL = "GET_PLAYERS_ONLINE_STATUS_URL";
    @StringProperty(description = "Sets common wallet request client class")
    public static final String KEY_CW_REQUEST_CLIENT_CLASS = "COMMON_WALLET_REQUEST_CLIENT_CLASS";
    @StringProperty(description = "Sets common wallet WS url")
    public static final String KEY_CW_WS_URL = "COMMON_WALLET_WS_URL";
    @StringProperty(description = "Sets common wallet cancel transaction url")
    public static final String KEY_CW_CANCEL_URL = "COMMON_WALLET_CANCEL_URL";
    @StringProperty(description = "Sets common wallet balance url")
    public static final String KEY_CW_BALANCE_URL = "COMMON_WALLET_BALANCE_URL";
    @StringProperty(description = "Sets common wallet status url")
    public static final String KEY_CW_STATUS_URL = "COMMON_WALLET_STATUS_URL";
    @StringProperty(description = "Sets common wallet wager url")
    public static final String KEY_CW_WAGER_URL = "COMMON_WALLET_WAGER_URL";
    @StringProperty(description = "Sets common wallet refund url")
    public static final String KEY_CW_REFUND_URL = "COMMON_WALLET_REFUND_URL";
    @StringProperty(description = "Sets common wallet authentication url")
    public static final String KEY_CW_AUTH_URL = "COMMON_WALLET_AUTH_URL";
    @StringProperty(description = "Sets common wallet start url for GTBETS")
    public static final String KEY_CW_START_GTBETS_URL = "COMMON_WALLET_START_GTBETS_URL";
    @StringProperty(description = "Sets url used for place bet operation when it is distinct from wager url")
    public static final String KEY_CW_CUSTOM_CREDIT_URL = "COMMON_WALLET_CUSTOM_CREDIT_URL";

    @BooleanProperty(description = "If enabled, sends common wallet api password for CW authentication")
    public static final String KEY_CW_AUTH_REQUIRED = "COMMON_WALLET_AUTH_REQUIRED";
    @StringProperty(description = "Sets common wallet authentication password")
    public static final String KEY_CW_AUTH_PASS = "COMMON_WALLET_AUTH_PASS";
    @StringProperty(description = "Sets common wallet user")
    public static final String KEY_CW_USER = "COMMON_WALLET_USER";
    @StringProperty(description = "Sets common wallet password")
    public static final String KEY_CW_PASS = "COMMON_WALLET_PASS";
    @BooleanProperty(description = "If enabled, turns on auto finish for gameSession")
    public static final String KEY_CW_AUTO_FINISH_REQUIRED = "COMMON_WALLET_AUTO_FINISH_REQUIRED";
    @StringProperty(description = "Sets Common wallet server type: AP, 007Bank, 63, Betklass")
    public static final String KEY_CW_SERVER_TYPE = "SERVER_TYPE";
    @BooleanProperty(description = "Enables session touch with default period of ServerSessionTimeout / 4")
    public static final String KEY_TOUCH_SESSION = "TOUCH_SESSION";
    @NumericProperty(description = "Defines a period for session to be touched with")
    public static final String KEY_TOUCH_SESSION_PERIOD = "TOUCH_SESSION_PERIOD";
    @BooleanProperty(description = "If enabled, update session last activity time")
    public static final String KEY_TOUCH_SESSION_ON_GET_TIME = "TOUCH_SESSION_ON_GET_TIME";
    @StringProperty(description = "Sets url for refreshing external session")
    public static final String KEY_REFRESH_EXTERNAL_SESSION_URL = "REFRESH_EXTERNAL_SESSION_URL";

    @StringProperty(description = "Sets integration service url for remote SB client")
    public static final String KEY_INTEGRATION_SERVICE_URL = "INTEGRATION_SERVICE_URL";
    @StringProperty(description = "Sets service url backup")
    public static final String KEY_INTEGRATION_SERVICE_URL_STANDBY = "INTEGRATION_SERVICE_URL_STANDBY";
    @StringProperty(description = "Sets username for SB remote client")
    public static final String KEY_INTEGRATION_SERVICE_USERNAME = "INTEGRATION_SERVICE_USERNAME";
    @StringProperty(description = "Sets password for SB remote client")
    public static final String KEY_INTEGRATION_SERVICE_PASSWORD = "INTEGRATION_SERVICE_PASSWORD";

    @StringProperty(description = "Sets url on which sends game history")
    public static final String KEY_HISTORY_INFORMER_SERVICE_URL = "KEY_HISTORY_INFORMER_SERVICE_URL";

    @StringProperty(description = "Sets Mr Green login")
    public static final String KEY_MRGREN_LOGIN = "KEY_MRGREN_LOGIN";
    @StringProperty(description = "Sets Mr Green password")
    public static final String KEY_MRGREN_PASSWORD = "KEY_MRGREN_PASSWORD";

    @StringProperty(description = "Sets partner id")
    public static final String KEY_PARTNER_ID = "PARTNER_ID";
    @StringProperty(description = "Sets partner password")
    public static final String KEY_PARTNER_KEY = "PARTNER_KEY";

    //TransferConfiguration.properties
    @StringProperty(description = "Common transfer payment processor class")
    public static final String KEY_PP_CLASS = "PP_CLASS";
    @StringProperty(description = "Sets integration password")
    public static final String KEY_INTEGRATION_PASSWORD = "INTEGRATION_PASSWORD";
    @BooleanProperty(description = "Disables using hash parameter for our API")
    public static final String KEY_DISABLE_HASHING_FOR_API = "DISABLE_HASHING_FOR_API";
    @StringProperty(description = "Pass key to generate hash value for any kind of our API")
    public static final String KEY_API_PASS_KEY = "INTEGRATION_PASS_KEY";
    @StringProperty(description = "Sets service integration password")
    public static final String KEY_SERVICE_INTEGRATION_PASSWORD = "SERVICE_INTEGRATION_PASSWORD";
    @StringProperty(description = "Sets ticket check address")
    public static final String KEY_TICKET_CHECK_ADDRESS = "TICKET_CHECK_ADDRESS";
    @BooleanProperty(description = "If enabled, track win operation in new game session")
    public static final String KEY_TRACK_WIN_IN_NEW_GAMESESSION = "TRACK_WIN_IN_NEW_GAMESESSION";

    @StringProperty(description = "Sets Common transfer client class")
    public static final String KEY_CT_CLIENT_CLASSNAME = "CT_CLIENT_CLASSNAME";
    @StringProperty(description = "Sets Common transfer pass key")
    public static final String KEY_CT_PASS_KEY = "CT_PASS_KEY";
    @BooleanProperty(description = "If enabled, sets original request method as POST")
    public static final String KEY_CT_REST_ISPOST = "CT_REST_ISPOST";
    @StringProperty(description = "Sets Common transfer REST authentication url")
    public static final String KEY_CT_REST_AUTH_URL = "CT_REST_AUTH_URL";
    @StringProperty(description = "Sets Common transfer REST transfer url")
    public static final String KEY_CT_REST_TRANSFER_URL = "CT_REST_TRANSFER_URL";
    @StringProperty(description = "Sets url for Common transfer REST end of game session")
    public static final String KEY_CT_REST_ENDGAMESESSION_URL = "CT_REST_ENDGAMESESSION_URL";
    @StringProperty(description = "Sets url for getting transaction status")
    public static final String KEY_CT_REST_STATUS_URL = "CT_REST_STATUS_URL";
    @StringProperty(description = "Set Common transfer Rest get balance url")
    public static final String KEY_CT_REST_BALANCE_URL = "CT_REST_BALANCE_URL";

    @StringProperty(description = "Sets Common transfer Vietbet find transaction url")
    public static final String KEY_CT_VIETBET_FIND_TRANSACTION_URL = "CT_VIETBET_FIND_TRANSACTION_URL";
    @StringProperty(description = "Sets Common transfer Vietbet post transaction url")
    public static final String KEY_CT_VIETBET_POST_TRANSACTION_URL = "CT_VIETBET_POST_TRANSACTION_URL";
    @StringProperty(description = "Sets url for Common transfer Vietbet validating customer")
    public static final String KEY_CT_VIETBET_VALIDATE_CUSTOMER_URL = "CT_VIETBET_VALIDATE_CUSTOMER_URL";

    @StringProperty(description = "Sets class for CloseGameProcessor")
    public static final String KEY_CLOSE_GAME_PROCESSOR = "CLOSE_GAME_PROCESSOR";

    @StringProperty(description = "Sets url for Notification close game processor")
    public static final String KEY_NOTIFICATION_CLOSE_GAME_PROCESSOR_URL = "NOTIFICATION_CLOSE_GAME_PROCESSOR_URL";

    @StringProperty(description = "Auth pass for close game processor endpoint")
    public static final String KEY_NOTIFICATION_CLOSE_GAME_AUTH_PASS = "NOTIFICATION_CLOSE_GAME_AUTH_PASS";

    @StringProperty(description = "Sets start game processor class")
    public static final String KEY_START_GAME_PROCESSOR = "START_GAME_PROCESSOR";

    @StringProperty(description = "Sets url for Notification start game processor")
    public static final String KEY_NOTIFICATION_START_GAME_PROCESSOR_URL = "NOTIFICATION_START_GAME_PROCESSOR_URL";

    @StringProperty(description = "Auth pass for start game processor endpoint")
    public static final String KEY_NOTIFICATION_START_GAME_AUTH_PASS = "NOTIFICATION_START_GAME_AUTH_PASS";

    @StringProperty(description = "Sets url for Notification start game processor")
    public static final String KEY_NOTIFICATION_ROOM_WAS_DEACTIVATED_URL = "NOTIFICATION_ROOM_WAS_DEACTIVATED_URL";

    @StringProperty(description = "Auth pass for start game processor endpoint")
    public static final String KEY_NOTIFICATION_ROOM_WAS_DEACTIVATED_AUTH_PASS = "NOTIFICATION_ROOM_WAS_DEACTIVATED_AUTH_PASS";

    //BonusConfiguration.properties
    @StringProperty(description = "Sets class for Bonus Manager")
    public static final String KEY_BM_CLASS = "BM_CLASS";
    @BooleanProperty(description = "If enabled, turns on Free round bonuses support for common transfer")
    public static final String KEY_IS_FRB_FOR_CT_SUPPORTED = "IS_FRB_FOR_CT_SUPPORTED";
    @StringProperty(description = "Sets class for FRBonus Manager")
    public static final String KEY_FRBM_CLASS = "FRBM_CLASS";
    @StringProperty(description = "Sets class for FRBonus Win Manager")
    public static final String KEY_FRBWINM_CLASS = "FRBWINM_CLASS";
    @NumericProperty(description = "Sets default amount of FRBonus chips")
    public static final String KEY_FRB_DEF_CHIPS = "FRB_DEF_CHIPS";
    @NumericProperty(description = "Sets default amount of FRBonus MQ chips for one shot")
    public static final String MQ_KEY_FRB_DEF_CHIPS = "MQ_FRB_DEF_CHIPS";

    @StringProperty(description = "Sets list of games with delimiter '|' in which FRB is enabled")
    public static final String KEY_FRB_GAMES_ENABLE = "FRB_GAMES_ENABLE";
    @StringProperty(description = "Sets list of games with delimiter '|' in which FRB is disabled")
    public static final String KEY_FRB_GAMES_DISABLE = "FRB_GAMES_DISABLE";

    @BooleanProperty(description = "If enabled then awarding FRB from CM and WS is denied")
    public static final String KEY_FRB_DENY_AWARDING_FROM_WS = "FRB_DENY_AWARDING_FROM_WS";
    @StringProperty(description = "True or false : if true, enables Bonus stub mode")
    public static final String KEY_BC_STUB_MODE = "BC_STUB_MODE";

    @StringProperty(description = "Sets bonus request client class ")
    public static final String KEY_BONUS_REQUEST_CLIENT_CLASS = "BONUS_REQUEST_CLIENT_CLASS";
    @StringProperty(description = "Sets FreeRound bonus request client class")
    public static final String KEY_FRBONUS_REQUEST_CLIENT_CLASS = "FRBONUS_REQUEST_CLIENT_CLASS";
    @StringProperty(description = "Sets bonus release url")
    public static final String KEY_BONUS_RELEASE_URL = "BONUS_RELEASE_URL";
    @StringProperty(description = "Sets bonus authentication url")
    public static final String KEY_BONUS_AUTH_URL = "BONUS_AUTH_URL";
    @StringProperty(description = "Sets bonus account info url")
    public static final String KEY_BONUS_ACCOUNTINFO_URL = "BONUS_ACCOUNTINFO_URL";
    @StringProperty(description = "Sets bonus pass key, uses for hash value")
    public static final String KEY_BONUS_KEY = "BONUS_PASS_KEY";
    @BooleanProperty(description = "If enabled, turns on check of bonus hash")
    public static final String KEY_BONUS_IS_HASH_VALUE = "BONUS_IS_HASH_VALUE";
    @BooleanProperty(description = "If enabled, sends award date and time")
    public static final String KEY_SEND_BONUS_AWARD_TIME = "SEND_BONUS_AWARD_TIME";
    @NumericProperty(description = "Minimal threshold of bonus (cents)")
    public static final String KEY_BONUS_THRESHOLD_MIN_KEY = "THRESHOLD_MIN_KEY";
    @BooleanProperty(description = "If enabled, lost bonus when it less or equal THRESHOLD_MIN_KEY")
    public static final String KEY_BONUS_INSTANT_LOST_ON_THRESHOLD = "INSTANT_LOST_ON_THRESHOLD";
    @BooleanProperty(description = "If enabled, turns off persisting of FRB win operations")
    public static final String KEY_NOT_PERSIST_FRBWIN_OPS = "NOT_PERSIST_FRBWIN_OPS";
    @StringProperty(description = "Sets amount of spins for auto play")
    public static final String KEY_AUTOPLAY_VALUES = "AUTOPLAY_VALUES";
    @BooleanProperty(description = "If enabled, sends in response currency symbol on game enter")
    public static final String KEY_SEND_CURRENCY_SYMBOL = "SEND_CURRENCY_SYMBOL";

    @BooleanProperty(description = "If enabled, game won't be started if there are any wallet uncompleted operations")
    public static final String KEY_NO_START_IF_WALLET_OP_UNCOMP = "NO_START_IF_WALLET_OP_UNCOMP";
    @StringProperty(description = "Sets string, which is start of game server name which should be changed")
    public static final String KEY_REPLACE_START_GS_FROM = "REPLACE_START_GS_FROM";
    @StringProperty(description = "Sets string, which is start of game server name which should be inserted " +
            "instead of old one")
    public static final String KEY_REPLACE_START_GS_TO = "REPLACE_START_GS_TO";
    @StringProperty(description = "Sets string, which is end of game server name which should be changed")
    public static final String KEY_REPLACE_END_GS_FROM = "REPLACE_END_GS_FROM";
    @StringProperty(description = "Sets string, which is end of game server name which should be inserted " +
            "instead of old one")
    public static final String KEY_REPLACE_END_GS_TO = "REPLACE_END_GS_TO";
    @NumericProperty(description = "Sets limit of players game sessions for bank (count)")
    public static final String KEY_GAMESESSIONS_LIMIT = "GAMESESSIONS_LIMIT";
    @NumericProperty(description = "Sets limit for playerSession (count)")
    public static final String KEY_PLAYERSESSIONS_LIMIT = "PLAYERSESSIONS_LIMIT";
    @StringProperty(description = "Sets email address for alerts")
    public static final String KEY_ALERTS_EMAIL_ADDRESS = "ALERTS_EMAIL_ADDRESS";
    @BooleanProperty(description = "If enabled, sends login errors to email")
    public static final String KEY_SEND_LOGIN_ERRORS_EMAIL = "SEND_LOGIN_ERRORS_EMAIL";
    @StringProperty(description = "Minimal amount in euro cents to send big win alert")
    public static final String KEY_MIN_WIN_TO_SEND = "MIN_WIN_TO_SEND";
    @StringProperty(description = "Sets url for FRBonus win")
    public static final String KEY_FR_BONUS_WIN_URL = "FR_BONUS_WIN_URL";
    @StringProperty(description = "Sets start game domain")
    public static final String KEY_START_GAME_DOMAIN = "START_GAME_DOMAIN";
    @BooleanProperty(description = "If enabled, uses error codes url to XML file with them")
    public static final String KEY_ERROR_CODES_XML_URL = "ERROR_CODES_XML_URL";
    //Assume freeRoundValidity parameter in minutes (instead of days)
    @BooleanProperty(description = "If enabled, uses time units minutes for free round validity")
    public static final String KEY_FREE_ROUND_VALIDITY_IN_MINUTES = "FREE_ROUND_VALIDITY_IN_MINUTES";
    @BooleanProperty(description = "If enabled, uses time units hours for free round validity")
    public static final String KEY_FREE_ROUND_VALIDITY_IN_HOURS = "FREE_ROUND_VALIDITY_IN_HOURS";

    @StringProperty(description = "Sets url for api service environment")
    public static final String KEY_API_SERVICE_GET_ENVIRONMENT_URL = "API_SERVICE_GET_ENVIRONMENT_URL";
    @StringProperty(description = "Sets url for api service fund account")
    public static final String KEY_API_SERVICE_FUND_ACCOUNT_URL = "API_SERVICE_FUND_ACCOUNT_URL";
    @StringProperty(description = "Sets url for getting active token")
    public static final String KEY_API_SERVICE_GET_ACTIVE_TOKEN_URL = "API_SERVICE_GET_ACTIVE_TOKEN_URL";

    //URLs for mobile versions of games
    @StringProperty(description = "Sets home page url for mobile")
    public static final String KEY_MOBILE_HOME_URL = "KEY_MOBILE_HOME_URL";
    @StringProperty(description = "Sets cashier url for mobile")
    public static final String KEY_MOBILE_CASHIER_URL = "KEY_MOBILE_CASHIER_URL";

    @StringProperty(description = "Sets home page url")
    public static final String KEY_HOME_URL = "HOME_URL";
    @StringProperty(description = "Sets time zone")
    public static final String KEY_TIME_ZONE = "TIME_ZONE";

    @StringProperty(description = "Sets custom js home function")
    public static final String KEY_JS_HOME = "JS_HOME";

    @BooleanProperty(description = "If enabled call launchHome function when game loaded in an iframe")
    public static final String KEY_LAUNCH_HOME_FROM_IFRAME = "LAUNCH_HOME_FROM_IFRAME";

    @StringProperty(description = "Default value: /common/standard/settings/customerspec_descriptor.xml")
    public static final String KEY_CUSTOMER_SETTINGS_URL = "CUSTOMER_SETTINGS_URL";

    @StringProperty(description = "Default value: _standard")
    public static final String KEY_CUSTOMER_SETTINGS_HTML5PC = "CUSTOMER_SETTINGS_HTML5PC";

    @StringProperty(description = "Sets additional flashvars")
    public static final String KEY_ADDITIONAL_FLASHVARS = "ADDITIONAL_FLASHVARS";

    @BooleanProperty(description = "If enabled, turns on parsing balance to long, which taken from client's side")
    public static final String KEY_PARSE_LONG = "PARSE_LONG";

    //standalone lobbies
    @StringProperty(description = "Sets jsp for standalone lobby")
    public static final String KEY_STANDALONE_LOBBY_JSP = "STANDALONE_LOBBY_JSP";

    @StringProperty(description = "Sets bonus page for standalone lobby")
    public static final String KEY_STANDALONE_LOBBY_BONUS_PAGE = "STANDALONE_LOBBY_BONUS_PAGE";

    @StringProperty(description = "Sets live dealer url for standalone lobby")
    public static final String KEY_STANDALONE_LOBBY_LIVEDEALER_PAGE = "STANDALONE_LOBBY_LIVEDEALER_PAGE";

    //5D international
    @BooleanProperty(description = "If enabled, standalone lobby doesn't need branding header")
    public static final String KEY_STANDALONE_LOBBY_DONT_NEED_BRAND_HEADER = "STANDALONE_LOBBY_DONT_NEED_BRAND_HEADER";

    //vietbet
    @BooleanProperty(description = "If enabled, standalone lobby doesn't need logout game")
    public static final String KEY_STANDALONE_LOBBY_DONT_NEED_LOGOUT_GAME = "STANDALONE_LOBBY_DONT_NEED_LOGOUT_GAME";

    @BooleanProperty(description = "If enabled, turns on logout with withdrawal")
    public static final String KEY_VIETBET_LOGOUT_WITH_WITHDRAWAL = "VIETBET_LOGOUT_WITH_WITHDRAWAL";

    //ptpt
    @BooleanProperty(description = "If enabled, turns on logout by full withdrawal")
    public static final String KEY_PTPT_LOGOUT_BY_FULL_WITHDRAWAL = "PTPT_LOGOUT_BY_FULL_WITHDRAWAL";

    @MandatoryProperty
    @StringProperty(description = "Sets PlayerSessionManager class")
    public static final String KEY_PSM_CLASS = "PSM_CLASS";

    @BooleanProperty(description = "If enabled, uses IndividualGameSettingsType.PLAYER")
    public static final String KEY_USE_PLAYER_GAME_SETTINGS = "PLAYER_GAME_SETTINGS";

    @StringProperty(description = "Sets individual game settings: PLAYER, FBLEVEL or NONE")
    public static final String KEY_USE_INDIVIDUAL_GAME_SETTINGS = "INDIVIDUAL_GAME_SETTINGS";

    @BooleanProperty(description = "If enabled, uses local ACS")
    public static final String KEY_USE_LOCAL_ACS = "USE_LOCAL_ACS";

    //for other systems need refine CommonwWallets processing (clear roundId) after clear lasthand
    @BooleanProperty(description = "If enabled, clears lasthand on coins change")
    public static final String KEY_CLEAR_LASTHAND_ON_CHANGE_COINS = "CLEAR_LASTHAND_ON_CHANGE_COINS";

    @BooleanProperty(description = "If enabled, clears lasthand on default coins change")
    public static final String KEY_CLEAR_LASTHAND_ON_CHANGE_DEF_COIN = "CLEAR_LASTHAND_ON_CHANGE_DEF_COIN";

    @StringProperty(description = "Sets url for sending daily bonus win")
    public static final String KEY_SEND_DAILY_BONUS_WIN_URL = "SEND_DAILY_BONUS_WIN_URL";

    @StringProperty(description = "Sets preferable SMTP server")
    public static final String KEY_PREFERABLE_SMTP_SERVER = "PREFERABLE_SMTP_SERVER";

    @StringProperty(description = "Sets daily bonus info url")
    public static final String KEY_DAILY_BONUS_INFO_URL = "DAILY_BONUS_INFO_URL";

    @StringProperty(description = "Sets SendBetInterceptor class name")
    public static final String KEY_SEND_BET_INTERCEPTOR_CLASS = "SEND_BET_INTERCEPTOR_CLASS";

    @NumericProperty(description = "Sets wallet task pending time in hours")
    public static final String KEY_WALLET_TASK_PENDING_TIME = "WALLET_TASK_PENDING_TIME";

    public static final int DEFAULT_TASK_PENDING_TIME = 48;

    @BooleanProperty(description = "If enabled, uses same domain for start game")
    public static final String KEY_USE_SAME_DOMAIN_FOR_START_GAME = "USE_SAME_DOMAIN_FOR_START_GAME";

    @NumericProperty(description = "Sets timeout for real mode sessions in milliseconds")
    public static final String KEY_REAL_MODE_SESSION_TIMEOUT = "REAL_MODE_SESSION_TIMEOUT";

    @NumericProperty(description = "Sets timeout for free mode sessions in milliseconds")
    public static final String KEY_FREE_MODE_SESSION_TIMEOUT = "FREE_MODE_SESSION_TIMEOUT";

    @StringProperty(description = "Sets url for game disabled error")
    public static final String KEY_GAME_DISABLED_ERROR_URL = "GAME_DISABLED_ERROR_URL";

    @StringProperty(description = "Sets url for connection broken error")
    public static final String KEY_CONNECTION_BROKEN_URL = "CONNECTION_BROKEN_URL";

    @StringProperty(description = "Sets url for game load error")
    public static final String KEY_GAME_LOAD_ERROR_URL = "GAME_LOAD_ERROR_URL";

    @BooleanProperty(description = "If enabled, adds availability to send wallet exception messages")
    public static final String KEY_SHOW_DELAYED_WALLET_MESSAGES = "SHOW_DELAYED_WALLET_MESSAGES";

    @BooleanProperty(description = "If enabled, adds availability to send external wallet messages and errors")
    public static final String KEY_SEND_EXT_WALLET_MESSAGES = "SEND_EXT_WALLET_MESSAGES";

    @NumericProperty(description = "Sets timeout for debit in milliseconds")
    public static final String KEY_DEBIT_TIMEOUT_MILLIS = "DEBIT_TIMEOUT_MILLIS";

    @NumericProperty(description = "Sets in-game timeout for credit in milliseconds")
    public static final String KEY_CREDIT_INGAME_TIMEOUT_MILLIS = "CREDIT_INGAME_TIMEOUT_MILLIS";

    @StringProperty(description = "Sets session error url")
    public static final String KEY_SESSION_ERROR_URL = "SESSION_ERROR_URL";

    @StringProperty(description = "Sets login error url")
    public static final String KEY_LOGIN_ERROR_URL = "LOGIN_ERROR_URL";

    @StringProperty(description = "Sets game start error url")
    public static final String KEY_GAME_START_ERROR_URL = "GAME_START_ERROR_URL";

    @BooleanProperty(description = "If enabled, uses single gameId for all devices")
    public static final String KEY_USE_SINGLE_GAMEID_FOR_ALL_DEVICES = "USE_SINGLE_GAMEID_FOR_ALL_DEVICES";

    @BooleanProperty(description = "If enabled, uses SWF shell")
    public static final String KEY_USE_SWF_SHELL = "USE_SWF_SHELL";

    @StringProperty(description = "Sets SWF shell directory")
    public static final String KEY_SWF_SHELL_DIRECTORY = "SWF_SHELL_DIRECTORY";

    @StringProperty(description = "Sets SWF shell template path")
    public static final String KEY_TEMPLATE_SWF_SHELL_PATH = "TEMPLATE_SWF_SHELL_PATH";

    @BooleanProperty(description = "If enabled, shows game localization error")
    public static final String KEY_SHOW_GAME_LOCALIZATION_ERROR = "SHOW_GAME_LOCALIZATION_ERROR";

    @StringProperty(description = "Sets game server domain")
    public static final String KEY_GAMESERVER_DOMAIN = "GAMESERVER_DOMAIN";

    public static final int DEFAULT_PGS_TTL = 7 * 24 * 3600;

    @BooleanProperty(description = "If enabled, sends VBA to external system")
    public static final String KEY_SEND_VBA_TO_EXTERNAL_SYSTEM = "SEND_VBA_TO_EXTERNAL_SYSTEM";

    @BooleanProperty(description = "If enabled, show external bet id in vab")
    public static final String KEY_SHOW_EXT_BET_ID_IN_VAB = "SHOW_EXT_BET_ID_IN_VAB";

    @NumericProperty(description = "Set ttl for wallet operations in seconds")
    public static final String KEY_WALLET_OPERATION_TTL = "WALLET_OPERATION_TTL";
    /**
     * used for acegaming - find bankId by domain name...
     */
    @StringProperty(description = "Sets bank domain name for getting bank info by domain name")
    public static final String KEY_BANK_DOMAIN_NAME = "BANK_DOMAIN_NAME";

    @StringProperty(description = "Sets vivo integration password")
    public static final String KEY_VIVO_INTEGRATION_PASSWORD = "VIVO_INTEGRATION_PASSWORD";

    @StringProperty(description = "Sets vivo start game url")
    public static final String KEY_VIVO_START_GAME_URL = "VIVO_START_GAME_URL";

    @StringProperty(description = "Sets vivo operator id")
    public static final String KEY_VIVO_OPERATOR_ID = "VIVO_OPERATOR_ID";

    @StringProperty(description = "Sets vivo server id")
    public static final String KEY_VIVO_SERVER_ID = "VIVO_SERVER_ID";

    @StringProperty(description = "Sets vivo tables url")
    public static final String KEY_VIVO_GET_TABLES_URL = "VIVO_GET_TABLES_URL";

    @StringProperty(description = "Sets class name for External wallet transaction handler")
    public static final String KEY_EXTERNAL_WALLET_TRANSACTION_HANDLER_CLASS_NAME =
            "EXTERNAL_WALLET_TRANSACTION_HANDLER_CLASS_NAME";

    @StringProperty(description = "Sets start game session notify url")
    public static final String KEY_START_GAMESESSION_NOTIFY_URL =
            "START_GAMESESSION_NOTIFY_URL";

    @StringProperty(description = "Sets end game session notify url")
    public static final String KEY_END_GAMESESSION_NOTIFY_URL =
            "END_GAMESESSION_NOTIFY_URL";

    @BooleanProperty(description = "If enabled, writes out FRB rounds left")
    public static final String KEY_FREE_BONUS_ROUND_SPIN_VIEW = "FREE_BONUS_ROUND_SPIN_VIEW";

    @BooleanProperty(description = "If enabled, process daily wallet operations")
    public static final String KEY_DAILY_WALLET_OPERATION = "DAILY_WALLET_OPERATION";

    @StringProperty(description = "Sets SSH static lobby path")
    public static final String KEY_SSH_STATIC_LOBBY_PATH = "SSH_STATIC_LOBBY_PATH";

    @BooleanProperty(description = "If enabled and bonus state is active, writes out round id")
    public static final String KEY_SEND_ROUND_ID = "SEND_ROUND_ID";

    @BooleanProperty(description = "If enabled, allows referrer domains")
    public static final String KEY_NEED_ALLOWED_REFERER_DOMAINS = "NEED_ALLOWED_REFERER_DOMAINS";

    @StringProperty(description = "Sets bank feed passkey for hash validation")
    public static final String BANK_FEEDS_PASSKEY = "BANK_FEEDS_PASSKEY";

    //format: name1=value1;name2=value2
    @StringProperty(description = "Sets special common wallet request headers")
    public static final String KEY_CW_SPECIAL_REQUEST_HEADERS = "CW_SPECIAL_REQUEST_HEADERS";

    @BooleanProperty(description = "Use http proxy")
    public static final String KEY_USE_HTTP_PROXY = "USE_HTTP_PROXY";

    @BooleanProperty(description = "If enabled, adds to bonus win parameters external gameId")
    public static final String KEY_SEND_GAMEID_ON_FRBWIN = "SEND_GAMEID_ON_FRBWIN";

    @BooleanProperty(description = "If enabled, adds to bonus win parameters external gameId, roundId, isRoundFinished" +
            "and gameSessionId")
    public static final String KEY_SEND_DETAILS_ON_FRBWIN = "SEND_DETAILS_ON_FRBWIN";

    @BooleanProperty(description = "If enabled, sends details on refund")
    public static final String KEY_SEND_DETAILS_ON_REFUND = "SEND_DETAILS_ON_REFUND";

    @BooleanProperty(description = "If enabled, turns on summarized FRB notification")
    public static final String KEY_SEND_SUMMARIZED_FRB_NOTIFICATION = "SEND_SUMMARIZED_FRB_NOTIFICATION";

    @BooleanProperty(description = "If enabled, adds to bonus release params GAMESESSIONID, TRANSACTIONID and external" +
            "game id")
    public static final String KEY_SEND_DETAILS_ON_BONUSRELEASE = "SEND_DETAILS_ON_BONUSRELEASE";

    @BooleanProperty(description = "If enabled, prints external bonus id with FRB info")
    public static final String KEY_SEND_DETAILS_ON_FRB_INFO = "SEND_DETAILS_ON_FRB_INFO";

    @StringProperty(description = "RoundId generator name")
    public static final String KEY_ROUNDID_GENERATOR_NAME = "ROUNDID_GENERATOR_NAME";

    @BooleanProperty(description = "If enabled, sends zero FRB win")
    public static final String KEY_SEND_ZEROFRBWIN = "SEND_ZEROFRBWIN";

    @BooleanProperty(description = "If enabled, sends client type on FRB win")
    public static final String KEY_SEND_CLIENT_TYPE_ON_FRBWIN = "SEND_CLIENT_TYPE_ON_FRBWIN";

    @BooleanProperty(description = "If enabled, turns on sending zero bet on FRB wins")
    public static final String KEY_SEND_ZEROBET_ON_FRBWIN = "SEND_SEND_ZEROBET_ON_FRBWIN";

    @StringProperty(description = "URL for sending Zerobet FRB")
    public static final String KEY_SEND_ZEROBET_FRB_URL = "SEND_ZEROBET_FRB_URL";

    @BooleanProperty(description = "If enabled, always sending not zero FRB bet")
    public static final String KEY_SEND_NOT_ZERO_FRB_BET = "SEND_NOT_ZERO_FRB_BET";

    @BooleanProperty(description = "If enabled, client do not send RESTART command when FRB is finished")
    public static final String KEY_NO_FRB_RESTART = "NO_FRB_RESTART";

    @BooleanProperty(description = "If enabled, turns on sending jackpot info for CommonWallet")
    public static final String KEY_SEND_JACKPOT_INFO_FOR_CW = "SEND_JACKPOT_INFO_FOR_CW";

    @BooleanProperty(description = "If enabled, turns on sending jackpots detailed contributions for CommonWallet")
    public static final String KEY_SEND_JP_DETAILS = "SEND_JP_DETAILS";

    @BooleanProperty(description = "If enabled, turns on mandatory on demographic info")
    public static final String KEY_MANDATORY_DEMOGRAPHIC_INFO = "MANDATORY_DEMOGRAPHIC_INFO";


    //format: oldGameId_1=newGameId_1;oldGameId_2=newGameId_2;oldGameId_3=newGameId_3
    @StringProperty(description = " Migration of game ids. Sets strings like oldGameId=newGameId with ';' as separator" +
            "between each pair")
    public static final String KEY_GAME_MIGRATION_CONFIG = "GAME_MIGRATION_CONFIG";

    @StringProperty(description = "TransactionData class name")
    public static final String KEY_TRANSACTION_DATA_CLASS = "TRANSACTION_DATA_CLASS";

    @StringProperty(description = "ExtendedGameplayProcessor class name")
    public static final String KEY_EXTENDED_GAMEPLAY_PROCESSOR = "EXTENDED_GAMEPLAY_PROCESSOR";

    @StringProperty(description = "GameSessionStateListener class name")
    public static final String KEY_GAME_SESSION_STATE_LISTENER = "GAME_SESSION_STATE_LISTENER";

    @BooleanProperty(description = "If enabled, uses winner feed name in GameSessionInfo")
    public static final String KEY_USE_WINNER_FEED = "USE_WINNER_FEED";

    @StringProperty(description = "Sets mode of jackpot feed. Can be SUM_OF_COINS, DETAILED, ONLY_HIGH_COIN," +
            "ONLY_HIGHEST_JACKPOT")
    public static final String KEY_JP_FEED_MODE = "JP_FEED_MODE";

    @BooleanProperty(description = "If enabled, uses feeds with external bank id folder")
    public static final String KEY_ADD_EXTERNAL_FILE = "ADD_EXTERNAL_ID_FILE";

    @BooleanProperty(description = "If enabled, sends jackpot win notifications")
    public static final String KEY_USE_JP_NOTIFICATION = "USE_JP_NOTIFICATION";

    @StringProperty(description = "Common wallet jar name")
    public static final String KEY_REPOSITORY_FILE = "REPOSITORY_FILE";

    @BooleanProperty(description = "Marks version as development")
    public static final String KEY_DEVELOPMENT_VERSION = "DEVELOPMENT_VERSION";

    @StringProperty(description = "Sets VIVO logo url")
    public static final String KEY_VIVO_LOGO = "VIVO_LOGO";

    @NumericProperty(description = "Tournament bank id")
    public static final String KEY_TOURNAMENT_BANK_ID = "TOURNAMENT_BANK_ID";

    @StringProperty(description = "Sets trusted hosts with ' ' delimiter")
    public static final String KEY_TRUSTED_HTTPS_HOSTS = "TRUSTED_HTTPS_HOSTS";

    @StringProperty(description = "Sets content delivery network urls with ';' delimiter")
    public static final String KEY_CDN_URLS = "CDN_URLS";

    @NumericProperty(description = "Sets content delivery network expire time in seconds")
    public static final String KEY_CDN_EXPIRE_TIME = "CDN_EXPIRE_TIME";

    private static final long DEFAULT_CDN_EXPIRE_TIME = 86400L;

    @BooleanProperty(description = "Force CDN Auto per bank option")
    public static final String KEY_CDN_FORCE_AUTO = "CDN_FORCE_AUTO";

    @BooleanProperty(description = "If enabled, specifies minigame template as start game page")
    public static final String KEY_IS_USE_MINIGAME_SHELL = "IS_USE_MINIGAME_SHELL";

    @BooleanProperty(description = "If enabled, specifies SWF template as start game page")
    public static final String KEY_IS_USE_SWF_LGA_SHELL = "IS_USE_SWF_LGA_SHELL";

    @BooleanProperty(description = "Enable if need to change language")
    public static final String KEY_NEED_CHANGE_LANG = "NEED_CHANGE_LANG";

    @StringProperty(description = "If true, do not pass session error url")
    public static final String KEY_NOT_PASS_SESSION_ERROR = "NOT_PASS_SESSION_ERROR";

    @NumericProperty(description = "Sets special error code for sessions")
    public static final String KEY_SPECIAL_SESSION_ERROR_CODE = "SPECIAL_SESSION_ERROR_CODE";

    @StringProperty(description = "Sets name of JS function which shows help URL")
    public static final String KEY_OPEN_HELP_HTML = "OPEN_HELP_HTML";

    @BooleanProperty(description = "If enabled, allows to close game window")
    public static final String KEY_NEED_CLOSE_WINDOW = "NEED_CLOSE_WINDOW";

    @BooleanProperty(description = "If enabled, turns on affactive popup")
    public static final String KEY_NEED_AFFACTIVE_POPUP = "NEED_AFFACTIVE_POPUP";

    @BooleanProperty(description = "If enabled, turns on drake popup")
    public static final String KEY_NEED_DRAKE_POPUP = "NEED_DRAKE_POPUP";

    @BooleanProperty(description = "If enabled, turns on gossip popup")
    public static final String KEY_NEED_GOSSIP_POPUP = "NEED_GOSSIP_POPUP";

    @BooleanProperty(description = "If enabled, turns on flash script")
    public static final String KEY_NEED_HAS_FLASH_SCRIPT = "NEED_HAS_FLASH_SCRIPT";

    @BooleanProperty(description = "If enabled, reloads game to update current balance value")
    public static final String KEY_IS_NEED_WT_RELOAD = "IS_NEED_WT_RELOAD";

    @BooleanProperty(description = "If enabled, opens cashier popup when Buy in clicked")
    public static final String KEY_NEED_OPEN_CACHIER_POPUP = "NEED_OPEN_CACHIER_POPUP";

    @BooleanProperty(description = "Need window parent parent location when Buy in clicked")
    public static final String KEY_NEED_WINDOW_PARENT_PARENT_LOCATION = "NEED_WINDOW_PARENT_PARENT_LOCATION";

    @BooleanProperty(description = "Need window parent location when Buy in clicked")
    public static final String KEY_NEED_WINDOW_PARENT_LOCATION = "NEED_WINDOW_PARENT_LOCATION";

    @BooleanProperty(description = "Need redirect and close to CashierUrl when Buy in clicked")
    public static final String KEY_NEED_REDIRECT_AND_CLOSE = "NEED_REDIRECT_AND_CLOSE";

    @BooleanProperty(description = "Need redirect to CashierUrl when Buy in clicked")
    public static final String KEY_NEED_REDIRECT = "NEED_REDIRECT";

    @BooleanProperty(description = "If enabled, turns on js instant cashier ")
    public static final String KEY_NEED_JS_INSTANT_CASHIER = "NEED_JS_INSTANT_CASHIER";

    @BooleanProperty(description = "If enabled, turns off popup messages in test mode")
    public static final String KEY_DONT_SHOW_MESSAGE_IN_TEST_MODE = "DONT_SHOW_MESSAGE_IN_TEST_MODE";

    @BooleanProperty(description = "If enabled, turns on SWFObject")
    public static final String KEY_IS_NEED_SWF_OBJECT = "NEED_SWF_OBJECT";

    @StringProperty(description = "Sets minimal win amount to put winner to banks winners feed")
    public static final String KEY_WINNER_FEED_MIN_WIN_AMOUNT = "WINNER_FEED_MIN_WIN_AMOUNT";

    @StringProperty(description = "Sets limit for amount of game sessions to be shown in banks winners feed")
    public static final String KEY_WINNER_FEED_GAME_SESSIONS_COUNT = "WINNER_FEED_GAME_SESSIONS_COUNT";

    @BooleanProperty(description = "Sets limit of game sessions for one player to be shown in banks winners feed")
    public static final String KEY_WINNER_FEED_GAME_SESSIONS_DISTINCT_BY_NICKNAME = "WINNER_FEED_GAME_SESSIONS_DISTINCT_BY_NICKNAME";

    @StringProperty(description = "Specifies period (in days) before TTL when record (that fits into winner feed) will be forcibly touched")
    public static final String KEY_WINNER_FEED_EXPIRATION_BARRIER = "WINNER_FEED_EXPIRATION_BARRIER";

    @StringProperty(description = "Sets url for getting game history actions for player")
    public static final String KEY_HISTORY_ACTION_URL = "HISTORY_ACTION_URL";

    @BooleanProperty(description = "If enabled, use input 'lang' param as localization for game title")
    public static final String KEY_LOCALIZE_GAME_TITLE = "LOCALIZE_GAME_TITLE";

    @NumericProperty(description = "Sets history time offset in minutes, range: [1-2880]")
    public static final String KEY_HISTORY_AND_VAB_TIME_OFFSET = "HISTORY_AND_VAB_TIME_OFFSET";

    @StringProperty(description = "DST time zone of history")
    public static final String KEY_HISTORY_AND_VAB_DST_ZONE = "HISTORY_AND_VAB_DST_ZONE";

    @NumericProperty(description = "Amount of history items per page")
    public static final String KEY_HISTORY_ITEMS_PER_PAGE = "HISTORY_ITEMS_PER_PAGE";

    @NumericProperty(description = "TTL of history token in milliseconds")
    public static final String KEY_HISTORY_TOKEN_TTL = "HISTORY_TOKEN_TTL";

    @StringProperty(description = "Static directory name of BankInfo")
    public static final String KEY_STATIC_DIRECTORY_NAME = "STATIC_DIRECTORY_NAME";

    @BooleanProperty(description = "If enabled, doesn't logout on start creating history session")
    public static final String KEY_DONT_LOGOUT_ON_START_HISTORY = "DONT_LOGOUT_ON_START_HISTORY";

    @BooleanProperty(description = "If enabled, adds availability to check if user exists on old system")
    public static final String KEY_CHECK_ACCOUNT_ON_OLD_SYSTEM = "CHECK_ACCOUNT_ON_OLD_SYSTEM";

    @StringProperty(description = "Win limit for jackpot (cents)")
    public static final String KEY_JACKPOT_WIN_LIMIT = "JACKPOT_WIN_LIMIT";

    @StringProperty(description = "Win limit for jackpot in euro (cents)")
    public static final String KEY_JACKPOT_WIN_LIMIT_IN_EUR = "JACKPOT_WIN_LIMIT_IN_EUR";

    @BooleanProperty(description = "If enabled, turns on Jackpot3 feed for bank")
    public static final String NEEDS_JACKPOT_3_FEED = "NEEDS_JACKPOT3_FEED";

    @BooleanProperty(description = "If enabled, creates account with empty firstname, lastname, email, country code" +
            "and with default currency")
    public static final String KEY_NOT_USE_ACCOUNT_INFO_URL_FOR_AUTH = "NOT_USE_ACCOUNT_INFO_URL_FOR_AUTH";

    private static final Long DEFAULT_WINNER_FEED_MIN_WIN_AMOUNT = 1L;
    private static final Integer DEFAULT_WINNER_FEED_GAME_SESSIONS_COUNT = 10;

    @BooleanProperty(description = "If enabled, removes last hand")
    public static final String KEY_NEED_REMOVE_LASTHAND = "NEED_REMOVE_LASTHAND";

    @NumericProperty(description = "Set wager delay in milliseconds")
    public static final String KEY_DELAY_ON_WAGER = "DELAY_ON_WAGER";

    @NumericProperty(description = "Set delay before new round after wager in milliseconds")
    public static final String KEY_DELAY_ON_WAGER_NEW_ROUND = "DELAY_ON_WAGER_NEW_ROUND";

    @BooleanProperty(description = "If enabled, imageURL protocol scheme in response of gamelist and gamelisExt actions " +
            "will be replaced by https if request is https")
    public static final String KEY_IS_NEED_HTTPS_IN_IMAGE_URL_IN_GAMELIST = "IS_NEED_HTTPS_IN_IMAGE_URL_IN_GAMELIST";

    @BooleanProperty(description = "If enabled, iframe with EC keepalive script can be embedded in mobile launcher. " +
            "GAMBLING MANAGEMENT feature")
    public static final String KEY_IS_NEED_TO_EMBED_KEEP_ALIVE_SCRIPT = "IS_NEED_TO_EMBED_KEEP_ALIVE_SCRIPT";

    @StringProperty(description = "Set url for remote doc tool")
    public static final String KEY_DOC_TOOL_REMOTE_URL = "DOC_TOOL_REMOTE_URL";

    @BooleanProperty(description = "If enabled, isRoundFinished parameter must be sended for CommonWallet API")
    public static final String KEY_NOT_IGNORE_ROUND_FINISHED_PARAM_ON_WAGER = "NOT_IGNORE_ROUND_FINISHED_PARAM_ON_WAGER";

    @BooleanProperty(description = "If enabled, saves game SID by round")
    public static final String KEY_SAVE_GAMESID_BY_ROUND = "SAVE_GAMESID_BY_ROUND";

    @BooleanProperty(description = "If enabled, bet history(excluding bonus sessions) may be loaded by date range, without gamesession binding")
    public static final String KEY_SAVE_SHORT_BET_INFO = "SAVE_SHORT_BET_INFO";

    @BooleanProperty(description = "If enabled along with SAVE_SHORT_BET_INFO, bet history(including bonus sessions) may be loaded by date range, without gamesession binding")
    public static final String KEY_SAVE_BONUS_SHORT_BET_INFO = "SAVE_BONUS_SHORT_BET_INFO";

    @NumericProperty(description = "Set minimal request interval between two SHORT_BET_INFO history requests in milliseconds")
    public static final String KEY_SHORT_BET_INFO_MIN_REQUEST_INTERVAL = "SHORT_BET_INFO_MIN_REQUEST_INTERVAL";

    @NumericProperty(description = "Set cassandra ttl for SHORT_BET_INFO in seconds")
    public static final String KEY_SHORT_BET_INFO_TTL = "SHORT_BET_INFO_TTL";

    @StringProperty(description = "Set fraud control processor class")
    public static final String KEY_FRAUD_CONTROL_PROCESSOR_CLASS = "FRAUD_CONTROL_PROCESSOR_CLASS";

    @BooleanProperty(description = "If enabled, uses Java8 proxy to work properly with new versions of SSL on SB")
    public static final String KEY_USES_JAVA8_PROXY = "USES_JAVA8_PROXY";

    @StringProperty(description = "Delay before finish unfinished games (milliseconds)")
    public static final String KEY_DELAY_BEFORE_FINISH_UNFINISHED_GAMES = "DELAY_BEFORE_FINISH_UNFINISHED_GAMES";

    @BooleanProperty(description = "If enabled, will be generated new account Id")
    public static final String KEY_GENERATE_ID_FOR_MIGRATION_DATA = "GENERATE_ID_FOR_MIGRATION_DATA";

    @StringProperty(description = "Sets bank migration status: READY, INPROGRESS, COMPLETED")
    public static final String KEY_MIGRATION_STATUS = "MIGRATION_STATUS";

    @BooleanProperty(description = "If enabled, then game session information with bets will upload to static.")
    public static final String KEY_DAILY_BETS = "DAILY_BETS";

    @BooleanProperty(description = "If enabled, additional currency check will be performed in auth for accounts " +
            "created via FRBonus API for their first login")
    public static final String KEY_IS_NEED_CURRENCY_CHECK_IN_AUTH = "IS_NEED_CURRENCY_CHECK_IN_AUTH";

    @BooleanProperty(description = "If enabled, sound should be enabled by default")
    public static final String KEY_SOUND_ENABLED_BY_DEFAULT_MOBILE = "SOUND_ENABLED_BY_DEFAULT_MOBILE";

    @BooleanProperty(description = "Client can refresh balance through periodically requests on game servlet")
    public static final String KEY_ASYNC_REFRESH_BALANCE = "ASYNC_REFRESH_BALANCE";

    @NumericProperty(description = "Interval between refresh balance requests from client when ASYNC_REFRESH_BALANCE enabled (seconds)")
    public static final String KEY_ASYNC_REFRESH_BALANCE_INTERVAL = "ASYNC_REFRESH_BALANCE_INTERVAL";

    @BooleanProperty(description = "Enable/disable ASYNC_REFRESH_BALANCE with REASON=\"AUTO_BACKGROUND\"")
    public static final String KEY_ASYNC_REFRESH_BALANCE_DENY_AUTO_BACKGROUND_REASON = "ASYNC_REFRESH_BALANCE_DENY_AUTO_BACKGROUND_REASON";

    @BooleanProperty(description = "Enable/disable ASYNC_REFRESH_BALANCE with REASON=\"AUTO_BACKGROUND\" to be queried from external side")
    public static final String KEY_ASYNC_UPDATE_BALANCE_DENY_AUTO_BACKGROUND_REASON = "ASYNC_UPDATE_BALANCE_DENY_AUTO_BACKGROUND_REASON";

    @BooleanProperty(description = "When client.auth is called try to send all pending operations in account")
    public static final String KEY_TRY_RESOLVE_PENDING_OPERATIONS_ON_AUTH = "TRY_RESOLVE_PENDING_OPERATIONS_ON_AUTH";

    @StringProperty(description = "Load JS library with this URL at game page")
    public static final String KEY_LOAD_JS_LIBRARY_URL = "LOAD_JS_LIBRARY_URL";

    @StringProperty(description = "List of most played games IDs separated by '|' without spaces")
    public static final String KEY_MOST_PLAYED_GAMES = "KEY_MOST_PLAYED_GAMES";

    @BooleanProperty(description = "If true send isBonusRound=true on debits in case of in-game bonuses")
    public static final String KEY_SEND_BONUS_FLAG = "SEND_BONUS_FLAG";

    @StringProperty(description = "Sets cron expression to schedule bank shutdown work for bank")
    public static final String KEY_CRON_SHUTDOWN_EXPRESSION = "CRON_SHUTDOWN_EXPRESSION";

    @BooleanProperty(description = "Collect spin statistic")
    public static final String KEY_SPIN_PROFILING = "SPIN_PROFILING";

    @BooleanProperty(description = "If enabled, turns on sending jackpot win email alert")
    public static final String KEY_SEND_JP_WON_EMAIL_ALERT = "SEND_JP_WON_EMAIL_ALERT";

    @StringProperty(description = "delimiter - ';'")
    public static final String KEY_SEND_JP_WON_EMAIL_RECIPIENTS = "SEND_JP_WON_EMAIL_RECIPIENTS";

    @StringProperty(description = "Multiplayer lobby start game URL (host)")
    public static final String KEY_MP_LOBBY_WS_URL = "MP_LOBBY_WS_URL";

    @StringProperty(description = "Game id pattern for routing launches into New Games module (for example: 00010)")
    public static final String KEY_NEW_GAMES_ROUTE_GAME_ID = "NEW_GAMES_ROUTE_GAME_ID";

    @StringProperty(description = "URL of New Games client launch entry (for example: http://localhost:5174/)")
    public static final String KEY_NEW_GAMES_CLIENT_URL = "NEW_GAMES_CLIENT_URL";

    @StringProperty(description = "URL of New Games API (for example: http://localhost:6400)")
    public static final String KEY_NEW_GAMES_API_URL = "NEW_GAMES_API_URL";

    @StringProperty(description = "Base URL of GS internal API for New Games runtime (for example: http://localhost:81)")
    public static final String KEY_NEW_GAMES_GS_INTERNAL_BASE_URL = "NEW_GAMES_GS_INTERNAL_BASE_URL";

    @BooleanProperty(description = "Hide show balance button in multiplayer games")
    public static final String KEY_MP_NOT_SHOW_UPDATE_BALANCE_BUTTON = "MP_NOT_SHOW_UPDATE_BALANCE_BUTTON";

    @BooleanProperty(description = " strong check of bank currency available")
    public static final String KEY_BANK_CURRENCIES_STRONG_CHECK = "BANK_CURRENCIES_STRONG_CHECK";

    @BooleanProperty(description = "Enables opening game history directly from games")
    public static final String KEY_ENABLE_IN_GAME_HISTORY = "ENABLE_IN_GAME_HISTORY";

    @StringProperty(description = "URL with player's game history to open in games")
    public static final String KEY_GAME_HISTORY_URL = "GAME_HISTORY_URL";

    @BooleanProperty(description = "Enables opening game history in the same window")
    public static final String KEY_OPEN_GAME_HISTORY_IN_SAME_WINDOW = "OPEN_GAME_HISTORY_IN_SAME_WINDOW";

    @BooleanProperty(description = "If enabled, sends external bonus id")
    public static final String KEY_SEND_EXT_BONUS_ID = "SEND_EXT_BONUS_ID";

    @StringProperty(description = "Playtech Sportiumes specific URL(clickable icon on client side)")
    public static final String KEY_GAME_MANAGMENT_URL = "GAME_MANAGMENT_URL";
    @StringProperty(description = "Playtech Sportiumes specific URL(clickable icon on client side)")
    public static final String KEY_GAMBLING_REGULATION_URL = "GAMBLING_REGULATION_URL";
    @StringProperty(description = "Playtech Sportiumes specific URL(clickable icon on client side)")
    public static final String KEY_AGE_RESTRICTION_URL = "AGE_RESTRICTION_URL";
    @StringProperty(description = "Playtech Sportiumes specific URL(clickable icon on client side)")
    public static final String KEY_STOP_URL = "STOP_URL";

    @StringProperty(description = "Should be set in format '20|2000', where first number is a feed size and second is big win level in cents")
    public static final String KEY_ROUND_WINS_FEED_CONFIG = "ROUND_WINS_FEED_CONFIG";

    @BooleanProperty(description = "Force to send client type for CW operations")
    public static final String KEY_ALWAYS_SEND_CLIENT_TYPE = "ALWAYS_SEND_CLIENT_TYPE";

    @EnumProperty(value = LogoutActionType.class, description = "Action, which should be performed when page with game is closed or reloaded")
    public static final String KEY_LOGOUT_ACTION = "LOGOUT_ACTION";

    @BooleanProperty(description = "Send gameId on auth")
    public static final String KEY_SEND_GAMEID_ON_AUTH = "SEND_GAMEID_ON_AUTH";

    @BooleanProperty(description = "Add gameId to hash on auth")
    public static final String KEY_ADD_GAMEID_TO_HASH_ON_AUTH = "ADD_GAMEID_TO_HASH_ON_AUTH";

    @BooleanProperty(description = "Add clientType to hash on auth")
    public static final String KEY_ADD_CLIENTTYPE_TO_HASH_ON_AUTH = "ADD_CLIENTTYPE_TO_HASH_ON_AUTH";

    @BooleanProperty(description = "Add clientType to hash on BetResult")
    public static final String KEY_ADD_CLIENTTYPE_TO_HASH_ON_WAGER = "ADD_CLIENTTYPE_TO_HASH_ON_WAGER";

    @BooleanProperty(description = "When this property is set, games should always be loaded from CDN and not from Origin server")
    public static final String KEY_CDN_DISABLE_ORIGIN = "CDN_DISABLE_ORIGIN";

    @BooleanProperty(description = "If TRUE balance from stub bet/win request will be ignored and calculate localy")
    public static final String KEY_IGNORE_STUB_BALANCE_FROM_ES = "IGNORE_STUB_BALANCE_FROM_ES";

    @NumericProperty(description = "Spin count between proposals of switching to real game")
    public static final String KEY_SPIN_COUNT_BETWEEN_REAL_GAME_PROPOSAL = "SPIN_COUNT_BETWEEN_REAL_GAME_PROPOSAL";

    @StringProperty(description = "Game Ids that could be prohibited or allowed. Should be in format '691|210|...' and so on")
    public static final String KEY_AUTOPLAY_GAMES = "AUTOPLAY_GAMES";

    @StringProperty(description = "URL for posting Max Quest Leaderboard results")
    public static final String KEY_LEADERBOARD_RESULTS_URL = "LEADERBOARD_RESULTS_URL";

    @BooleanProperty(description = "Should be checked for licensees who have integrated leaderboard using ugly URL-Encoded JSON message format")
    public static final String KEY_LEADERBOARD_RESULTS_LEGACY_FORMAT = "LEADERBOARD_RESULTS_LEGACY_FORMAT";

    @BooleanProperty(description = "When enabled client should use mutual SSL authentication")
    public static final String KEY_USE_MUTUAL_SSL_AUTH = "USE_MUTUAL_SSL_AUTH";

    @BooleanProperty(description = "When enabled SSL Certificates Checker omits the bank")
    public static final String KEY_SKIP_SSL_CHECK = "SKIP_SSL_CHECK";

    @BooleanProperty(description = "GDPR off if the property is true")
    public static final String GDPR_OFF = "GDPR_OFF";

    @BooleanProperty(description = "When enabled client should send real_bet/real_win when part of a bet may be returned to a player ")
    public static final String KEY_CW_SEND_REAL_BET_WIN = "CW_SEND_REAL_BET_WIN";

    @BooleanProperty(description = "When property is set forces game to keep aspect ratio")
    public static final String KEY_KEEP_SCALE = "KEEP_SCALE";

    @BooleanProperty(description = "When enabled player should not be awarded with MQ start bonus")
    public static final String KEY_MQ_PLAYER_START_BONUS_DISABLED = "MQ_PLAYER_START_BONUS_DISABLED";

    @BooleanProperty(description = "When enabled min/max wallet info saved")
    public static final String KEY_CW_SAVE_MIN_MAX = "CW_SAVE_MIN_MAX";

    @NumericProperty(description = "Games Levels: Min bet value (cents)")
    public static final String KEY_GL_MIN_BET = "GL_MIN_BET";

    @NumericProperty(description = "Games Levels: Max bet value (cents)")
    public static final String KEY_GL_MAX_BET = "GL_MAX_BET";

    @NumericProperty(description = "Games Levels: Max exposure (cents)")
    public static final String KEY_GL_MAX_EXPOSURE = "GL_MAX_EXPOSURE";

    @NumericProperty(description = "Games Levels: Default bet value (cents)")
    public static final String KEY_GL_DEFAULT_BET = "GL_DEFAULT_BET";

    @NumericProperty(description = "Games Levels: OFRB bet value (cents)")
    public static final String KEY_GL_OFRB_BET = "GL_OFRB_BET";

    @BooleanProperty(description = "Games Levels: When enabled GL_OFRB_BET regulator affects non-GL slots as well")
    public static final String KEY_GL_USE_OFRB_BET_FOR_NONGL_SLOTS = "GL_USE_OFRB_BET_FOR_NONGL_SLOTS";

    @BooleanProperty(description = "Games Levels: When enabled GL_OFRB ignores previously applied FRB_COIN values")
    public static final String KEY_GL_OFRB_OVERRIDES_PREDEFINED_COINS = "GL_OFRB_OVERRIDES_PREDEFINED_COINS";

    @NumericProperty(description = "Games Levels: OCB max bet value for Slot (cents)")
    public static final String KEY_GL_OCB_MAX_BET = "GL_OCB_MAX_BET";

    @NumericProperty(description = "Games Levels: OCB max limit value for Table (cents)")
    public static final String KEY_GL_OCB_MAX_TABLE_LIMIT = "GL_OCB_MAX_TABLE_LIMIT";

    @NumericProperty(description = "Games Levels: Number of coins in output set")
    public static final String KEY_GL_NUMBER_OF_COINS = "GL_NUMBER_OF_COINS";

    @NumericProperty(description = "Games Levels: Number of chips in output set")
    public static final String KEY_GL_NUMBER_OF_CHIPS = "GL_NUMBER_OF_CHIPS";

    @BooleanProperty(description = "Games Levels: When enabled all GL_* values are configured in the bank's Default Currency, not EUR")
    public static final String KEY_GL_USE_DEFAULT_CURRENCY = "GL_USE_DEFAULT_CURRENCY";

    @BooleanProperty(description = "When enabled player nickname should be passed to MP")
    public static final String KEY_MP_USE_NICKNAME_IF_PROVIDED = "MP_USE_NICKNAME_IF_PROVIDED";

    @StringProperty(description = "Contains symbols allowed to the MP nickname")
    public static final String KEY_MP_NICKNAME_ALLOWED_SYMBOLS = "MP_NICKNAME_ALLOWED_SYMBOLS";

    @BooleanProperty(description = "When enabled uses unfinished gameId instead of selected")
    public static final String KEY_OVERRIDE_GAME_ID_IF_FOUND_UNFINISHED_GAME =
            "OVERRIDE_GAME_ID_IF_FOUND_UNFINISHED_GAME";

    @BooleanProperty(description = "When enabled send rounds in GameSessionEndedAlert")
    public static final String KEY_SEND_ROUNDS_FOR_GAME_SESSION_ENDED_ALERT =
            "SEND_ROUNDS_FOR_GAME_SESSION_ENDED_ALERT";

    @EnumProperty(value = MaxQuestWeaponMode.class, description = "Mode for MQ special weapons")
    public static final String KEY_MQ_WEAPONS_MODE = "MQ_WEAPONS_MODE";

    @BooleanProperty(description = "When true then bank allow rounds without bets.")
    public static final String KEY_ROUND_WINS_WITHOUT_BETS_ALLOWED = "ROUND_WINS_WITHOUT_BETS_ALLOWED";

    @BooleanProperty(description = "Disables background loading of MQ resources")
    public static final String KEY_DISABLE_MQ_BACKGROUND_LOADING = "DISABLE_MQ_BACKGROUND_LOADING";

    @StringProperty(description = "URL used by MQ tournament lobby to redirect to real mode")
    public static final String KEY_MQ_TOURNAMENT_REAL_MODE_URL = "MQ_TOURNAMENT_REAL_MODE_URL";

    @NumericProperty(description = "Finished tournament time to exclude from lobby in hours")
    public static final String KEY_TOURNAMENT_EXCLUDE_TIME_IN_HOURS = "TOURNAMENT_EXCLUDE_TIME_IN_HOURS";

    @StringProperty(description = "URL to which the result of the tournament is sent")
    public static final String KEY_TOURNAMENT_RESULT_API_URL = "TOURNAMENT_RESULT_API_URL";

    @NumericProperty(description = "Interval in milliseconds to notify tournament players when their bet does not pass qualification requirements")
    public static final String KEY_BET_DOES_NOT_QUALIFY_NOTIFICATION_TIMER_INTERVAL_IN_MS = "BET_DOES_NOT_QUALIFY_NOTIFICATION_TIMER_INTERVAL_IN_MS";

    @BooleanProperty(description = "Whether Buy Feature is disabled")
    public static final String KEY_BUY_FEATURE_DISABLED = "BUY_FEATURE_DISABLED";

    @BooleanProperty(description = "Whether Buy Feature is disabled for Cash Bonus")
    public static final String KEY_BUY_FEATURE_DISABLED_FOR_CASH_BONUS = "BUY_FEATURE_DISABLED_FOR_CASH_BONUS";

    @BooleanProperty(description = "When enabled client should send special weapon bet amount")
    public static final String KEY_CW_SEND_SW_BET = "CW_SEND_SW_BET";

    @BooleanProperty(description = "Whether to send MaxWin value on CMD=ENTER and for HTML5PC shell")
    public static final String KEY_SEND_MAX_WIN_INFO = "SEND_MAX_WIN_INFO";

    @NumericProperty(description = "Initial balance for free and guest games")
    public static final String KEY_FREE_BALANCE = "FREEBALANCE";

    @BooleanProperty(description = "If enabled bank is allowed to be used in stress-testing")
    public static final String KEY_STRESS_TEST_BANK = "STRESS_TEST_BANK";

    @NumericProperty(description = "Multiplier for the free balance forming")
    public static final String KEY_FREE_BALANCE_MULTIPLIER = "FREEBALANCE_MULTIPLIER";

    @BooleanProperty(description = "If enabled do not show time in game client")
    public static final String KEY_NOT_DISPLAY_TIME = "NOT_DISPLAY_TIME";

    @StringProperty(description = "Room ordering type in Max Quest games")
    public static final String KEY_MQ_ROOMS_SORT_ORDER = "MQ_ROOMS_SORT_ORDER";

    @BooleanProperty(description = "If enabled, the winnings will be automatically credited to the balance")
    public static final String KEY_SUPPORT_PROMO_BALANCE_TRANSFER = "SUPPORT_PROMO_BALANCE_TRANSFER";

    @StringProperty(description = "Aliases for specific client currency codes: FAKE1=REAL1;FAKE2=REAL2")
    public static final String KEY_CURRENCY_ALIASES = "CURRENCY_ALIASES";

    @StringProperty(description = "Formatting string to show the currency value in gamehistory.jsp, example1: \"'$'{0}\" , example2: \"{0} PM\"")
    public static final String KEY_CURRENCY_FORMAT_STRING = "CURRENCY_FORMAT_STRING";

    @BooleanProperty(description = "If enabled, switch Holiday Icon")
    public static final String KEY_CHRISTMAS_MODE = "CHRISTMAS_MODE";

    @BooleanProperty(description = "If enabled, close game session on any game error")
    public static final String KEY_LOGOUT_ON_ERROR = "LOGOUT_ON_ERROR";

    @StringProperty(description = "Map with currency rate multipliers: VND=1000;IDR=1000")
    public static final String KEY_CURRENCY_RATE_MULTIPLIER_MAP = "CURRENCY_RATE_MULTIPLIER_MAP";

    @BooleanProperty(description = "If enabled, use local session tracking for game sessions. Used only with REAL_MODE_SESSION_TIMEOUT")
    public static final String KEY_JVM_SESSION_TRACKING = "JVM_SESSION_TRACKING";

    @BooleanProperty(description = "If enabled, send message to opener window with postMessage")
    public static final String KEY_POST_MESSAGE_TO_OPENER = "POST_MESSAGE_TO_OPENER";
    @BooleanProperty(description = "If enabled, send SID to parent window with postMessage")
    public static final String KEY_POST_SID_TO_PARENT = "POST_SID_TO_PARENT";

    @StringProperty(description = "Origin that is allowed to receive messages sent with postMessage")
    public static final String KEY_ALLOWED_ORIGIN = "ALLOWED_ORIGIN";

    @StringProperty(description = "Url for the page to display a Fatal Error")
    public static final String KEY_FATAL_ERROR_PAGE_URL = "FATAL_ERROR_PAGE_URL";

    @StringProperty(description = "Domains that are allowed to be passed via game launch options (for example in homeUrl)")
    public static final String KEY_ALLOWED_DOMAINS = "ALLOWED_DOMAINS";

    @BooleanProperty(description = "Whether to ignore &username= on launch via /isbstartgame.do")
    public static final String KEY_ISB_IGNORE_USERNAME = "ISB_IGNORE_USERNAME";

    @BooleanProperty(description = "If enabled, send statistics for cash bonus sessions")
    public static final String KEY_SEND_BONUS_SESSION_STATISTICS = "SEND_BONUS_SESSION_STATISTICS";

    @StringProperty(description = "Sets customer brand name")
    public static final String KEY_CUSTOMER_BRAND_NAME = "CUSTOMER_BRAND_NAME";

    @StringProperty(description = "Emails separated by ; to send report about unsuccessful notify drive won")
    public static final String KEY_NOTIFY_DRIVE_WON_MAIL_LIST = "NOTIFY_DRIVE_WON_MAIL_LIST";

    @BooleanProperty(description = "If enabled, doesn't allow reusing url to load session")
    public static final String KEY_PREVENT_LAUNCH_URL_REUSE = "PREVENT_LAUNCH_URL_REUSE";
    @StringProperty(description = "Emails separated by ; to send report about pending wallet operation")
    public static final String KEY_PENDING_OPERATION_EMAIL_RECIPIENTS = "PENDING_OPERATION_EMAIL_RECIPIENTS";

    @BooleanProperty(description = "If enabled, allow reusing url to load session by IP")
    public static final String KEY_ALLOW_LAUNCH_URL_REUSE_BY_IP = "ALLOW_LAUNCH_URL_REUSE_BY_IP";

    @EnumProperty(value = MaxQuestClientLogLevel.class, description = "Log level for MQ Client")
    public static final String KEY_MQ_CLIENT_LOG_LEVEL = "MQ_CLIENT_LOG_LEVEL";

    // Runtime naming-cleanup compatibility aliases (read fallback only).
    // These keys allow staged migration away from MQ_* naming without breaking persisted configs.
    private static final String KEY_ABS_PLAYER_START_BONUS_DISABLED = "ABS_PLAYER_START_BONUS_DISABLED";
    private static final String KEY_ABS_WEAPONS_MODE = "ABS_WEAPONS_MODE";
    private static final String KEY_DISABLE_ABS_BACKGROUND_LOADING = "DISABLE_ABS_BACKGROUND_LOADING";
    private static final String KEY_ABS_TOURNAMENT_REAL_MODE_URL = "ABS_TOURNAMENT_REAL_MODE_URL";
    private static final String KEY_ABS_ROOMS_SORT_ORDER = "ABS_ROOMS_SORT_ORDER";
    private static final String KEY_ABS_CLIENT_LOG_LEVEL = "ABS_CLIENT_LOG_LEVEL";
    private static final String KEY_ABS_FRB_DEF_CHIPS = "ABS_FRB_DEF_CHIPS";

    @StringProperty(description = "Sets home page url for  MQ")
    public static final String KEY_HOME_URL_HOST = "HOME_URL_HOST";

    @StringProperty(description = "Sets auth credential for MQB site")
    public static final String KEY_HOST_AUTH_CREDENTIAL = "URL_HOST_AUTH_CREDENTIAL";

    @BooleanProperty(description = "If enabled, add “description” parameter to all BonusWin requests")
    public static final String KEY_ADD_FRB_ZERO_WIN_DESCRIPTION = "ADD_FRB_ZERO_WIN_DESCRIPTION";

    @NumericProperty(description = "If value = -1, winners feed is not shown; default: 0")
    public static final String KEY_WINNERS_FEED_MASK_LENGTH = "WINNERS_FEED_MASK_LENGTH";

    @BooleanProperty(description = "If enabled, set zero instead of negative balance during login")
    public static final String KEY_IGNORE_NEGATIVE_BALANCE_ON_AUTH = "IGNORE_NEGATIVE_BALANCE_ON_AUTH";

    //2 days
    private static final Integer DEFAULT_WINNER_FEED_EXPIRATION_BARRIER = 2;
    private static final Integer DEFAULT_REFRESH_BALANCE_INTERVAL = 10;
    public static final String DEFAULT_OPEN_HELP_HTML = "openHelpHTML";
    public static final String DEFAULT_HISTORY_ACTION = "/gamehistory.do";
    public static final String DEFAULT_GAME_TITLE_LANG = "en";
    public static final String ARRAY_DELIMITER = " ";
    public static final int STANDARD_SPIN_COUNT_VALUE = 5;
    public static final Double MQ_BATTLEGROUND_RAKE_DEFAULT_PERCENT = 5.0;

    private long id;
    private String externalBankId;
    private String externalBankIdDescription;
    private Currency defaultCurrency;
    private Limit limit;
    private List<Coin> coins;
    private List<Currency> currencies = new ArrayList<>();
    private Map<String, String> properties = new HashMap<>();
    private String defaultLanguage = "en";
    private String cashierUrl;
    private String freeGameOverRedirectUrl;
    private boolean persistBets = true;
    private boolean persistWalletOps;
    private boolean persistGameSessions = true;

    private boolean persistPlayerSessions = true;
    private boolean persistAccounts = true;
    private PlayerGameSettingsType pgsType = PlayerGameSettingsType.NONE;


    //pgsTTL in seconds
    private int pgsTTL = DEFAULT_PGS_TTL;
    private long subCasinoId;
    private transient BidirectionalMultivalueMap<Long, Long> gameMigrationConfigMap;
    private RefererDomains allowedRefererDomains = new RefererDomains();
    private RefererDomains forbiddenRefererDomains = new RefererDomains();


    public Map<String, String> getProperties() {
        return cloneProperties(properties);
    }

    private Map<String, String> cloneProperties(Map<String, String> original) {
        return new HashMap<>(original);
    }

    public BankInfo() {
    }

    public BankInfo(long id, String externalBankId, String externalBankIdDescription, Currency defaultCurrency,
                    Limit limit, List<Coin> coins) {
        this.id = id;
        this.externalBankId = externalBankId;
        this.externalBankIdDescription = externalBankIdDescription;
        this.defaultCurrency = defaultCurrency;
        this.limit = limit;
        this.coins = coins;
        currencies = new ArrayList<>();
        properties = new HashMap<>();
        addCurrency(defaultCurrency);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        final String property = PropertyUtils.getStringProperty(properties, KEY_ISENABLED);
        if (property == null) {
            LOG.debug("isEnabled is null, set TRUE as default");
            setEnabled(true);
            return true;
        }
        return Boolean.TRUE.toString().equalsIgnoreCase(property);
    }

    public synchronized void setEnabled(boolean flag) {
        setProperty(KEY_ISENABLED, String.valueOf(flag).toUpperCase());
    }

    public Limit getLimit() {
        return limit;
    }

    public boolean isCurrencyExist(Currency currency) {
        return currencies.contains(currency);
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

    public void addCurrency(Currency currency) {
        if (!isCurrencyExist(currency)) {
            LOG.debug("addCurrency bankId:" + id + " currency:" + currency + " added");
            currencies.add(currency);
        }
    }

    public void removeCurrency(Currency currency) {
        if (isCurrencyExist(currency)) {
            LOG.debug("removeCurrency bankId:" + id + " currency:" + currency + " removed");
            currencies.remove(currency);
        }
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public List<Coin> getCoins() {
        return coins;
    }

    public void setCoins(List<Coin> coins) {
        this.coins = coins;
    }

    public String getGameMigrationConfig() {
        return PropertyUtils.getStringProperty(properties, KEY_GAME_MIGRATION_CONFIG);
    }

    public BidirectionalMultivalueMap<Long, Long> getGameMigrationConfigMap() {
        if (gameMigrationConfigMap == null) {
            try {
                gameMigrationConfigMap = CollectionUtils.stringToLongMap(getGameMigrationConfig());
            } catch (Exception e) {
                LOG.error("getGameMigrationConfigMap error", e);
                gameMigrationConfigMap = new BidirectionalMultivalueMap(Collections.emptyMap(), Collections.emptyMap());
            }
        }
        return gameMigrationConfigMap;
    }

    public void clearGameMigrationConfigMap() {
        gameMigrationConfigMap = null;
    }

    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(Currency defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public String getExternalBankId() {
        return externalBankId;
    }

    public void setProperty(String key, String value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

    public void clearProperties() {
        if (properties != null) {
            properties.clear();
        }
    }

    public void removeProperty(String key) {
        if (properties != null) {
            properties.remove(key);
        }
    }

    public void setExternalBankId(String externalBankId) {
        this.externalBankId = externalBankId;
    }

    public String getExternalBankIdDescription() {
        return externalBankIdDescription;
    }

    public void setExternalBankIdDescription(String externalBankIdDescription) {
        this.externalBankIdDescription = externalBankIdDescription;
    }

    public Coin[] getCafeCoinsAsArray() {
        return coins.toArray(new Coin[coins.size()]);
    }

    public boolean isInitProperty() {
        return properties != null && !properties.isEmpty();
    }

    /* WalletConfiguration.properties start */

    public long getWalletTaskPendingTime() {
        Integer time = PropertyUtils.getIntProperty(properties, KEY_WALLET_TASK_PENDING_TIME);
        if (subCasinoId == 58 && id == 4378) {
            // temporary for test tracking, revert after testing
            return time != null && time > 0 ? TimeUnit.SECONDS.toMillis(time) : TimeUnit.SECONDS.toMillis(DEFAULT_TASK_PENDING_TIME);
        } else {
            return time != null && time > 0 ? TimeUnit.HOURS.toMillis(time) : TimeUnit.HOURS.toMillis(DEFAULT_TASK_PENDING_TIME);
        }
    }

    public String getWPMClass() {
        return PropertyUtils.getStringProperty(properties, KEY_WPM_CLASS);
    }

    public boolean isCWRefundSupported() {
        return getBooleanProperty(properties, KEY_CW_REFUND_SUPPORTED);
    }

    public boolean isCW4BonusPartsSupported() {
        return getBooleanProperty(properties, KEY_CW4_BONUS_PARTS_SUPPORTED);
    }

    public boolean isCWSendAmountInDollars() {
        return getBooleanProperty(properties, KEY_CW_SEND_AMOUNT_IN_DOLLARS);
    }

    public boolean isCWNotAddNegativeBetToWin() {
        return getBooleanProperty(properties, KEY_CW_NOT_ADD_NEGATIVE_BET_TO_WIN);
    }

    public boolean isStubMode() {
        return getBooleanProperty(properties, KEY_STUB_MODE);
    }

    public boolean isAddTokenMode() {
        return getBooleanProperty(properties, KEY_ADD_TOKEN_MODE);
    }

    public boolean isSendBankIdToExtApi() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_BANKID_TO_EXT_API);
    }

    public boolean isAllowUpdatePlayersStatusInPrivateRoom() {
        return getBooleanProperty(properties, KEY_ALLOW_UPDATE_PLAYERS_STATUS_IN_PRIVATE_ROOM);
    }

    public String getUpdatePlayerStatusInPrivateRoomUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_UPDATE_PLAYER_STATUS_IN_PRIVATE_ROOM_URL);
    }

    public String getUpdatePlayersRoomsNumberUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_UPDATE_PLAYERS_ROOMS_NUMBER_URL);
    }

    public String getFriendsUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_GET_FRIENDS_URL);
    }

    public String getInvitePlayersToPrivateRoomUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_INVITE_PLAYERS_TO_PRIVATE_ROOM_URL);
    }

    public String getPlayersOnlineStatusUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_GET_PLAYERS_ONLINE_STATUS_URL);
    }

    public Long getMasterBankId() {
        return PropertyUtils.getLongProperty(properties, KEY_MASTER_BANK_ID);
    }

    public boolean isSaveAndSendTokenInGameWallet() {
        return getBooleanProperty(properties, KEY_SAVE_AND_SEND_TOKEN_IN_GAME_WALLET_MODE);
    }

    public boolean isParseLong() {
        return getBooleanProperty(properties, KEY_PARSE_LONG);
    }

    public String getCWRequestClientClass() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_REQUEST_CLIENT_CLASS);
    }

    public String getCWWSUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_WS_URL);
    }

    public String getCWCancelUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_CANCEL_URL);
    }

    public String getCWBalanceUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_BALANCE_URL);
    }

    public String getCWStatusUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_STATUS_URL);
    }

    public String getCWWagerUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_WAGER_URL);
    }

    public String getCWAuthUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_AUTH_URL);
    }

    public String getCWStartGtbetsUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_START_GTBETS_URL);
    }

    public String getCWCustomCreditUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_CUSTOM_CREDIT_URL);
    }

    public boolean isNotPersistFRBWinOps() {
        return getBooleanProperty(properties, KEY_NOT_PERSIST_FRBWIN_OPS);
    }

    public boolean isNoStartGameIfWalletOpUncompleted() {
        return getBooleanProperty(properties, KEY_NO_START_IF_WALLET_OP_UNCOMP);
    }

    public boolean isRequiredAuthParam() {
        return getBooleanProperty(properties, KEY_CW_AUTH_REQUIRED);
    }

    public boolean isAutoFinishRequired() {
        return getBooleanProperty(properties, KEY_CW_AUTO_FINISH_REQUIRED);
    }

    public String getAuthPassword() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_AUTH_PASS);
    }

    public String getCWUser() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_USER);
    }

    public String getCWPassword() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_PASS);
    }

    public String getIntegrationServiceUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_INTEGRATION_SERVICE_URL);
    }

    public String getIntegrationServiceStandByUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_INTEGRATION_SERVICE_URL_STANDBY);
    }

    public String getIntegrationServiceUserName() {
        return PropertyUtils.getStringProperty(properties, KEY_INTEGRATION_SERVICE_USERNAME);
    }

    public String getIntegrationServicePassword() {
        return PropertyUtils.getStringProperty(properties, KEY_INTEGRATION_SERVICE_PASSWORD);
    }

    public String getHistoryInformerServiceUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_HISTORY_INFORMER_SERVICE_URL);
    }

    public String getMrgrenLogin() {
        return PropertyUtils.getStringProperty(properties, KEY_MRGREN_LOGIN);
    }

    public String getMrgrenPassword() {
        return PropertyUtils.getStringProperty(properties, KEY_MRGREN_PASSWORD);
    }

    public String getServerType() {
        String serverType = PropertyUtils.getStringProperty(properties, KEY_CW_SERVER_TYPE);
        if (serverType == null) {
            return BankConstants.AP;
        }
        return serverType;
    }

    public boolean isTouchSession() {
        return PropertyUtils.getBooleanProperty(properties, KEY_TOUCH_SESSION);
    }

    public Long getTouchSessionPeriod() {
        return getLongProperty(properties, KEY_TOUCH_SESSION_PERIOD);
    }

    public boolean isTouchSessionOnGetTime() {
        return PropertyUtils.getBooleanProperty(properties, KEY_TOUCH_SESSION_ON_GET_TIME);
    }

    public String getRefreshExternalSessionUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_REFRESH_EXTERNAL_SESSION_URL);
    }

    /* WalletConfiguration.properties finish */

    /* TransferConfiguration.properties start */

    public String getPPClass() {
        return PropertyUtils.getStringProperty(properties, KEY_PP_CLASS);
    }

    public String getIntegrationPassword() {
        return PropertyUtils.getStringProperty(properties, KEY_INTEGRATION_PASSWORD);
    }

    public boolean isUsingHashingForAPIEnabled() {
        return !PropertyUtils.getBooleanProperty(properties, KEY_DISABLE_HASHING_FOR_API);
    }

    public String getAPIPassKey() {
        return PropertyUtils.getStringProperty(properties, KEY_API_PASS_KEY);
    }

    public String getServiceIntegrationPassword() {
        return PropertyUtils.getStringProperty(properties, KEY_SERVICE_INTEGRATION_PASSWORD);
    }

    public String getTicketCheckAddress() {
        return PropertyUtils.getStringProperty(properties, KEY_TICKET_CHECK_ADDRESS);
    }

    public String getCTClientClass() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_CLIENT_CLASSNAME);
    }

    public String getCTPassKey() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_PASS_KEY);
    }

    public String getBankFeedsPasskey() {
        return PropertyUtils.getStringProperty(properties, BANK_FEEDS_PASSKEY);
    }

    public Boolean isPOST() {
        return getBooleanProperty(properties, KEY_CT_REST_ISPOST);
    }

    public String getCTRESTAuthURL() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_REST_AUTH_URL);
    }

    public String getCTRESTTransferURL() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_REST_TRANSFER_URL);
    }

    public String getCTRESTEndGameSessionURL() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_REST_ENDGAMESESSION_URL);
    }

    public String getCTRESTBalanceURL() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_REST_BALANCE_URL);
    }

    public String getCTRESTTransactionStatusURL() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_REST_STATUS_URL);
    }

    public String getCTVietbetFindTransactionUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_VIETBET_FIND_TRANSACTION_URL);
    }

    public String getCTVietbetPostTransactionUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_VIETBET_POST_TRANSACTION_URL);
    }

    public String getCTVietbetValidateCustomerUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CT_VIETBET_VALIDATE_CUSTOMER_URL);
    }


    public String getCloseGameProcessorClass() {
        return PropertyUtils.getStringProperty(properties, KEY_CLOSE_GAME_PROCESSOR);
    }

    public String getNotificationCloseGameProcessorUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_NOTIFICATION_CLOSE_GAME_PROCESSOR_URL);
    }

    public String getNotificationCloseGameAuthPass() {
        return PropertyUtils.getStringProperty(properties, KEY_NOTIFICATION_CLOSE_GAME_AUTH_PASS);
    }

    public String getStartGameProcessorClass() {
        return PropertyUtils.getStringProperty(properties, KEY_START_GAME_PROCESSOR);
    }

    public String getNotificationStartGameProcessorUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_NOTIFICATION_START_GAME_PROCESSOR_URL);
    }

    public String getNotificationStartGameAuthPass() {
        return PropertyUtils.getStringProperty(properties, KEY_NOTIFICATION_START_GAME_AUTH_PASS);
    }

    public String getNotificationRoomWasDeactivatedUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_NOTIFICATION_ROOM_WAS_DEACTIVATED_URL);
    }

    public String getNotificationRoomWasDeactivatedAuthPass() {
        return PropertyUtils.getStringProperty(properties, KEY_NOTIFICATION_ROOM_WAS_DEACTIVATED_AUTH_PASS);
    }

    /* TransferConfiguration.properties finish */

    /* BonusConfiguration.properties start */
    public String getBMClass() {
        return PropertyUtils.getStringProperty(properties, KEY_BM_CLASS);
    }

    public boolean isFRBForCTSupported() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_FRB_FOR_CT_SUPPORTED);
    }

    public String getFRBMClass() {
        return PropertyUtils.getStringProperty(properties, KEY_FRBM_CLASS);
    }

    public String getFRBonusWinManager() {
        return PropertyUtils.getStringProperty(properties, KEY_FRBWINM_CLASS);
    }

    public boolean isBCStubMode() {
        return getBooleanProperty(properties, KEY_BC_STUB_MODE);
    }

    public String getBonusRequestClientClass() {
        return PropertyUtils.getStringProperty(properties, KEY_BONUS_REQUEST_CLIENT_CLASS);
    }

    public String getBonusFRRequestClientClass() {
        return PropertyUtils.getStringProperty(properties, KEY_FRBONUS_REQUEST_CLIENT_CLASS);
    }

    public boolean isFrbDenyAwardingFromWs() {
        return PropertyUtils.getBooleanProperty(properties, KEY_FRB_DENY_AWARDING_FROM_WS);
    }

    public String getBonusAuthUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_BONUS_AUTH_URL);
    }

    public String getBonusReleaseUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_BONUS_RELEASE_URL);
    }

    public String getBonusAccountInfoUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_BONUS_ACCOUNTINFO_URL);
    }

    public String getBonusPassKey() {
        return PropertyUtils.getStringProperty(properties, KEY_BONUS_KEY);
    }

    public boolean isHashValueEnable() {
        return getBooleanProperty(properties, KEY_BONUS_IS_HASH_VALUE);
    }

    public boolean isSendBonusAwardTime() {
        return getBooleanProperty(properties, KEY_SEND_BONUS_AWARD_TIME);
    }

    public Long getBonusThresholdMinKey() {
        Long bonusThreshold = getLongProperty(properties, KEY_BONUS_THRESHOLD_MIN_KEY);
        if (bonusThreshold == null) {
            LOG.error("BonusThresholdMinKey not found for bank: {}, use default=100", id);
            bonusThreshold = 100L;
        }
        return bonusThreshold;
    }

    public boolean isBonusInstantLostOnThreshold() {
        return getBooleanProperty(properties, KEY_BONUS_INSTANT_LOST_ON_THRESHOLD);
    }
    /* BonusConfiguration.properties finish */

    public String getPSMClass() {
        return PropertyUtils.getStringProperty(properties, KEY_PSM_CLASS);
    }

    public String getBankDomainName() {
        return PropertyUtils.getStringProperty(properties, KEY_BANK_DOMAIN_NAME);
    }

    public String getCWSpecialRequestHeaders() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_SPECIAL_REQUEST_HEADERS);
    }


    /**
     * Need for unit test
     *
     * @param key
     * @return
     */
    public String getStringProperty(String key) {
        return PropertyUtils.getStringProperty(properties, key);
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getCashierUrl() {
        return cashierUrl;
    }

    public boolean isCashierUrlExist() {
        return !isTrimmedEmpty(cashierUrl);
    }

    public void setCashierUrl(String cashierUrl) {
        this.cashierUrl = cashierUrl;
    }

    public String getFreeGameOverRedirectUrl() {
        return freeGameOverRedirectUrl;
    }

    public boolean isFreeGameOverRedirectUrlExist() {
        return !isTrimmedEmpty(freeGameOverRedirectUrl);
    }

    public void setFreeGameOverRedirectUrl(String freeGameOverRedirectUrl) {
        this.freeGameOverRedirectUrl = freeGameOverRedirectUrl;
    }

    public boolean isPersistBets() {
        return persistBets;
    }

    public void setPersistBets(boolean persistBets) {
        this.persistBets = persistBets;
    }

    public String getStartGameDomain() {
        return PropertyUtils.getStringProperty(properties, KEY_START_GAME_DOMAIN);
    }

    public boolean isReplaceStartServerName() {
        return getReplaceStartServerFrom() != null && getReplaceStartServerTo() != null;
    }

    public boolean isReplaceEndServerName() {
        return getReplaceEndServerFrom() != null && getReplaceEndServerTo() != null;
    }

    public String getReplaceStartServerFrom() {
        return PropertyUtils.getStringProperty(properties, KEY_REPLACE_START_GS_FROM);
    }

    public String getReplaceStartServerTo() {
        return PropertyUtils.getStringProperty(properties, KEY_REPLACE_START_GS_TO);
    }

    public String getReplaceEndServerFrom() {
        return PropertyUtils.getStringProperty(properties, KEY_REPLACE_END_GS_FROM);
    }

    public String getReplaceEndServerTo() {
        return PropertyUtils.getStringProperty(properties, KEY_REPLACE_END_GS_TO);
    }

    public String getAlertsEmailAddress() {
        return PropertyUtils.getStringProperty(properties, KEY_ALERTS_EMAIL_ADDRESS);
    }

    public boolean isSendLoginErrorsToEmail() {
        return !isTrimmedEmpty(getAlertsEmailAddress()) &&
                getBooleanProperty(properties, KEY_SEND_LOGIN_ERRORS_EMAIL);
    }

    public Long getMinWinToSend() {
        String str = getStringProperty(KEY_MIN_WIN_TO_SEND);
        try {
            if (!isTrimmedEmpty(str)) {
                return Long.valueOf(str);
            }
        } catch (NumberFormatException ex) {
            LOG.warn("Can't parse long value " + str);
        }
        return null;
    }

    public String getStandaloneLobbyJspName() {
        return PropertyUtils.getStringProperty(properties, KEY_STANDALONE_LOBBY_JSP);
    }

    // Used for:
    // 1. FRb win with OriginalFRBonusWinManager and IFRBonusClient
    // 2. FRB win notifications with EmptyFRBonusWinManager
    public String getFRBonusWinURL() {
        return PropertyUtils.getStringProperty(properties, KEY_FR_BONUS_WIN_URL);
    }

    public Integer getGameSessionsLimit() {
        return PropertyUtils.getIntProperty(properties, KEY_GAMESESSIONS_LIMIT);
    }

    public Integer getPlayerSessionsLimit() {
        return PropertyUtils.getIntProperty(properties, KEY_PLAYERSESSIONS_LIMIT);
    }

    public boolean isErrorCodesXmlUrl() {
        return getBooleanProperty(properties, KEY_ERROR_CODES_XML_URL);
    }

    public boolean isPersistWalletOps() {
        return persistWalletOps;
    }

    public void setPersistWalletOps(boolean persistWalletOps) {
        this.persistWalletOps = persistWalletOps;
    }

    public boolean isPersistGameSessions() {
        return persistGameSessions;
    }

    public void setPersistGameSessions(boolean persistGameSessions) {
        this.persistGameSessions = persistGameSessions;
    }

    public boolean isPersistPlayerSessions() {
        return persistPlayerSessions;
    }

    public void setPersistPlayerSessions(boolean persistPlayerSessions) {
        this.persistPlayerSessions = persistPlayerSessions;
    }

    public boolean isPersistAccounts() {
        return persistAccounts;
    }

    public void setPersistAccounts(boolean persistAccounts) {
        this.persistAccounts = persistAccounts;
    }

    public PlayerGameSettingsType getPgsType() {
        return pgsType == null ? PlayerGameSettingsType.NONE : pgsType;
    }

    public void setPgsType(PlayerGameSettingsType pgsType) {
        this.pgsType = pgsType;
    }

    public int getPgsTTL() {
        return pgsTTL;
    }

    public void setPgsTTL(int pgsTTL) {
        this.pgsTTL = pgsTTL;
    }

    public String getCWMType() {
        return PropertyUtils.getStringProperty(properties, KEY_CWM_TYPE);
    }

    public String getRefundBetUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CW_REFUND_URL);
    }

    public String getFrbDefChips() {
        if (isTrimmedEmpty(PropertyUtils.getStringProperty(properties, KEY_FRB_DEF_CHIPS))
                || "null".equals(PropertyUtils.getStringProperty(properties, KEY_FRB_DEF_CHIPS))) {
            return "1000";
        }
        return PropertyUtils.getStringProperty(properties, KEY_FRB_DEF_CHIPS);
    }

    public String getMQFrbDefChips() {
        String chips = getStringPropertyWithAliases(MQ_KEY_FRB_DEF_CHIPS, KEY_ABS_FRB_DEF_CHIPS);
        if (!isValidPropertyValue(chips)) {
            return "5";
        }
        return chips;
    }

    public String getAutoPlayValues() {
        return PropertyUtils.getStringProperty(properties, KEY_AUTOPLAY_VALUES);
    }

    public boolean isCurrencySymbolRequired() {
        return getBooleanProperty(properties, KEY_SEND_CURRENCY_SYMBOL);
    }

    public Map<String, String> getAdditionalFlashVars() {
        String additionalFlashVarsAsString = getStringProperty(KEY_ADDITIONAL_FLASHVARS);
        if (isTrimmedEmpty(additionalFlashVarsAsString)) {
            return Maps.newHashMap();
        }
        return MAP_SPLITTER.split(additionalFlashVarsAsString);
    }

    public String getStandaloneLobbyBonusPage() {
        return PropertyUtils.getStringProperty(properties, KEY_STANDALONE_LOBBY_BONUS_PAGE);
    }

    public String getStandaloneLobbyLivedealerPage() {
        return PropertyUtils.getStringProperty(properties, KEY_STANDALONE_LOBBY_LIVEDEALER_PAGE);
    }

    public String getMobileHomeURL() {
        return PropertyUtils.getStringProperty(properties, KEY_MOBILE_HOME_URL);
    }

    public String getHomeURL() {
        return PropertyUtils.getStringProperty(properties, KEY_HOME_URL);
    }

    public String getJsHome() {
        return PropertyUtils.getStringProperty(properties, KEY_JS_HOME);
    }

    public boolean isLaunchHomeFromIFrame() {
        return PropertyUtils.getBooleanProperty(properties, KEY_LAUNCH_HOME_FROM_IFRAME);
    }


    public String getTimeZone() {
        return PropertyUtils.getStringProperty(properties, KEY_TIME_ZONE);
    }

    public String getCustomerSettingsUrl() {
        String settingUrl = PropertyUtils.getStringProperty(properties, KEY_CUSTOMER_SETTINGS_URL);
        if (com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(settingUrl)) {
            settingUrl = "/common/standard/settings/customerspec_descriptor.xml";
        }
        return settingUrl;
    }

    public String getCustomerSettingsHtml5Pc() {
        String settingPath = PropertyUtils.getStringProperty(properties, KEY_CUSTOMER_SETTINGS_HTML5PC);
        if (com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(settingPath)) {
            settingPath = "/html5pc/common/_standard/settings/";
        }
        return settingPath;
    }

    public String getMobileCashierURL() {
        return PropertyUtils.getStringProperty(properties, KEY_MOBILE_CASHIER_URL);
    }

    public boolean isFreeRoundValidityInMinutes() {
        return getBooleanProperty(properties, KEY_FREE_ROUND_VALIDITY_IN_MINUTES);
    }

    public boolean isFreeRoundValidityInHours() {
        return getBooleanProperty(properties, KEY_FREE_ROUND_VALIDITY_IN_HOURS);
    }

    public String getAPIServiceEnvironmentUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_API_SERVICE_GET_ENVIRONMENT_URL);
    }

    public String getAPIServiceFundAccountUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_API_SERVICE_FUND_ACCOUNT_URL);
    }

    public String getAPIServiceActiveTokenUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_API_SERVICE_GET_ACTIVE_TOKEN_URL);
    }

    public boolean isUsePlayerGameSettings() {
        return getBooleanProperty(properties, KEY_USE_PLAYER_GAME_SETTINGS);
    }

    public IndividualGameSettingsType getIndividualGameSettingsType() {
        if (isUsePlayerGameSettings()) {
            return IndividualGameSettingsType.PLAYER;
        }
        return IndividualGameSettingsType.getByName(PropertyUtils.getStringProperty(properties,
                KEY_USE_INDIVIDUAL_GAME_SETTINGS));
    }

    public boolean isUseLocalAcs() {
        return getBooleanProperty(properties, KEY_USE_LOCAL_ACS);
    }

    public boolean isClearLastHandOnChangeCoins() {
        return getBooleanProperty(properties, KEY_CLEAR_LASTHAND_ON_CHANGE_COINS);
    }

    public boolean isClearLastHandOnChangeDefaultCoin() {
        return getBooleanProperty(properties, KEY_CLEAR_LASTHAND_ON_CHANGE_DEF_COIN);
    }

    public String getSendDailyBonusWinUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_SEND_DAILY_BONUS_WIN_URL);
    }

    public String getDailyBonusInfoUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_DAILY_BONUS_INFO_URL);
    }

    public String getPreferableSmtpServer() {
        return PropertyUtils.getStringProperty(properties, KEY_PREFERABLE_SMTP_SERVER);
    }

    public String getSendBetInterceptorClass() {
        return PropertyUtils.getStringProperty(properties, KEY_SEND_BET_INTERCEPTOR_CLASS);
    }

    public String getPartnerId() {
        return PropertyUtils.getStringProperty(properties, KEY_PARTNER_ID);
    }

    public String getPartnerKey() {
        return PropertyUtils.getStringProperty(properties, KEY_PARTNER_KEY);
    }

    public boolean isUseSameDomainForStartGame() {
        return getBooleanProperty(properties, KEY_USE_SAME_DOMAIN_FOR_START_GAME);
    }

    public Long getFreeModeSessionTimeout() {
        return getLongProperty(properties, KEY_FREE_MODE_SESSION_TIMEOUT);
    }

    public Long getRealModeSessionTimeout() {
        return getLongProperty(properties, KEY_REAL_MODE_SESSION_TIMEOUT);
    }

    public String getGameDisabledErrorUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_GAME_DISABLED_ERROR_URL);
    }

    public String getConnectionBrokenUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_CONNECTION_BROKEN_URL);
    }

    public String getGameLoadErrorUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_GAME_LOAD_ERROR_URL);
    }

    public Long getDebitTimeout() {
        return PropertyUtils.getLongProperty(properties, KEY_DEBIT_TIMEOUT_MILLIS);
    }

    public Long getCreditInGameTimeout() {
        return PropertyUtils.getLongProperty(properties, KEY_CREDIT_INGAME_TIMEOUT_MILLIS);
    }

    public boolean isShowDelayedWalletMessages() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SHOW_DELAYED_WALLET_MESSAGES);
    }

    public boolean isSendExternalWalletMessages() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_EXT_WALLET_MESSAGES);
    }

    public String getSessionErrorUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_SESSION_ERROR_URL);
    }

    public String getLoginErrorUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_LOGIN_ERROR_URL);
    }

    public String getGameStartErrorUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_GAME_START_ERROR_URL);
    }

    public boolean isUseSingleGameIdForAllDevices() {
        return getBooleanProperty(properties, KEY_USE_SINGLE_GAMEID_FOR_ALL_DEVICES);
    }

    public boolean isUseSwfShell() {
        return getBooleanProperty(properties, KEY_USE_SWF_SHELL);
    }

    public String getSwfShellDirectory() {
        return PropertyUtils.getStringProperty(properties, KEY_SWF_SHELL_DIRECTORY);
    }

    public String getTemplateSwfShellPath() {
        return getStringProperty(KEY_TEMPLATE_SWF_SHELL_PATH);
    }

    public boolean isShowGameLocalizationError() {
        return getBooleanProperty(properties, KEY_SHOW_GAME_LOCALIZATION_ERROR);
    }

    public boolean isSendVbaToExternalSystem() {
        return getBooleanProperty(properties, KEY_SEND_VBA_TO_EXTERNAL_SYSTEM);
    }

    public boolean isShowExtBetIdInVAB() {
        return getBooleanProperty(properties, KEY_SHOW_EXT_BET_ID_IN_VAB);
    }

    public String getGameServerDomain() {
        return PropertyUtils.getStringProperty(properties, KEY_GAMESERVER_DOMAIN);
    }

    public long getSubCasinoId() {
        return subCasinoId;
    }

    public void setSubCasinoId(long subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    public String getVivoIntegrationPassword() {
        return PropertyUtils.getStringProperty(properties, KEY_VIVO_INTEGRATION_PASSWORD);
    }

    public String getVivoStartGameUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_VIVO_START_GAME_URL);
    }

    public String getVivoOperatorId() {
        return PropertyUtils.getStringProperty(properties, KEY_VIVO_OPERATOR_ID);
    }

    public String getVivoServerId() {
        return PropertyUtils.getStringProperty(properties, KEY_VIVO_SERVER_ID);
    }

    private static final String DEFAULT_EXT_HANDLER = "com.dgphoenix.casino.gs.managers.payment.wallet.ExternalTransactionHandler";

    public String getExternalTransactionHandlerClassName() {
        String handler = PropertyUtils.getStringProperty(properties, KEY_EXTERNAL_WALLET_TRANSACTION_HANDLER_CLASS_NAME);
        if (!StringUtils.isTrimmedEmpty(handler)) {
            return handler;
        } else {
            return DEFAULT_EXT_HANDLER;
        }
    }

    public String getVivoTablesUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_VIVO_GET_TABLES_URL);
    }

    public String getStartGameSessionNotifyUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_START_GAMESESSION_NOTIFY_URL);
    }

    public String getEndGameSessionNotifyUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_END_GAMESESSION_NOTIFY_URL);
    }

    public boolean isFreeBonusRoundSpinView() {
        return getBooleanProperty(properties, KEY_FREE_BONUS_ROUND_SPIN_VIEW);
    }

    public boolean isDailyWalletOperation() {
        return getBooleanProperty(properties, KEY_DAILY_WALLET_OPERATION);
    }

    public String getSshStaticLobbyPath() {
        return PropertyUtils.getStringProperty(properties, KEY_SSH_STATIC_LOBBY_PATH);
    }

    public boolean isSendRoundId() {
        return getBooleanProperty(properties, KEY_SEND_ROUND_ID);
    }

    public boolean isUseHttpProxy() {
        return getBooleanProperty(properties, KEY_USE_HTTP_PROXY);
    }

    public boolean isSendGameIdOnFrbWin() {
        return getBooleanProperty(properties, KEY_SEND_GAMEID_ON_FRBWIN);
    }

    public boolean isSendDetailsOnFrbWin() {
        return getBooleanProperty(properties, KEY_SEND_DETAILS_ON_FRBWIN);
    }

    public boolean isSendDetailsOnRefund() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_DETAILS_ON_REFUND);
    }

    public boolean isSendSummarizedFrbNotification() {
        return getBooleanProperty(properties, KEY_SEND_SUMMARIZED_FRB_NOTIFICATION);
    }

    public boolean isSendDetailsOnBonusRelease() {
        return getBooleanProperty(properties, KEY_SEND_DETAILS_ON_BONUSRELEASE);
    }

    public String getRoundIdGeneratorName() {
        return PropertyUtils.getStringProperty(properties, KEY_ROUNDID_GENERATOR_NAME);
    }

    public boolean isSendZeroFrbWin() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_ZEROFRBWIN);
    }

    public boolean isSendClientTypeOnFRBWin() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_CLIENT_TYPE_ON_FRBWIN);
    }

    public boolean isSendZeroBetOnFrbWin() {
        return getBooleanProperty(properties, KEY_SEND_ZEROBET_ON_FRBWIN);
    }

    public String getSendZeroBetFrbUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_SEND_ZEROBET_FRB_URL);
    }

    public boolean isSendNotZeroFrbBet() {
        return getBooleanProperty(properties, KEY_SEND_NOT_ZERO_FRB_BET);
    }

    public boolean isNoFrbRestart() {
        return PropertyUtils.getBooleanProperty(properties, KEY_NO_FRB_RESTART);
    }

    public boolean isSendJackpotInfoForCommonWallet() {
        return getBooleanProperty(properties, KEY_SEND_JACKPOT_INFO_FOR_CW);
    }

    public boolean isSendJPDetails() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_JP_DETAILS);
    }

    public boolean isDemographicInfoMandatory() {
        return getBooleanProperty(properties, KEY_MANDATORY_DEMOGRAPHIC_INFO);
    }


    public String getTransactionDataClass() {
        return PropertyUtils.getStringProperty(properties, KEY_TRANSACTION_DATA_CLASS);
    }

    public String getExtendedGameplayProcessor() {
        return PropertyUtils.getStringProperty(properties, KEY_EXTENDED_GAMEPLAY_PROCESSOR);
    }

    public String getGameSessionStateListener() {
        return PropertyUtils.getStringProperty(properties, KEY_GAME_SESSION_STATE_LISTENER);
    }

    public boolean isUseWinnerFeed() {
        return getBooleanProperty(properties, KEY_USE_WINNER_FEED);
    }

    public String getJPFeedMode() {
        return PropertyUtils.getStringProperty(properties, KEY_JP_FEED_MODE);
    }

    public boolean isAddExternalFile() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ADD_EXTERNAL_FILE);
    }

    public boolean isUseJpNotification() {
        return getBooleanProperty(properties, KEY_USE_JP_NOTIFICATION);
    }

    public String getRepositoryFile() {
        return PropertyUtils.getStringProperty(properties, KEY_REPOSITORY_FILE);
    }

    public boolean isDevelopmentVersion() {
        return getBooleanProperty(properties, KEY_DEVELOPMENT_VERSION);
    }

    private void assertRefferrerDomainsNotNull() {
        if (allowedRefererDomains == null) {
            allowedRefererDomains = new RefererDomains();
        }
        if (forbiddenRefererDomains == null) {
            forbiddenRefererDomains = new RefererDomains();
        }
    }

    public boolean isDomainAllowed(String domain) {
        assertRefferrerDomainsNotNull();

        if (allowedRefererDomains.isEmpty() && isNeedAllowedRefererDomains()) {
            return false;
        }

        return !forbiddenRefererDomains.isForbidden(domain) && allowedRefererDomains.isAllowed(domain);
    }

    public void setAllowedRefererDomains(String allowedRefererDomains) {
        assertRefferrerDomainsNotNull();
        this.allowedRefererDomains.setRefererDomains(allowedRefererDomains);
    }

    public String getAllowedRefererDomains() {
        assertRefferrerDomainsNotNull();
        return allowedRefererDomains.toString();
    }

    public void setForbiddenRefererDomains(String forbiddenRefererDomains) {
        assertRefferrerDomainsNotNull();
        this.forbiddenRefererDomains.setRefererDomains(forbiddenRefererDomains);
    }

    public String getForbiddenRefererDomains() {
        assertRefferrerDomainsNotNull();
        return forbiddenRefererDomains.toString();
    }

    public boolean isNeedAllowedRefererDomains() {
        return getBooleanProperty(properties, KEY_NEED_ALLOWED_REFERER_DOMAINS);
    }

    public List<Long> getMostPlayedGames() {
        String value = PropertyUtils.getStringProperty(properties, KEY_MOST_PLAYED_GAMES);
        if (value == null) {
            value = "402|512|788|784|384|482|277|704|308|344|771|755|79|775|637|700";
        }

        return CollectionUtils.stringToListOfLongs(value, "|");
    }

    public String getCustomerBrandName() {
        return PropertyUtils.getStringProperty(properties, KEY_CUSTOMER_BRAND_NAME);
    }

    public boolean isPreventLaunchUrlReuse() {
        return getBooleanProperty(properties, KEY_PREVENT_LAUNCH_URL_REUSE);
    }

    public boolean isAllowLaunchUrlReuseByIp() {
        return getBooleanProperty(properties, KEY_ALLOW_LAUNCH_URL_REUSE_BY_IP);
    }

    public MaxQuestClientLogLevel getMQClientLogLevel() {
        return getEnumPropertyWithAliases(KEY_MQ_CLIENT_LOG_LEVEL, MaxQuestClientLogLevel.ERROR,
                KEY_ABS_CLIENT_LOG_LEVEL);
    }

    public String getHomeUrlHost() {
        return getStringProperty(KEY_HOME_URL_HOST);
    }

    public String getHostAuthCredential() {
        return getStringProperty(KEY_HOST_AUTH_CREDENTIAL);
    }

    public boolean isAddFrbZeroWinDescription() {
        return getBooleanProperty(properties, KEY_ADD_FRB_ZERO_WIN_DESCRIPTION);
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        BankInfo fromCopy = (BankInfo) entry;
        externalBankId = fromCopy.getExternalBankId();
        externalBankIdDescription = fromCopy.getExternalBankIdDescription();
        defaultCurrency = fromCopy.getDefaultCurrency();
        limit = fromCopy.getLimit();
        coins = fromCopy.getCoins();
        currencies = ((BankInfo) entry).getCurrencies();
        properties = fromCopy.properties;
        defaultLanguage = fromCopy.getDefaultLanguage();
        cashierUrl = fromCopy.getCashierUrl();
        freeGameOverRedirectUrl = fromCopy.freeGameOverRedirectUrl;
        persistBets = fromCopy.persistBets;
        persistWalletOps = fromCopy.persistWalletOps;
        persistGameSessions = fromCopy.persistGameSessions;
        persistPlayerSessions = fromCopy.persistPlayerSessions;
        persistAccounts = fromCopy.persistAccounts;
        pgsType = fromCopy.pgsType;
        pgsTTL = fromCopy.pgsTTL;
        subCasinoId = fromCopy.subCasinoId;
        allowedRefererDomains = fromCopy.allowedRefererDomains;
        forbiddenRefererDomains = fromCopy.forbiddenRefererDomains;
    }

    public String getVivoLogo() {
        return PropertyUtils.getStringProperty(properties, KEY_VIVO_LOGO);
    }

    public Long getTournamentBankId() {
        return getLongProperty(properties, KEY_TOURNAMENT_BANK_ID);
    }

    public String getTrustedHttpsHosts() {
        return PropertyUtils.getStringProperty(properties, KEY_TRUSTED_HTTPS_HOSTS);
    }

    public Long getWagerDelay() {
        return PropertyUtils.getLongProperty(properties, KEY_DELAY_ON_WAGER);
    }

    public boolean isStandaloneLobbyDontNeedBrandHeader() {
        return PropertyUtils.getBooleanProperty(properties, KEY_STANDALONE_LOBBY_DONT_NEED_BRAND_HEADER);
    }

    public synchronized void setStandaloneLobbyNeedLogoutGame(boolean flag) {
        setProperty(KEY_STANDALONE_LOBBY_DONT_NEED_LOGOUT_GAME, String.valueOf(flag).toUpperCase());
    }

    public boolean isStandaloneLobbyNeedLogoutGame() {
        return PropertyUtils.getBooleanProperty(properties, KEY_STANDALONE_LOBBY_DONT_NEED_LOGOUT_GAME);
    }

    public synchronized void setVietbetLogoutWithWithdrawal(boolean flag) {
        setProperty(KEY_VIETBET_LOGOUT_WITH_WITHDRAWAL, String.valueOf(flag).toUpperCase());
    }

    public boolean isVietbetLogoutWithWithdrawal() {
        return PropertyUtils.getBooleanProperty(properties, KEY_VIETBET_LOGOUT_WITH_WITHDRAWAL);
    }


    public Set<String> getTrustedHttpsHostsAsSet() {
        String s = getTrustedHttpsHosts();
        if (isTrimmedEmpty(s)) {
            return null;
        }
        Set<String> result = new HashSet<>();
        Collections.addAll(result, s.split(ARRAY_DELIMITER));
        return result;
    }

    public String getCdnUrls() {
        return getStringProperty(KEY_CDN_URLS);
    }

    public long getCdnExpireTime() {
        Long expireTime = PropertyUtils.getLongProperty(properties, KEY_CDN_EXPIRE_TIME);
        if (expireTime == null) {
            return DEFAULT_CDN_EXPIRE_TIME;
        }
        return expireTime;
    }

    public Long getSpecialSessionErrorCode() {
        return PropertyUtils.getLongProperty(properties, KEY_SPECIAL_SESSION_ERROR_CODE);
    }

    public Map<String, String> getCdnUrlsMap() {
        return CollectionUtils.stringToMap(getCdnUrls());
    }

    public boolean isCdnForceAuto() {
        return PropertyUtils.getBooleanProperty(properties, KEY_CDN_FORCE_AUTO);
    }

    public void setNeedRemoveLasthand(boolean flag) {
        setProperty(KEY_NEED_REMOVE_LASTHAND, String.valueOf(flag).toUpperCase());
    }

    public boolean isNeedRemoveLasthand() {
        return PropertyUtils.getBooleanProperty(properties, KEY_NEED_REMOVE_LASTHAND);
    }

    public void setNoUseAccountInfoUrlForAuth(boolean flag) {
        setProperty(KEY_NOT_USE_ACCOUNT_INFO_URL_FOR_AUTH, String.valueOf(flag).toUpperCase());
    }

    public boolean isNoUseAccountInfoUrlForAuth() {
        return PropertyUtils.getBooleanProperty(properties, KEY_NOT_USE_ACCOUNT_INFO_URL_FOR_AUTH);
    }

    public boolean isCTBank() {
        return getWPMClass() == null && getCTClientClass() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BankInfo bankInfo = (BankInfo) o;

        if (id != bankInfo.id) {
            return false;
        }
        return !(externalBankId != null ? !externalBankId.equals(bankInfo.externalBankId) : bankInfo.externalBankId != null);

    }

    public boolean isSaveGameSidByRound() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SAVE_GAMESID_BY_ROUND);
    }

    public boolean isSaveShortBetInfo() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SAVE_SHORT_BET_INFO);
    }

    public boolean isSaveBonusShortBetInfo() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SAVE_BONUS_SHORT_BET_INFO);
    }

    public Long getShortBetInfoMinRequestInterval() {
        return PropertyUtils.getLongProperty(properties, KEY_SHORT_BET_INFO_MIN_REQUEST_INTERVAL);
    }

    public Integer getShortBetInfoTtl() {
        return PropertyUtils.getIntProperty(properties, KEY_SHORT_BET_INFO_TTL);
    }

    public void setSaveGameSidByRound(boolean flag) {
        setProperty(KEY_SAVE_GAMESID_BY_ROUND, String.valueOf(flag).toUpperCase());
    }

    public String getFraudControlProcessorClass() {
        return PropertyUtils.getStringProperty(properties, KEY_FRAUD_CONTROL_PROCESSOR_CLASS);
    }

    private <T extends Enum> T getEnumProperty(String key, T defaultValue) {
        return getEnumPropertyWithAliases(key, defaultValue);
    }

    private <T extends Enum> T getEnumPropertyWithAliases(String key, T defaultValue, String... aliases) {
        try {
            String stringProperty = getStringPropertyWithAliases(key, aliases);
            if (!isValidPropertyValue(stringProperty)) {
                return defaultValue;
            }

            Enum anEnum = null;
            try {
                anEnum = Enum.valueOf(defaultValue.getClass(), stringProperty);
            } catch (Exception e) {
                LOG.error("getEnumProperty: unknown name: " + stringProperty +
                        ", for enum=" + defaultValue.getClass(), e);
            }
            if (anEnum != null) {
                return (T) anEnum;
            } else {
                return defaultValue;
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private boolean getBooleanPropertyWithAliases(String key, boolean defaultValue, String... aliases) {
        String value = getStringPropertyWithAliases(key, aliases);
        if (!isValidPropertyValue(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private String getStringPropertyWithAliases(String key, String... aliases) {
        String value = getStringProperty(key);
        if (isValidPropertyValue(value)) {
            return value;
        }
        if (aliases == null) {
            return value;
        }
        for (String alias : aliases) {
            value = getStringProperty(alias);
            if (isValidPropertyValue(value)) {
                return value;
            }
        }
        return value;
    }

    private boolean isValidPropertyValue(String value) {
        return !isTrimmedEmpty(value) && !"null".equalsIgnoreCase(value.trim());
    }

    public void setEnumProperty(String key, Enum anEnum) {
        if (anEnum == null) {
            removeProperty(key);
        } else {
            setProperty(key, anEnum.name());
        }
    }

    @Override
    public int hashCode() {
        return (int) (id ^ id >>> 32);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BankInfo");
        sb.append("[id=").append(id);
        sb.append(", isEnabled=").append(isEnabled());
        sb.append(", subCasinoId=").append(subCasinoId);
        sb.append(", externalBankId='").append(externalBankId).append('\'');
        sb.append(", externalBankIdDescription='").append(externalBankIdDescription).append('\'');
        sb.append(", allowedRefererDomains='").append(allowedRefererDomains).append('\'');
        sb.append(", forbiddenRefererDomains='").append(forbiddenRefererDomains).append('\'');
        sb.append(", defaultCurrency=").append(defaultCurrency);
        sb.append(", limit=").append(limit);
        sb.append(", coins=").append(coins);
        sb.append(", currencies=").append(currencies);
        sb.append(", cashierUrl=").append(cashierUrl);
        sb.append(", persistBets=").append(persistBets);
        sb.append(", persistAccounts=").append(persistAccounts);
        sb.append(", persistGameSessions=").append(persistGameSessions);
        sb.append(", persistPlayerSessions=").append(persistPlayerSessions);
        sb.append(", persistWalletOps=").append(persistWalletOps);
        sb.append(", freeGameOverRedirectUrl=").append(freeGameOverRedirectUrl);
        sb.append(", pgsType=").append(getPgsType());

        sb.append(", |WalletConfiguration| WPMClass=");
        sb.append(getWPMClass());
        sb.append(", CWMType=");
        sb.append(getCWMType());


        sb.append(", stubMode=");
        sb.append(isStubMode());

        sb.append(", CWRequestClientClass=");
        sb.append(getCWRequestClientClass());

        sb.append(", CWWSUrl=");
        sb.append(getCWWSUrl());

        sb.append(", CWCancelUrl=");
        sb.append(getCWCancelUrl());

        sb.append(", CWBalanceUrl=");
        sb.append(getCWBalanceUrl());

        sb.append(", CWStatusUrl=");
        sb.append(getCWStatusUrl());

        sb.append(", CWWagerUrl=");
        sb.append(getCWWagerUrl());

        sb.append(", CWAuthUrl=");
        sb.append(getCWAuthUrl());

        sb.append(", CW3RefundUrl=");
        sb.append(getRefundBetUrl());

        sb.append(", requiredAuthParam=");
        sb.append(isRequiredAuthParam());

        sb.append(", autoFinishRequired=");
        sb.append(isAutoFinishRequired());

        sb.append(", authPassword=");
        sb.append(getAuthPassword());

        sb.append(", integrationServiceUrl=");
        sb.append(getIntegrationServiceUrl());

        sb.append(", integrationServiceUserName=");
        sb.append(getIntegrationServiceUserName());

        sb.append(", integrationServicePassword=");
        sb.append(getIntegrationServicePassword());

        sb.append(", serverType=");
        sb.append(getServerType());

        sb.append(", historyInformerServiceUrl=");
        sb.append(getHistoryInformerServiceUrl());
        sb.append(", defaultLanguage=").append(getDefaultLanguage());


        sb.append(",|TransferConfiguration| PPClass=");
        sb.append(getPPClass());

        sb.append(", integrationPassword=");
        sb.append(getIntegrationPassword());

        sb.append(", serviceIntegrationPassword=");
        sb.append(getServiceIntegrationPassword());

        sb.append(", ticketCheckAddress=");
        sb.append(getTicketCheckAddress());

        sb.append(", CTClientClass=");
        sb.append(getCTClientClass());

        sb.append(", " + "PSM Class=");
        sb.append(getPSMClass());

        sb.append(", CTPassKey=");
        sb.append(getCTPassKey());

        sb.append(", post=");
        sb.append(isPOST());

        sb.append(", CTRESTAuthURL=");
        sb.append(getCTRESTAuthURL());

        sb.append(", CTRESTTransferURL=");
        sb.append(getCTRESTTransferURL());

        sb.append(", CTRESTEndGameSessionURL=");
        sb.append(getCTRESTEndGameSessionURL());

        sb.append(", CTRESTBalanceURL=");
        sb.append(getCTRESTBalanceURL());

        sb.append(", CTRESTTransactionStatusURL=");
        sb.append(getCTRESTTransactionStatusURL());

        sb.append(", CloseGameProcessorClass=");
        sb.append(getCloseGameProcessorClass());

        sb.append(",|BonusConfiguration| BMClass=");
        sb.append(getBMClass());

        sb.append(", " + KEY_IS_FRB_FOR_CT_SUPPORTED + "=");
        sb.append(isFRBForCTSupported());

        sb.append(", FRBMClass=");
        sb.append(getFRBMClass());

        sb.append(", FRBWinManagerClass=");
        sb.append(getFRBonusWinManager());

        sb.append(", FRBDefChips=");
        sb.append(getFrbDefChips());

        sb.append(", MQFRBDefChips=");
        sb.append(getMQFrbDefChips());

        sb.append(", BCStubMode=");
        sb.append(isBCStubMode());

        sb.append(", BonusRequestClientClass=");
        sb.append(getBonusRequestClientClass());

        sb.append(", FRBonusRequestClientClass=");
        sb.append(getBonusFRRequestClientClass());

        sb.append(", BonusAuthUrl=");
        sb.append(getBonusAuthUrl());

        sb.append(", BonusReleaseUrl=");
        sb.append(getBonusReleaseUrl());

        sb.append(", BonusAccountInfoUrl=");
        sb.append(getBonusAccountInfoUrl());

        sb.append(", FRBWinUrl=");
        sb.append(getFRBonusWinURL());

        sb.append(", BonusPassKey=");
        sb.append(getBonusPassKey());

        sb.append(", BonusThresholdMinKey=");
        sb.append(getBonusThresholdMinKey());

        sb.append(", BonusInstantLostOnThreshold=");
        sb.append(isBonusInstantLostOnThreshold());

        sb.append(", " + KEY_AUTOPLAY_VALUES + "=").append(getAutoPlayValues());

        sb.append(", " + KEY_SEND_CURRENCY_SYMBOL + "=").append(isCurrencySymbolRequired());

        sb.append(", KEY_ALERTS_EMAIL_ADDRESS=").append(getAlertsEmailAddress());
        sb.append(", KEY_SEND_LOGIN_ERRORS_EMAIL=").append(isSendLoginErrorsToEmail());

        sb.append(", " + KEY_START_GAME_DOMAIN + "=");
        sb.append(getStartGameDomain());

        sb.append(", " + KEY_NO_START_IF_WALLET_OP_UNCOMP + "=");
        sb.append(isNoStartGameIfWalletOpUncompleted());

        sb.append(", " + KEY_REPLACE_START_GS_FROM + "=");
        sb.append(getReplaceStartServerFrom());

        sb.append(", " + KEY_REPLACE_START_GS_TO + "=");
        sb.append(getReplaceStartServerTo());

        sb.append(", " + KEY_REPLACE_END_GS_FROM + "=");
        sb.append(getReplaceEndServerFrom());

        sb.append(", " + KEY_REPLACE_END_GS_TO + "=");
        sb.append(getReplaceEndServerTo());

        sb.append(", " + KEY_STANDALONE_LOBBY_JSP + "=");
        sb.append(getStandaloneLobbyJspName());

        sb.append(", " + KEY_STANDALONE_LOBBY_BONUS_PAGE + "=");
        sb.append(getStandaloneLobbyBonusPage());

        sb.append(", " + KEY_STANDALONE_LOBBY_LIVEDEALER_PAGE + "=");
        sb.append(getStandaloneLobbyLivedealerPage());

        sb.append(", " + KEY_MOBILE_HOME_URL + "=");
        sb.append(getMobileHomeURL());

        sb.append(", " + KEY_MOBILE_CASHIER_URL + "=");
        sb.append(getMobileCashierURL());

        sb.append(", " + KEY_GAMESESSIONS_LIMIT + "=");
        sb.append(getGameSessionsLimit());

        sb.append(", " + KEY_PLAYERSESSIONS_LIMIT + "=");
        sb.append(getPlayerSessionsLimit());

        sb.append(", PARSE_LONG(cent format)=");
        sb.append(isParseLong());

        sb.append(", " + KEY_ERROR_CODES_XML_URL + "=");
        sb.append(isErrorCodesXmlUrl());

        sb.append(", " + KEY_FREE_ROUND_VALIDITY_IN_MINUTES + "=");
        sb.append(isFreeRoundValidityInMinutes());

        sb.append(", " + KEY_FREE_ROUND_VALIDITY_IN_HOURS + "=");
        sb.append(isFreeRoundValidityInHours());

        sb.append(", " + KEY_USE_PLAYER_GAME_SETTINGS + "=");
        sb.append(isUsePlayerGameSettings());

        sb.append(", " + KEY_USE_INDIVIDUAL_GAME_SETTINGS + "=");
        sb.append(getIndividualGameSettingsType());

        sb.append(", " + KEY_USE_LOCAL_ACS + "=");
        sb.append(isUseLocalAcs());

        sb.append(", " + KEY_CLEAR_LASTHAND_ON_CHANGE_COINS + "=");
        sb.append(isClearLastHandOnChangeCoins());

        sb.append(", " + KEY_CLEAR_LASTHAND_ON_CHANGE_DEF_COIN + "=");
        sb.append(isClearLastHandOnChangeDefaultCoin());

        sb.append(", " + KEY_SEND_DAILY_BONUS_WIN_URL + "=");
        sb.append(getSendDailyBonusWinUrl());

        sb.append(", " + KEY_DAILY_BONUS_INFO_URL + "=");
        sb.append(getDailyBonusInfoUrl());

        sb.append(", " + KEY_PREFERABLE_SMTP_SERVER + "=");
        sb.append(getPreferableSmtpServer());

        sb.append(", " + KEY_SEND_BET_INTERCEPTOR_CLASS + "=");
        sb.append(getSendBetInterceptorClass());

        sb.append(", " + KEY_USE_SINGLE_GAMEID_FOR_ALL_DEVICES + "=").append(isUseSingleGameIdForAllDevices());

        sb.append(", " + KEY_SHOW_GAME_LOCALIZATION_ERROR + "=").append(isShowGameLocalizationError());

        sb.append(", " + KEY_GAMESERVER_DOMAIN + "=").append(getGameServerDomain());

        sb.append(", " + KEY_SEND_VBA_TO_EXTERNAL_SYSTEM + "=").append(isSendVbaToExternalSystem());

        if (getPartnerId() != null) {
            sb.append(", ").append(KEY_PARTNER_ID).append("=").append(getPartnerId());
        }
        if (getPartnerKey() != null) {
            sb.append(", ").append(KEY_PARTNER_KEY).append("=").append(getPartnerKey());
        }

        sb.append(", " + KEY_USE_SAME_DOMAIN_FOR_START_GAME + "=");
        sb.append(isUseSameDomainForStartGame());
        if (getFreeModeSessionTimeout() != null) {
            sb.append(", ").append(KEY_FREE_MODE_SESSION_TIMEOUT).append("=").append(getFreeModeSessionTimeout());
        }
        if (getRealModeSessionTimeout() != null) {
            sb.append(", ").append(KEY_REAL_MODE_SESSION_TIMEOUT).append("=").append(getRealModeSessionTimeout());
        }
        if (getSessionErrorUrl() != null) {
            sb.append(", ").append(KEY_SESSION_ERROR_URL).append("=").append(getSessionErrorUrl());
        }
        if (PropertyUtils.getIntProperty(properties, KEY_WALLET_TASK_PENDING_TIME) != null) {
            sb.append(", ").append(KEY_WALLET_TASK_PENDING_TIME).append("=").append(TimeUnit.MILLISECONDS.toHours(getWalletTaskPendingTime()));
        }
        if (PropertyUtils.getStringProperty(properties, KEY_FREE_BONUS_ROUND_SPIN_VIEW) != null) {
            sb.append(", ").append(KEY_FREE_BONUS_ROUND_SPIN_VIEW).append("=").append(isFreeBonusRoundSpinView());
        }
        if (PropertyUtils.getStringProperty(properties, KEY_DAILY_WALLET_OPERATION) != null) {
            sb.append(", ").append(KEY_DAILY_WALLET_OPERATION).append("=").append(isDailyWalletOperation());
        }
        sb.append(", " + KEY_BANK_DOMAIN_NAME + "=").append(getBankDomainName());
        sb.append(", " + KEY_VIVO_INTEGRATION_PASSWORD + "=").append(getVivoIntegrationPassword());
        sb.append(", " + KEY_VIVO_START_GAME_URL + "=").append(getVivoStartGameUrl());
        sb.append(", " + KEY_VIVO_OPERATOR_ID + "=").append(getVivoOperatorId());
        sb.append(", " + KEY_VIVO_SERVER_ID + "=").append(getVivoServerId());
        sb.append(", " + KEY_VIVO_GET_TABLES_URL + "=").append(getVivoTablesUrl());
        sb.append(", " + KEY_EXTERNAL_WALLET_TRANSACTION_HANDLER_CLASS_NAME + "=").
                append(getExternalTransactionHandlerClassName());
        sb.append(", " + KEY_START_GAMESESSION_NOTIFY_URL + "=").append(getStartGameSessionNotifyUrl());
        sb.append(", " + KEY_END_GAMESESSION_NOTIFY_URL + "=").append(getEndGameSessionNotifyUrl());
        sb.append(", " + KEY_SSH_STATIC_LOBBY_PATH + "=").append(getSshStaticLobbyPath());
        sb.append(", " + KEY_SEND_ROUND_ID + "=").append(isSendRoundId());
        sb.append(", " + KEY_USE_HTTP_PROXY).append("=").append(isUseHttpProxy());
        sb.append(", ").append(KEY_SEND_GAMEID_ON_FRBWIN).append("=").append(isSendGameIdOnFrbWin());
        sb.append(", ").append(KEY_SEND_ZEROBET_ON_FRBWIN).append("=").append(isSendZeroBetOnFrbWin());
        sb.append(", ").append(KEY_SEND_ZEROBET_FRB_URL).append("=").append(getSendZeroBetFrbUrl());
        sb.append(", ").append(KEY_ROUNDID_GENERATOR_NAME).append("=").append(getRoundIdGeneratorName());
        sb.append(", ").append(KEY_SEND_JACKPOT_INFO_FOR_CW).append("=").append(isSendJackpotInfoForCommonWallet());
        sb.append(", ").append(KEY_MANDATORY_DEMOGRAPHIC_INFO).append("=").append(isDemographicInfoMandatory());
        sb.append(", ").append(KEY_FRB_DENY_AWARDING_FROM_WS).append("=").append(isFrbDenyAwardingFromWs());
        sb.append(", ").append(KEY_MIN_WIN_TO_SEND).append("=").append(getMinWinToSend());
        sb.append(", ").append(KEY_USES_JAVA8_PROXY).append("=").append(isUsesJava8Proxy());

        sb.append(", ").append(KEY_GAME_MIGRATION_CONFIG).append("=").append(getGameMigrationConfig());
        sb.append(", ").append(KEY_CW_SPECIAL_REQUEST_HEADERS).append("=").append(getCWSpecialRequestHeaders());
        sb.append(", ").append(KEY_TRANSACTION_DATA_CLASS).append("=").append(getTransactionDataClass());
        sb.append(", ").append(KEY_EXTENDED_GAMEPLAY_PROCESSOR).append("=").append(getExtendedGameplayProcessor());

        sb.append(", ").append(KEY_USE_WINNER_FEED).append("=").append(isUseWinnerFeed());
        if (getJPFeedMode() != null) {
            sb.append(", ").append(KEY_JP_FEED_MODE).append("=").append(getJPFeedMode());
        }
        sb.append(", ").append(KEY_USE_JP_NOTIFICATION).append("=").append(isUseJpNotification());
        sb.append(", ").append(KEY_REPOSITORY_FILE).append("=").append(getRepositoryFile());
        sb.append(", ").append(KEY_DEVELOPMENT_VERSION).append("=").append(isDevelopmentVersion());

        sb.append(", ").append(KEY_CW_REFUND_SUPPORTED).append("=").append(isCWRefundSupported());
        sb.append(", ").append(KEY_CW4_BONUS_PARTS_SUPPORTED).append("=").append(isCW4BonusPartsSupported());
        sb.append(", ").append(KEY_CW_SEND_AMOUNT_IN_DOLLARS).append("=").append(isCWSendAmountInDollars());
        sb.append(", ").append(KEY_CW_NOT_ADD_NEGATIVE_BET_TO_WIN).append("=").append(isCWNotAddNegativeBetToWin());
        sb.append(", ").append(KEY_ALLOW_UPDATE_PLAYERS_STATUS_IN_PRIVATE_ROOM).append("=").append(isAllowUpdatePlayersStatusInPrivateRoom());
        sb.append(", UpdatePlayerStatusInPrivateRoomUrl=");
        sb.append(getUpdatePlayerStatusInPrivateRoomUrl());
        sb.append(", FatalErrorPageUrl=");
        sb.append(getFatalErrorPageUrl());
        sb.append(", UpdatePlayersRoomsNumberUrl=");
        sb.append(getUpdatePlayersRoomsNumberUrl());
        sb.append(", FriendsUrl=");
        sb.append(getFriendsUrl());
        sb.append(", InvitePlayersToPrivateRoomUrl=");
        sb.append(getInvitePlayersToPrivateRoomUrl());
        sb.append(", PlayersOnlineStatusUrl=");
        sb.append(getPlayersOnlineStatusUrl());
        sb.append(", ").append(KEY_CHECK_ACCOUNT_ON_OLD_SYSTEM).append("=").append(isCheckAccountOnOldSystem());
        sb.append(", ").append(KEY_DONT_LOGOUT_ON_START_HISTORY).append("=").append(isDontLogoutOnStartHistory());
        sb.append(", ").append(KEY_TRUSTED_HTTPS_HOSTS).append("=").append(getTrustedHttpsHosts());
        if (getTournamentBankId() != null) {
            sb.append(", ").append(KEY_TOURNAMENT_BANK_ID).append("=").append(getTournamentBankId());
        }
        sb.append(", ").append(KEY_DAILY_BETS).append("=").append(isDailyBets());
        sb.append(", ").append(KEY_ASYNC_REFRESH_BALANCE).append("=").append(isAsyncRefreshBalance());
        sb.append(", ").append(KEY_ASYNC_REFRESH_BALANCE_DENY_AUTO_BACKGROUND_REASON).append("=").append(isRefreshBalanceAutoDenied());
        sb.append(", ").append(KEY_ASYNC_UPDATE_BALANCE_DENY_AUTO_BACKGROUND_REASON).append("=").append(isUpdateBalanceAutoDenied());
        sb.append(", ").append(KEY_BANK_CURRENCIES_STRONG_CHECK).append("=").append(isBankCurrenciesStrongCheck());
        sb.append(", ").append(KEY_ALWAYS_SEND_CLIENT_TYPE).append("=").append(isAlwaysSendClientType());
        sb.append(", ").append(KEY_SAVE_GAMESID_BY_ROUND).append("=").append(isSaveGameSidByRound());
        sb.append(", ").append(KEY_MQ_WEAPONS_MODE).append("=").append(getMaxQuestWeaponMode());
        sb.append(", ").append(KEY_ROUND_WINS_WITHOUT_BETS_ALLOWED).append("=").append(isRoundWinsWithoutBetsAllowed());
        sb.append(", ").append(KEY_PREVENT_LAUNCH_URL_REUSE).append("=").append(isPreventLaunchUrlReuse());
        sb.append(", ").append(KEY_ALLOW_LAUNCH_URL_REUSE_BY_IP).append("=").append(isAllowLaunchUrlReuseByIp());
        sb.append(", ").append(KEY_MQ_CLIENT_LOG_LEVEL).append("=").append(getMQClientLogLevel());
        sb.append(", ").append(KEY_HOME_URL_HOST).append("=").append(getHomeUrlHost());
        sb.append(", ").append(KEY_ADD_FRB_ZERO_WIN_DESCRIPTION).append("=").append(isAddFrbZeroWinDescription());
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        output.writeLong(id, true);
        output.writeLong(subCasinoId, true);
        output.writeString(externalBankId);
        output.writeString(externalBankIdDescription);
        kryo.writeObjectOrNull(output, defaultCurrency, Currency.class);
        kryo.writeObjectOrNull(output, limit, Limit.class);
        kryo.writeClassAndObject(output, coins == null ? new ArrayList<>() : coins);
        kryo.writeClassAndObject(output, currencies);
        kryo.writeClassAndObject(output, properties);
        output.writeString(defaultLanguage);
        output.writeString(cashierUrl);
        output.writeString(freeGameOverRedirectUrl);
        output.writeBoolean(persistBets);
        output.writeBoolean(persistWalletOps);
        output.writeBoolean(persistGameSessions);
        output.writeBoolean(persistPlayerSessions);
        output.writeBoolean(persistAccounts);
        output.writeString(allowedRefererDomains == null ? null : allowedRefererDomains.toString());
        output.writeString(forbiddenRefererDomains == null ? null : forbiddenRefererDomains.toString());
        kryo.writeObject(output, getPgsType());
        output.writeInt(pgsTTL);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt(true);
        id = input.readLong(true);
        subCasinoId = input.readLong(true);
        externalBankId = input.readString();
        externalBankIdDescription = input.readString();
        if (ver >= 2) {
            defaultCurrency = kryo.readObjectOrNull(input, Currency.class, Currency.SERIALIZER);
            limit = kryo.readObjectOrNull(input, Limit.class);
        } else {
            defaultCurrency = kryo.readObject(input, Currency.class, Currency.SERIALIZER);
            limit = kryo.readObject(input, Limit.class);
        }
        try {
            coins = (List<Coin>) kryo.readClassAndObject(input);
        } catch (Exception e) {
            LOG.error("read error: cannot load coins (may be coins is null) for bankId=" + id, e);
            coins = new ArrayList<>();
        }
        Registration currencyListClass = kryo.readClass(input);
        if (currencyListClass != null) {
            currencies = (List<Currency>) kryo.readObject(input, currencyListClass.getType(), Currency.LIST_SERIALIZER);
        } else {
            currencies = new ArrayList<>();
        }

        try {
            properties = (Map<String, String>) kryo.readClassAndObject(input);
        } catch (Exception e) {
            LOG.error("read error: cannot load properties (may be properties is null) for bankId=" + id, e);
            properties = new HashMap<>();
        }

        defaultLanguage = input.readString();
        cashierUrl = input.readString();
        freeGameOverRedirectUrl = input.readString();
        persistBets = input.readBoolean();
        persistWalletOps = input.readBoolean();
        persistGameSessions = input.readBoolean();
        persistPlayerSessions = input.readBoolean();
        persistAccounts = input.readBoolean();
        if (ver == 0) {
            allowedRefererDomains = new RefererDomains();
            forbiddenRefererDomains = new RefererDomains();
        } else {
            allowedRefererDomains = new RefererDomains(input.readString());
            forbiddenRefererDomains = new RefererDomains(input.readString());
        }
        pgsType = kryo.readObject(input, PlayerGameSettingsType.class);
        pgsTTL = input.readInt();
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
       gen.writeNumberField("id", id);
       gen.writeNumberField("subCasinoId", subCasinoId);
       gen.writeStringField("externalBankId", externalBankId);
       gen.writeStringField("externalBankIdDescription", externalBankIdDescription);
       gen.writeObjectField("defaultCurrency", defaultCurrency);
       gen.writeObjectField("limit", limit);
       serializeListField(gen, "coins", coins == null ? new ArrayList<>() : coins, new TypeReference<List<Coin>>() {});
       serializeListField(gen, "currencies", currencies, new TypeReference<List<Currency>>() {});
       serializeMapField(gen, "properties", properties, new TypeReference<Map<String,String>>() {});
       gen.writeStringField("defaultLanguage", defaultLanguage);
       gen.writeStringField("cashierUrl", cashierUrl);
       gen.writeStringField("freeGameOverRedirectUrl", freeGameOverRedirectUrl);
       gen.writeBooleanField("persistBets", persistBets);
       gen.writeBooleanField("persistWalletOps", persistWalletOps);
       gen.writeBooleanField("persistGameSessions", persistGameSessions);
       gen.writeBooleanField("persistPlayerSessions", persistPlayerSessions);
       gen.writeBooleanField("persistAccounts", persistAccounts);
       gen.writeStringField("allowedRefererDomainsStr", allowedRefererDomains == null ? null : allowedRefererDomains.toString());
       gen.writeStringField("forbiddenRefererDomainsStr", forbiddenRefererDomains == null ? null : forbiddenRefererDomains.toString());
       gen.writeNumberField("pgsTypeId", getPgsType().ordinal());
       gen.writeNumberField("pgsTTL", pgsTTL);
    }

    @Override
    public BankInfo deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode n = p.getCodec().readTree(p);

        id = n.get("id").longValue();
        subCasinoId = n.get("subCasinoId").longValue();
        externalBankId = readNullableText(n, "externalBankId");
        externalBankIdDescription = readNullableText(n, "externalBankIdDescription");
        defaultCurrency = ((ObjectMapper) p.getCodec()).convertValue(n.get("defaultCurrency"), Currency.class);
        limit = ((ObjectMapper) p.getCodec()).convertValue(n.get("limit"), Limit.class);
        try {
            coins = ((ObjectMapper) p.getCodec()).convertValue(n.get("coins"), new TypeReference<List<Coin>>() {});
        } catch (Exception e) {
            LOG.error("read error: cannot load coins (may be coins is null) for bankId=" + id, e);
            coins = new ArrayList<>();
        }
        currencies = ((ObjectMapper) p.getCodec()).convertValue(n.get("currencies"), new TypeReference<List<Currency>>() {});

        try {
            properties = ((ObjectMapper) p.getCodec()).convertValue(n.get("properties"), new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            LOG.error("read error: cannot load properties (may be properties is null) for bankId=" + id, e);
            properties = new HashMap<>();
        }

        defaultLanguage = readNullableText(n, "defaultLanguage");
        cashierUrl = readNullableText(n, "cashierUrl");
        freeGameOverRedirectUrl = readNullableText(n, "freeGameOverRedirectUrl");
        persistBets = n.get("persistBets").asBoolean();
        persistWalletOps = n.get("persistWalletOps").asBoolean();
        persistGameSessions = n.get("persistGameSessions").asBoolean();
        persistPlayerSessions = n.get("persistPlayerSessions").asBoolean();
        persistAccounts = n.get("persistAccounts").asBoolean();
        allowedRefererDomains = new RefererDomains(readNullableText(n, "allowedRefererDomainsStr"));
        forbiddenRefererDomains = new RefererDomains(readNullableText(n, "forbiddenRefererDomainsStr"));
        pgsType = PlayerGameSettingsType.values()[n.get("pgsTypeId").intValue()];
        pgsTTL = n.get("pgsTTL").asInt();

        return this;
    }

    public boolean isUseMiniGameShell() {
        return getBooleanProperty(properties, KEY_IS_USE_MINIGAME_SHELL);
    }

    public boolean isWinnerFeedDistinctEnabled() {
        return getBooleanProperty(properties, KEY_WINNER_FEED_GAME_SESSIONS_DISTINCT_BY_NICKNAME);
    }

    public boolean isUseSwfLgaShell() {
        return getBooleanProperty(properties, KEY_IS_USE_SWF_LGA_SHELL);
    }

    public boolean isNeedChangeLang() {
        return getBooleanProperty(properties, KEY_NEED_CHANGE_LANG);
    }

    public boolean isNotPassSessionError() {
        return getBooleanProperty(properties, KEY_NOT_PASS_SESSION_ERROR);
    }

    public String getOpenHelpHtml() {
        String html = getStringProperty(KEY_OPEN_HELP_HTML);
        return isTrimmedEmpty(html) ? DEFAULT_OPEN_HELP_HTML : html;
    }

    public boolean isNeedCloseWindow() {
        return getBooleanProperty(properties, KEY_NEED_CLOSE_WINDOW);
    }

    public boolean needAffactivePopup() {
        return getBooleanProperty(properties, KEY_NEED_AFFACTIVE_POPUP);
    }

    public boolean needDrakePopup() {
        return getBooleanProperty(properties, KEY_NEED_DRAKE_POPUP);
    }

    public boolean needGossipPopup() {
        return getBooleanProperty(properties, KEY_NEED_GOSSIP_POPUP);
    }

    public boolean isNeedHasFlashScript() {
        return getBooleanProperty(properties, KEY_NEED_HAS_FLASH_SCRIPT);
    }

    public boolean isNeedWtReload() {
        return getBooleanProperty(properties, KEY_IS_NEED_WT_RELOAD);
    }

    public boolean needOpenCachierPopup() {
        return getBooleanProperty(properties, KEY_NEED_OPEN_CACHIER_POPUP);
    }

    public boolean needWindowParentParentLocation() {
        return getBooleanProperty(properties, KEY_NEED_WINDOW_PARENT_PARENT_LOCATION);
    }

    public boolean needWindowParentLocation() {
        return getBooleanProperty(properties, KEY_NEED_WINDOW_PARENT_LOCATION);
    }

    public boolean needRedirectAndClose() {
        return getBooleanProperty(properties, KEY_NEED_REDIRECT_AND_CLOSE);
    }

    public boolean isNeedRedirect() {
        return getBooleanProperty(properties, KEY_NEED_REDIRECT);
    }

    public boolean needJsInstantCashier() {
        return getBooleanProperty(properties, KEY_NEED_JS_INSTANT_CASHIER);
    }

    public boolean isDontShowMessageInTestMode() {
        return getBooleanProperty(properties, KEY_DONT_SHOW_MESSAGE_IN_TEST_MODE);
    }

    public boolean isNeedSWFObject() {
        return getBooleanProperty(properties, KEY_IS_NEED_SWF_OBJECT);
    }

    public Long getWinnerFeedMinWinAmount() {
        return getLongPropertyOrDefault(KEY_WINNER_FEED_MIN_WIN_AMOUNT, DEFAULT_WINNER_FEED_MIN_WIN_AMOUNT);
    }

    public int getWinnerFeedGameSessionsCount() {
        return (int) getLongPropertyOrDefault(KEY_WINNER_FEED_GAME_SESSIONS_COUNT, (long) DEFAULT_WINNER_FEED_GAME_SESSIONS_COUNT);
    }

    public int getWinnerFeedExpirationBarrier() {
        return (int) getLongPropertyOrDefault(KEY_WINNER_FEED_EXPIRATION_BARRIER, (long) DEFAULT_WINNER_FEED_EXPIRATION_BARRIER);
    }

    protected long getLongPropertyOrDefault(String keyWinnerFeedMinWinAmount, Long defaultMinWinAmount) {
        Long minWinAmount;
        try {
            minWinAmount = getLongProperty(properties, keyWinnerFeedMinWinAmount);
            if (minWinAmount == null) {
                throw new NumberFormatException();
            }
        } catch (Exception ignored) {
            minWinAmount = defaultMinWinAmount;
        }
        return minWinAmount;
    }

    public String getHistoryActionURL() {
        String bankHistoryAction = getStringProperty(KEY_HISTORY_ACTION_URL);
        return isTrimmedEmpty(bankHistoryAction) ? DEFAULT_HISTORY_ACTION : bankHistoryAction;
    }

    public boolean localizeGameTitle() {
        return getBooleanProperty(properties, KEY_LOCALIZE_GAME_TITLE);
    }

    public int getHistoryTimeOffset() {
        Integer offset = PropertyUtils.getIntProperty(properties, KEY_HISTORY_AND_VAB_TIME_OFFSET);
        return (offset != null && offset > 0 && offset <= TimeUnit.DAYS.toMinutes(2)) ? offset : 0;
    }

    public ZoneId getHistoryDSTZone() {
        String dstTimeZone = PropertyUtils.getStringProperty(properties, KEY_HISTORY_AND_VAB_DST_ZONE);
        if (!StringUtils.isTrimmedEmpty(dstTimeZone)) {
            ZoneId zone = ZoneId.of(dstTimeZone);
            return !zone.getId().equals("GMT") ? zone : null;
        } else {
            return null;
        }
    }

    public int getHistoryOffsetInclDst(long millis) {
        int result = getHistoryTimeOffset();
        if (getHistoryDSTZone() != null) {
            result += TimeUnit.MILLISECONDS.toMinutes(getHistoryDSTZone().getRules().getDaylightSavings(Instant.ofEpochMilli(millis)).toMillis());
        }
        return result;
    }

    public int getHistoryItemsPerPage() {
        Integer count = PropertyUtils.getIntProperty(properties, KEY_HISTORY_ITEMS_PER_PAGE);
        return (count != null && count > 0) ? count : 0;
    }

    public int getHistoryTokenTTL() {
        Integer ttl = PropertyUtils.getIntProperty(properties, KEY_HISTORY_TOKEN_TTL);
        return (ttl != null && ttl > 0) ? ttl : 0;
    }

    public String getStaticDirectoryName() {
        return PropertyUtils.getStringProperty(properties, KEY_STATIC_DIRECTORY_NAME);
    }

    public boolean isDontLogoutOnStartHistory() {
        return getBooleanProperty(properties, KEY_DONT_LOGOUT_ON_START_HISTORY);
    }

    public boolean isCheckAccountOnOldSystem() {
        return getBooleanProperty(properties, KEY_CHECK_ACCOUNT_ON_OLD_SYSTEM);
    }

    public Double getJackpotWinLimit() {
        return getLimitValue(KEY_JACKPOT_WIN_LIMIT);
    }

    public Double getJackpotWinLimitInEUR() {
        return getLimitValue(KEY_JACKPOT_WIN_LIMIT_IN_EUR);
    }

    private Double getLimitValue(String limitKey) {
        String strLimit = getStringProperty(limitKey);
        if (isTrimmedEmpty(strLimit)) {
            return null;
        } else {
            try {
                return Double.valueOf(strLimit);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    public boolean needsJackpot3Feed() {
        return getBooleanProperty(properties, NEEDS_JACKPOT_3_FEED);
    }

    public boolean isNeedHttpsInImageURLInGameList() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_NEED_HTTPS_IN_IMAGE_URL_IN_GAMELIST);
    }

    public boolean isNeedToEmbedKeepAliveScript() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_NEED_TO_EMBED_KEEP_ALIVE_SCRIPT);
    }

    public String getRemoteDocToolURL() {
        return getStringProperty(KEY_DOC_TOOL_REMOTE_URL);
    }

    public boolean isNotIgnoreRoundFinishedParamOnWager() {
        return PropertyUtils.getBooleanProperty(properties, KEY_NOT_IGNORE_ROUND_FINISHED_PARAM_ON_WAGER);
    }

    public boolean isPTPTLogoutByFullWithdrawal() {
        return PropertyUtils.getBooleanProperty(properties, KEY_PTPT_LOGOUT_BY_FULL_WITHDRAWAL);
    }

    public boolean isFRBConfigurationValid() {
        String frbGames = PropertyUtils.getStringProperty(properties, KEY_FRB_GAMES_ENABLE);
        String frbGamesDisable = PropertyUtils.getStringProperty(properties, KEY_FRB_GAMES_DISABLE);
        return isNotBlank(getFRBonusWinManager())
                && isNotBlank(getBonusFRRequestClientClass())
                && (isNotBlank(getFRBonusWinURL()) || (isNotBlank(getPPClass()) && isFRBForCTSupported()))
                && (isTrimmedEmpty(frbGames) || isTrimmedEmpty(frbGamesDisable));
    }

    public boolean isBonusConfigurationValid() {
        return isNotBlank(getBonusReleaseUrl())
                && isNotBlank(getBonusAuthUrl())
                && (isNotBlank(getBonusAccountInfoUrl()) || isNoUseAccountInfoUrlForAuth())
                && isNotBlank(getBonusRequestClientClass());
    }

    public boolean isUsesJava8Proxy() {
        return PropertyUtils.getBooleanProperty(properties, KEY_USES_JAVA8_PROXY);
    }

    public Long getDelayBeforeFinishUnfinishedGames() {
        return getLongProperty(properties, KEY_DELAY_BEFORE_FINISH_UNFINISHED_GAMES);
    }

    public boolean needGenerateIdForMigrationData() {
        return PropertyUtils.getBooleanProperty(properties, KEY_GENERATE_ID_FOR_MIGRATION_DATA);
    }

    public String getMigrationStatus() {
        return getStringProperty(KEY_MIGRATION_STATUS);
    }

    public boolean isDailyBets() {
        return getBooleanProperty(properties, KEY_DAILY_BETS);
    }

    public boolean isNeedCurrencyCheckInAuth() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_NEED_CURRENCY_CHECK_IN_AUTH);
    }

    public boolean isSendDetailsOnFrbInfo() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_DETAILS_ON_FRB_INFO);
    }

    public Long getWagerNewRoundDelay() {
        return PropertyUtils.getLongProperty(properties, KEY_DELAY_ON_WAGER_NEW_ROUND);
    }

    public boolean isAsyncRefreshBalance() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ASYNC_REFRESH_BALANCE);
    }

    public int getAsyncRefreshBalanceInterval() {
        return Optional.ofNullable(PropertyUtils.getIntProperty(properties, KEY_ASYNC_REFRESH_BALANCE_INTERVAL))
                .orElse(DEFAULT_REFRESH_BALANCE_INTERVAL);
    }

    public boolean isRefreshBalanceAutoDenied() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ASYNC_REFRESH_BALANCE_DENY_AUTO_BACKGROUND_REASON);
    }

    public boolean isUpdateBalanceAutoDenied() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ASYNC_UPDATE_BALANCE_DENY_AUTO_BACKGROUND_REASON);
    }

    public boolean isTryResolvePendingOperationsOnAuth() {
        return PropertyUtils.getBooleanProperty(properties, KEY_TRY_RESOLVE_PENDING_OPERATIONS_ON_AUTH);
    }

    public String getLoadJsLibraryUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_LOAD_JS_LIBRARY_URL);
    }

    public String getCronShutdown() {
        return PropertyUtils.getStringProperty(properties, KEY_CRON_SHUTDOWN_EXPRESSION);
    }

    public boolean isSendBonusFlag() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_BONUS_FLAG);
    }

    public boolean isSpinProfilingEnabled() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SPIN_PROFILING);
    }

    public boolean isSendJackpotWonEmailAlert() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_JP_WON_EMAIL_ALERT);
    }

    public String getSendJackpotWonEmailRecipients() {
        return PropertyUtils.getStringProperty(properties, KEY_SEND_JP_WON_EMAIL_RECIPIENTS);
    }

    public String getMpLobbyWsUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_MP_LOBBY_WS_URL);
    }

    public String getNewGamesRouteGameId() {
        return PropertyUtils.getStringProperty(properties, KEY_NEW_GAMES_ROUTE_GAME_ID);
    }

    public String getNewGamesClientUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_NEW_GAMES_CLIENT_URL);
    }

    public String getNewGamesApiUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_NEW_GAMES_API_URL);
    }

    public String getNewGamesGsInternalBaseUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_NEW_GAMES_GS_INTERNAL_BASE_URL);
    }

    public boolean isNotShowUpdateBalanceButtonForMultiplayerGames() {
        return PropertyUtils.getBooleanProperty(properties, KEY_MP_NOT_SHOW_UPDATE_BALANCE_BUTTON);
    }

    public boolean isCurrencyCodeAllowed(String code) {
        if (!isBankCurrenciesStrongCheck()) {
            return true;
        }
        for (Currency currency : getCurrencies()) {
            if (currency.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBankCurrenciesStrongCheck() {
        return PropertyUtils.getBooleanProperty(properties, KEY_BANK_CURRENCIES_STRONG_CHECK);
    }

    public boolean isInGameHistoryEnabled() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ENABLE_IN_GAME_HISTORY);
    }

    public String getGameHistoryUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_GAME_HISTORY_URL);
    }

    public boolean isSupportPromoBalanceTransfer() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SUPPORT_PROMO_BALANCE_TRANSFER);
    }

    public boolean isOpenGameHistoryInSameWindow() {
        return PropertyUtils.getBooleanProperty(properties, KEY_OPEN_GAME_HISTORY_IN_SAME_WINDOW);
    }


    public boolean isSendExtBonusId() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_EXT_BONUS_ID);
    }

    public String getGameManagmentUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_GAME_MANAGMENT_URL);
    }

    public String getGamblingRegulationUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_GAMBLING_REGULATION_URL);
    }

    public String getAgeRestrictionUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_AGE_RESTRICTION_URL);
    }

    public String getStopUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_STOP_URL);
    }

    public boolean isUseRoundWinsFeed() {
        return !isTrimmedEmpty(PropertyUtils.getStringProperty(properties, KEY_ROUND_WINS_FEED_CONFIG));
    }

    public String getRoundWinsFeedConfig() {
        return PropertyUtils.getStringProperty(properties, KEY_ROUND_WINS_FEED_CONFIG);
    }

    public boolean isAlwaysSendClientType() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ALWAYS_SEND_CLIENT_TYPE);
    }

    public LogoutActionType getLogoutAction() {
        return getEnumProperty(KEY_LOGOUT_ACTION, LogoutActionType.DEFAULT);
    }

    public boolean isSendGameIdOnAuth() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_GAMEID_ON_AUTH);
    }

    public void setSendGameIdOnAuth(boolean value) {
        setProperty(KEY_SEND_GAMEID_ON_AUTH, String.valueOf(value));
    }

    public boolean isAddGameIdToHashOnAuth() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ADD_GAMEID_TO_HASH_ON_AUTH);
    }

    public boolean isAddClientTypeToHashOnAuth() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ADD_CLIENTTYPE_TO_HASH_ON_AUTH);
    }

    public boolean isAddClientTypeToHashOnWager() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ADD_CLIENTTYPE_TO_HASH_ON_WAGER);
    }

    public void setAddGameIdToHashOnAuth(boolean value) {
        setProperty(KEY_ADD_GAMEID_TO_HASH_ON_AUTH, String.valueOf(value));
    }

    public boolean shouldNotUseOriginAsCDN() {
        return PropertyUtils.getBooleanProperty(properties, KEY_CDN_DISABLE_ORIGIN);
    }

    public void setIgnoreStubBalanceFromES(boolean value) {
        setProperty(KEY_IGNORE_STUB_BALANCE_FROM_ES, String.valueOf(value));
    }

    public boolean isIgnoreStubBalanceFromES() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IGNORE_STUB_BALANCE_FROM_ES);
    }

    public int getSpinCountBetweenRealGameProposal() {
        Integer spinCount = PropertyUtils.getIntProperty(properties, KEY_SPIN_COUNT_BETWEEN_REAL_GAME_PROPOSAL);
        if (spinCount == null || spinCount <= 0) {
            spinCount = STANDARD_SPIN_COUNT_VALUE;
        }
        return spinCount;
    }

    public String getLeaderboardResultsUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_LEADERBOARD_RESULTS_URL);
    }

    public boolean shouldUseLegacyLeaderboardResultsFormat() {
        return PropertyUtils.getBooleanProperty(properties, KEY_LEADERBOARD_RESULTS_LEGACY_FORMAT);
    }

    public boolean isUseMutualSSLAuth() {
        return PropertyUtils.getBooleanProperty(properties, KEY_USE_MUTUAL_SSL_AUTH);
    }

    public boolean isSkipSSLCheck() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SKIP_SSL_CHECK);
    }

    public Long getFRBBet() {
        return PropertyUtils.getLongProperty(properties, KEY_GL_OFRB_BET);
    }

    public boolean isUseFRBBetForNonGLSlots() {
        return PropertyUtils.getBooleanProperty(properties, KEY_GL_USE_OFRB_BET_FOR_NONGL_SLOTS);
    }

    public boolean isStaticFrbCoinsIgnored() {
        return PropertyUtils.getBooleanProperty(properties, KEY_GL_OFRB_OVERRIDES_PREDEFINED_COINS);
    }

    public Long getOCBMaxBet() {
        return PropertyUtils.getLongProperty(properties, KEY_GL_OCB_MAX_BET);
    }

    public Long getOCBMaxTableLimit() {
        return PropertyUtils.getLongProperty(properties, KEY_GL_OCB_MAX_TABLE_LIMIT);
    }

    public boolean isGLUseDefaultCurrency() {
        return PropertyUtils.getBooleanProperty(properties, KEY_GL_USE_DEFAULT_CURRENCY);
    }

    public Long getMaxBet() {
        return PropertyUtils.getLongProperty(properties, KEY_GL_MAX_BET);
    }

    public Long getMinBet() {
        return PropertyUtils.getLongProperty(properties, KEY_GL_MIN_BET);
    }

    public Long getMaxWin() {
        return PropertyUtils.getLongProperty(properties, KEY_GL_MAX_EXPOSURE);
    }

    public Long getDefaultBet() {
        return PropertyUtils.getLongProperty(properties, KEY_GL_DEFAULT_BET);
    }

    public Integer getCoinsNumber() {
        return PropertyUtils.getIntProperty(properties, KEY_GL_NUMBER_OF_COINS);
    }

    public boolean isGDPROff() {
        return PropertyUtils.getBooleanProperty(properties, GDPR_OFF);
    }

    public boolean isCWSendRealBetWin() {
        return PropertyUtils.getBooleanProperty(properties, KEY_CW_SEND_REAL_BET_WIN);
    }

    public boolean shouldKeepScale() {
        return PropertyUtils.getBooleanProperty(properties, KEY_KEEP_SCALE);
    }

    public boolean isMqStartBonusDisabled() {
        return getBooleanPropertyWithAliases(KEY_MQ_PLAYER_START_BONUS_DISABLED, false,
                KEY_ABS_PLAYER_START_BONUS_DISABLED);
    }

    public boolean isSoundEnabledByDefaultMobile() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SOUND_ENABLED_BY_DEFAULT_MOBILE);
    }

    public boolean isSaveMinMaxWallet() {
        return PropertyUtils.getBooleanProperty(properties, KEY_CW_SAVE_MIN_MAX);
    }

    public boolean isMpUseProvidedNickname() {
        return PropertyUtils.getBooleanProperty(properties, KEY_MP_USE_NICKNAME_IF_PROVIDED);
    }

    public String getMpNicknameAllowedSymbols() {
        return PropertyUtils.getStringProperty(properties, KEY_MP_NICKNAME_ALLOWED_SYMBOLS);
    }

    public boolean isOverrideGameIdIfFoundUnfinished() {
        return PropertyUtils.getBooleanProperty(properties, KEY_OVERRIDE_GAME_ID_IF_FOUND_UNFINISHED_GAME);
    }

    public boolean isSendRoundsForGameSessionEndedAlert() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_ROUNDS_FOR_GAME_SESSION_ENDED_ALERT);
    }

    public MaxQuestWeaponMode getMaxQuestWeaponMode() {
        return getEnumPropertyWithAliases(KEY_MQ_WEAPONS_MODE, MaxQuestWeaponMode.LOOT_BOX,
                KEY_ABS_WEAPONS_MODE);
    }

    public boolean isRoundWinsWithoutBetsAllowed() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ROUND_WINS_WITHOUT_BETS_ALLOWED);
    }

    public boolean isMQBackgroundLoadingDisabled() {
        return getBooleanPropertyWithAliases(KEY_DISABLE_MQ_BACKGROUND_LOADING, false,
                KEY_DISABLE_ABS_BACKGROUND_LOADING);
    }

    public String getMQTournamentRealModeUrl() {
        return getStringPropertyWithAliases(KEY_MQ_TOURNAMENT_REAL_MODE_URL,
                KEY_ABS_TOURNAMENT_REAL_MODE_URL);
    }

    public long getTournamentExcludeTime() {
        long defaultExcludeTime = 24;
        Long excludeTime = getLongProperty(properties, KEY_TOURNAMENT_EXCLUDE_TIME_IN_HOURS);
        return TimeUnit.HOURS.toMillis(excludeTime == null ? defaultExcludeTime : excludeTime);
    }

    public String getTournamentResultApiUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_TOURNAMENT_RESULT_API_URL);
    }

    public long getBetDoesNotQualifyNotificationTimerIntervalInMs() {
        Long interval = getLongProperty(properties, KEY_BET_DOES_NOT_QUALIFY_NOTIFICATION_TIMER_INTERVAL_IN_MS);
        return interval == null ? TimeUnit.MINUTES.toMillis(10) : interval;
    }

    public boolean isBuyFeatureDisabled() {
        return PropertyUtils.getBooleanProperty(properties, KEY_BUY_FEATURE_DISABLED);
    }

    public boolean isBuyFeatureDisabledForCashBonus() {
        return PropertyUtils.getBooleanProperty(properties, KEY_BUY_FEATURE_DISABLED_FOR_CASH_BONUS);
    }

    public boolean isCWSendSpecialWeaponBet() {
        return PropertyUtils.getBooleanProperty(properties, KEY_CW_SEND_SW_BET);
    }

    public boolean isSendMaxWin() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_MAX_WIN_INFO);
    }

    public Integer getWalletOperationTtl() {
        return PropertyUtils.getIntProperty(properties, KEY_WALLET_OPERATION_TTL);
    }

    public Long getFreeBalance() {
        return PropertyUtils.getLongProperty(properties, KEY_FREE_BALANCE);
    }

    public boolean isTrackWinInNewGameSession() {
        return PropertyUtils.getBooleanProperty(properties, KEY_TRACK_WIN_IN_NEW_GAMESESSION);
    }

    public boolean isStressTestBank() {
        return PropertyUtils.getBooleanProperty(properties, KEY_STRESS_TEST_BANK);
    }

    public void setStressTestBank(boolean value) {
        setProperty(KEY_STRESS_TEST_BANK, String.valueOf(value));
    }

    public Integer getFreeBalanceMultiplier() {
        return PropertyUtils.getIntProperty(properties, KEY_FREE_BALANCE_MULTIPLIER);
    }

    public boolean isNotDisplayTime() {
        return PropertyUtils.getBooleanProperty(properties, KEY_NOT_DISPLAY_TIME);
    }

    public String getMQRoomsSortOrder() {
        String order = getStringPropertyWithAliases(KEY_MQ_ROOMS_SORT_ORDER, KEY_ABS_ROOMS_SORT_ORDER);
        return isTrimmedEmpty(order) ? "ASC" : order;
    }

    public Map<String, String> getCurrencyAliases() {
        return CollectionUtils.stringToMap(getStringProperty(KEY_CURRENCY_ALIASES));
    }

    public String getCurrencyFormatString() {//Example  MMC:"'$'{0}", MQC:"{0} QC"
        String currencyFormatString = PropertyUtils.getStringProperty(properties, KEY_CURRENCY_FORMAT_STRING);
        if (StringUtils.isTrimmedEmpty(currencyFormatString)) {
            currencyFormatString = "{0}";
        }
        return currencyFormatString;
    }

    public boolean isChristmasMode() {
        return PropertyUtils.getBooleanProperty(properties, KEY_CHRISTMAS_MODE);
    }

    public boolean isLogoutOnError() {
        return PropertyUtils.getBooleanProperty(properties, KEY_LOGOUT_ON_ERROR);
    }

    //not use directly, use BankInfoCache.getCurrencyRateMultiplier()
    public Map<String, String> getCurrencyRateMultipliers() {
        String multipliers = getStringProperty(KEY_CURRENCY_RATE_MULTIPLIER_MAP);
        if (isTrimmedEmpty(multipliers)) {
            return Collections.emptyMap();
        }
        try {
            return MAP_SPLITTER.split(multipliers);
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid data format in property: {} for bankId: {}", KEY_CURRENCY_RATE_MULTIPLIER_MAP, id, e);
            return Collections.emptyMap();
        }
    }

    public boolean isUseJvmSessionTracking() {
        return PropertyUtils.getBooleanProperty(properties, KEY_JVM_SESSION_TRACKING);
    }

    public boolean isPostSidToParent() {
        return PropertyUtils.getBooleanProperty(properties, KEY_POST_SID_TO_PARENT);
    }

    public boolean isPostMessageToOpener() {
        return PropertyUtils.getBooleanProperty(properties, KEY_POST_MESSAGE_TO_OPENER);
    }

    public String getAllowedOrigin() {
        String origin = PropertyUtils.getStringProperty(properties, KEY_ALLOWED_ORIGIN);
        return isTrimmedEmpty(origin) ? "*" : origin;
    }

    public String getFatalErrorPageUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_FATAL_ERROR_PAGE_URL);
    }

    public List<String> getAllowedDomains() {
        String value = PropertyUtils.getStringProperty(properties, KEY_ALLOWED_DOMAINS);
        if (StringUtils.isTrimmedEmpty(value)) {
            return Collections.emptyList();
        }
        return Splitter.on('|').splitToList(value);
    }

    public boolean isISoftBetIgnoreUsernameOnLaunch() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ISB_IGNORE_USERNAME);
    }

    public boolean isSendBonusSessionStatistics() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SEND_BONUS_SESSION_STATISTICS);
    }

    public String getNotifyDriveWonMailList() {
        return PropertyUtils.getStringProperty(properties, KEY_NOTIFY_DRIVE_WON_MAIL_LIST);
    }

    public String getPendingOperationMailList() {
        return PropertyUtils.getStringProperty(properties, KEY_PENDING_OPERATION_EMAIL_RECIPIENTS);
    }

    public int getWinnersFeedMaskLength() {
        return PropertyUtils.getIntProperty(properties, KEY_WINNERS_FEED_MASK_LENGTH, 0);
    }

    public boolean isIgnoreNegativeBalanceOnAuth() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IGNORE_NEGATIVE_BALANCE_ON_AUTH);
    }
}
