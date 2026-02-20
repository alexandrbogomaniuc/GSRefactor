import WebSocketInteractionController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/server/WebSocketInteractionController';
import {LobbyWebSocketInteractionInfo, SERVER_MESSAGES, CLIENT_MESSAGES} from '../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import {DEBUG_WEB_SOCKET_URL} from '../../../config/Constants';
import {APP} from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyScreen from '../../../main/LobbyScreen';
import LobbyApp from '../../../LobbyAPP';
import LobbyExternalCommunicator from '../../../external/LobbyExternalCommunicator';
import {GAME_MESSAGES, LOBBY_MESSAGES} from '../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import PseudoGameWebSocketInteractionController from './PseudoGameWebSocketInteractionController';
import FRBController from '../../custom/frb/FRBController';
import TournamentModeController from '../../custom/tournament/TournamentModeController';
import DialogController from '../../uis/custom/dialogs/DialogController';
import BattlegroundBuyInConfirmationDialogController from '../../uis/custom/dialogs/custom/BattlegroundBuyInConfirmationDialogController';
import BattlegroundController from '../../custom/battleground/BattlegroundController';
import TutorialController from '../../uis/custom/tutorial/TutorialController';
import SettingsScreenController from '../../uis/custom/secondary/settings/SettingsScreenController';
import LobbyPendingOperationController from '../../custom/LobbyPendingOperationController';
import { ERROR_CODE_TYPES } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/interaction/server/WebSocketInteractionInfo';
import Application from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/Application';
import BattlegroundBuyInConfirmationDialogControllerCAF from '../../uis/custom/dialogs/custom/BattlegroundBuyInConfirmationDialogControllerCAF';

class LobbyWebSocketInteractionController extends WebSocketInteractionController
{
	static get EVENT_ON_SERVER_MESSAGE() 					{ return WebSocketInteractionController.EVENT_ON_SERVER_MESSAGE };
	static get EVENT_ON_SERVER_CONNECTION_CLOSED() 			{ return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED };
	static get EVENT_ON_SERVER_CONNECTION_OPENED() 			{ return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED };
	static get EVENT_ON_SERVER_ERROR_MESSAGE() 				{ return WebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE };
	static get EVENT_ON_SERVER_OK_MESSAGE() 				{ return WebSocketInteractionController.EVENT_ON_SERVER_OK_MESSAGE };
	
