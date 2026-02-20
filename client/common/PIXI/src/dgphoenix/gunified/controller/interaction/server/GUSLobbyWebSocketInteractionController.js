import WebSocketInteractionController from '../../../../unified/controller/interaction/server/WebSocketInteractionController';
import GUSLobbyWebSocketInteractionInfo, { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';
import GUSLobbyScreen from '../../../view/main/GUSLobbyScreen';
import GUSLobbyTooltipsController from '../../uis/custom/tooltips/GUSLobbyTooltipsController';
import GUSLobbyTutorialController from '../../uis/custom/tutorial/GUSLobbyTutorialController';
import GUSLobbyApplication from '../../main/GUSLobbyApplication';
import GUSLobbyFRBController from '../../custom/frb/GUSLobbyFRBController';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from '../../external/GUSLobbyExternalCommunicator';
import GUSPseudoGameWebSocketInteractionController from './GUSPseudoGameWebSocketInteractionController';
import GUDialogController from '../../uis/custom/dialogs/GUDialogController';
import GUSLobbyTournamentModeController from '../../custom/tournament/GUSLobbyTournamentModeController';
import I18 from '../../../../unified/controller/translations/I18';
import { APP } from '../../../../unified/controller/main/globals';
import Timer from '../../../../unified/controller/time/Timer';
import GUSBattlegroundBuyInConfirmationDialogController from '../../uis/custom/dialogs/custom/GUSBattlegroundBuyInConfirmationDialogController';
import GUSLobbyBattlegroundController from '../../custom/battleground/GUSLobbyBattlegroundController';
import GUSLobbyPendingOperationController from '../../gameplay/GUSLobbyPendingOperationController';
import { ERROR_CODE_TYPES } from '../../../../unified/model/interaction/server/WebSocketInteractionInfo';

const DEBUG_WEB_SOCKET_URL = 'ws://games-mp-gp3.dgphoenix.com/websocket/mplobby';

class GUSLobbyWebSocketInteractionController extends WebSocketInteractionController
{
	static get EVENT_ON_SERVER_MESSAGE()								{ return WebSocketInteractionController.EVENT_ON_SERVER_MESSAGE }
	static get EVENT_ON_SERVER_CONNECTION_CLOSED()						{ return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED }
	static get EVENT_ON_SERVER_CONNECTION_OPENED()						{ return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED }
	static get EVENT_ON_SERVER_ERROR_MESSAGE()							{ return WebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE }
	static get EVENT_ON_SERVER_OK_MESSAGE()								{ return WebSocketInteractionController.EVENT_ON_SERVER_OK_MESSAGE }

	static get EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE()					{ return "EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE" }
	static get EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE()	{ return "EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE" }
	static get EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE()				{ return "EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE" }
	static get EVENT_ON_SERVER_LOBBY_TIME_UPDATED_MESSAGE()				{ return "EVENT_ON_SERVER_LOBBY_TIME_UPDATED_MESSAGE" }
	static get EVENT_ON_SERVER_STATS_MESSAGE()							{ return "EVENT_ON_SERVER_STATS_MESSAGE" }
	static get EVENT_ON_SERVER_WEAPONS_MESSAGE()						{ return "EVENT_ON_SERVER_WEAPONS_MESSAGE" }
	static get EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE()			{ return "EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE" }
	static get EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED()				{ return "EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED" }
	static get EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE()				{ return "EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE" }
	static get EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND() 							{ return "EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND" };
	static get EVENT_ON_SERVER_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE() 	{ return "EVENT_ON_SERVER_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE" };

	constructor(aOptInfo)
	{
		super(aOptInfo || new GUSLobbyWebSocketInteractionInfo());

		this._recoveryStake = undefined;
		this._checkNicknameTimer_t = null;
		this._fTournamentModeInfo_tmi = null;
	}

	get _debugWebSocketUrl()
	{
		return DEBUG_WEB_SOCKET_URL;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let lobbyScreen = APP.lobbyScreen;
		lobbyScreen.on(GUSLobbyScreen.EVENT_ON_START_GAME_URL_REQUIRED, this._onStartGameUrlRequired, this);
		lobbyScreen.on(GUSLobbyScreen.EVENT_ON_BATTLEGROUND_START_GAME_URL_REQUIRED, this._onBattlegroundStartGameUrlRequired, this);
		lobbyScreen.on(GUSLobbyScreen.EVENT_ON_NICKNAME_CHECK_REQUIRED, this._onNickNameCheckRequired, this);
		lobbyScreen.on(GUSLobbyScreen.EVENT_ON_NICKNAME_CHANGE_REQUIRED, this._onChangeNickNameRequired, this);
		lobbyScreen.on(GUSLobbyScreen.EVENT_ON_GET_LOBBY_TIME_REQUIRED, this._onGetLobbyTimeRequired, this);
		lobbyScreen.on(GUSLobbyScreen.EVENT_ON_WEAPONS_REQUIRED, this._onWeaponsRequired, this);

		if (APP.isTutorialSupported)
		{
			APP.tutorialController.on(GUSLobbyTutorialController.EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED, this._onTipsStateChanged, this);
		}
		else
		{
			APP.tooltipsController.on(GUSLobbyTooltipsController.EVENT_ON_TIPS_STATE_CHANGED, this._onTipsStateChanged, this);
		}
		

		APP.on(GUSLobbyApplication.EVENT_ON_LOBBY_REFRESH_BALANCE_REQUIRED, this._onLobbyRefreshBalanceRequired, this)
		APP.on(GUSLobbyApplication.EVENT_ON_LOBBY_RESTART_REQUIRED, this._onLobbyRestartRequired, this);

		APP.on(GUSLobbyApplication.EVENT_ON_LOBBY_ASSETS_LOADING_ERROR, this._onLobbyAssetsLoadingError, this);
		APP.on(GUSLobbyApplication.EVENT_ON_WEBGL_CONTEXT_LOST, this._onWebglContextLost, this);

		APP.on(GUSLobbyApplication.EVENT_ON_OFFLINE, this._onOffline, this);
		APP.on(GUSLobbyApplication.EVENT_ON_ONLINE_RESTORED, this._onOnlineRestored, this);

		APP.FRBController.on(GUSLobbyFRBController.EVENT_FRB_RESTART_REQUIRED, this._onFRBRestartRequired, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived.bind(this));

		let pseudoGamewebSocketInteractionController = APP.pseudoGamewebSocketInteractionController;
		pseudoGamewebSocketInteractionController.on(GUSPseudoGameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onPseudoGameServerErrorMessage, this);

		let dialogsController = APP.dialogsController;
		
		let lobbyRebuyDialogController = dialogsController.lobbyRebuyDialogController;
		lobbyRebuyDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onLobbyRebuyDialogRequestConfirmed, this);

		let lobbyRebuyFailedDialogController = dialogsController.lobbyRebuyFailedDialogController;
		lobbyRebuyFailedDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onLobbyRebuyFailedDialogRequestConfirmed, this);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(GUSLobbyTournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);

		let battlegroundConfirmationDialogController = dialogsController.battlegroundBuyInConfirmationDialogController;
		battlegroundConfirmationDialogController.on(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);

		APP.battlegroundController.on(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_ROOM_ID_RECEIVED, this._onBattlegroundRoomIdReceived, this);

		APP.pendingOperationController.on(GUSLobbyPendingOperationController.EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED, this._onRefreshPendingOperationStatusRequired, this);
		APP.pendingOperationController.on(GUSLobbyPendingOperationController.EVENT_ON_PENDING_OPERATION_STATUS_TRACKING_TURNED_OFF, this._onPendingOperationStatusTrackingTurnedOff, this);
	}

	_onFRBRestartRequired()
	{
		this._recoveryStake = undefined;
	}

	_onConnectionOpened()
	{
		super._onConnectionOpened();

		let lobbyScreen = APP.lobbyScreen;
		if (lobbyScreen.isReady)
		{
			this._sendEnterRequest();
		}
		else
		{
			lobbyScreen.on(GUSLobbyScreen.EVENT_ON_READY, this._onLobbyScreenReady, this);
		}

	}

	_onOffline()
	{
		this._closeConnectionIfPossible();
		this.emit(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, {wasClean: false});
	}

	_onOnlineRestored()
	{
		this._startRecoveringSocketConnection();
	}

	_onConnectionClosed(event)
	{
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.LOBBY_CONNECTION_STATE, { state: false });

		super._onConnectionClosed(event);
	}

	_startReconnectingOnConnectionLost()
	{
		if (this._fTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			return;
		}

		super._startReconnectingOnConnectionLost();
	}

	_activateReconnectTimeout()
	{
		super._activateReconnectTimeout();
	}

	_specifyErrorCodeSeverity(messageData, requestData)
	{
		let errorCode = messageData.code;
		let errorCodeSeverity;

		if (
					errorCode == WebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION
					&& APP.pendingOperationController.info.isPendingOperationHandlingSupported
				)
		{
			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else
		{
			errorCodeSeverity = super._specifyErrorCodeSeverity(messageData, requestData);
		}

		return errorCodeSeverity;
	}

	_onLobbyScreenReady()
	{
		this._sendEnterRequest();
	}

	_onLobbyRestartRequired()
	{
		this._sendEnterRequest();
	}

	_specifyEventMessageType(messageData)
	{
		let eventType;
		switch (messageData.class)
		{
			case SERVER_MESSAGES.ENTER_LOBBY_RESPONSE:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE;

				//DEBUG...
				/*
				messageData.battleground =
				{
					buyIns: [100, 200, 500, 1000, 2000, 5000, 500000],
					alreadySeatRoomId: 200133,
					//startGameUrl: "https://host/websocket/mpgame?SID=3453&serverId=1&lang=en&roomId=200133"

					//startGameUrl: "http://127.0.0.1:8081/?SID=1_b82840341b0fa475bda20000017a8616_VVAQUAEECwxaUBVWClIODVVWD10cQ1lFVV5eTxcGXAsaAAQI&serverId=1&lang=en&roomId=7390004&stake=5&WEB_SOCKET_URL=ws://gs1-mp-beta.discreetgaming.com/websocket/mpgame"
				}
				*/
				//...DEBUG
				break;
			case SERVER_MESSAGES.GET_START_GAME_URL_RESPONSE:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.GET_BATTLEGROUND_START_URL_RESPONSE:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.BALANCE_UPDATED:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE;
				break;
			case SERVER_MESSAGES.LOBBY_TIME_UPDATED:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_LOBBY_TIME_UPDATED_MESSAGE;
				break;
			case SERVER_MESSAGES.STATS:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_STATS_MESSAGE;
				break;
			case SERVER_MESSAGES.WEAPONS_RESPONSE:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_WEAPONS_MESSAGE;
				break;
			case SERVER_MESSAGES.BONUS_STATUS_CHANGED:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE;
				break;
			case SERVER_MESSAGES.TOURNAMENT_STATE_CHANGED:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED;
				break;
			case SERVER_MESSAGES.RE_BUY_RESPONSE:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.CANCEL_BATTLEGROUND_ROUND:
				eventType = GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND;
				break;
			default:
				eventType = super._specifyEventMessageType(messageData);
				break;
		}

		return eventType;
	}

	_handleServerMessage(messageData, requestData)
	{
		super._handleServerMessage(messageData, requestData);

		let msgClass = messageData.class;
		switch (msgClass)
		{
			case SERVER_MESSAGES.ENTER_LOBBY_RESPONSE:
				if (this._recoveryStake !== undefined)
				{
					let recoveryStake = this._recoveryStake;
					this._recoveryStake = undefined;

					this._requestGameUrl({ stake: recoveryStake });
				}

				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.LOBBY_CONNECTION_STATE, { state: true });
				break;
		}
	}

	_sendEnterRequest()
	{
		let lEnterRequestParams_obj = {
			sid: APP.urlBasedParams.SID,
			serverId: APP.urlBasedParams.GAMESERVERID,
			mode: APP.urlBasedParams.MODE,
			lang: I18.currentLocale,
			gameId: APP.appParamsInfo.gameId,
			noFRB: APP.appParamsInfo.noFRB,
			battlegroundBuyIn: APP.appParamsInfo.battlegroundBuyIn
		}


		if (APP.appParamsInfo.bonusId != undefined)
		{
			if (!APP.lobbyBonusController.info.isCleared)
			{
				lEnterRequestParams_obj.bonusId = APP.appParamsInfo.bonusId;
			}
		}

		if (APP.appParamsInfo.tournamentId != undefined)
		{
			lEnterRequestParams_obj.tournamentId = APP.appParamsInfo.tournamentId;
		}

		if (APP.appParamsInfo.isBattlegroundGame)
		{
			lEnterRequestParams_obj.continueIncompleteRound  = APP.appParamsInfo.continueIncompleteRound;
		}

		this._sendRequest(CLIENT_MESSAGES.ENTER, lEnterRequestParams_obj);
	}

	_onStartGameUrlRequired(event)
	{
		if (this._isConnectionOpened)
		{
			if (APP.battlegroundController.info.isBattlegroundGameStarted)
			{
				this._sendGetBattlegroundStartGameUrl();
			}
			else
			{
				this._requestGameUrl({stake:event.stake});
			}
		}
		else
		{
			if (event.retryAfterConnectionRecovered)
			{
				this._recoveryStake = event.stake;
			}
		}
	}

	_requestGameUrl(data)
	{
		this._sendRequest(CLIENT_MESSAGES.GET_START_GAME_URL, data);
	}

	_onNickNameCheckRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.CHECK_NICKNAME_AVAILABILITY, { nickname: event.nickname });
	}

	_onChangeNickNameRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.CHANGE_NICKNAME, { nickname: event.nickname });
	}

	_onTipsStateChanged(event)
	{
		this._sendRequest(CLIENT_MESSAGES.CHANGE_TOOL_TIPS, { disableTooltips: !event.state });
	}

	_onWeaponsRequired()
	{
		this._sendRequest(CLIENT_MESSAGES.GET_WEAPONS, {});
	}

	_onLobbyRebuyDialogRequestConfirmed()
	{
		if (
			this._hasUnRespondedRequest(CLIENT_MESSAGES.RE_BUY)
			|| !this._fTournamentModeInfo_tmi.rebuyAllowed
			|| this._fTournamentModeInfo_tmi.isRebuyLimitExceeded
		)
		{
			return;
		}

		this._sendRequest(CLIENT_MESSAGES.RE_BUY, {});
	}

	_onLobbyRebuyFailedDialogRequestConfirmed()
	{
		if (this._hasUnRespondedRequest(CLIENT_MESSAGES.RE_BUY))
		{
			return;
		}

		this._sendRequest(CLIENT_MESSAGES.RE_BUY, {});
	}

	_onLobbyRefreshBalanceRequired()
	{
		this._sendRequest(CLIENT_MESSAGES.REFRESH_BALANCE, {});
	}

	_onGetLobbyTimeRequired()
	{
		this._sendRequest(CLIENT_MESSAGES.GET_LOBBY_TIME, {});
	}

	_onLobbyAssetsLoadingError()
	{
		this._blockAfterCriticalError();
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_onWebglContextLost()
	{
		this._blockAfterCriticalError();
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_onTournamentModeStateChanged()
	{
		let lTournamentModeInfo_tmi = this._fTournamentModeInfo_tmi;
		if (lTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			this._stopReconnecting();
			this._blockAfterCriticalError();
			this._stopServerMesagesHandling();
			this._closeConnectionIfPossible();
		}
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (GUSLobbyWebSocketInteractionController.isFatalError(event.data.errorType))
				{
					this._blockAfterCriticalError();
					this._stopServerMesagesHandling();
					this._closeConnectionIfPossible();
				}
				break;

			case GAME_MESSAGES.CHECK_LOBBY_CONNECTION:
				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.LOBBY_CONNECTION_STATE, { state: this._isConnectionOpened });
				break;

			case GAME_MESSAGES.GAME_SCRIPT_LOAD_ERROR:
			case GAME_MESSAGES.GAME_LOADING_ERROR:
				APP.handleAssetsLoadingError();
				this._blockAfterCriticalError();
				this._stopServerMesagesHandling();
				this._closeConnectionIfPossible();
				break;
			case GAME_MESSAGES.WEBGL_CONTEXT_LOST:
				this._blockAfterCriticalError();
				this._stopServerMesagesHandling();
				this._closeConnectionIfPossible();
				break;
		}
	}

	_onBattlegroundRoomIdReceived(event)
	{
		this._sendRequest(CLIENT_MESSAGES.GET_ROOM_INFO, { roomId: event.roomId });
	}

	_onBattlegroundBuyInConfirmed(event)
	{
		this._sendGetBattlegroundStartGameUrl(event);
	}

	_sendConfirmBattlegroundBuyIn()
	{
		this._sendRequest(CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN, {});
	}

	_sendGetBattlegroundStartGameUrl(event)
	{
		this._sendRequest(CLIENT_MESSAGES.GET_BATTLEGROUND_START_URL, {buyIn: event.buyIn || APP.battlegroundController.info.getSelectedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn});
	}

	_onBattlegroundStartGameUrlRequired(event)
	{
		this._sendGetBattlegroundStartGameUrl(event);
	}

	_onPseudoGameServerErrorMessage(event)
	{
		if (GUSLobbyWebSocketInteractionController.isFatalError(event.errorType))
		{
			this._blockAfterCriticalError();
			this._stopServerMesagesHandling();
			this._closeConnectionIfPossible();
		}
	}

	_sendRequest(requestClass, requestData)
	{
		if (!this._isConnectionOpened)
		{
			return;
		}

		let lastRequestTime = this._requestsClassLastTimes_obj[requestClass];
		let lCheckNickname_bln = (requestClass == CLIENT_MESSAGES.CHECK_NICKNAME_AVAILABILITY);

		if (lastRequestTime && lCheckNickname_bln)
		{
			if (this._checkNicknameTimer_t)
			{
				this._checkNicknameTimer_t.destructor();
				this._checkNicknameTimer_t = null;
			}

			let timeDiff = Date.now() - lastRequestTime;
			let requestTimeLimit = this.info.getRequestTimeLimit(requestClass);
			if (timeDiff < requestTimeLimit)
			{
				let timeDelay = Math.max(requestTimeLimit - timeDiff + 1, 0);

				this._checkNicknameTimer_t = new Timer(this._resendRequest.bind(this, requestClass, requestData), timeDelay);
				return;
			}
		}

		super._sendRequest(requestClass, requestData);
	}

	//PENDING_OPERATION...
	_onRefreshPendingOperationStatusRequired(event)
	{
		let lParams_obj = {};
		lParams_obj.sid = APP.urlBasedParams.SID;

		this._sendRequest(CLIENT_MESSAGES.CHECK_PENDING_OPERATION_STATUS, lParams_obj);
	}

	_onPendingOperationStatusTrackingTurnedOff(event)
	{
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CHECK_PENDING_OPERATION_STATUS);
	}
	//...PENDING_OPERATION
}

export default GUSLobbyWebSocketInteractionController