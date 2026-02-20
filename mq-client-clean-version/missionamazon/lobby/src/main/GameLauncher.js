import EventDispatcher from '../../../../common/PIXI/src/dgphoenix/unified/controller/events/EventDispatcher';
import LobbyScreen from '../main/LobbyScreen';
import LobbyLayout from '../layout/LobbyLayout';
import GameLaunchUI from '../ui/GameLaunchUI';
import LobbyExternalCommunicator from '../external/LobbyExternalCommunicator';
import {GAME_MESSAGES} from '../external/LobbyExternalCommunicator';
import {APP} from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../controller/interaction/server/LobbyWebSocketInteractionController';
import {GAME_CLIENT_MESSAGES} from '../model/interaction/server/LobbyWebSocketInteractionInfo';
import I18 from '../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import LobbyAPP from '../LobbyAPP';
import { SUPPORTED_ERROR_CODES } from '../../../../common/PIXI/src/dgphoenix/unified/model/interaction/server/WebSocketInteractionInfo';
import DialogController from '../controller/uis/custom/dialogs/DialogController';

class GameLauncher extends EventDispatcher
{
	static get EVENT_ON_GAME_ADDED() { return 'onGameAdded'; }
	static get EVENT_ON_GAME_CLOSED() { return 'onGameClosed'; }
	static get EVENT_ON_GAME_LOADING_ERROR() { return 'onGameLoadingError'; }

	constructor(aLobbyContainer_sprt, container)
	{
		super();

		this._app = APP;
		this._lobbyContainer = aLobbyContainer_sprt;
		this._appLayout = APP.layout;
		this._currentGameUrl = undefined;
		this._isGameLoadingInProgress = false;
		this._fIsGamePreloaderReady_bl = false;
		this._fIsSwitchToLobbyExpected_bl = false;
		this._fIsGameInitialStarted_bl = false;

		this.initView(container);

		this._app.lobbyScreen.on(LobbyScreen.EVENT_ON_GAME_LAUNCH_INITIATED, this.onGameLaunchInitiated.bind(this));
		this._app.lobbyScreen.on(LobbyScreen.EVENT_ON_GAME_URL_READY, this.onGameURLReady.bind(this));
		APP.layout.on(LobbyLayout.EVENT_ON_GAME_READY, this.onGameReady.bind(this));

		APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this.onGameMessageReceived.bind(this));

