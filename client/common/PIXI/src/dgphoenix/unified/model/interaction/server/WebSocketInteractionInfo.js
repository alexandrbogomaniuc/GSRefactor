import SimpleInfo from '../../base/SimpleInfo';

/** Classes of server messages supported by all clients. */
const SERVER_MESSAGES = 
{
	ERROR: 			'Error',
	OK: 			'Ok',
	PENDING_OPERATION_STATUS: 	"PendingOperationStatus",
	FINISH_GAME_SESSION_RESPONSE: "FinishGameSessionResponse",
	ENTER_LOBBY_RESPONSE:"EnterLobbyResponse"
}

/** Supported types of error codes. */
const ERROR_CODE_TYPES = 
{
	FATAL_ERROR: 	"FATAL_ERROR",
	ERROR: 			"ERROR",
	WARNING: 		"WARNING"
}

/** Ranges of error codes. */
const ERROR_CODE_RANGES = 
{
	FATAL_ERROR: 	{from: 1, 		to: 999},
	ERROR: 			{from: 1000, 	to: 4999},
	WARNING: 		{from: 5000,	to: 9999},
	WALLET_ERROR: 	{from: 100000,	to: 100999}
}

/** Error codes from server that client handles.  */
const SUPPORTED_ERROR_CODES = 
{
	INTERNAL_ERROR: 1,
	SERVER_SHUTDOWN: 2,
	INVALID_SESSION: 3,
	DEPRECATED_REQUEST: 4,
	FOUND_PENDING_OPERATION: 5,
	CONFLICTING_LOBBY_SESSION: 8,
	ROOM_WAS_DEACTIVATED: 10,
	ILLEGAL_NICKNAME: 1000,
	BAD_REQUEST: 1001,
	NOT_LOGGED_IN: 1002,
	ROOM_NOT_FOUND: 1003,
	TOO_MANY_OBSERVERS: 1004,
	ROOM_MOVED: 1005,
	TOO_MANY_PLAYER: 1006,
	NOT_SEATER: 1007,
	NOT_ENOUGH_MONEY: 1008,
	BAD_STAKE: 1009,
	BAD_BUYIN: 1010,
	// CHANGE_STAKE_NOT_ALLOWED: 1011,	//DEPRECATED, not in use more
	ROOM_NOT_OPEN: 1012,
	ROUND_NOT_STARTED: 1013,
	NEED_SITOUT: 1014,
	WRONG_WEAPON: 1015,
	NICKNAME_NOT_AVAILABLE: 1016,
	AVATAR_PART_NOT_AVAILABLE: 1017,
	NOT_ENOUGH_BULLETS: 1018,
	REQUEST_FREQ_LIMIT_EXCEEDED: 1019,
	QUEST_COLLECT_ERROR: 1020,
	BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE: 1021,
	QUEST_ALREADY_COLLECTED: 1022,
	WRONG_COORDINATES: 1023, // when trying to place the mine on the map
	WRONG_FREE_SHOTS: 1025, // when trying to send free shots to server, which user don't have
	NOT_FATAL_BAD_BUYIN: 1026, // when trying to buy ammo/lootboxes - not critical error, so retry possible
	CHANGE_BET_NOT_ALLOWED: 1027,
	NOT_ALLOWED_CHANGE_NICKNAME: 1028,
	NOT_ALLOWED_PLACE_BULLET: 1029,
	NOT_ALLOWED_SITIN: 1030,
	BET_NOT_FOUND : 1031,
	BAD_MULTIPLIER : 1032,
	CANCEL_BET_NOT_ALLOWED : 1033,
	TEMPORARY_PENDING_OPERATION : 1034,
	NOT_ALLOWED_START_ROUND : 1035,
	NOT_CONFIRM_BUYIN : 1036,
	ROUND_ALREADY_FINISHED : 1037,
	SERVER_REBOOT: 1039,
	NOT_ALLOWED_KICK: 1040,
	OBSERVER_DOESNT_EXIST: 1041,
	SIT_OUT_NOT_ALLOWED: 1042,
	BUYIN_NOT_ALLOWED_ALREADY: 1043,
	PREV_OPERATION_IS_NOT_COMPLETE: 100004,
	OPERATION_FAILED: 100301,
	INSUFFICIENT_FUNDS: 100300,
	UNKNOWN_TRANSACTION_ID: 100302,
	EXPIRED_WEBSITE_SESSION: 100310,
	SW_PURCHASE_LIMIT_EXCEEDED: 100311
}