	static get EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND() 							{ return "EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND" };
	static get EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE() 								{ return "EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE" };
	static get EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE() 				{ return "EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE() 	{ return "EVENT_ON_SERVER_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE() 							{ return "EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE" };
	static get EVENT_ON_SERVER_LOBBY_TIME_UPDATED_MESSAGE() 						{ return "EVENT_ON_SERVER_LOBBY_TIME_UPDATED_MESSAGE" };
	static get EVENT_ON_SERVER_STATS_MESSAGE() 										{ return "EVENT_ON_SERVER_STATS_MESSAGE" };
	static get EVENT_ON_SERVER_WEAPONS_MESSAGE() 									{ return "EVENT_ON_SERVER_WEAPONS_MESSAGE" };
	static get EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE() 						{ return "EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE" };
	static get EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED() 							{ return "EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED" };
	static get EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE()							{ return "EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_GAME_ROOM_INFO_RESPONSE()							{ return "EVENT_ON_SERVER_GAME_ROOM_INFO_RESPONSE" };
	static get EVENT_ON_SERVER_PRIVATE_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE() { return "EVENT_ON_SERVER_PRIVATE_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE" };

	constructor()
	{
		super(new LobbyWebSocketInteractionInfo());

		this._recoveryStake = undefined;
		this._checkNicknameTimer_t = null;
		this._fTournamentModeInfo_tmi = null;
	}

	get _debugWebSocketUrl()
	{
		return DEBUG_WEB_SOCKET_URL;
	}

	get _settingsScreenController()
	{
		return APP.secondaryScreenController.settingsScreenController;
	}

	get _paytableScreenController()
	{
		return APP.secondaryScreenController.paytableScreenController;
	}
	
	get isConnectionOpened()
	{
		return this._isConnectionOpened;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let lobbyScreen = APP.lobbyScreen;
		lobbyScreen.on(LobbyScreen.EVENT_ON_START_GAME_URL_REQUIRED, this._onStartGameUrlRequired, this);
		lobbyScreen.on(LobbyScreen.EVENT_ON_BATTLEGROUND_START_GAME_URL_REQUIRED, this._onBattlegroundStartGameUrlRequired, this);

		lobbyScreen.on(LobbyScreen.EVENT_ON_NICKNAME_CHECK_REQUIRED, this._onNickNameCheckRequired, this);
		lobbyScreen.on(LobbyScreen.EVENT_ON_NICKNAME_CHANGE_REQUIRED, this._onChangeNickNameRequired, this);
		lobbyScreen.on(LobbyScreen.EVENT_ON_AVATAR_CHANGE_REQUIRED, this._onChangeAvatarRequired, this);
		lobbyScreen.on(LobbyScreen.EVENT_ON_GET_LOBBY_TIME_REQUIRED, this._onGetLobbyTimeRequired, this);
		lobbyScreen.on(LobbyScreen.EVENT_ON_WEAPONS_REQUIRED, this._onWeaponsRequired, this);

		APP.on(TutorialController.EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED, this._onTutorialNeededStateChanged, this);
		this._settingsScreenController.on(SettingsScreenController.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, this._onTutorialNeededStateChanged, this);

		APP.on(LobbyApp.EVENT_ON_LOBBY_REFRESH_BALANCE_REQUIRED, this._onLobbyRefreshBalanceRequired, this)
		APP.on(LobbyApp.EVENT_ON_LOBBY_RESTART_REQUIRED, this._onLobbyRestartRequired, this);

		APP.on(LobbyApp.EVENT_ON_LOBBY_ASSETS_LOADING_ERROR, this._onLobbyAssetsLoadingError, this);
		APP.on(LobbyApp.EVENT_ON_WEBGL_CONTEXT_LOST, this._onWebglContextLost, this);

		APP.on(LobbyApp.EVENT_ON_OFFLINE, this._onOffline, this);
		APP.on(LobbyApp.EVENT_ON_ONLINE_RESTORED, this._onOnlineRestored, this);

		APP.FRBController.on(FRBController.EVENT_FRB_RESTART_REQUIRED, this._onFRBRestartRequired, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived.bind(this));

		let pseudoGamewebSocketInteractionController = APP.pseudoGamewebSocketInteractionController;
		pseudoGamewebSocketInteractionController.on(PseudoGameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onPseudoGameServerErrorMessage, this);

		let dialogsController = APP.dialogsController;

		let lobbyRebuyDialogController = dialogsController.lobbyRebuyDialogController;
		lobbyRebuyDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onLobbyRebuyDialogRequestConfirmed, this);

		let lobbyRebuyFailedDialogController = dialogsController.lobbyRebuyFailedDialogController;
		lobbyRebuyFailedDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onLobbyRebuyFailedDialogRequestConfirmed, this);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);


		let battlegroundConfirmationDialogController = dialogsController.battlegroundBuyInConfirmationDialogController;
		battlegroundConfirmationDialogController.on(BattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);

		let battlegroundConfirmationDialogControllerCAF = dialogsController.battlegroundBuyInConfirmationDialogControllerCAF;
		battlegroundConfirmationDialogControllerCAF.on(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_BATTLEGROUND_BUY_IN_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);


		APP.battlegroundController.on(BattlegroundController.EVENT_BATTLEGROUND_ROOM_ID_RECEIVED, this._onBattlegroundRoomIdReceived, this);

