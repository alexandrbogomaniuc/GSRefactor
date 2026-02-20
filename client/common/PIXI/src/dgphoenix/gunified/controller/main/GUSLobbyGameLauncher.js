import EventDispatcher from '../../../unified/controller/events/EventDispatcher';
import GUSLobbyScreen from '../../view/main/GUSLobbyScreen';
import GUSLobbyGameLaunchUI from '../../view/main/GUSLobbyGameLaunchUI';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES } from '../external/GUSLobbyExternalCommunicator';
import { APP } from '../../../unified/controller/main/globals';
import I18 from '../../../unified/controller/translations/I18';
import { SUPPORTED_ERROR_CODES } from '../../../unified/model/interaction/server/WebSocketInteractionInfo';
import GUSLobbyWebSocketInteractionController from '../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyLayout from '../../view/layout/GUSLobbyLayout';
import GUDialogController from '../uis/custom/dialogs/GUDialogController';

class GUSLobbyGameLauncher extends EventDispatcher
{
	static get EVENT_ON_GAME_ADDED()			{ return 'onGameAdded'; }
	static get EVENT_ON_GAME_CLOSED()			{ return 'onGameClosed'; }
	static get EVENT_ON_GAME_LOADING_ERROR()	{ return 'onGameLoadingError'; }

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

		this.initView(container);

		this._app.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_GAME_LAUNCH_INITIATED, this.onGameLaunchInitiated.bind(this));
		this._app.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_GAME_URL_READY, this.onGameURLReady.bind(this));
		
		APP.layout.on(GUSLobbyLayout.EVENT_ON_GAME_READY, this.onGameReady.bind(this));

		APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this.onGameMessageReceived, this);

		let forceSitOutDialogController = APP.dialogsController.forceSitOutDialogController;
		forceSitOutDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onForceSitOutDialogControllerDialogRequestConfirmed, this);
	}

	initView(container)
	{
		this._view = this.__provideGameLaunchUIInstance();

		container.addChild(this._view);
	}

	__provideGameLaunchUIInstance()
	{
		return new GUSLobbyGameLaunchUI(APP.preLoader);
	}

	onGameLaunchInitiated()
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

	onGameURLReady(event)
	{
		this.addGame(event.startGameUrl, event.alreadySeatRoomId);
	}

	onGameReady()
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
			gameUrl += `&gamePath=${APP.appParamsInfo.gamePath}`;
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

		gameUrl += `&commonPathForActionGames=${APP.appParamsInfo.commonPathForActionGames}`;

		if (aOptAlreadySeatRoomId_num >= 0)
		{
			gameUrl += `&ALREADY_SEAT_ROOM_ID=${aOptAlreadySeatRoomId_num}`;
		}

		this._currentGameUrl = gameUrl;
		let game = this._appLayout.addGame(gameUrl);

		this.emit(GUSLobbyGameLauncher.EVENT_ON_GAME_ADDED, { gameFrame: game.gameFrame });
	}

	onGameMessageReceived(event)
	{
		let msgType = event.type;
		let msgData = event.data;

		if (
			msgType !== GAME_MESSAGES.WEAPONS_UPDATED &&
			msgType !== GAME_MESSAGES.WEAPONS_INTERACTION_CHANGED &&
			msgType !== GAME_MESSAGES.WEAPON_SELECTED &&
			msgType !== GAME_MESSAGES.GAME_POINTER_DOWN &&
			msgType !== GAME_MESSAGES.PLAYER_INFO_UPDATED &&
			msgType !== GAME_MESSAGES.KEYBOARD_BUTTON_CLICKED &&
			msgType !== GAME_MESSAGES.REFRESH_COMMON_PANEL_REQUIRED
		)
		{
			//console.log("onGameMessageReceived", msgType);
		}

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

				this.emit(GUSLobbyGameLauncher.EVENT_ON_GAME_LOADING_ERROR, { key: event.data.key, message: event.data.message });
				break;

			case GAME_MESSAGES.ROOM_CLOSED:
				this._fIsSwitchToLobbyExpected_bl = msgData.switchToLobbyExpected;
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
				// fast fire lobby is no longer in use 
				//this.switchToLobby();
				this._goToHomeDirectly();
				break;

			case GAME_MESSAGES.GAME_STARTED:
				this._isGameLoadingInProgress = false;

				this._app.pause();
				break;
			case GAME_MESSAGES.IS_SHOW_SCREEN:
				this._appLayout.showCommonPanel();
				break;

			case GAME_MESSAGES.GAME_SOUND_BUTTON_CLICKED:
				this._app.soundSettingsController.setSoundsOn(event.data);
				break;

			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
				this._app.commonPanelController.turnGameUI(true);
				break;
		}
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

	_onForceSitOutDialogControllerDialogRequestConfirmed(event)
	{
		this._fIsSwitchToLobbyExpected_bl = true;
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
	}

	restartLobby()
	{
		this._view.show();
		this._lobbyContainer.hide();

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.once(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		this._app.restartLobby();
	}

	_onServerEnterLobbyMessage()
	{
		this._view.hide();
		this._lobbyContainer.show();
	}
}

export default GUSLobbyGameLauncher;