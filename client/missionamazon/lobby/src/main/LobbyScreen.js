import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import LobbyView from './LobbyView';
import EditProfileScreenController from '../controller/uis/custom/secondary/edit_profile/EditProfileScreenController';
import LobbyStateInfo from '../model/state/LobbyStateInfo';
import PlayerInfo from '../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import LobbyWebSocketInteractionController from '../controller/interaction/server/LobbyWebSocketInteractionController';
import PseudoGameWebSocketInteractionController from '../controller/interaction/server/PseudoGameWebSocketInteractionController';
import {CLIENT_MESSAGES, SERVER_MESSAGES} from '../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyExternalCommunicator from '../external/LobbyExternalCommunicator';
import {GAME_MESSAGES, LOBBY_MESSAGES} from '../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import DialogController from '../controller/uis/custom/dialogs/DialogController';
import SecondaryScreenController from '../controller/uis/custom/secondary/SecondaryScreenController';
import { Utils } from '../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import FRBController from '../controller/custom/frb/FRBController';
import LobbyBonusController from './../controller/uis/custom/bonus/LobbyBonusController';
import TournamentModeController from './../controller/custom/tournament/TournamentModeController';
import RoomMovedErrorRequestsLimitDialogController from '../controller/uis/custom/dialogs/custom/RoomMovedErrorRequestsLimitDialogController';
import LobbyPendingOperationController from '../controller/custom/LobbyPendingOperationController';
import BattlegroundController from '../controller/custom/battleground/BattlegroundController';

const ERROR_REQUESTS_START_GAME_URL_LIMIT = 3;

class LobbyScreen extends Sprite
{
	static get EVENT_ON_READY()										{return 'onLobbyScreenReady';}
	static get EVENT_ON_NICKNAME_AVAILABILITY_CAHNGED()				{return "onNicknameAvailabilityChanged";}
	static get EVENT_ON_NICKNAME_ACCEPTED()							{return "onNicknameAccepted";}
	static get EVENT_ON_NICKNAME_DENIED()							{return "onNicknameDenied";}
	static get EVENT_ON_AVATAR_ACCEPTED()							{return "onAvatarAccepted";}
	static get EVENT_ON_AVATAR_DENIED()								{return "onAvatarDenied";}
	static get EVENT_ON_GAME_URL_READY()							{return "onGameURLRead";}
	static get EVENT_ON_PSEUDO_GAME_URL_READY()						{return "onPseudoGameURLRead";}
	static get EVENT_ON_GAME_LAUNCH_INITIATED()						{return "onGameLaunchInitiated";}
	static get EVENT_ON_LOBBY_BACK_TO_GAME()						{return "backToGame";}
	static get EVENT_ON_PROFILE_STYLES_INIT()						{return "onProfileStylesInit";}
	static get EVENT_ON_START_GAME_URL_REQUIRED()					{return "onStartGameUrlRequired";}
	static get EVENT_ON_BATTLEGROUND_START_GAME_URL_REQUIRED()		{return "onBattlegroundStartGameUrlRequired";}
	static get EVENT_ON_GET_LOBBY_TIME_REQUIRED()					{return "onGetLobbyTimeRequired";}
	static get EVENT_ON_WEAPONS_REQUIRED()							{return "onWeaponsRequired";}
	static get EVENT_ON_LOBBY_SHOW()								{return "onLobbyShow";}
	static get EVENT_ON_LOBBY_HIDE()								{return "onLobbyHide";}

	static get EVENT_ON_LAST_ROOM_RESITOUT_COMPLETED()				{return "EVENT_ON_LAST_ROOM_RESITOUT_COMPLETED";}
	static get EVENT_ON_ALREADY_SW_WIN_INFO_UPDATED()				{return "EVENT_ON_ALREADY_SW_WIN_INFO_UPDATED";}
	static get EVENT_ON_FIRE_SETTINGS_UPDATED()						{return "EVENT_ON_FIRE_SETTINGS_UPDATED";}

	static get EVENT_ON_LOBBY_SETTINGS_CLICKED()					{return LobbyView.EVENT_ON_LOBBY_SETTINGS_CLICKED;}
	static get EVENT_ON_LOBBY_INFO_CLICKED()						{return LobbyView.EVENT_ON_LOBBY_INFO_CLICKED;}
	static get EVENT_ON_LOBBY_PROFILE_CLICKED()						{return LobbyView.EVENT_ON_LOBBY_PROFILE_CLICKED;}
	static get EVENT_ON_LOBBY_CASHIER_CLICKED()						{return LobbyView.EVENT_ON_LOBBY_CASHIER_CLICKED;}
	static get EVENT_ON_PLAYER_STATS_ON_SCREEN_UPDATED()			{return LobbyView.EVENT_ON_PLAYER_STATS_UPDATED;}
	static get EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK() 			{return LobbyView.EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK;}
	static get EVENT_ON_LAUNCH_GAME_CLICKED() 						{return LobbyView.EVENT_ON_LAUNCH_GAME_CLICKED;}	
	static get EVENT_ON_NICKNAME_CHECK_REQUIRED()					{return EditProfileScreenController.EVENT_ON_NICKNAME_CHECK_REQUIRED;}
	static get EVENT_ON_NICKNAME_CHANGE_REQUIRED()					{return EditProfileScreenController.EVENT_ON_NICKNAME_CHANGE_REQUIRED;}
	static get EVENT_ON_AVATAR_CHANGE_REQUIRED()					{return EditProfileScreenController.EVENT_ON_AVATAR_CHANGE_REQUIRED;}
	static get EVENT_ON_PSEUDO_GAME_COMPENSATION_REQUIRED()			{return PseudoGameWebSocketInteractionController.EVENT_ON_COMPENSATION_REQUIRED;}