		APP.pendingOperationController.on(LobbyPendingOperationController.EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED, this._onRefreshPendingOperationStatusRequired, this);
		APP.pendingOperationController.on(LobbyPendingOperationController.EVENT_ON_PENDING_OPERATION_STATUS_TRACKING_TURNED_OFF, this._onPendingOperationStatusTrackingTurnedOff, this);
		

		
		
		// DEBUG...
		// window.addEventListener(
		//    "keydown", this.keyDownHandler.bind(this), false
		//  );
		// ...DEBUG
	}

	_onBattlegroundRoomIdReceived(event)
	{
		this._sendRequest(CLIENT_MESSAGES.GET_ROOM_INFO, { roomId: event.roomId });
	}

	

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 32)
	// 	{
	// 		if (this._webSocket && this._webSocket.readyState === WebSocket.OPEN)
	// 		{
	// 			//bonus status changed...
	// 			let msgBonusRelease = {
	// 										"id": 345345,
	// 										"type":"CASHBONUS",
	// 										"oldStatus":"ACTIVE",
	// 										"newStatus":"RELEASED",
	// 										"reason": "Bonus released",
	// 										"date":1531731629785,
	// 										"rid":-1,
	// 										"class":"BonusStatusChanged"
	// 									};

	// 			let msgBonusLost = {"id": 345345, "type":"CASHBONUS", "oldStatus":"ACTIVE", "newStatus":"LOST", "reason":
	// 			 							"Bonus lost", "date":1531731629785,
	// 			 							"rid":-1,"class":"BonusStatusChanged"};

	// 			this._processServerMessage(msgBonusLost);
	// 			//...bonus status changed
	//  			//network error...
	//   			// this._processServerMessage({"code": 1,"msg": "Internal error","date": 1496748898812,"class": "Error","rid": 1});
	// 	  		//...network error
	// 			//error code...
	// 			// this._processServerMessage({"code": 1003,"msg": "ROOM_NOT_FOUND","date": 1496748898812,"class": "Error","rid": 1});
	// 			//...error code

	// 			//network error...
	//  			// this._processServerMessage({"code": 1,"msg": "Internal error","date": 1496748898812,"class": "Error","rid": 1});
	//  			//...network error

	// 			//close connection...
	// 			// this.emit(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, {wasClean: false});
	// 			// this._closeConnectionIfPossible();
	// 			// this._startReconnectingOnConnectionLost();
	// 			//...close connection

	// 			//tournament cancelled...
	// 			this._processServerMessage({"date": Date.now(), "class": "TournamentStateChanged", "rid": -1, "id":23142,"oldState":"ACTIVE","newState":"CANCELLED","reason":"Tournament time is up"});
	// 			//...tournament cancelled
	// 		}
	// 	}
	// }
	//...DEBUG

	//DEBUG...
	// _processServerMessage(messageData)
	// {
	// 	if (!window.hasEnterLobby && messageData.class == "EnterLobbyResponse")
	// 	{
	// 		window.hasEnterLobby = true;
	// 		messageData.cashBonusInfo = {
	// 			id: 345345,
	// 			amountToRelease: 0,
	// 			balance: 10000,
	// 			amount: 10000,
	// 			status: "ACTIVE"
	// 		}
	// 		if (messageData.balance)
	// 		{
	// 			messageData.balance = 10000;
	// 		}
	// 	}
	// 	super._processServerMessage(messageData);
	// }
	//...DEBUG

	_onFRBRestartRequired(event)
	{
		this._recoveryStake = undefined;
	}

	_onConnectionOpened()
	{
		console.log("Lobby -> _onConnectionOpened");
		super._onConnectionOpened();

		let lobbyScreen = APP.lobbyScreen;
		if (lobbyScreen.isReady)
		{
			this._sendEnterRequest();
		}
		else
		{
			lobbyScreen.on(LobbyScreen.EVENT_ON_READY, this._onLobbyScreenReady, this);
		}

	}

	_onOffline()
	{
		this._closeConnectionIfPossible();
		this.emit(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, {wasClean: false});
	}

	_onOnlineRestored()
	{
		this._startRecoveringSocketConnection();
	}

	_onConnectionClosed(event)
	{
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.LOBBY_CONNECTION_STATE, {state: false});

		console.log("Lobby -> _onConnectionClosed");

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
		console.log("Lobby -> _activateReconnectTimeout");
		super._activateReconnectTimeout();
	}

	_specifyErrorCodeSeverity(messageData, requestData)
	{
		let errorCode = messageData.code;
		let errorCodeSeverity;
        console.log("LobbyError - ServerError " + JSON.stringify(messageData))
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

	_onLobbyScreenReady(event)
	{
		this._sendEnterRequest();
	}

	_onLobbyRestartRequired(event)
	{
		this._sendEnterRequest();
	}

	_specifyEventMessageType(messageData)
	{
		let eventType;
		switch(messageData.class)
		{
			case SERVER_MESSAGES.ENTER_LOBBY_RESPONSE:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE;

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
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.GET_BATTLEGROUND_START_URL_RESPONSE:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.BALANCE_UPDATED:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE;
				break;
			case SERVER_MESSAGES.LOBBY_TIME_UPDATED:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_LOBBY_TIME_UPDATED_MESSAGE;
				break;
			case SERVER_MESSAGES.STATS:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_STATS_MESSAGE;
				break;
			case SERVER_MESSAGES.WEAPONS_RESPONSE:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_WEAPONS_MESSAGE;
				break;
			case SERVER_MESSAGES.BONUS_STATUS_CHANGED:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE;
				break;
			case SERVER_MESSAGES.TOURNAMENT_STATE_CHANGED:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED;
				break;
			case SERVER_MESSAGES.RE_BUY_RESPONSE:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.GET_ROOM_INFO:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_GAME_ROOM_INFO_RESPONSE;
				break;
			case SERVER_MESSAGES.CANCEL_BATTLEGROUND_ROUND:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND;
				break;
			case SERVER_MESSAGES.GET_PRIVATE_BATTLEGROUND_START_URL_RESPONSE:
				eventType = LobbyWebSocketInteractionController.EVENT_ON_SERVER_PRIVATE_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE;
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
		switch(msgClass)
		{
			case SERVER_MESSAGES.ENTER_LOBBY_RESPONSE:
				if (this._recoveryStake !== undefined)
				{
					let recoveryStake = this._recoveryStake;
					this._recoveryStake = undefined;

					this._requestGameUrl({stake:recoveryStake});
				}

				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.LOBBY_CONNECTION_STATE, {state: true});
				break;
		}
	}

	_sendEnterRequest()
	{
		let lEnterRequestParams_obj = {	sid: APP.urlBasedParams.SID,
										serverId: APP.urlBasedParams.GAMESERVERID,
										mode: APP.urlBasedParams.MODE,
										lang: I18.currentLocale,
										gameId: APP.appParamsInfo.gameId,
										noFRB: APP.appParamsInfo.noFRB,
										battlegroundBuyIn: APP.appParamsInfo.battlegroundBuyIn
		}

		if (APP.appParamsInfo.privateRoomId !== undefined)
		{
			lEnterRequestParams_obj.privateRoom = true;
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
		this._sendRequest(CLIENT_MESSAGES.CHECK_NICKNAME_AVAILABILITY, {nickname: event.nickname});
	}

	_onChangeNickNameRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.CHANGE_NICKNAME, {nickname: event.nickname});
	}

	_onChangeAvatarRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.CHANGE_AVATAR, {borderStyle: event.borderStyle, hero: event.hero, background: event.background});
	}

	_onTutorialNeededStateChanged(event)
	{
		this._sendRequest(CLIENT_MESSAGES.CHANGE_TUTORIAL_NEEDED, {disableTooltips: !event.value});
	}

	_onPicksUpSWStateChanged(event)
	{
		this._sendRequest(CLIENT_MESSAGES.CHANGE_PICKS_UP_SW, {didThePlayerWinSWAlready: !event.state});
	}

	_onWeaponsRequired()
	{
		this._sendRequest(CLIENT_MESSAGES.GET_WEAPONS, {});
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
		/*TODO [os]: debug (comment for debug)...*/
		if(APP.isCAFMode)
		{
			this._sendRequest(CLIENT_MESSAGES.GET_PRIVATE_BATTLEGROUND_START_URL, {privateRoomId: APP.appParamsInfo.privateRoomId});
		}
		else
		/*TODO [os]: ...debug (comment for debug)*/
		{
			if(APP.appParamsInfo.prefRoomId)
			{
				console.log("prefRoomId request caught ");
				this._sendRequest(CLIENT_MESSAGES.GET_BATTLEGROUND_START_URL, {roomId:APP.appParamsInfo.prefRoomId,buyIn: event.buyIn || APP.battlegroundController.info.getSelectedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn});
			}else{
				console.log("no prefRoomId request  ");
				this._sendRequest(CLIENT_MESSAGES.GET_BATTLEGROUND_START_URL, {buyIn: event.buyIn || APP.battlegroundController.info.getSelectedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn});
			}
		}
	}

	_onBattlegroundStartGameUrlRequired(event)
	{
		this._sendGetBattlegroundStartGameUrl(event);
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

	_onLobbyRefreshBalanceRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.REFRESH_BALANCE, {});
	}

	_onGetLobbyTimeRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.GET_LOBBY_TIME, {});
	}

	_onLobbyAssetsLoadingError(event)
	{
		this._blockAfterCriticalError();
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_onWebglContextLost(event)
	{
		this._blockAfterCriticalError();
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_onTournamentModeStateChanged(event)
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
				if (
						LobbyWebSocketInteractionController.isFatalError(event.data.errorType)
						|| event.data.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.ROOM_WAS_DEACTIVATED
						|| (
								APP.isCAFMode
								&& (
										event.data.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.TOO_MANY_OBSERVERS
										|| event.data.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.TOO_MANY_PLAYER
									)
							)
					)
				{
					this._blockAfterCriticalError();
					this._stopServerMesagesHandling();
					this._closeConnectionIfPossible();
				}
				break;

			case GAME_MESSAGES.CHECK_LOBBY_CONNECTION:
				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.LOBBY_CONNECTION_STATE, {state: this._isConnectionOpened});
				break;

			case GAME_MESSAGES.GAME_SCRIPT_LOAD_ERROR:
			case GAME_MESSAGES.GAME_LOADING_ERROR:
				APP.handleAssetsLoadingError();

				this._blockAfterCriticalError();
				this._stopServerMesagesHandling();
				this._closeConnectionIfPossible();
				break;
			case GAME_MESSAGES.PLAYER_INFO_UPDATED:
				this._playerInfoUpdatedHandler(event.data);
				break;
			case GAME_MESSAGES.WEBGL_CONTEXT_LOST:
				this._blockAfterCriticalError();
				this._stopServerMesagesHandling();
				this._closeConnectionIfPossible();
				break;
		}
	}

	_playerInfoUpdatedHandler(settings)
	{
		if(
			settings.data.lockOnTarget ||
			settings.data.targetPriority ||
			settings.data.autoFire ||
			settings.data.fireSpeed
			)
		{
			let lRequest_obj = {
				lLockOnTarget_bl: settings.data.lockOnTarget.value,
				lTargetPriority_int: settings.data.targetPriority.value,
				lAutoFire_bl: settings.data.autoFire.value,
				lFireSpeed_int: settings.data.fireSpeed.value
			};

			//this._sendRequest(CLIENT_MESSAGES.CHANGE_FIRE_SETTINGS, lRequest_obj); 0009982: MAX DUEL PIRATES - UPDATE TO NEW MATH (dialog when a player picks up a special weapon)
		}
	}

	_onPseudoGameServerErrorMessage(event)
	{
		let serverData = event.messageData;

		if (LobbyWebSocketInteractionController.isFatalError(event.errorType))
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

export default LobbyWebSocketInteractionController