/** Classes of messages that game-client (room) can send to server. */
const GAME_CLIENT_MESSAGES = 
{
	OPEN_ROOM: 						"OpenRoom",
	CLOSE_ROOM: 					"CloseRoom",
	GET_FULL_GAME_INFO: 			"GetFullGameInfo",
	SIT_IN: 						"SitIn",
	SIT_OUT: 						"SitOut",
	SHOT: 							"Shot",
	CHANGE_STAKE: 					"ChangeStake",
	BUY_IN: 						"BuyIn",
	PURCHASE_WEAPON_LOOT_BOX: 		"PurchaseWeaponLootBox",
	REFRESH_BALANCE: 				"RefreshBalance",
	SWITCH_WEAPON: 					"SwitchWeapon",
	CLOSE_ROUND_RESULTS: 			"CloseRoundResults",
	MINE_COORDINATES:				"MineCoordinates",
	ADD_FREE_SHOTS_TO_QUEUE:		"AddFreeShotsToQueue",
	RE_BUY: 						"ReBuy",
	BET_LEVEL:						"BetLevel",
	BULLET:							"Bullet",
	BULLET_CLEAR:					"BulletClear",
	CONFIRM_BATTLEGROUND_BUY_IN: 	"ConfirmBattlegroundBuyIn",
	WEAPON_PAID_MULTIPLIER_UPDATE_REQUIRED: 	"UpdateWeaponPaidMultiplier",
	BATTLEGROUND_START_PRIVATE_ROOM: 			"StartBattlegroundPrivateRoom",
	BATTLEGROUND_KICK: 							"Kick",
	BATTLEGROUND_REINVITE:						"CancelKick",	
	BATTLEGROUND_INVITE:						"PrivateRoomInvite",				
	CHECK_PENDING_OPERATION_STATUS: 			"CheckPendingOperationStatus",
	SEND_LATENCY:								"Latency"
}

/** Default minimal interval (in milliseconds) that should be kept between sending requests of the same class. */
const COMMON_REQUEST_TIME_LIMIT = 500;

/**
 * WebSocket interaction info.
 * @class
 */
class WebSocketInteractionInfo extends SimpleInfo
{
	constructor()
	{
		super();

		this._socketUrl = undefined;
		this._sessionId = undefined;
		this._serverMessagesHandlingAllowed = true;
		this._lastServerMessageTime = undefined;
		this._lastServerMessageApplyTime = undefined;
		this._fConnectionOpenClientTimeStamp_int = undefined;
		this._fDelayedFatalErrors_int_arr = [];
	}

	set socketUrl (value)
	{
		this._socketUrl = value;
	}

	/** WebSocket url.*/
	get socketUrl ()
	{
		return this._socketUrl;
	}

	set sessionId (value)
	{
		this._sessionId = value;
	}

	/**
	 * Session id.
	 * @type {string}
	 */
	get sessionId ()
	{
		return this._sessionId;
	}

	set serverMessagesHandlingAllowed (value)
	{
		this._serverMessagesHandlingAllowed = value;
	}

	/** 
	 * If false - received server message won't be processed.
	 * @type {boolean}
	 */
	get serverMessagesHandlingAllowed ()
	{
		return this._serverMessagesHandlingAllowed;
	}

	/**
	 * Specifies minimal interval (in milliseconds) that should be kept between sending requests of the same class.
	 * @param {string} aRequest_str - Request class.
	 * @returns {number}
	 */
	getRequestTimeLimit(aRequest_str)
	{
		return COMMON_REQUEST_TIME_LIMIT;
	}