	static get EVENT_ON_BATTLEGROUND_START_URL_UPDATED()			{return 'EVENT_ON_BATTLEGROUND_START_URL_UPDATED';}
	static get EVENT_ON_ERROR_ROOM_MOVED_REQUESTS_LIMIT_REACHED()	{return 'EVENT_ON_ERROR_ROOM_MOVED_REQUESTS_LIMIT_REACHED'; }

	showLobby()
	{
		this._showLobby();
	}

	hideLobby()
	{
		this._hideLobby();
	}

	showBlur()
	{
		this._showBlur();
	}

	hideBlur()
	{
		this._hideBlur();
	}

	clearInfo()
	{
		this.alreadySeatRoomId = -1;
	}

	get playerInfo()
	{
		return this._fPlayer_obj;
	}

	constructor()
	{
		super();

		this._fLobbyView_sprt = null;
		this.getRoomParams = null;

		this._fAvailableAvatarStyles_obj = null;
		this._fApplyingNickname_str = null;

		this.isScreenVisible = false;
		this.secondaryScreenState = LobbyStateInfo.SCREEN_NONE;

		this._fPlayer_obj = {
			nickname: "Unknown",
			currency: "USD"
		};

		this.gameInitiated = false;
		this._fIsReconnect_bl = false; //means re-join room a.k.a. lasthand
		this._fIsResitOut_bl = false;

		this.alreadySeatRoomId = -1;

		this._fStartGameUrlRequireSent_bln = false;
		this._recoveryGameUrlUpdateRequired = false;
		this._ready = false;

		this._fFireSettingsInitiated_bln = false;

		this._fIsNeedRequestStartGameUrlAfterLobbySocketReconnetion_bl = null;
		this._fRoomIdNeedForRequestStartGameUrlAfterLobbySocketReconnetion_num = null;

		this._fErrorGetStartGameUrlAttempts_num = 0;

		this._fCurrentSelectedRoomStake_num = null;
	}

	get _playerController()
	{
		return APP.playerController;
	}

	get _settingsScreenController()
	{
		return APP.secondaryScreenController.settingsScreenController;
	}

	init()
	{
		this.createLobbyField();
		this.start();

		this._ready = true;
		this.emit(LobbyScreen.EVENT_ON_READY);
	}

	get isReady()
	{
		return this._ready;
	}

	get resitOutInProcess()
	{
		return this._fIsResitOut_bl;
	}

	createLobbyField()
	{
		this._fBaseContainer_sprt = this.addChild(new Sprite());
		this._fTopContainer_sprt = this.addChild(new Sprite());

		let backOffsetY_num = ((APP.config.margin.bottom || 0) - (APP.config.margin.top || 0)) / 2;
		let back = this._fBaseContainer_sprt.addChild(APP.library.getSprite('lobby/back'));
		back.position.y = backOffsetY_num;
	}

	start()
	{
		let GET = parseGet();
		window.GET = GET;
		if (GET.room)
		{
			this.getRoomParams = GET.room;
		}

		APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameExternalMessageReceived, this);

		this._startHandleWebSocketMessages();

