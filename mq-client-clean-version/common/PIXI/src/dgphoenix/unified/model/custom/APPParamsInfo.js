import SimpleInfo from '../base/SimpleInfo';
import { APP } from '../../controller/main/globals';

/**
 * Application window params, and params from window.getParams
 * @class
 */
class APPParamsInfo extends SimpleInfo
{
	static get FUNC_GET_CUSTOMERSPEC_DESCRIPTOR_URL()	{return "getCustomerspecDescriptorStoragePathURL"};
	static get FUNC_GET_LOBBY_PATH()			{return "getLobbyPath"};
	static get FUNC_GET_GAME_PATH()				{return "getGamePath"};
	static get FUNC_GET_PARAMS()				{return "getParams"};
	static get FUNC_OPEN_MQB_LOBBY()			{return "openMQBLobby"};

	static get PARAM_BANK_ID()					{return "bankId"};
	static get PARAM_SERVER_ID()				{return "serverId"};
	static get PARAM_SESSION_ID()				{return "sessionId"};
	static get PARAM_WEB_SOCKET()				{return "websocket"};
	static get PARAM_MODE()						{return "mode"};
	static get PARAM_LANG()						{return "lang"};
	static get PARAM_GAME_ID()					{return "gameId"};
	static get PARAM_BONUS_ID() 				{return "bonusId"}
	static get PARAM_TOURNAMENT_ID()			{return "tournamentId"}

	static get PARAM_BATTLEGROUND_BUY_IN()		{return "battlegroundBuyIn"};
	static get PARAM_NO_FRB()					{return "noFRB"};
	static get PARAM_BUYIN_FUNC_NAME()			{return "JS_BUYIN_FUNC_NAME"};
	static get PARAM_CLOSE_ERROR_FUNC_NAME()	{return "JS_CLOSE_ERROR_FUNC_NAME"};
	static get PARAM_HISTORY_FUNC_NAME()		{return "JS_HISTORY"};
	static get PARAM_HOME_FUNC_NAME()			{return "JS_HOME"};
	static get PARAM_TIMER_OFFSET()				{return "MQ_TIMER_OFFSET"};
	static get PARAM_TIMER_FREQUENCY()			{return "MQ_TIMER_FREQ"};
	static get PARAM_TEST_SYSTEM()				{return "TEST_SYSTEM"};
	static get PARAM_CUSTOMER_ID()				{return "MQ_CUSTOMER_ID"};
	static get PARAM_COMMON_ASSETS_PATH()		{return "commonPathForActionGames"};
	static get PARAM_UPDATE_BALANCE_FREQ()		{return "MQ_UPDATE_BALANCE_FREQ"};
	static get PARAM_WEAPONS_SAVING_ALLOWED() 	{return "MQ_WEAPONS_SAVING_ALLOWED"};
	static get PARAM_QUESTS_SAVING_ENABLED() 	{return "DS_QUESTS_ENABLED"};
	static get PARAM_ERROR_HANDLING_ALLOWED() 	{return "MQ_CLIENT_ERROR_HANDLING"};
	static get PARAM_DISABLE_MQ_AUTOFIRING() 	{return "DISABLE_MQ_AUTOFIRING"};
	static get PARAM_ROOMS_SORT_ORDER() 		{return "ROOMS_SORT_ORDER"};
	static get PARAM_IS_BATTLEGROUND_GAME() 	{return "isBattleGroundGame"};
	static get PARAM_PRIVATE_ROOM_ID() 			{return "privateRoomId"};
	static get PARAM_BATTLEGROUND_CONTINUE_INCOMPLETE_ROUND()			{return "continueIncompleteRound"};
	static get PARAM_PREFERED_ROOM_ID()			{return "prefRoomId"};

	static get PARAM_SHOW_PROMO_DETAILS_FUNC_NAME()		{return "JS_SHOW_PROMO_DETAILS_FUNC_NAME"};
	static get PARAM_ACTIVE_PROMOS()					{return "ACTIVE_PROMOS"};

	static get PARAM_WEAPONS_MODE()						{return "MQ_WEAPONS_MODE"};