	/**
	 * Specifies minimal interval (in milliseconds) that should be kept between requests with different class.
	 * @param {Object} aRequestData_obj 
	 * @returns {{requestClasses : string[], interval: number}}
	 */
	getRequestDifferentTypesTimeLimit(aRequestData_obj)
	{
		let lIntervals_arr = this.__differentRequestTypesAvailableTimeIntervals;

		if (!lIntervals_arr || !lIntervals_arr.length)
		{
			return undefined;
		}

		for (let i=0; i<lIntervals_arr.length; i++)
		{
			let lCurDescr_obj = lIntervals_arr[i];
			let lSourceRequestIndex_int = -1;

			let lCurDescrRequests_obj_arr = lCurDescr_obj.requests;
			if (!!lCurDescrRequests_obj_arr && lCurDescrRequests_obj_arr.length > 1)
			{
				for (let i=0; i<lCurDescrRequests_obj_arr.length; i++)
				{
					let lCurDescrRequest_obj = lCurDescrRequests_obj_arr[i];
					if (lCurDescrRequest_obj.class == aRequestData_obj.class)
					{
						if (!!lCurDescrRequest_obj.param)
						{
							for (let lParamName_str in lCurDescrRequest_obj.param)
							{
								if (lCurDescrRequest_obj.param[lParamName_str] === aRequestData_obj[lParamName_str])
								{
									lSourceRequestIndex_int = i;
									break;
								}
							}
						}
						break;
					}
				}
			}

			if (lSourceRequestIndex_int >= 0)
			{
				let lCompareRequestClasses_str_arr = [];
				for (let i=0; i<lCurDescrRequests_obj_arr.length; i++)
				{
					if (i === lSourceRequestIndex_int)
					{
						continue;
					}

					lCompareRequestClasses_str_arr.push(lCurDescrRequests_obj_arr[i].class);
				}

				return {
							requestClasses: lCompareRequestClasses_str_arr,
							interval: lCurDescr_obj.interval
						}
			}
		}

		return undefined;
	}

	/**
	 * Array of objects with this structure (source request can have additional params): 
	 * { requests: [{class: <REQUEST_class_1>, param: <SPEC_PARAMS_VALUES_IF_REQUIRED>}, {class: <REQUEST_class_2>}], interval: <TIME_INTERVAL_IN_MS> }
	 * @protected
	 */
	get __differentRequestTypesAvailableTimeIntervals()
	{
		return null;
	}

	set lastServerMessageTime(value)
	{
		if (!(+value > 0))
		{
			return;
		}

		this._lastServerMessageTime = +value;
		this._lastServerMessageApplyTime = Date.now();
	}

	/**
	 * The last timestamp got from server.
	 * @type {number}
	 */
	get lastServerMessageTime()
	{
		return this._lastServerMessageTime;
	}

	/**
	 * Is the last timestamp server defined or not.
	 * @type {boolean}
	 */
	get isLastServerMessageTimeDefined()
	{
		return this._lastServerMessageTime !== undefined;
	}

	/** Timestamp (client's date) when the last server date was applied on client side. */
	get lastServerMessageApplyTime()
	{
		return this._lastServerMessageApplyTime;
	}

	/** Client side timestamp when websocket connection was opened. */
	get connectionOpenClientTimeStamp()
	{
		return this._fConnectionOpenClientTimeStamp_int;
	}

	set connectionOpenClientTimeStamp(value)
	{
		this._fConnectionOpenClientTimeStamp_int = value;
	}

	/**
	 * Indicates whether timestamp when websocket connection was opened is defined or not.
	 */
	get isConnectionOpenClientTimeStampDefined()
	{
		return this._fConnectionOpenClientTimeStamp_int !== undefined;
	}

	/**
	 * Add fatal error code to 'delayed' list (for example, for cases when fatal error message should be displayed ).
	 * @param {number} aErrorCode_int 
	 */
	addDelayedFatalError(aErrorCode_int)
	{
		this._fDelayedFatalErrors_int_arr = this._fDelayedFatalErrors_int_arr || [];

		if (this._fDelayedFatalErrors_int_arr.indexOf(aErrorCode_int) < 0)
		{
			this._fDelayedFatalErrors_int_arr.push(aErrorCode_int);
		}
	}

	/**
	 * Indicates is any critical error delayed or not.
	 * @type {boolean}
	 */
	get isAnyCriticalErrorDelayed()
	{
		return this._fDelayedFatalErrors_int_arr && !!this._fDelayedFatalErrors_int_arr.length;
	}

	/**
	 * Checks if critical error code is delayed or not.
	 * @param {number} aErrorCode_int
	 * @return {boolean}
	 */
	isCriticalErrorDelayed(aErrorCode_int)
	{
		return this._fDelayedFatalErrors_int_arr && !!this._fDelayedFatalErrors_int_arr.length && this._fDelayedFatalErrors_int_arr.indexOf(aErrorCode_int) >= 0;
	}
}

export {WebSocketInteractionInfo, SERVER_MESSAGES, ERROR_CODE_TYPES, ERROR_CODE_RANGES, SUPPORTED_ERROR_CODES, GAME_CLIENT_MESSAGES}