		APP.dialogsController.returnToGameDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onReturnToGameConfirmed, this);
		APP.dialogsController.returnToGameDialogController.on(DialogController.EVENT_REQUEST_NOT_CONFIRMED, this._onReturnToGameDenied, this);
		APP.dialogsController.roomMovedErrorRequestsLimitDialogController.on(RoomMovedErrorRequestsLimitDialogController.EVENT_REQUEST_CONFIRMED, this._onRequestsLimitDialogConfirmed, this);

		APP.secondaryScreenController.on(SecondaryScreenController.EVENT_SCREEN_ACTIVATED, this._onSecondaryScreenActivated, this);
		APP.secondaryScreenController.on(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenDeactivated, this);

		APP.FRBController.on(FRBController.EVENT_FRB_LOBBY_INTRO_CONFIRMED, this._onFrbLobbyIntroConfirmed, this);
		APP.FRBController.on(FRBController.EVENT_FRB_RESTART_REQUIRED, this._onFRBRestartRequired, this);

		APP.lobbyBonusController.on(LobbyBonusController.EVENT_ON_BONUS_ENTER_ROOM_REQUIRED, this._onBonusEnterRoomRequired, this);
		APP.tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_ENTER_ROOM_REQUIRED, this._onTournamentEnterRoomRequired, this);

		APP.pendingOperationController.on(LobbyPendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);

		APP.battlegroundController.on(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_ALLREADY_DONE_IN_TOURNAMENT_LOBBY, this._onBattlegroundBuyInAllreadyDoneInTournamentlobby, this);
		APP.battlegroundController.on(BattlegroundController.EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL, this._onBattlegroundNeedUpdateStartGameUrl, this);
	}

	onSomeBonusStateChanged()
	{
		this._weaponsRequestSuspicion();
	}

	_onSecondaryScreenActivated(event)
	{
		this._fLobbyView_sprt && this._fLobbyView_sprt.hide();
		this._showBlur()
	}

	_onSecondaryScreenDeactivated(event)
	{
		this._fLobbyView_sprt && this._fLobbyView_sprt.show();
		this._hideBlur()
	}

	_showBlur()
	{
		if (APP.isMobile) return;
		let blurFilter = new PIXI.filters.BlurFilter();
		blurFilter.blur = 10;

		this.filters = [blurFilter];
	}

	_hideBlur()
	{
		if (APP.isMobile) return;
		this.filters = null;
	}

	_onGameExternalMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (LobbyWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					this._handleGameGeneralError(event.data.errorCode);
				}
				break;
			case GAME_MESSAGES.GAME_RECOVERY_ON_CONNECTION_LOST_STARTED:

				let webSocketInteractionController = APP.webSocketInteractionController;
				if (!webSocketInteractionController.isConnectionOpened) //If the socket is currently closed, then the start game url request should be postponed
				{
					this._fIsNeedRequestStartGameUrlAfterLobbySocketReconnetion_bl = true;
					this._fRoomIdNeedForRequestStartGameUrlAfterLobbySocketReconnetion_num = event.data.roomId;
					break;
				}

				this._recoveryGameUrlUpdateRequired = true;
				this._tryToRequestStartGameUrlDependingOnMode(true, undefined, event.data.roomId);
				break;

			case GAME_MESSAGES.ROOM_NO_EMPTY_PLACE_TO_SEAT:
				this._recoveryGameUrlUpdateRequired = true;
				this._tryToRequestStartGameUrlDependingOnMode(true);
				break;
			case GAME_MESSAGES.GAME_ROUND_STATE_CHANGED:
			case GAME_MESSAGES.ROUND_RESULT_ACTIVATED:
			case GAME_MESSAGES.GAME_IN_PROGRESS:
			case GAME_MESSAGES.WEAPONS_UPDATED:
			case GAME_MESSAGES.WEAPON_SELECTED:
				this._fErrorGetStartGameUrlAttempts_num = 0;
				break;
			case GAME_MESSAGES.DO_NOT_COUNT_START_GAME_RECONNECTION_ATTEMPT:
				this._doNotCountReconnectAttemptForStartGameUrl = true; 
				break;
		}
	}

	_handleGameGeneralError(errorCode)
	{
		let supported_codes = LobbyWebSocketInteractionController.ERROR_CODES;
		let stake = undefined;
		switch(errorCode)
		{
			case supported_codes.TOO_MANY_OBSERVERS:
				stake = this._fCurrentSelectedRoomStake_num;
			case supported_codes.TOO_MANY_PLAYER:
				if (APP.isCAFMode)
				{
					// in CAF mode no need to try to get another room, you are invited to this specific room
					break;
				}
			case supported_codes.ROOM_NOT_FOUND:
				this._recoveryGameUrlUpdateRequired = true;
				this._tryToRequestStartGameUrlDependingOnMode(true);
				break;
			case supported_codes.ROOM_MOVED:
				APP.logger.i_pushError("LobbyScreen. Room moved to another server. The game unsuccessfully tried to restore connection.");
				this._recoveryGameUrlUpdateRequired = true;
				this._tryToRequestStartGameUrlDependingOnMode(true);
				break;
			case supported_codes.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE:
				if (
						APP.isBattlegroundGame
						&& (
								!APP.isCAFMode
								|| !APP.playerController.info.isCAFRoomManager
							)
					)
				{
					this._tryToRequestStartGameUrlDependingOnMode(true);
				}
				else
				{
					this.requestStartGameUrl(true, stake);
				}
				break;
		}
	}

	//SERVER INTERACTION...
	_startHandleWebSocketMessages()
	{
		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlResponseMessage, this);
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_LOBBY_TIME_UPDATED_MESSAGE, this._onLobbyTimeUpdatedMessage, this);
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_OK_MESSAGE, this.onServerOkMessage, this);
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this.onServerErrorMessage, this);
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_STATS_MESSAGE, this.onServerStatsMessage, this);
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_WEAPONS_MESSAGE, this._onWeaponsResponse, this);
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlResponseMessage, this);
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_PRIVATE_BATTLEGROUND_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlResponseMessage, this);
	}

	_onServerEnterLobbyMessage(event)
	{
		this.enterLobbyResponse(event.messageData);
	}

	onServerStatsMessage(event)
	{
		let lStats_obj = event.messageData;

		this._fPlayer_obj.kills = lStats_obj.kills;
		this._fPlayer_obj.rounds= lStats_obj.rounds;
		this._fPlayer_obj.place = lStats_obj.place;

		if (this._fLobbyView_sprt)
		{
			this.lobbyUI.refreshIndicatorsView();
		}
		else
		{
			this.emit(LobbyScreen.EVENT_ON_PLAYER_STATS_ON_SCREEN_UPDATED);
		}
	}

	_onLobbyTimeUpdatedMessage(event)
	{
		APP.commonPanelController.onTimeSyncResponseReceived(event.messageData);
	}

	_onServerStartGameUrlResponseMessage(event)
	{
		this._fStartGameUrlRequireSent_bln = false;
		this._fSwitchToLobbyReject_bln = false;

		if (this._fIsResitOut_bl)
		{
			let gameUrl = APP.appParamsInfo.gamePath + (typeof event.messageData === "object" ? event.messageData.startGameUrl : event.messageData);
			this.emit(LobbyScreen.EVENT_ON_PSEUDO_GAME_URL_READY, {pseudoGameParams:parseGet(gameUrl)});
		}
		else if (this._recoveryGameUrlUpdateRequired 
				|| !APP.battlegroundController.info.isBattlegroundMode() && this._fIsNeedRequestStartGameUrlAfterLobbySocketReconnetion_bl)
		{
			this._recoveryGameUrlUpdateRequired = false;
			let gameStartUrl = event.messageData.startGameUrl;
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.GAME_URL_UPDATED, {gameUrl: gameStartUrl, isBattleground: false});
		}
		else if (
					APP.battlegroundController.info.isBattlegroundMode() 
					&& 
					(
						APP.battlegroundController.info.isGameStartUrlOnContinueWaitingUpdateExpected 
						|| 
						APP.battlegroundController.info.isGameStartUrlOnPlayAgainUpdateExpected
						||
						this._fIsNeedRequestStartGameUrlAfterLobbySocketReconnetion_bl
					)
					
				)
		{
			let gameStartUrl = event.messageData.startGameUrl;
			this.emit(LobbyScreen.EVENT_ON_BATTLEGROUND_START_URL_UPDATED);
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.GAME_URL_UPDATED, {gameUrl: gameStartUrl, isBattleground: true, isNeedReconnectOnChange: true});
		}
		else
		{
			this.openGameClientUrl(event.messageData);
		}

		this._fIsNeedRequestStartGameUrlAfterLobbySocketReconnetion_bl = null;
	}

	onServerErrorMessage(event)
	{
		this._handleErrorCode(event.messageData, event.requestData, event.errorType);
	}

	_handleErrorCode(serverData, requestData, errorType)
	{
		switch (serverData.code)
		{
			case LobbyWebSocketInteractionController.ERROR_CODES.NOT_LOGGED_IN:
			case LobbyWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
				this._hideLobby();
				break;
			case LobbyWebSocketInteractionController.ERROR_CODES.TOO_MANY_OBSERVERS:
				if (APP.FRBController.info.isActivated || APP.lobbyBonusController.info.isActivated || APP.tournamentModeController.info.isTournamentMode)
				{
					break;
				}
			case LobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_FOUND:
			case LobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN:
				this._recoveryGameUrlUpdateRequired = false;
				this.cancelLasthand();
				break;
			case LobbyWebSocketInteractionController.ERROR_CODES.ILLEGAL_NICKNAME:
				this._onNicknameDenied();
				break;
			case LobbyWebSocketInteractionController.ERROR_CODES.NICKNAME_NOT_AVAILABLE:
				this._onNicknameNotConfirmed();
				break;
			case LobbyWebSocketInteractionController.ERROR_CODES.AVATAR_PART_NOT_AVAILABLE:
				this._onAvatarDenied();
				break;
		}
	}

	onServerOkMessage(event)
	{
		let requestClass = undefined;
		let requestData = event.requestData;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}

		if (requestClass !== undefined)
		{
			switch(requestClass)
			{
				case CLIENT_MESSAGES.CHECK_NICKNAME_AVAILABILITY:
					this._onNicknameConfirmed();
					break;
				case CLIENT_MESSAGES.CHANGE_NICKNAME:
					this._onNicknameConfirmed();
					this._onNicknameAccepted();
					break;
				case CLIENT_MESSAGES.CHANGE_AVATAR:
					this._onAvatarAccepted();
					break;
			}
		}
	}

	_onLobbyServerConnectionClosed(event)
	{
		this._resetPendingOperationRecoveryGameUrlInfo();

		if (event.wasClean)
		{
			return;
		}

		this._removeProfileListeners();

		this._hideLobby();
	}
	//...SERVER INTERACTION

	//RETURN TO GAME...
	_onReturnToGameConfirmed(event)
	{
		this._resitInRoom();
	}

	_resitInRoom()
	{
		this.emit(LobbyScreen.EVENT_ON_GAME_LAUNCH_INITIATED);

		if (APP.battlegroundController.info.isBattlegroundMode() && APP.battlegroundController.info.isBattlegroundGameStarted)
		{
			this.requestBattlegroundStartGameUrl();
		}
		else
		{
			this.requestStartGameUrl(false);
		}
	}

	_onFrbLobbyIntroConfirmed(event)
	{
		if (this.alreadySeatRoomId >= 0)
		{
			this._resitInRoom();
		}
		else if (this._fFRBStake_num !== undefined)
		{
			if (!this.gameInitiated)
			{
				this.emit(LobbyScreen.EVENT_ON_GAME_LAUNCH_INITIATED);
			}
			this.requestStartGameUrl(false, this._fFRBStake_num);
		}
	}

	_onBonusEnterRoomRequired()
	{
		if (!this.gameInitiated)
		{
			this.emit(LobbyScreen.EVENT_ON_GAME_LAUNCH_INITIATED);
		}
		this.requestStartGameUrl(false);
	}

	_onTournamentEnterRoomRequired()
	{
		if (!this.gameInitiated)
		{
			this.emit(LobbyScreen.EVENT_ON_GAME_LAUNCH_INITIATED);
		}
		this.requestStartGameUrl(false);
	}

	_onFRBRestartRequired(event)
	{
		this._fStartGameUrlRequireSent_bln = false;
		this._recoveryGameUrlUpdateRequired = false;
	}

	_onReturnToGameDenied(event)
	{
		this._fIsResitOut_bl = true;

		let wsInteractionController = APP.pseudoGamewebSocketInteractionController;
		wsInteractionController.on(PseudoGameWebSocketInteractionController.EVENT_ON_CONNECTION_RECOVERY_STARTED, this._onPseudoGameRecoveryOnConnectionLostStarted, this);
		wsInteractionController.on(PseudoGameWebSocketInteractionController.EVENT_ON_CONNECTION_CLOSED, this._onResitOutRoomCompleted, this);
		wsInteractionController.on(PseudoGameWebSocketInteractionController.EVENT_ON_COMPENSATION_REQUIRED, this.emit, this);

		this.requestStartGameUrl(false);
	}

	_onResitOutRoomCompleted(event)
	{
		this._fIsResitOut_bl = false;

		let wsInteractionController = APP.pseudoGamewebSocketInteractionController;
		wsInteractionController.off(PseudoGameWebSocketInteractionController.EVENT_ON_CONNECTION_RECOVERY_STARTED, this._onPseudoGameRecoveryOnConnectionLostStarted, this);
		wsInteractionController.off(PseudoGameWebSocketInteractionController.EVENT_ON_CONNECTION_CLOSED, this._onResitOutRoomCompleted, this);
		wsInteractionController.off(PseudoGameWebSocketInteractionController.EVENT_ON_COMPENSATION_REQUIRED, this.emit, this);

		this._showLobby();
		this.cancelLasthand();

		this.emit(LobbyScreen.EVENT_ON_LAST_ROOM_RESITOUT_COMPLETED);
	}

	_onPseudoGameRecoveryOnConnectionLostStarted(event)
	{
		this._recoveryGameUrlUpdateRequired = true;
		this.requestStartGameUrl(true);
	}
	//...RETURN TO GAME

	cancelLasthand()
	{
		this._fIsReconnect_bl = false;
		this.alreadySeatRoomId = -1;
	}

	get isRoomLasthand()
	{
		return this._fIsReconnect_bl;
	}

	openGameClientUrl(data)
	{
		let gameUrl = data;
		if (typeof data === "object")
		{
			gameUrl = data.startGameUrl;
		}
		else
		{
			data = {class: SERVER_MESSAGES.GET_START_GAME_URL_RESPONSE};
		}

		gameUrl = APP.appParamsInfo.gamePath + gameUrl;

		if (this._playerController.info.currentStake !== undefined)
		{
			gameUrl += "&playerStake=" + this._playerController.info.currentStake;
		}

		if (this.gameInitiated)
		{
			if (this._fIsNeedRequestStartGameUrlAfterLobbySocketReconnetion_bl)
			{
				if (APP.battlegroundController.info.isBattlegroundMode() && APP.battlegroundController.info.isBattlegroundGameStarted)
				{
					this.requestBattlegroundStartGameUrl();
				}
				else
				{
					this.requestStartGameUrl(true, undefined, this._fRoomIdNeedForRequestStartGameUrlAfterLobbySocketReconnetion_num);
				}
			}
			else
			{
				this.emit(LobbyScreen.EVENT_ON_LOBBY_BACK_TO_GAME, {startGameUrl: gameUrl});
				let delay = 300;
				APP.layout.showGamesScreen(delay);
			}
		}
		else
		{
			let lLastAlreadySeatRoomId = this.alreadySeatRoomId;
			this._fIsReconnect_bl = false;
			this.alreadySeatRoomId = -1;
			this.gameInitiated = true;
			this.emit(LobbyScreen.EVENT_ON_GAME_URL_READY, {startGameUrl: gameUrl, alreadySeatRoomId: lLastAlreadySeatRoomId});
			this.visible = false;
		}
	}

	enterLobbyResponse(data)
	{
		this._fPlayer_obj.currency = data.currency;

		if (data.currency)
		{
			APP.currencyInfo.i_setCurrencyId(data.currency.code);
		}

		let nickName = data.nickname || "";
		let nickGlyphs = data.nicknameGlyphs || PlayerInfo.DEFAULT_NICK_GLYPHS;
		if (nickName.length > 0)
		{
			nickName = Utils.filterGlyphs(nickName, nickGlyphs, true);
		}
		this._fPlayer_obj.nickname = nickName;

		this._fPlayer_obj.avatar = {
			border: data.avatar.borderStyle,
			hero: data.avatar.hero,
			back: data.avatar.background
		};

		this._fPlayer_obj.kills = data.kills;
		this._fPlayer_obj.rounds = data.rounds;
		this._fPlayer_obj.place = data.place;

		this._fPlayer_obj.didThePlayerWinSWAlready = data.didThePlayerWinSWAlready || true // 0009982: MAX DUEL PIRATES - UPDATE TO NEW MATH (dialog when a player picks up a special weapon)
		this.emit(LobbyScreen.EVENT_ON_ALREADY_SW_WIN_INFO_UPDATED);

		let lDefaultFireSettings_obj = {
			lockOnTarget: true,
			targetPriority: 2,
			autoFire: true,
			fireSpeed: 2
		}

		this._fPlayer_obj.fireSettings = data.fireSettings || lDefaultFireSettings_obj // 0009982: MAX DUEL PIRATES - UPDATE TO NEW MATH (dialog when a player picks up a special weapon)

		if (!this._fFireSettingsInitiated_bln)
		{
			this._fFireSettingsInitiated_bln = true;
			this.emit(LobbyScreen.EVENT_ON_FIRE_SETTINGS_UPDATED);
		}

		this._fAvailableAvatarStyles_obj = {
			borders: data.borders,
			heroes: data.heroes,
			backs: data.backgrounds
		}

		this._fPlayer_obj.rankPoints = data.rankPoints;

		this.onPaytable(data);

		this._addProfileListeners();

		this.emit(LobbyScreen.EVENT_ON_PROFILE_STYLES_INIT, {
			availableStyles: this._fAvailableAvatarStyles_obj,
			userStyles: this._fPlayer_obj.avatar
		});

		this.alreadySeatRoomId = data.alreadySeatRoomId || data.roomId;
		this._fIsReconnect_bl = (APP.layout.isLobbyLayoutVisible || APP.gameLauncher.isSwitchToLobbyExpected) && (this.alreadySeatRoomId != -1); //note: if LobbyLayout is NOT visible - this means we're in the game room already, that's why in that case we don't need to consider reconnect/lasthand is true MQ-1181

		let lIsFRBActivated_bl = APP.FRBController.info.isActivated;
		if (!this._fIsReconnect_bl && APP.layout.isLobbyLayoutVisible && !lIsFRBActivated_bl)
		{
			this._showLobby();
		}

		if (data.frBonusInfo)
		{
			let lStake_num = data.frBonusInfo.stake ? data.frBonusInfo.stake : (data.stake ? data.stake : (data.stakes ? data.stakes[0] : undefined));
			this._fFRBStake_num = lIsFRBActivated_bl ? lStake_num : undefined;
		}

		if (this._fLobbyView_sprt)
		{
			this.lobbyUI.refreshIndicatorsView();
		}

		if (this._fStartGameUrlRequireSent_bln)
		{
			if (this._recoveryGameUrlUpdateRequired)
			{
				this.requestStartGameUrl(true, undefined, data.roomId);
			}
		}

		this._weaponsRequestSuspicion();

		this.onGetLobbyTimeRequired();

		if(data.battleground)
		{
			//START IMMEDIATELY IF START URL PRESENTED...
			let lStartGameURL_str = data.battleground.startGameUrl;
			if(lStartGameURL_str)
			{
				if (APP.pendingOperationController.info.isPendingOperationStatusCheckInProgress)
				{
					if (this.gameInitiated)
					{
						this._fAfterPendingOperationGameStartUrlInfo_obj = { func: this.requestBattlegroundStartGameUrl, params: undefined }
					}
					else
					{
						this._fAfterPendingOperationGameStartUrlInfo_obj = { func: this.openGameClientUrl, params: [{startGameUrl: lStartGameURL_str}] }
					}
				}
				else
				{
					this.openGameClientUrl( {startGameUrl: lStartGameURL_str} );
				}
			}
			//...START IMMEDIATELY IF START URL PRESENTED
		}
		else if (this._fIsNeedRequestStartGameUrlAfterLobbySocketReconnetion_bl) //if the lobby socket was not connected when the game socket connection was disconnected
		{
			this.requestStartGameUrl(true, undefined, data.roomId);
		}
	}

	_onBattlegroundBuyInAllreadyDoneInTournamentlobby(event)
	{
		this.requestBattlegroundStartGameUrl();
	}

	_onBattlegroundNeedUpdateStartGameUrl(event)
	{
		this.requestBattlegroundStartGameUrl(event.buyIn);
	}

	_resetPendingOperationRecoveryGameUrlInfo()
	{
		delete this._fAfterPendingOperationGameStartUrlInfo_obj;

		this._fAfterPendingOperationGameStartUrlInfo_obj = null;
	}

	_onPendingOperationCompleted(aEvent_e)
	{
		if (this._fAfterPendingOperationGameStartUrlInfo_obj)
		{
			this._fAfterPendingOperationGameStartUrlInfo_obj.func.apply(this, this._fAfterPendingOperationGameStartUrlInfo_obj.params)

			this._resetPendingOperationRecoveryGameUrlInfo();
		}
	}

	onGetLobbyTimeRequired()
	{
		this.emit(LobbyScreen.EVENT_ON_GET_LOBBY_TIME_REQUIRED);
	}

	//LOBBY VIEW...
	_showLobby()
	{
		let lIsFRBActivated_bl = APP.FRBController.info.isActivated;
		let lIsEnterLobbyRequestinProgress_bl = APP.webSocketInteractionController.hasUnRespondedRequest(CLIENT_MESSAGES.ENTER)
												|| APP.webSocketInteractionController.hasDelayedRequests(CLIENT_MESSAGES.ENTER);
		
		if (lIsFRBActivated_bl || lIsEnterLobbyRequestinProgress_bl)
		{
			return;
		}

		this.lobbyUI.updateLobbyData();
		this.lobbyUI.show();

		this.emit(LobbyScreen.EVENT_ON_LOBBY_SHOW);

		this._weaponsRequestSuspicion();
	}

	_hideLobby()
	{
		if (this._fLobbyView_sprt && this._fLobbyView_sprt.visible)
		{
			this.lobbyUI.hide();
		}

		this.cancelLasthand();

		this.emit(LobbyScreen.EVENT_ON_LOBBY_HIDE);
	}

	get lobbyUI()
	{
		return this._fLobbyView_sprt || (this._fLobbyView_sprt = this._initLobbyView());
	}

	get availableAvatarStyles()
	{
		return this._fAvailableAvatarStyles_obj;
	}

	_initLobbyView()
	{
		let l_sprt = this._fBaseContainer_sprt.addChild(new LobbyView(this._fPlayer_obj));
		l_sprt.on(LobbyView.EVENT_ON_LAUNCH_GAME_CLICKED, this.onLaunchGameBtnClicked, this);
		l_sprt.on(LobbyView.EVENT_ON_LOBBY_SETTINGS_CLICKED, this.emit, this);
		l_sprt.on(LobbyView.EVENT_ON_LOBBY_INFO_CLICKED, this.emit, this);
		l_sprt.on(LobbyView.EVENT_ON_LOBBY_PROFILE_CLICKED, this.emit, this);
		l_sprt.on(LobbyView.EVENT_ON_LOBBY_CASHIER_CLICKED, this.emit, this);
		l_sprt.on(LobbyView.EVENT_ON_PLAYER_STATS_UPDATED, this.emit, this);
		l_sprt.on(LobbyView.EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK, this.emit, this);

		l_sprt.init();
		l_sprt.visible = false;
		l_sprt.position.set(0, 11);
		return l_sprt;
	}
	//...LOBBY VIEW

	//PAYTABLE...
	onPaytable(data)
	{
		APP.secondaryScreenController.paytableScreenController.onPaytableResponse(data);
	}
	//...PAYTABLE

	//PROFILE...
	_addProfileListeners()
	{
		this._removeProfileListeners();

		let l_epsc = APP.secondaryScreenController.editProfileScreenController;

		l_epsc.on(EditProfileScreenController.EVENT_ON_AVATAR_CHANGE_REQUIRED, this._onProfileSettingUpdate, this);
		l_epsc.on(EditProfileScreenController.EVENT_ON_NICKNAME_CHANGE_REQUIRED, this._onNickNameApplyAttempt, this);
		l_epsc.on(EditProfileScreenController.EVENT_ON_NICKNAME_CHECK_REQUIRED, this._onCheckNicknameRequired, this);
	}

	_removeProfileListeners()
	{
		let l_epsc = APP.secondaryScreenController.editProfileScreenController;

		l_epsc.off(EditProfileScreenController.EVENT_ON_AVATAR_CHANGE_REQUIRED, this._onProfileSettingUpdate, this);
		l_epsc.off(EditProfileScreenController.EVENT_ON_NICKNAME_CHANGE_REQUIRED, this._onNickNameApplyAttempt, this);
		l_epsc.off(EditProfileScreenController.EVENT_ON_NICKNAME_CHECK_REQUIRED, this._onCheckNicknameRequired, this);
	}

	_onProfileSettingUpdate(data)
	{
		let settings = data.settings;

		this.emit(LobbyScreen.EVENT_ON_AVATAR_CHANGE_REQUIRED, {borderStyle: settings.border, hero: settings.hero, background: settings.back});
	}

	_onAvatarDenied()
	{
		this.emit(LobbyScreen.EVENT_ON_AVATAR_DENIED);
	}

	_onAvatarAccepted()
	{
		let lStyle_obj = APP.secondaryScreenController.editProfileScreenController.avatarStyle;

		this.emit(LobbyScreen.EVENT_ON_AVATAR_ACCEPTED);

		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.AVATAR_UPDATED, {
			borderStyle: lStyle_obj.border,
			hero: lStyle_obj.hero,
			background: lStyle_obj.back
		});
	}

	_onCheckNicknameRequired(data)
	{
		this.emit(LobbyScreen.EVENT_ON_NICKNAME_CHECK_REQUIRED, {nickname: data.nickname});
	}

	_onNicknameConfirmed()
	{
		this.emit(LobbyScreen.EVENT_ON_NICKNAME_AVAILABILITY_CAHNGED, {available: true});
	}

	_onNicknameNotConfirmed()
	{
		this.emit(LobbyScreen.EVENT_ON_NICKNAME_AVAILABILITY_CAHNGED, {available: false});
	}

	_onNicknameDenied()
	{
		this._fApplyingNickname_str = this._fPlayer_obj.nickname;

		this.emit(LobbyScreen.EVENT_ON_NICKNAME_AVAILABILITY_CAHNGED, {available: false});
		this.emit(LobbyScreen.EVENT_ON_NICKNAME_DENIED);
	}

	_onNickNameApplyAttempt(data)
	{
		this._fApplyingNickname_str = data.nickname;

		this.emit(LobbyScreen.EVENT_ON_NICKNAME_CHANGE_REQUIRED, {nickname: this._fApplyingNickname_str});
	}

	_onNicknameAccepted()
	{
		this._fPlayer_obj.nickname = this._fApplyingNickname_str;

		this.emit(LobbyScreen.EVENT_ON_NICKNAME_ACCEPTED);
	}
	//...PROFILE

	onScreenVisibleChange(isVisible)
	{
		this.isScreenVisible = isVisible;
	}

	onSecondaryScreenStateChange(state)
	{
		this.secondaryScreenState = state;
	}

	onLaunchGameBtnClicked(event)
	{
		this._fCurrentSelectedRoomStake_num = event.stake;
		this.emit(LobbyScreen.EVENT_ON_LAUNCH_GAME_CLICKED)

		if (!this.gameInitiated)
		{
			this.emit(LobbyScreen.EVENT_ON_GAME_LAUNCH_INITIATED);
		}

		APP.battlegroundController.info.setConfirmedBuyInCost(undefined);

		this.requestStartGameUrl(false, event.stake);
	}

	requestStartGameUrl(retryAfterConnectionRecovered = false, stake = undefined, aOptRoomId_num)
	{
		if (APP.pendingOperationController.info.isPendingOperationStatusCheckInProgress)
		{
			this._fAfterPendingOperationGameStartUrlInfo_obj = { func: this.requestStartGameUrl, params: arguments }
			return;
		}

		let data = {};

		if(stake !== undefined)
		{
			data.stake = stake
		}
		else if(this._playerController.info.currentStake !== undefined)
		{
			data.stake = this._playerController.info.currentStake;
		}
		else if(this._playerController.info.enterRoomStake !== undefined)
		{
			data.stake = this._playerController.info.enterRoomStake;
		}

		if (this.alreadySeatRoomId >= 0)
		{
			data.roomId = this.alreadySeatRoomId;
		}
		else if (aOptRoomId_num !== undefined && aOptRoomId_num >= 0)
		{
			data.roomId = aOptRoomId_num;
		}
		data.retryAfterConnectionRecovered = retryAfterConnectionRecovered;

		this._fStartGameUrlRequireSent_bln = true;
		this.emit(LobbyScreen.EVENT_ON_START_GAME_URL_REQUIRED, data);
	}

	requestBattlegroundStartGameUrl(aOptBuyIn_num=undefined)
	{
		if (APP.pendingOperationController.info.isPendingOperationStatusCheckInProgress)
		{
			this._fAfterPendingOperationGameStartUrlInfo_obj = { func: this.requestBattlegroundStartGameUrl, params: arguments }
			
			return;
		}
		
		let lParams_obj = {};
		if (!isNaN(aOptBuyIn_num))
		{
			lParams_obj.buyIn = aOptBuyIn_num;
		}
		this.emit(LobbyScreen.EVENT_ON_BATTLEGROUND_START_GAME_URL_REQUIRED, lParams_obj);
	}

	tick(delta)
	{
	}

	_tryToRequestStartGameUrlDependingOnMode(retryAfterConnectionRecovered = false, stake = undefined, aOptRoomId_num)
	{		
		if (!this._countConnectAttemptAndCheckRequestPossibility())
		{
			return;
		}

		if (APP.battlegroundController.info.isBattlegroundMode() && APP.battlegroundController.info.isBattlegroundGameStarted)
		{
			this.requestBattlegroundStartGameUrl();
		}
		else
		{
			this.requestStartGameUrl(retryAfterConnectionRecovered, stake, aOptRoomId_num);
		}
	}

	_countConnectAttemptAndCheckRequestPossibility()
	{
		if (this._fErrorGetStartGameUrlAttempts_num < ERROR_REQUESTS_START_GAME_URL_LIMIT)
		{
			if(this._doNotCountReconnectAttemptForStartGameUrl)
			{
				this._fErrorGetStartGameUrlAttempts_num = 0;
				this._doNotCountReconnectAttemptForStartGameUrl = false;
				return true;
			}else{
				this._fErrorGetStartGameUrlAttempts_num++;
				return true;
			}
		}
		{
			this._fErrorGetStartGameUrlAttempts_num = 0;
			this.emit(LobbyScreen.EVENT_ON_ERROR_ROOM_MOVED_REQUESTS_LIMIT_REACHED);
			return false;
		}
	}

	_onRequestsLimitDialogConfirmed(event)
	{
		this._tryToRequestStartGameUrlDependingOnMode();
	}

	_weaponsRequestSuspicion()
	{
		if (APP.isKeepSWModeActive)
		{
			this._sendWeaponsRequest();
		}
	}

	_sendWeaponsRequest()
	{
		this.emit(LobbyScreen.EVENT_ON_WEAPONS_REQUIRED);
	}

	_onWeaponsResponse(event)
	{
		let lWeapons_arr = event.messageData.weapons;
		this.lobbyUI.onWeaponsUpdated(lWeapons_arr);
	}
}

export default LobbyScreen;

function parseGet(aOptLocation_str = undefined)
{
	let get = {};

	let s = aOptLocation_str ? aOptLocation_str : window.location.toString();
	let p = s.indexOf("?");
	let tmp, params;
	if(p >= 0)
	{
		s = s.substr(p + 1, s.length);
		params = s.split("&");
		for(let i = 0; i < params.length; i++)
		{
			tmp = params[i].split("=");
			get[tmp[0]] = tmp[1];
		}
	}

	return get;
}