	static get PARAM_BACKGROUND_LOADING_DISABLED()		{return "DISABLE_MQ_BACKGROUND_LOADING"};
	static get PARAM_CW_SEND_REAL_BET_WIN_NAME()		{return "CW_SEND_REAL_BET_WIN"};

	static get PARAM_CLIENT_LOG_LEVEL()			{return "CLIENT_LOG_LEVEL";}
	static get PARAM_LOBBY_WEB_SOCKET()			{return "LOBBY_WEB_SOCKET";}

	static get PARAM_MQ_RESTRICT_COIN_FRACTION_LENGTH()	{return "MQ_RESTRICT_COIN_FRACTION_LENGTH";}

	static get DEBUG_PARAM_TRACK_STUBS()		{return "trackstubs"};
	static get DEBUG_PARAM_FPS_RAM()			{return "fps"};

	/** Path to customer descriptor folder, provided through window.getCustomerspecDescriptorStoragePathURL */
	get customerspecDescriptorUrl()
	{
		return this._fCustomerspecDescriptorUrl_str || null;
	}

	set customerspecDescriptorUrl(aValue_str)
	{
		this._fCustomerspecDescriptorUrl_str = aValue_str;
	}

	set lobbyPath(aValue_str)
	{
		this._fLobbyPath_str = aValue_str;
	}

	/** Path to lobby folder, provided through window.getLobbyPath */
	get lobbyPath()
	{
		return this._fLobbyPath_str;
	}

	set gamePath(aValue_str)
	{
		this._fGamePath_str = aValue_str;
	}

	/** Path to game folder, provided through window.getGamePath */
	get gamePath()
	{
		return this._fGamePath_str;
	}

	set params(aValue_obj)
	{
		this._fParams_obj = aValue_obj || {};
	}

	/** Returns parameter value by parameter name. */
	getParamValue(aParamName_str)
	{
		return this._fParams_obj[aParamName_str] || undefined;
	}