		let forceSitOutDialogController = APP.dialogsController.forceSitOutDialogController;
		forceSitOutDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onForceSitOutDialogControllerDialogRequestConfirmed, this);
	}

	initView(container)
	{
		this._view = new GameLaunchUI(APP.preLoader);

		container.addChild(this._view);
	}

	onGameLaunchInitiated(event)
	{
		this._view.show();
		this._lobbyContainer.hide();
		this._appLayout.hideCommonPanel();

		this._isGameLoadingInProgress = true;
	}

	get isGameLoadingInProgress()
	{
		return this._isGameLoadingInProgress;
	}

	set isGameLoadingInProgress(aVal_bln)
	{
		this._isGameLoadingInProgress = aVal_bln;
	}

	get isGamePreloaderReady()
	{
		return this._fIsGamePreloaderReady_bl;
	}

	get isSwitchToLobbyExpected()
	{
		return this._fIsSwitchToLobbyExpected_bl;
	}

	get isGameInitialStarted()
	{
		return this._fIsGameInitialStarted_bl;
	}

	onGameURLReady(event)
	{
		this.addGame(event.startGameUrl, event.alreadySeatRoomId);
	}

	onGameReady(event)
	{
		this._appLayout.addGamesScreen();
		this._appLayout.coverGamesScreenByLobby();
	}

	addGame(gameUrl, aOptAlreadySeatRoomId_num = -1)
	{
		// DEBUG...
		if (APP.isDebugMode)
		{
			let p = gameUrl.toString().indexOf("?");
			let url_params_str = gameUrl.substr(p + 1, gameUrl.length);

			let gameLangParam = `LANG=${I18.currentLocale}`;
			url_params_str = url_params_str.replace("LANG=en", gameLangParam);
			url_params_str = url_params_str.replace("lang=en", gameLangParam);

			let debugParamValue = APP.isDebugMode ? "true" : "false";
			let debugParam = `&DEBUG=${debugParamValue}`;

			gameUrl = 'http://localhost:8081/?' + url_params_str + debugParam;
		}

		let errorHandlingParamValue = APP.isErrorHandlingMode ? "true" : "false";
		let errorHandlingParam = `&ERROR_HANDLING=${errorHandlingParamValue}`;
		gameUrl += errorHandlingParam;

		let debugMsgValue = APP.isDebugsrmsg ? "true" : "false";
		let debugMsgParam = `&DEBUGSRMSG=${debugMsgValue}`;
		gameUrl += debugMsgParam;
		// ...DEBUG

		let lTournamentModeInfo_tmi = APP.tournamentModeController.info;
		if (lTournamentModeInfo_tmi.isTournamentMode)
		{
			gameUrl += `&TOURNAMENT_MODE=true`;
			gameUrl += `&TOURNAMENT_STATE=${lTournamentModeInfo_tmi.tournamentState}`;
		}

		if (aOptAlreadySeatRoomId_num >= 0)
		{
			gameUrl += `&ALREADY_SEAT_ROOM_ID=${aOptAlreadySeatRoomId_num}`;
		}

		this._currentGameUrl = gameUrl;
		let game = this._appLayout.addGame(gameUrl);

		this.emit(GameLauncher.EVENT_ON_GAME_ADDED, {gameFrame: game.gameFrame});
	}

	onGameMessageReceived(event)
	{
		let msgType = event.type;
		let msgData = event.data;

		switch (msgType)
		{
			case GAME_MESSAGES.APPLICATION_READY:
				this._app.dispatchInitialMessages();
				break;

			case GAME_MESSAGES.PRELOADER_READY:
				this._isGameLoadingInProgress = true;
				this._fIsGamePreloaderReady_bl = true;

				this._appLayout.showGamesScreen();
				this._lobbyContainer.show();
				
				this._view.hide();
				break;

			case GAME_MESSAGES.GAME_LOADING_ERROR:
				this._view.hide();

				this.emit(GameLauncher.EVENT_ON_GAME_LOADING_ERROR, {key:event.data.key, message: event.data.message});
				break;

			case GAME_MESSAGES.BACK_TO_LOBBY:
				let lErrorCodeReason_num = msgData.errorCodeReason;
				if (
						(APP.FRBController.info.isActivated || APP.lobbyBonusController.info.isActivated || APP.tournamentModeController.info.isTournamentMode)
						&& lErrorCodeReason_num == SUPPORTED_ERROR_CODES.TOO_MANY_OBSERVERS
					)
				{
					break;
				}
				// no more lobby in FastFire games 
				//this.switchToLobby();
				this._goToHomeDirectly();
				break;

			case GAME_MESSAGES.ROOM_CLOSED:
				this._fIsSwitchToLobbyExpected_bl = msgData.switchToLobbyExpected;
				break;

			case GAME_MESSAGES.GAME_STARTED:
				this._isGameLoadingInProgress = false;
				this._fIsGameInitialStarted_bl = true;

				this._appLayout.showCommonPanel();
				this._app.pause();
				break;

			case GAME_MESSAGES.GAME_SOUND_BUTTON_CLICKED:
				this._app.soundSettingsController.setSoundsOn(event.data);
				break;

			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
				this._app.commonPanelController.turnGameUI(true);
				break;
				}
		}

	_onForceSitOutDialogControllerDialogRequestConfirmed(event)
	{
		this._fIsSwitchToLobbyExpected_bl = true;
	}

	_goToHomeDirectly(event)
	{
		APP.goToHome();
	}

	callWindowMethod (aMethodName_str, aParams_obj_arr)
	{
		var lRet_obj;
		try
		{
			lRet_obj = window[aMethodName_str].apply(window, aParams_obj_arr);
		}
		catch (a_obj)
		{
			throw new Error(`An error occured while trying to call JS Environment method: METHOD NAME = ${aMethodName_str}; PARAMS = ${aParams_obj_arr}`);
		}
		return lRet_obj;
	}

	switchToLobby()
	{
		this._fIsSwitchToLobbyExpected_bl = false;
		
		this._view.hide();
		this._lobbyContainer.show();

		this._app.resume();
		this._app.commonPanelController.turnGameUI(false);
		this._appLayout.coverGamesScreenByLobby();

		if (this._app.lobbyBonusController.info.isLobbyRestartRequired
			|| this._app.lobbyBonusController.info.isRoomRestartRequired
			|| this._app.FRBController.info.isLobbyRestartRequired
			|| this._app.FRBController.info.isRoomRestartRequired)
		{
			this.restartLobby();
		}

		APP.commonPanelController.onSwitchToLobby();
	}

	restartLobby()
	{
		this._view.show();
		this._lobbyContainer.hide();

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.once(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		this._app.restartLobby();
	}

	_onServerEnterLobbyMessage(event)
	{
		this._view.hide();
		this._lobbyContainer.show();
	}
}

export default GameLauncher;