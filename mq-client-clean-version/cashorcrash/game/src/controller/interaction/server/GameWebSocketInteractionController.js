import WebSocketInteractionController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/server/WebSocketInteractionController';
import { GameWebSocketInteractionInfo, SERVER_MESSAGES, CLIENT_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import { DEBUG_WEB_SOCKET_URL } from '../../../config/Constants';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameApp from '../../../CrashAPP';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import BalanceController from '../../main/BalanceController';
import RoundController from '../../gameplay/RoundController';
import { ROUND_STATES } from '../../../model/gameplay/RoundInfo';
import GamePlayersController from '../../gameplay/players/GamePlayersController';
import BetsController from '../../gameplay/bets/BetsController';
import { ERROR_CODE_TYPES } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/interaction/server/WebSocketInteractionInfo';
import RoomController from '../../gameplay/RoomController';
import PendingOperationController from '../../gameplay/PendingOperationController';
import GameplayController from '../../gameplay/GameplayController';
import BattlegroundCafRoomManagerDialogController from '../../uis/custom/battleground/caf/BattlegroundCafRoomManagerDialogController';

class GameWebSocketInteractionController extends WebSocketInteractionController {
	static get EVENT_ON_SERVER_MESSAGE() { return WebSocketInteractionController.EVENT_ON_SERVER_MESSAGE };
	static get EVENT_ON_SERVER_CONNECTION_CLOSED() { return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED };
	static get EVENT_ON_SERVER_CONNECTION_OPENED() { return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED };
	static get EVENT_ON_SERVER_ERROR_MESSAGE() { return WebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE };
	static get EVENT_ON_SERVER_OK_MESSAGE() { return WebSocketInteractionController.EVENT_ON_SERVER_OK_MESSAGE };

	static get EVENT_ON_SERVER_ENTER_GAME_MESSAGE() { return "EVENT_ON_SERVER_ENTER_GAME_MESSAGE" };
	static get EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE() { return "EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE" };
	static get EVENT_ON_SERVER_GAME_TIME_UPDATED_MESSAGE() { return "EVENT_ON_SERVER_GAME_TIME_UPDATED_MESSAGE" };
	static get EVENT_ON_SERVER_STATS_MESSAGE() { return "EVENT_ON_SERVER_STATS_MESSAGE" };
	static get EVENT_ON_SERVER_GAME_ROOM_INFO_RESPONSE() { return "EVENT_ON_SERVER_GAME_ROOM_INFO_RESPONSE" };
	static get EVENT_ON_SERVER_CRASH_GAME_INFO_MESSAGE() { return "EVENT_ON_SERVER_CRASH_GAME_INFO_MESSAGE" };
	static get EVENT_ON_SERVER_CRASH_STATE_INFO_MESSAGE() { return "EVENT_ON_SERVER_CRASH_STATE_INFO_MESSAGE" };
	static get EVENT_ON_SERVER_SIT_IN_RESPONSE_MESSAGE() { return "EVENT_ON_SERVER_SIT_IN_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_SIT_OUT_RESPONSE_MESSAGE() { return "EVENT_ON_SERVER_SIT_OUT_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_GAME_STATE_CHANGED_MESSAGE() { return "EVENT_ON_SERVER_GAME_STATE_CHANGED_MESSAGE" };
	static get EVENT_ON_SERVER_CRASH_BET_RESPONSE_MESSAGE() { return "EVENT_ON_SERVER_CRASH_BET_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_CRASH_CANCEL_BET_RESPONSE_MESSAGE() { return "EVENT_ON_SERVER_CRASH_CANCEL_BET_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_ROUND_RESULT_MESSAGE() { return "EVENT_ON_SERVER_ROUND_RESULT_MESSAGE" };
	static get EVENT_ON_CRASH_CANCEL_AUTOEJECT_RESPONSE_MESSAGE() { return "EVENT_ON_CRASH_CANCEL_AUTOEJECT_RESPONSE_MESSAGE" };
	static get EVENT_ON_CRASH_CHANGE_AUTOEJECT_RESPONSE_MESSAGE() { return "EVENT_ON_CRASH_CHANGE_AUTOEJECT_RESPONSE_MESSAGE" };
	static get EVENT_ON_CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE_MESSAGE() { return "EVENT_ON_CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE_MESSAGE" };
	static get EVENT_ON_CRASH_ALL_COPLAYERS_BETS_REJECTED_RESPONSE_MESSAGE() { return "EVENT_ON_CRASH_ALL_COPLAYERS_BETS_REJECTED_RESPONSE_MESSAGE" };
	static get EVENT_ON_CRASH_ALL_MASTER_BETS_CONFIRMED_RESPONSE_MESSAGE() { return "EVENT_ON_CRASH_ALL_MASTER_BETS_CONFIRMED_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND() { return "EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND" };
	static get EVENT_ON_SERVER_RECONNECTION_START() { return "EVENT_ON_SERVER_RECONNECTION_START" };
	static get EVENT_ON_SERVER_LATENCY_REQUEST() { return "EVENT_ON_SERVER_LATENCY_REQUEST" };
	static get EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE() { return "EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE" };

	hasDelayedRequests(aRequestClass_str, aOptRequestParams_obj = undefined) {
		return super.hasDelayedRequests(aRequestClass_str, aOptRequestParams_obj);
	}

	hasUnRespondedRequest(aRequestClass_str, aOptRequestParams_obj = undefined) {
		return super.hasUnRespondedRequest(aRequestClass_str, aOptRequestParams_obj);
	}

	constructor() {
		super(new GameWebSocketInteractionInfo());
	}

	get _debugWebSocketUrl() {
		return DEBUG_WEB_SOCKET_URL;
	}

	get _webSocketUrl() {
		let lSocketUrl_str = APP.urlBasedParams.WEB_SOCKET_URL;
		return lSocketUrl_str.replace("mplobby", "mpunified");
	}


	forcePlayState(roundId_val) {
		console.warn("Fatal NET lag game - forcing play state");
		this._processServerMessage({ class: SERVER_MESSAGES.GAME_STATE_CHANGED, date: Date.now(), rid: -1, roundId: roundId_val, roundStartTime: Date.now() - 10, state: "PLAY", ttnx: -1 })
	}


	__initControlLevel() {
		super.__initControlLevel();

		APP.on(GameApp.EVENT_ON_GAME_ASSETS_LOADING_ERROR, this._onGameAssetsLoadingError, this);
		APP.on(GameApp.EVENT_ON_APPLICATION_FATAL_ERROR, this._onApplicationFatalErrorOccured, this);

		APP.on(GameApp.EVENT_ON_OFFLINE, this._onOffline, this);
		APP.on(GameApp.EVENT_ON_ONLINE_RESTORED, this._onOnlineRestored, this);

		APP.gameController.balanceController.on(BalanceController.EVENT_SERVER_BALANCE_REFRESH_REQUIRED, this._onServerBalanceRefreshRequired, this);

		let lGamaplayController_gpc = this._fGameplayController_gpc = APP.gameController.gameplayController;

		let lRoomController_rc = this._fRoomController_rc = lGamaplayController_gpc.roomController;
		lRoomController_rc.on(RoomController.EVENT_ON_ROOM_STATE_CHANGED, this._onRoomStateChanged, this);

		this._fRoundController_rc = lGamaplayController_gpc.roundController;
		this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);

		this._fGamePlayersController_gpsc = lGamaplayController_gpc.gamePlayersController;
		this._fGamePlayersController_gpsc.on(GamePlayersController.EVENT_ON_ROOM_JOIN_INITIATED, this._onRoomJoinInitiated, this);
		this._fGamePlayersController_gpsc.on(GamePlayersController.EVENT_ON_ROOM_LEAVE_INITIATED, this._onRoomLeaveInitiated, this);
		this._fGamePlayersController_gpsc.on(GamePlayersController.EVENT_ON_MASTER_PLAYER_OUT, this._onMasterPlayerOut, this);

		this._fBetsController_bsc = this._fGamePlayersController_gpsc.betsController;
		this._fBetsController_bsc.on(BetsController.EVENT_ON_BETS_ACCEPTED, this._onNewBetsAccepted, this);
		this._fBetsController_bsc.on(BetsController.EVENT_ON_BET_CANCEL_INITIATED, this._onBetCancelInitiated, this);
		this._fBetsController_bsc.on(BetsController.EVENT_ON_CANCEL_ALL_BETS_INITIATED, this._onBetCancelAllBetsInitiated, this);
		this._fBetsController_bsc.on(BetsController.EVENT_ON_CANCEL_AUTOEJECT_INITIATED, this._onCancelAutoEjectInitiated, this);
		this._fBetsController_bsc.on(BetsController.EVENT_ON_EDIT_AUTO_EJECT_INITIATED, this._onEditAutoEjectInitiated, this);
		lRoomController_rc.on(RoomController.EVENT_ON_PRIVATE_BATTLEGROUND_START_GAME_URL_REQUEST, this._onPrivateBattlegroundStartGameURLRequested, this);

		let lPendingOperationController_poc = APP.gameController.gameplayController.pendingOperationController;
		lPendingOperationController_poc.on(PendingOperationController.EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED, this._onRefreshPendingOperationStatusRequired, this);
		APP.gameController.gameplayController.on(GameplayController.LATENCY_REQUEST, this._sendLatencyRequest, this);
		if (APP.isBattlegroundGame && APP.isCAFMode) {
			let battlegroundCafRoomManagerDialogController = APP.dialogsController._cafRoomManagerController;
			battlegroundCafRoomManagerDialogController.on(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_MANAGER_START_ROUND_CLICKED, this.startCafRound, this)
			battlegroundCafRoomManagerDialogController.on(BattlegroundCafRoomManagerDialogController.EVENT_ON_INVITE_FRIEND_CLICKED, this._onBattlegroundPlayerInviteTriggered, this);
			battlegroundCafRoomManagerDialogController.on(BattlegroundCafRoomManagerDialogController.BATTLEGROUND_CAF_PLAYER_KICK_TRIGGERED, this._onBattlegroundPlayerKickTriggered, this);
			battlegroundCafRoomManagerDialogController.on(BattlegroundCafRoomManagerDialogController.BATTLEGROUND_CAF_PLAYER_CANCEL_KICK_TRIGGERED, this._onBattlegroundPlayerCancelKickTriggered, this);
		}


		// DEBUG...
		// window.addEventListener(
		//    "keydown", this.keyDownHandler.bind(this), false
		//  );
		// ...DEBUG
	}

	_onBattlegroundPlayerCancelKickTriggered(event) {
		this._sendRequest(CLIENT_MESSAGES.BATTLEGROUND_REINVITE, { nickname: event.nickname });
	}

	startCafRound(event) {
		this._sendRequest(CLIENT_MESSAGES.BATTLEGROUND_START_PRIVATE_ROOM, {});
	}

	_onPrivateBattlegroundStartGameURLRequested(even) {
		this._sendRequest(CLIENT_MESSAGES.GET_PRIVATE_BATTLEGROUND_START_URL, { privateRoomId: APP.appParamsInfo.privateRoomId });
	}


	_sendLatencyRequest(event) {
		const requestBody = {
			serverTs: event.serverTs,
			serverAckTs: event.serverAckTs,
			clientTs: event.clientTs,
			clientAckTs: event.clientAckTs,
			step: event.step
		}
		this._sendRequest(CLIENT_MESSAGES.SEND_LATENCY, requestBody);
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 32)
	// 	{
	// 		this.info.serverMessagesHandlingAllowed = false;
	// 	}
	// }
	//...DEBUG

	_onOffline() {
		this._closeConnectionIfPossible();
		if (!this._fRoundController_rc.info.isRoundPlayState) {
			this.emit(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED);
		}
	}

	_onOnlineRestored() {
		if (this._fRoundController_rc.info.isRoundPlayState) {
			this.emit(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED);
		}
		this._establishConnection();
	}

	_onConnectionOpened() {
		console.log("Socket -> _onConnectionOpened");
		super._onConnectionOpened();

		this._sendEnterRequest();
	}

	_onConnectionClosed(event) {
		console.log("Socket -> _onConnectionClosed");

		let lError = 'ConnectionClosed code:' + event.code + ' reason:' + event.reason + ' type:' + event.type;
		this._sendConnectionErrorDebug(lError);

		super._onConnectionClosed(event);
	}

	_onConnectionError(error) {
		console.log("[WSIC] _onConnectionError", error);

		let lError = 'ConnectionError reason:' + error;
		this._sendConnectionErrorDebug(lError);

		super._onConnectionClosed(error);
	}

	_sendConnectionErrorDebug(error) {
		try {
			var xhr = new XMLHttpRequest();

			let lSocket = APP.appParamsInfo.webSocket;
			lSocket = lSocket.split("/")[2];
			let lUrl = 'https://' + lSocket + '/common/logdebug.jsp?';

			let lSid = 'sid=' + APP.urlBasedParams.SID;
			let lGameId = '&gameId=' + APP.appParamsInfo.gameId;
			let lError = '&error=' + error;
			lUrl = lUrl + lSid + lGameId + lError;

			xhr.open('GET', lUrl, false);
			xhr.send();
		}
		catch (e) {
		}
	}

	//override
	get recoveringConnectionInProgress() {
		return super.recoveringConnectionInProgress;
	}

	_activateReconnectTimeout() {
		console.log("Socket -> _activateReconnectTimeout");
		super._activateReconnectTimeout();
	}

	_reconnectOnConnectionLost() {
		this.emit(GameWebSocketInteractionController.EVENT_ON_SERVER_RECONNECTION_START);
		super._reconnectOnConnectionLost();
	}

	_isMessageCompleteDisregardRequired(messageData) {
		let l_ri = this._fRoundController_rc.info;
		let l_bl = false;

		if (
			!this._fRoomController_rc.info.isRoomOpened
			&& (
				messageData.class === SERVER_MESSAGES.SIT_IN_RESPONSE
				|| messageData.class === SERVER_MESSAGES.SIT_OUT_RESPONSE
				|| messageData.class === SERVER_MESSAGES.GAME_STATE_CHANGED
				|| messageData.class === SERVER_MESSAGES.CRASH_STATE_INFO
				|| messageData.class === SERVER_MESSAGES.CRASH_BET_RESPONSE
				|| messageData.class === SERVER_MESSAGES.CRASH_CANCEL_BET_RESPONSE
				|| messageData.class === SERVER_MESSAGES.ROUND_RESULT
				|| messageData.class === SERVER_MESSAGES.CRASH_CANCEL_AUTOEJECT_RESPONSE
				|| messageData.class === SERVER_MESSAGES.CRASH_CHANGE_AUTOEJECT_RESPONSE
				|| messageData.class === SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE
				|| messageData.class === SERVER_MESSAGES.CRASH_ALL_COPLAYERS_BETS_REJECTED_RESPONSE
				|| messageData.class === SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_CONFIRMED_RESPONSE
				|| messageData.class === SERVER_MESSAGES.CANCEL_BATTLEGROUND_ROUND
				|| messageData.class === SERVER_MESSAGES.STATS
			)
		) {
			// we should not proceed messages that change state of the game while we are waiting for response to OPEN_ROOM,
			// because we will get full info of the game/round only in this response (CrashGameInfo)
			l_bl = true;
		}
		else {
			switch (messageData.class) {
				case SERVER_MESSAGES.GAME_STATE_CHANGED:
					l_bl = (!APP.isBattlegroundGame || messageData.state !== ROUND_STATES.WAIT)
						&& messageData.state === l_ri.roundState
						&& messageData.roundId === l_ri.roundId;
					break;
				case SERVER_MESSAGES.ROUND_RESULT:
					l_bl = l_ri.isRoundResultRecieved
						&& messageData.roundId === l_ri.roundId;
					break;
			}
		}

		return l_bl;
	}

	_specifyEventMessageType(messageData) {
		let eventType;
		switch (messageData.class) {
			case SERVER_MESSAGES.ENTER_GAME_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_ENTER_GAME_MESSAGE;
				break;
			case SERVER_MESSAGES.BALANCE_UPDATED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE;
				break;
			case SERVER_MESSAGES.GAME_TIME_UPDATED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_GAME_TIME_UPDATED_MESSAGE;
				break;
			case SERVER_MESSAGES.STATS:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_STATS_MESSAGE;
				break;
			case SERVER_MESSAGES.CRASH_GAME_INFO:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_CRASH_GAME_INFO_MESSAGE;
				break;

			case SERVER_MESSAGES.CRASH_STATE_INFO:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_CRASH_STATE_INFO_MESSAGE;
				break;


			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_SIT_IN_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.SIT_OUT_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_SIT_OUT_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.GAME_STATE_CHANGED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_GAME_STATE_CHANGED_MESSAGE;
				break;
			case SERVER_MESSAGES.CRASH_BET_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_CRASH_BET_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.CRASH_CANCEL_BET_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_CRASH_CANCEL_BET_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.ROUND_RESULT:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_ROUND_RESULT_MESSAGE;
				break;

			case SERVER_MESSAGES.CRASH_CANCEL_AUTOEJECT_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_CRASH_CANCEL_AUTOEJECT_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.CRASH_CHANGE_AUTOEJECT_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_CRASH_CHANGE_AUTOEJECT_RESPONSE_MESSAGE;

			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.CRASH_ALL_COPLAYERS_BETS_REJECTED_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_CRASH_ALL_COPLAYERS_BETS_REJECTED_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_CONFIRMED_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_CRASH_ALL_MASTER_BETS_CONFIRMED_RESPONSE_MESSAGE;
				break;

			case SERVER_MESSAGES.CANCEL_BATTLEGROUND_ROUND:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND;
				break;
			case SERVER_MESSAGES.LATENCY:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_LATENCY_REQUEST;
				break;
			case SERVER_MESSAGES.GET_START_GAME_URL_RESPONSE:
				console.log("CAF: getBTGUrlResponseReceived");
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE;
			default:
				eventType = super._specifyEventMessageType(messageData);
				break;
		}

		return eventType;
	}

	_specifyErrorCodeSeverity(messageData, requestData) {
		let errorCode = messageData.code;
		let errorCodeSeverity;

		if (
			errorCode == WebSocketInteractionController.ERROR_CODES.ROOM_NOT_FOUND
			|| errorCode == WebSocketInteractionController.ERROR_CODES.ROOM_MOVED
		) {
			errorCodeSeverity = ERROR_CODE_TYPES.FATAL_ERROR;
		}
		else if (errorCode == WebSocketInteractionController.ERROR_CODES.BAD_REQUEST && requestData && requestData.class == CLIENT_MESSAGES.SIT_OUT) {
			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else if (
			errorCode == WebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION
			&& APP.gameController.gameplayController.pendingOperationController.info.isPendingOperationHandlingSupported
		) {
			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else if (errorCode == WebSocketInteractionController.ERROR_CODES.ROOM_WAS_DEACTIVATED) {
			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else {
			errorCodeSeverity = super._specifyErrorCodeSeverity(messageData);
		}

		return errorCodeSeverity;
	}

	_handleGeneralError(errorCode, requestData) {
		super._handleGeneralError(errorCode, requestData);

		let supported_codes = WebSocketInteractionController.ERROR_CODES;
		switch (errorCode) {
			case supported_codes.CHANGE_BET_NOT_ALLOWED:
			case supported_codes.BET_NOT_FOUND:
				switch (requestData.class) {
					case CLIENT_MESSAGES.CRASH_CHANGE_AUTOEJECT:
						this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CRASH_CANCEL_AUTOEJECT);
						this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CRASH_CHANGE_AUTOEJECT);
						break;
				}
				break;

			case supported_codes.TOO_MANY_OBSERVERS:
			case supported_codes.TOO_MANY_PLAYER:
				this._recoverAfterServerShutdownRequired = true;
				this._stopServerMesagesHandling();
				this._closeConnectionIfPossible();
				this._startRecoveringSocketConnection();
				break;
		}
	}

	_handleServerMessage(messageData, requestData) {
		super._handleServerMessage(messageData, requestData);

		let msgClass = messageData.class;
		switch (msgClass) {
			case SERVER_MESSAGES.ENTER_GAME_RESPONSE:
				if (messageData.minStake === undefined || messageData.maxStake === undefined) {
					this._blockAfterCriticalError();
					this._stopServerMesagesHandling();
					this._closeConnectionIfPossible();
				}
				break;
			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE:
				if (messageData.errorCode !== undefined) {
					if (
						this.isFatalError(messageData.errorCode)
						|| messageData.errorCode === GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE
					) {
						this._blockAfterCriticalError();
						this._stopServerMesagesHandling();
						this._closeConnectionIfPossible();
					}
				}
				break;

		}
	}

	_onRoomStateChanged(event) {
		let lRoomInfo_ri = this._fRoomController_rc.info;
		if (lRoomInfo_ri.isRoomOpeningState) {
			this._sendOpenRoomRequest();
		}
		else if (lRoomInfo_ri.isRoomClosingState) {
			this._sendRequest(CLIENT_MESSAGES.CLOSE_ROOM, { roomId: lRoomInfo_ri.roomId });
		}
	}

	_sendEnterRequest() {
		let lEnterRequestParams_obj = {
			sid: APP.urlBasedParams.SID,
			serverId: APP.urlBasedParams.GAMESERVERID,
			mode: APP.urlBasedParams.MODE,
			lang: I18.currentLocale,
			gameId: APP.appParamsInfo.gameId
		}

		if (APP.isBattlegroundGame) {
			// APP.appParamsInfo.battlegroundBuyIn should be used only in ENTER request, in other cases in the game we should use 
			// buyIn value from CrashGameInfo message (https://jira.dgphoenix.com/browse/CRG-523)
			lEnterRequestParams_obj.battlegroundBuyIn = APP.appParamsInfo.battlegroundBuyIn;
			lEnterRequestParams_obj.continueIncompleteRound = APP.appParamsInfo.continueIncompleteRound;
		}

		if (APP.isCAFMode) {
			lEnterRequestParams_obj.privateRoom = true;
		}

		this._sendRequest(CLIENT_MESSAGES.ENTER, lEnterRequestParams_obj);
	}

	_sendOpenRoomRequest() {
		console.log("OpenRoom Request");
		let lOpenRoomRequestParams_obj = null;

		if (APP.appParamsInfo.prefRoomId) {
			lOpenRoomRequestParams_obj = {
				sid: APP.urlBasedParams.SID,
				serverId: APP.urlBasedParams.GAMESERVERID,
				roomId: this._fRoomController_rc.info.roomId,
				lang: I18.currentLocale,
				roomId: APP.appParamsInfo.prefRoomId
			}

		} else {
			lOpenRoomRequestParams_obj = {
				sid: APP.urlBasedParams.SID,
				serverId: APP.urlBasedParams.GAMESERVERID,
				roomId: this._fRoomController_rc.info.roomId,
				lang: I18.currentLocale
			}
		}
		this._sendRequest(CLIENT_MESSAGES.OPEN_ROOM, lOpenRoomRequestParams_obj);
	}

	_onGameAssetsLoadingError(event) {
		this._blockAfterCriticalError();
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_onApplicationFatalErrorOccured(event) {
		this._blockAfterCriticalError();
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	//BALANCE REFRESH...
	_onServerBalanceRefreshRequired(event) {
		this._sendRequest(CLIENT_MESSAGES.REFRESH_BALANCE, {});
	}
	//...BALANCE REFRESH

	//PENDING OPERATION STATUS REFRESH...
	_onRefreshPendingOperationStatusRequired(event) {
		let lParams_obj = {};
		lParams_obj.sid = APP.urlBasedParams.SID;

		this._sendRequest(CLIENT_MESSAGES.CHECK_PENDING_OPERATION_STATUS, lParams_obj);
	}
	//...PENDING OPERATION STATUS REFRESH

	//GAMEPLAY...
	_onRoundStateChanged(event) {
		let l_ri = this._fRoundController_rc.info;
		if (l_ri.isRoundWaitState) {
			// send FullGameInfo in WAIT state to get actual history
			this._tryToRequestFullGameInfo();
		}
		else if (l_ri.isRoundPlayState) {
			// required while change auto-eject multiplier is not possible during rocket flight ...
			this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CRASH_CHANGE_AUTOEJECT);
			// ... required while change auto-eject multiplier is not possible during rocket flight
		}
	}

	_tryToRequestFullGameInfo() {
		if (this._isConnectionOpened) {
			this._sendRequest(CLIENT_MESSAGES.GET_FULL_GAME_INFO, {});
		}
		else if (this._isConnectionClosed) {
			if (this.recoveringConnectionInProgress || this._blockedAfterCriticalError) {
				// no actions required
			}
			else {
				this._establishConnection();
			}
		}
	}

	_onNewBetsAccepted(event) {
		// {"class":"CrashBet","crashBetAmount":1,"autoPlay":false,"rid":334,"date":1644481639649}
		// {"class":"CrashBet","crashBetAmount":1,"autoPlay":true,"multiplier":5,"rid":144,"date":1644485505147}

		if (APP.isBattlegroundGame && APP.gameController.gameplayController.gamePlayersController.betsController.isPlaceBetInProgress()) {
			console.log("WARNING: can't confirm BUY IN, try again later.");
			return;
		}

		let lBets_bi_arr = event.bets;
		let lRequestData_obj = {};

		if (lBets_bi_arr.length === 1) {
			lRequestData_obj = this._generateCrashBetRequestData(lBets_bi_arr[0]);
			this._sendRequest(CLIENT_MESSAGES.CRASH_BET, lRequestData_obj);
			return;
		}

		lRequestData_obj.bets = [];
		for (let i = 0; i < lBets_bi_arr.length; i++) {
			let lBetInfo_bi = lBets_bi_arr[i];
			let lBetRequestData_obj = this._generateCrashBetRequestData(lBetInfo_bi);
			lRequestData_obj.bets.push(lBetRequestData_obj);
		}

		this._sendRequest(CLIENT_MESSAGES.CRASH_BETS, lRequestData_obj);
	}

	_generateCrashBetRequestData(aBetInfo_bi) {
		let lRequestData_obj = {};
		lRequestData_obj.betId = aBetInfo_bi.betId;
		lRequestData_obj.crashBetAmount = APP.isBattlegroundGame ? APP.gameController.info.battlegroundBetValue : aBetInfo_bi.betAmount;
		if (aBetInfo_bi.isAutoEject) {
			lRequestData_obj.autoPlay = true;
			lRequestData_obj.multiplier = aBetInfo_bi.autoEjectMultiplier;
		}
		else {
			lRequestData_obj.autoPlay = false;
		}

		return lRequestData_obj;
	}

	_onBetCancelInitiated(event) {
		// {"class":"CrashCancelBet","crashBetId":"1644659112401_0","rid":10,"date":1644659113470}

		let lBetInfo_bi = event.betInfo;
		let lIsEjectType_bl = event.isEject;

		let lRequestData_obj = {};
		lRequestData_obj.crashBetId = lBetInfo_bi.betId;
		lRequestData_obj.placeNewBet = !lIsEjectType_bl;

		this._sendRequest(CLIENT_MESSAGES.CRASH_CANCEL_BET, lRequestData_obj);
	}

	_onBetCancelAllBetsInitiated(event) {
		this._sendRequest(CLIENT_MESSAGES.CRASH_CANCEL_ALL_BETS, {});
	}

	_onCancelAutoEjectInitiated(event) {
		// { "betId": 1497174620105_0, "date": 1497174203319, "rid": 10, "class": "CrashCancelAutoEject"}
		let lBetInfo_bi = event.betInfo;
		let lRequestData_obj = {};
		lRequestData_obj.betId = lBetInfo_bi.betId;
		lRequestData_obj.excludeParams = lRequestData_obj.excludeParams || {};
		lRequestData_obj.excludeParams.cancelType = event.cancelType;

		this._sendRequest(CLIENT_MESSAGES.CRASH_CANCEL_AUTOEJECT, lRequestData_obj);
	}

	_onEditAutoEjectInitiated(event) {
		let lBetInfo_bi = event.betInfo;
		let lRequestData_obj = {};
		lRequestData_obj.betId = lBetInfo_bi.betId;
		lRequestData_obj.multiplier = event.autoEjectMultiplier;

		this._sendRequest(CLIENT_MESSAGES.CRASH_CHANGE_AUTOEJECT, lRequestData_obj);
	}

	_onRoomJoinInitiated(event) {
		this._sendRequest(CLIENT_MESSAGES.SIT_IN, {});
	}

	_onRoomLeaveInitiated(event) {
		this._sendRequest(CLIENT_MESSAGES.SIT_OUT, {});
	}

	_onMasterPlayerOut(event) {
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CRASH_BET);
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CRASH_BETS);
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CRASH_CANCEL_BET);
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CRASH_CANCEL_ALL_BETS);
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CRASH_CANCEL_AUTOEJECT);
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CRASH_CHANGE_AUTOEJECT);
	}

	_onBattlegroundPlayerInviteTriggered(event) {
		console.log("invite request " + event.nicknames);
		this._sendRequest(CLIENT_MESSAGES.BATTLEGROUND_INVITE, { nicknames: event.nicknames });
	}


	_onBattlegroundPlayerKickTriggered(event) {
		console.log("kick request " + event.nicknames);
		this._sendRequest(CLIENT_MESSAGES.BATTLEGROUND_KICK, { nickname: event.nickname });
	}

	//...GAMEPLAY
}

export default GameWebSocketInteractionController