	/** Rooms order sorting type. */
	get roomsOrder()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_ROOMS_SORT_ORDER];
	}

	set privateRoomId(aId_int)
	{
		this._fParams_obj[APPParamsInfo.PARAM_PRIVATE_ROOM_ID] = aId_int;
	}

	/** CAF room id. */
	get privateRoomId()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_PRIVATE_ROOM_ID] || undefined;
	}

	/** @deprecated */
	get privateRoom()  /*TODO [os]: remove this getter when all caf games will exclude using this parametr*/
	{
		return this.privateRoomId ? true : undefined;
	}

	get restrictCoinFractionLength()
	{
		if (this._fParams_obj[APPParamsInfo.PARAM_MQ_RESTRICT_COIN_FRACTION_LENGTH] !== undefined)
		{
			return Number(this._fParams_obj[APPParamsInfo.PARAM_MQ_RESTRICT_COIN_FRACTION_LENGTH]);
		}

		return undefined;
	}

	set restrictCoinFractionLength(aValue_int)
	{
		return this._fParams_obj[APPParamsInfo.PARAM_MQ_RESTRICT_COIN_FRACTION_LENGTH] = aValue_int;
	}

	set isBattlegroundGame(aIsBattlegroundGame_bl)
	{
		this._fParams_obj[APPParamsInfo.PARAM_IS_BATTLEGROUND_GAME] = aIsBattlegroundGame_bl;
	}

	/** Is game launched in Battlegrounds mode or not. */
	get isBattlegroundGame()
	{
		let lisBattlegroundGame_str = this._fParams_obj[APPParamsInfo.PARAM_IS_BATTLEGROUND_GAME];
		
		switch (lisBattlegroundGame_str)
		{
			case true:
			case "true":
				return true;
			default:
				return false;
		}
	}

	get noFRB()
	{
		let lNoFRB_str = this._fParams_obj[APPParamsInfo.PARAM_NO_FRB];
		if (lNoFRB_str && lNoFRB_str == "true")
		{
			return true;
		}

		return false;
	}

	/** Battleground BuyIn cost in cents. */
	get battlegroundBuyIn()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_BATTLEGROUND_BUY_IN];
	}

	get prefRoomId()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_PREFERED_ROOM_ID];
	}

	set isStubsTrackingRequired(aIsRequired_bl)
	{
		this._fIsStubsTrackingRequired_bl = aIsRequired_bl;
	}

	get isStubsTrackingRequired()
	{
		return this._fIsStubsTrackingRequired_bl;
	}

	set isFPSRAMDisplayRequired(aIsRequired_bl)
	{
		this._fIsFPSRAMDisplayRequired_bl = aIsRequired_bl;
	}

	get isFPSRAMDisplayRequired()
	{
		return this._fIsFPSRAMDisplayRequired_bl && this.testSystem;
	}
	
	set bankId(aValue_str)
	{
		this._fParams_obj[APPParamsInfo.PARAM_BANK_ID] = aValue_str;
	}

	/** Bank id. */
	get bankId()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_BANK_ID];
	}

	set gameId(aId_int)
	{
		return this._fParams_obj[APPParamsInfo.PARAM_GAME_ID] = aId_int;
	}

	/** Launched game id. */
	get gameId()
	{
		return +this._fParams_obj[APPParamsInfo.PARAM_GAME_ID] || undefined;
	}

	/** Cash bonus id. */
	get bonusId()
	{
		const lBonusId_num = +this._fParams_obj[APPParamsInfo.PARAM_BONUS_ID];
		if (isNaN(lBonusId_num))
			return undefined;
		return  lBonusId_num;
	}

	/** Tournament id. */
	get tournamentId()
	{
		const lTournamentId_num = +this._fParams_obj[APPParamsInfo.PARAM_TOURNAMENT_ID];
		if (isNaN(lTournamentId_num))
			return undefined;
		return  lTournamentId_num;
	}

	set serverId(aValue_str)
	{
		this._fParams_obj[APPParamsInfo.PARAM_SERVER_ID] = aValue_str;
	}

	/** Server id. */
	get serverId()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_SERVER_ID];
	}

	/** Session id. */
	get sessionId()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_SESSION_ID];
	}

	/** WebSocket url. */
	get webSocket()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_WEB_SOCKET];
	}

	set lobbyWebSocket(aValue_str)
	{
		this._fParams_obj[APPParamsInfo.PARAM_LOBBY_WEB_SOCKET] = aValue_str;
	}

	/** Lobby WebSocket url. */
	get lobbyWebSocket()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_LOBBY_WEB_SOCKET];
	}

	set mode(aValue_str)
	{
		this._fParams_obj[APPParamsInfo.PARAM_MODE] = aValue_str;
	}

	/** Game mode (free/real). */
	get mode()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_MODE];
	}

	/** Localization id. */
	get lang()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_LANG];
	}

	/** Name of window function that opens cashier. */
	get buyInFuncName()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_BUYIN_FUNC_NAME];
	}

	/** Indicates whether buyInFuncName is already defined or not. */
	get buyInFuncDefined()
	{
		return this.buyInFuncName !== undefined;
	}

	/** Indicates whether closeErrorFuncName is already defined or not. */
	get closeErrorFuncNameDefined()
	{
		return !!this._fParams_obj[APPParamsInfo.PARAM_CLOSE_ERROR_FUNC_NAME];
	}

	/** Name of window function that should be called when fatal error dialog is closed. */
	get closeErrorFuncName()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_CLOSE_ERROR_FUNC_NAME];
	}

	/** Name of window function that should be called when history button (on bottom panel) is clicked. */
	get historyFunName()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_HISTORY_FUNC_NAME];
	}

	/** Name of window function that should be called when home button (on bottom panel) is clicked. */
	get homeFuncName()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_HOME_FUNC_NAME];
	}

	/** Indicates whether homeFuncName is already defined or not. */
	get homeFuncNameDefined()
	{
		return this.homeFuncName !== undefined;
	}
	
	/** Timezone offset. */
	get timerOffset()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_TIMER_OFFSET];
	}

	/** Frequency (in milliseconds) to sync time with server. */
	get timerFrequency()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_TIMER_FREQUENCY];
	}

	set testSystem(aIsTestSystem_bl)
	{
		this._fParams_obj[APPParamsInfo.PARAM_TEST_SYSTEM] = aIsTestSystem_bl;
	}

	get testSystem()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_TEST_SYSTEM];
	}

	/**
	 * @deprecated
	 */
	get customerId()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_CUSTOMER_ID];
	}

	get continueIncompleteRound()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_BATTLEGROUND_CONTINUE_INCOMPLETE_ROUND];
	}

	setCommonAssetsVersion(aVersion_str)
	{
		/*TODO [os]: to be removed when loading of version.json will be removed from LobbyAPP (is used for btg Rules)*/
		this._fCommonAssetsVersion_str = aVersion_str;
	}

	/** Version of assets from common folder. */
	getCommonAssetsVersion()
	{
		if(!this._fCommonAssetsVersion_str)
		{
			return Date.now() + "" + Math.random();
		}

		return this._fCommonAssetsVersion_str;
	}

	/** Path to common folder, provided through window.getParams */
	get commonPathForActionGames()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_COMMON_ASSETS_PATH] || APP.urlBasedParams[APPParamsInfo.PARAM_COMMON_ASSETS_PATH];
	}

	/** Frequency (in milliseconds) to sync balance with server. */
	get updateBalanceFrequency()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_UPDATE_BALANCE_FREQ];
	}

	/** Is weapons saving (between rounds) mode on. Not supported now. */
	get weaponsSavingAllowed()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_WEAPONS_SAVING_ALLOWED] == 'true';
	}

	/** @deprecated */
	get questsSavingEnabled()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_QUESTS_SAVING_ENABLED] == 'true';
	}

	/** Autofire enable/disable mode */
	get disableAutofireng()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_DISABLE_MQ_AUTOFIRING] == 'true';
	}

	/** Is loading assets in background allowed or not. */
	get backgroundLoadingAllowed()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_BACKGROUND_LOADING_DISABLED] != 'true';
	}

	/** Is runtime errors handling allowed or not. */
	get errorHandlingAllowed()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_ERROR_HANDLING_ALLOWED] == 'true';
	}

	/** @deprecated */
	get showPromoDetailsFuncName()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_SHOW_PROMO_DETAILS_FUNC_NAME];
	}

	/** @deprecated */
	get activePromos()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_ACTIVE_PROMOS];
	}

	/** @deprecated */
	get isActivePromosDefined()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_ACTIVE_PROMOS] !== undefined;
	}

	/** @deprecated */
	get weaponsMode()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_WEAPONS_MODE];
	}

	/** Is Common wallet API version supported or not. */
	get isCWSendRealBetWinMode()
	{
		return this._fParams_obj[APPParamsInfo.PARAM_CW_SEND_REAL_BET_WIN_NAME] == 'true';
	}

	/** Level of client logs that can be sent to server. */
	get clientLogLevel()
	{
		if (this._fParams_obj[APPParamsInfo.PARAM_CLIENT_LOG_LEVEL] === undefined)
		{
			return "ERROR"; //ERRORs only if the param is not defined
		}

		return  this._fParams_obj[APPParamsInfo.PARAM_CLIENT_LOG_LEVEL];
	}

	set clientLogLevel(aValue_num)
	{
		this._fParams_obj[APPParamsInfo.PARAM_CLIENT_LOG_LEVEL] = aValue_num;
	}

	//INIT...
	constructor(aOptId_obj, aOptParentInfo_usi)
	{
		super(aOptId_obj, aOptParentInfo_usi);

		this._fParams_obj = {};
		this._fCustomerspecDescriptorUrl_str = undefined;
		this._fLobbyPath_str = undefined;
		this._fGamePath_str = undefined;
	}
	//...INIT

	destroy()
	{
		this._fParams_obj = null;
		this._fCustomerspecDescriptorUrl_str = undefined;
		this._fLobbyPath_str = undefined;
		this._fGamePath_str = undefined;

		super.destroy();
	}
}

export default APPParamsInfo;