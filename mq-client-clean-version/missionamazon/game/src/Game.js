import Application from '../../../common/PIXI/src/dgphoenix/unified/controller/main/Application';
import SpineLibrary from '../../../common/PIXI/src/dgphoenix/unified/model/resources/SpineLibrary';
import GameScreen from './main/GameScreen';
import GameExternalCommunicator from './external/GameExternalCommunicator';
import {GAME_MESSAGES, LOBBY_MESSAGES} from './external/GameExternalCommunicator';
import GameSoundsController from './controller/sounds/GameSoundsController';
import SoundsBackgoundLoadingController from './controller/sounds/SoundsBackgoundLoadingController';
import GamePlayerController from './controller/custom/GamePlayerController';
import GameWebSocketInteractionController from './controller/interaction/server/GameWebSocketInteractionController';
import {CLIENT_MESSAGES} from './model/interaction/server/GameWebSocketInteractionInfo';
import GamePreloaderSoundButtonController from './controller/uis/custom/preloader/GamePreloaderSoundButtonController';
import Crate from './ui/Crate';
import BrowserSupportController from '../../../common/PIXI/src/dgphoenix/unified/controller/preloading/BrowserSupportController';
import LoaderUI from './external/GameLoaderUI';
import ProfilingInfo from '../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo';
import KeyboardControlProxy from '../../../common/PIXI/src/dgphoenix/unified/controller/interaction/keyboard/KeyboardControlProxy';
import GameProfilingController from './controller/profiling/GameProfilingController';
import GameDebuggingController from './controller/debug/GameDebuggingController';
import GameSettingsController from './controller/uis/custom/GameSettingsController';
import GameErrorHandlingController from './controller/error/GameErrorHandlingController';
import TournamentModeController from './controller/custom/tournament/TournamentModeController';
import PlayerInfo from '../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import GUSCurrencyInfo from '../../../common/PIXI/src/dgphoenix/gunified/model/currency/GUSCurrencyInfo';
import { APP } from '../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GamePendingOperationController from './controller/custom/GamePendingOperationController';
import CustomCurrencyInfo from './model/currency/CustomCurrencyInfo';

/**
 * @augments Application
 */
class Game extends Application
{
	static get EVENT_ON_SOUND_SETTINGS_CHANGED() 				{return "onSoundSettingsChanged";}
	static get EVENT_ON_AVATAR_UPDATED() 						{return "onAvatarUpdated";}
	static get EVENT_ON_PLAYER_INFO_UPDATED() 					{return "onPlayerInfoUpdated";}
	static get EVENT_ON_GAME_SECONDARY_SCREEN_ACTIVATED() 		{return "onGameSecondaryScreenActivated";}
	static get EVENT_ON_GAME_SECONDARY_SCREEN_DEACTIVATED() 	{return "onGameSecondaryScreenDeactivated";}
	static get EVENT_ON_CLOSE_ROOM()							{return GameScreen.EVENT_ON_CLOSE_ROOM;}
	static get EVENT_ON_LOBBY_VISIBILITY_CHANGED()				{return "onLobbyVisibilityChanged";}
	static get EVENT_ON_ASSETS_LOADING_ERROR()					{return "EVENT_ON_GAME_ASSETS_LOADING_ERROR";}
	static get EVENT_DEBUG_MESSAGE() 							{return "EVENT_DEBUG_MESSAGE";}
	static get EVENT_ON_GAME_SETTINGS_INITIALIZED() 			{return "EVENT_ON_GAME_SETTINGS_INITIALIZED";}
	static get EVENT_ON_BONUS_CANCEL_ROOM_RELOAD()				{return "EVENT_ON_BONUS_CANCEL_ROOM_RELOAD";}
	static get EVENT_ON_OFFLINE()								{return "EVENT_ON_OFFLINE";}
	static get EVENT_ON_ONLINE_RESTORED()						{return "EVENT_ON_ONLINE_RESTORED";}
	static get EVENT_ON_ROUND_TIME_UPDATE()                     {return "EVENT_ON_ROUND_TIME_UPDATE";}

	//override
	get isHeavenSpineUsageAllowed()
	{
		return true;
	}

	constructor(...args)
	{
		super(...args);

		this.currentWindow = null;
		this._gameScreen = null;

		this._mobileValidator = null;

		this._fTickHandle_arr = null;

		SpineLibrary.atlasScale = 2;
		this.spineLibrary = new SpineLibrary();

		this.soundsController = new GameSoundsController();

		this._fGamePlayerController_gpc = new GamePlayerController();

		this._fBrowserSupportController_bsc = new BrowserSupportController();

		this._fSoundsBgLoadingController_ssblc = null;
		this._fGameDebuggingController_ggc = null;

		this._fGameSettingsController_gsc = null;

		this._fActiveDialogs_obj = {};
		this._fIsAnyDialogActive_bl = false;
		this._fIsSecondaryScreenActive_bl = false;

		this._fIsBattlegroundMode_bl = null;
		this._fisConfirmBuyinDialogExpectedOnLastHand_bl = null;
	}

	//CURRENCY INFO...
	__isCurrencyInfoSupported()
	{
		return true;
	}

	__generateCurrencyInfo()
	{
		return new CustomCurrencyInfo();
	}
	//...CURRENCY INFO

	get isBattlegroundGamePlayMode()
	{
		return /*this.isBattlegroundGame && */this.gameScreen.isBattlegroundMode;
	}

	get isBattlegroundMode()
	{
		return this._fIsBattlegroundMode_bl;
	}

	set isBattlegroundMode(value)
	{
		this._fIsBattlegroundMode_bl = value;
	}

	get isConfirmBuyinDialogExpectedOnLastHand()
	{
		return this._fisConfirmBuyinDialogExpectedOnLastHand_bl;
	}

	set isConfirmBuyinDialogExpectedOnLastHand(value)
	{
		this._fisConfirmBuyinDialogExpectedOnLastHand_bl = value;
	}

	get browserSupportController()
	{
		return this._fBrowserSupportController_bsc;
	}

	get applicationFolderURL()
	{
		let appFolderURL = this.appParamsInfo.gamePath;
		if (!appFolderURL)
		{
			appFolderURL = super.applicationFolderURL;
		}
		return appFolderURL;
	}

	get mobileValidator()
	{
		return this._mobileValidator;
	}

	set mobileValidator(validator)
	{
		this._mobileValidator = validator;
	}

	get playerController()
	{
		return this._fGamePlayerController_gpc;
	}

	get tournamentModeController()
	{
		return this._fTournamentModeController;
	}

	get isMobile()
	{
		let lPlatformInfo_obj = window.getPlatformInfo ? window.getPlatformInfo() : null;
		let lIsMobile_bln = lPlatformInfo_obj.mobile;

		if (this._mobileValidator)
		{
			return Boolean(this._mobileValidator.mobile() || this._mobileValidator.tablet());
		}
		else if (lPlatformInfo_obj)
		{
			return Boolean(
							lPlatformInfo_obj.mobile
							|| (
									(
										lPlatformInfo_obj.name === "Safari"
										|| navigator.platform === 'MacIntel'
									)
									&& (navigator.maxTouchPoints > 0)
								)
							);
		}
	}

	get isDeprecetedInternetExplorer()
	{
		let lUserAgent_str = window.navigator.userAgent;
		let lTrident_num = lUserAgent_str.indexOf('Trident/'); // IE 11 (Trident Engine)
		return (lTrident_num > 0) ? true : false
	}

	onApplicationReady()
	{
		this._fBrowserSupportController_bsc.init();

		this.once(Game.EVENT_ON_SOUND_SETTINGS_CHANGED, () => {this.emit(Game.EVENT_ON_SOUND_CONTEXT_NEED_TO_BE_CHECKED)});

		this.soundsController.init(this.config.audio.stereo, this._fBrowserSupportController_bsc.i_getInfo().isSoundEnabled);

		this.externalCommunicator = new GameExternalCommunicator();
		this.externalCommunicator.addTargetWindow(window.parent);

		this.externalCommunicator.subscribeMessagesReceive();
		this.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this.onLobbyMessageReceived.bind(this));

		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.APPLICATION_READY);

		this.errorHandingController = new GameErrorHandlingController();
		this.errorHandingController.init();

		this._fGamePlayerController_gpc.init();
		this._fGamePlayerController_gpc.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this.playerInfoUpdated, this);

		this._fTournamentModeController = new TournamentModeController();
		this._fTournamentModeController.init();

		this._initPendingOperationController();

		this.keyboardControlProxy.on(KeyboardControlProxy.i_EVENT_BUTTON_CLICKED, this._onKeyboardButtonClicked, this);

		this._initGameDebuggingController();
		this.gameSettingsController; // for initialization

		this.on("createLoaderUI", this._onLoaderUICreated, this);

		super.onApplicationReady();
	}

	_onLoaderUICreated()
	{
		this.loaderUI.preloaderSoundButtonController.on(GamePreloaderSoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this.onSoundOnClicked, this);
		this.loaderUI.preloaderSoundButtonController.on(GamePreloaderSoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this.onSoundOffClicked, this);
	}

	//override
	get _unnecessaryPreloaderAssets()
	{
		return ['preloader/loading_bar/back', 'preloader/loading_bar/fill', 'TALobbyPreloaderLogo',
				'preloader/teasers/teaser_0', 'preloader/teasers/teaser_1','preloader/teasers/teaser_0_btg','preloader/teasers/teaser_1_btg','preloader/teasers/teaser_2_btg',
				'preloader/play_now_button_base_enabled', 'preloader/play_now_button_base_disabled', 'preloader/play_now_button_base_selected',
				'TALobbyPreloaderLogo', 'TAPreloaderBrand'];
	}

	/*showPreloader()
	{
		super.showPreloader();
	}

	playPreloaderSounds()
	{
	}*/

	playerInfoUpdated(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.PLAYER_INFO_UPDATED, {data: event.data});
	}

	onLoaderUIReady()
	{
		super.onLoaderUIReady();
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.PRELOADER_READY);
	}

	initSoundsBackgroundLoading()
	{
		this.soundsBgLoadingController.init();
	}

	get soundsBgLoadingController()
	{
		let lSoundsBgLoadingController_ssblc = this._fSoundsBgLoadingController_ssblc;
		if (!lSoundsBgLoadingController_ssblc)
		{
			lSoundsBgLoadingController_ssblc = new SoundsBackgoundLoadingController();
		}

		return lSoundsBgLoadingController_ssblc;
	}

	run()
	{
		if (!this.preloaderStage)
		{
			return;
		}

		super.run();

		this.removePreloader();

		this.on("contextmenu", this._onContextMenu, this);
	}

	_onWebglContextLost(event)
	{
		super._onWebglContextLost(event);

		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.WEBGL_CONTEXT_LOST);
	}

	_onContextMenu(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.CONTEXT_MENU);
	}

	onPreloadComplete()
	{
		if (this.tournamentModeController.info.isTournamentOnClientCompletedState)
		{
			return;
		}

		if (!this.isMobile)
		{
			this._fTickHandle_arr = [
				this._onTexturesInited.bind(this),
				Crate.initMaskedTextures
			];
		}
		else
		{
			this._onTexturesInited();
		}
	}

	_onTexturesInited()
	{
		if (this.browserSupportController.info.isAudioContextSuspended || APP.mobileValidator.ios() )
		{
			this.loaderUI.showClickToStart();
			this.loaderUI.once(LoaderUI.EVENT_ON_CLICK_TO_START_CLICKED, this.onPreloaderClickToStart, this);
		}
		else
		{
			this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_START_CLICKED);
			this.startServerInteraction();
		}
	}

	onPreloaderClickToStart()
	{
		if (this.tournamentModeController.info.isTournamentOnClientCompletedState)
		{
			return;
		}

		if (this.isBattlegroundMode)
		{
			this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_START_CLICKED);
		}

		this.browserSupportController.unlockContext();
		this.startServerInteraction();
	}

	startServerInteraction(aOptRoomUrl_str=undefined)
	{
		if (this.tournamentModeController.info.isTournamentOnClientCompletedState)
		{
			return;
		}

		this.initWebSocket();
		this._startHandlingServerMessages();

		this.startGame(aOptRoomUrl_str);

		//window.APP = this;
	}

	_generateWebSocketInteractionInstance()
	{
		return new GameWebSocketInteractionController();
	}

	_startHandlingServerMessages()
	{
		let wsInteractionController = this.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_GET_ROOM_INFO_RESPONSE_MESSAGE, this.onGetRoomInfoResponseMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onServerConnectionOpened, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_CONNECTION_RECOVERY_STARTED, this._onServerConnectionRecoveryStarted, this);
	}

	closeCurrentWindow()
	{
		if (this.currentWindow)
		{
			if (this.currentWindow.destroy) this.currentWindow.destroy();
			else this.stage.view.removeChild(this.currentWindow);

			this.currentWindow = null;
		}
	}

	showWindow(view)
	{
		this.closeCurrentWindow();
		this.stage.view.addChild(view);
		this.currentWindow = view;
	}

	startGame(aOptRoomUrl_str=undefined)
	{
		this.showWindow(this.gameScreen);
		this.currentWindow.init(aOptRoomUrl_str);

		this.currentWindow.gameStateController.on(GameScreen.EVENT_ON_GAME_ROUND_STATE_CHANGED, this.onGameRoundStateChanged, this);
		this.soundsController.initListeners(this.currentWindow);

		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_STARTED);
		this.emit("onGameStarted");
	}

	get gameScreen()
	{
		return this._gameScreen || (this._gameScreen = this._initGameScreen());
	}

	handleOffline()
	{
		this.setTickerPaused(true, "connection");
		this.emit(Game.EVENT_ON_OFFLINE);
	}

	handleOnline()
	{
		this.emit(Game.EVENT_ON_ONLINE_RESTORED);
		this.webSocketInteractionController.once(GameWebSocketInteractionController.EVENT_ON_RESTORE_AFTER_OFFLINE_FINISHED, () => {
			this.setTickerPaused(false, "connection");
		}, this);
	}

	_initGameScreen()
	{
		let gameScreen = new GameScreen();

		gameScreen.on(GameScreen.EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED, this.onBackToLobby, this);
		gameScreen.on(GameScreen.EVENT_SOUND_ON_BUTTON_CLICKED, this.onSoundOnClicked, this);
		gameScreen.on(GameScreen.EVENT_SOUND_OFF_BUTTON_CLICKED, this.onSoundOffClicked, this);
		gameScreen.on(GameScreen.EVENT_REFRESH_COMMON_PANEL_REQUIRED, this.onRefreshCommonPanelRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_GAME_RESTORED_AFTER_UNSEASONABLE_REQUEST, this._onRestoredAfterUnreasonableRequest, this);
		gameScreen.on(GameScreen.EVENT_ON_GAME_RESTORE_AFTER_UNSEASONABLE_REQUEST_CANCELED, this._onRestoreAfterUnreasonableRequestCanceled, this);
		gameScreen.on(GameScreen.EVENT_ON_SIT_OUT_REQUIRED, this._onSitOutRequired, this);
		gameScreen.on(GameScreen.EVENT_NOT_ENOUGH_MONEY_DIALOG_REQUIRED, this._onNotEnoughMoneyDialogRequired, this);
		gameScreen.on(GameScreen.EVENT_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_REQUIRED, this._onSWPurchaseLimitExceededDialogRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_FORCE_SIT_OUT_REQUIRED, this._onForceSitOutRequired, this);
		gameScreen.on(GameScreen.EVENT_MID_ROUND_COMPENSATE_SW_REQUIRED, this._onMidRoundCompensateSWRequired, this);
		gameScreen.on(GameScreen.EVENT_ROUND_RESULT_RETURN_SW_RESPONSE, this._onRoundResultReturnSWResponse, this);
		gameScreen.on(GameScreen.EVENT_ROUND_RESULT_RETURN_SW_DIALOG_SHOW_VALIDATE, this._onRoundResultReturnSWDialogValidate, this);
		gameScreen.on(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED, this._onRoundResultScreenDeactivated, this);
		gameScreen.on(GameScreen.EVENT_ON_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME, this._onPicksUpSpecialWeaponsFirstTime, this);
		gameScreen.on(GameScreen.EVENT_MID_ROUND_EXIT_REQUIRED, this._onMidRoundExitRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this._onRoundResultScreenActivated, this);
		gameScreen.on(GameScreen.EVENT_ON_CLOSE_ROOM, this.emit, this);
		gameScreen.on(GameScreen.EVENT_ON_NO_EMPTY_SEATS, this._onNoPlaceToSeatHandler, this);

		return gameScreen;
	}

	//DEBUG...
	get gameDebuggingController()
	{
		return this._fGameDebuggingController_ggc || (this._fGameDebuggingController_ggc = this._initGameDebuggingController());
	}

	_initGameDebuggingController()
	{
		let l_ggc = new GameDebuggingController();
		this._fGameDebuggingController_ggc = l_ggc;
		l_ggc.i_init();
		return l_ggc;
	}
	//...DEBUG

	get gameSettingsController()
	{
		return this._fGameSettingsController_gsc || (this._fGameSettingsController_gsc = this._initGameSettingsController());
	}

	_initGameSettingsController()
	{
		let l_gsc = new GameSettingsController();
		l_gsc.i_init();
		this._fGameSettingsController_gsc = l_gsc;

		return l_gsc;
	}

	handleAssetsLoadingError(errorKey, errorMessage)
	{
		let messageData = {};
		messageData.key = errorKey;
		messageData.message = errorMessage;

		this.emit(Game.EVENT_ON_ASSETS_LOADING_ERROR, {data:messageData});

		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_LOADING_ERROR, {data:messageData});
		APP.logger.i_pushError(`Game. Assets Loading Error! ${errorKey}: ${errorMessage} `);
	}

	onLobbyMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case LOBBY_MESSAGES.UPDATE_BATTLEGROUND_MODE:
				this.isBattlegroundMode = event.data.isBattlegroundMode;
				this.isConfirmBuyinDialogExpectedOnLastHand = event.data.isConfirmBuyinDialogExpectedOnLastHand;
				break;

			case LOBBY_MESSAGES.BACK_TO_GAME:
				this.resume(event.data);
				break;
			case LOBBY_MESSAGES.SOUND_SETTINGS_CHANGED:
				this.emit(Game.EVENT_ON_SOUND_SETTINGS_CHANGED, event.data);
				break;
			case LOBBY_MESSAGES.PLAYER_INFO_UPDATED:
				if (event.data.balance != undefined)
				{
					if (APP.isWebSocketInteractionInitiated && APP.webSocketInteractionController.isSitoutRequestInProgress)
					{
						delete event.data.balance;
					}
				}

				if (event.data.data[PlayerInfo.KEY_CURRENCY_CODE])
				{
					this.currencyInfo.i_setCurrencyId(event.data.data[PlayerInfo.KEY_CURRENCY_CODE].value);
				}

				this.emit(Game.EVENT_ON_PLAYER_INFO_UPDATED, event.data);
				break;
			case LOBBY_MESSAGES.AVATAR_UPDATED:
				this.emit(Game.EVENT_ON_AVATAR_UPDATED, event.data);
				break;
			case LOBBY_MESSAGES.SECONDARY_SCREEN_DEACTIVATED:
				this._fIsSecondaryScreenActive_bl = false;
				this.emit(Game.EVENT_ON_GAME_SECONDARY_SCREEN_DEACTIVATED);
				break;
			case LOBBY_MESSAGES.SECONDARY_SCREEN_ACTIVATED:
				this._fIsSecondaryScreenActive_bl = true;
				this.emit(Game.EVENT_ON_GAME_SECONDARY_SCREEN_ACTIVATED);
				break;
			case LOBBY_MESSAGES.LOBBY_VISIBILITY_CHANGED:
				this.emit(Game.EVENT_ON_LOBBY_VISIBILITY_CHANGED, event.data);
				break;
			case LOBBY_MESSAGES.CONTEXT_MENU:
				this.emit("lobbycontextmenu");
				break;
			case LOBBY_MESSAGES.LOBBY_LOADING_ERROR:
			case LOBBY_MESSAGES.WEBGL_CONTEXT_LOST:
				if (this._loaderUI && this._loaderUI.loader)
				{
					this._loaderUI.loader.stopLoading();
				}
				break;
			case LOBBY_MESSAGES.PROFILES_VALUES_INITIALIZATION:
				if (this.profilingController.info.profiles) return;
				this.i_onProfilesReady(event.data);
				console.log("%cvfxProfileValue = " + this.profilingController.info.vfxProfileValue, 'background: green; color: white; display: block');
				APP.logger.i_pushDebug(`Game. Profiles: ${JSON.stringify(this.profilingController.info.profiles)}`);
				/*console.log(this.profilingController.info.i_isProfileValueGreaterThan(
																		ProfilingInfo.i_VFX_LEVEL_PROFILE,
																		ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_VFX_LEVEL_PROFILE].LOW)
																	);*/
				break;
			case LOBBY_MESSAGES.DEBUG_MESSAGE:
				this.emit(Game.EVENT_DEBUG_MESSAGE, event.data);
				break;
			case LOBBY_MESSAGES.WEBGL_STATE_INFO:
				window.isWebGLSupported = event.data; //boolean
				break;
			case LOBBY_MESSAGES.GAME_SETTINGS_INITIALIZATION:
				this.emit(Game.EVENT_ON_GAME_SETTINGS_INITIALIZED, event.data);
				break;
			case LOBBY_MESSAGES.BONUS_CANCELED_DURING_LOAD:
				this.emit("EVENT_ON_BONUS_LOADING_CANCEL");
				if (this.currentWindow && this.currentWindow.subloadingController)
				{
					this.currentWindow.subloadingController.i_bonusLoadingCancelRequired();
				}
				break;
			case LOBBY_MESSAGES.BONUS_CANCEL_ROOM_RELOAD:
				this.emit(Game.EVENT_ON_BONUS_CANCEL_ROOM_RELOAD);
				break;

			case LOBBY_MESSAGES.DIALOG_ACTIVATED:
				this._updateDialogActiveState(event.data.dialogId, true);
				break;
			case LOBBY_MESSAGES.DIALOG_DEACTIVATED:
				this._updateDialogActiveState(event.data.dialogId, false);
				break;

		}
	}

	_updateDialogActiveState(aDialogId_num, aIsActive_bl)
	{
		aIsActive_bl == Boolean(aIsActive_bl);

		if (aDialogId_num !== undefined)
		{
			this._fActiveDialogs_obj[aDialogId_num] = aIsActive_bl;

			if (aIsActive_bl)
			{
				this._fIsAnyDialogActive_bl = true;
			}
			else
			{
				this._fIsAnyDialogActive_bl = false;

				for (var dlgId in this._fActiveDialogs_obj)
				{
					if (!!this._fActiveDialogs_obj[dlgId])
					{
						this._fIsAnyDialogActive_bl = true;
						break;
					}
					
				}
			}			
		}
	}

	get isAnyDialogActive()
	{
		return this._fIsAnyDialogActive_bl;
	}

	isDialogActive(aDialogId_int)
	{
		return(this._fActiveDialogs_obj[aDialogId_int]);
	}

	get isSecondaryScreenActive()
	{
		return this._fIsSecondaryScreenActive_bl;
	}

	onBackToLobby(aEvent_obj)
	{
		// console.log("[Game] onBackToLobby -> pause");
		this.pause();
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BACK_TO_LOBBY, {errorCodeReason: aEvent_obj.errorCodeReason});
	}

	onSoundOnClicked()
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_SOUND_BUTTON_CLICKED, {soundsOn: false});
	}

	onSoundOffClicked()
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_SOUND_BUTTON_CLICKED, {soundsOn: true});
	}

	onGameRoundStateChanged(e)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_ROUND_STATE_CHANGED, {state: e.value});
	}

	onRefreshCommonPanelRequired(e)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.REFRESH_COMMON_PANEL_REQUIRED, {data:e.data})
	}

	onGetRoomInfoResponseMessage(event)
	{
		this.run();
	}

	_onServerConnectionClosed(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.SERVER_CONNECTION_CLOSED, {wasClean: event.wasClean});
	}

	_onServerConnectionOpened(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.SERVER_CONNECTION_OPENED);
	}

	_onServerConnectionRecoveryStarted(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_RECOVERY_ON_CONNECTION_LOST_STARTED, {roomId: event.roomId});
	}

	_onKeyboardButtonClicked(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.KEYBOARD_BUTTON_CLICKED, {code: event.code});
	}

	_onPicksUpSpecialWeaponsFirstTime()
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME_REQUIRED);
	}

	_onMidRoundExitRequired(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.MID_ROUND_EXIT_REQUIRED, {roomId: event.roomId});
	}

	_onNoPlaceToSeatHandler(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ROOM_NO_EMPTY_PLACE_TO_SEAT);
	}

	_onServerErrorMessage(event)
	{
		// console.log("_onServerErrorMessage >> ", event);
		let serverData = event.messageData;
		let requestData = event.requestData;
		let requestClass = undefined;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}

		let externalMessageAllowed = true;
		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
				if (requestClass === CLIENT_MESSAGES.SIT_IN)
				{
					externalMessageAllowed = this.tickerAllowed;
				}
				break;
		}

		if (externalMessageAllowed)
		{
			this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED,
				{errorType: event.errorType, errorCode: event.messageData.code, errorTime: event.messageData.date, requestClass: requestClass, rid: event.messageData.rid, requestData: requestData});
		}
		APP.logger.i_pushError(`Internal error! ${JSON.stringify(event.messageData)}`);
	}

	_onRestoredAfterUnreasonableRequest(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.RESTORED_AFTER_UNREASONABLE_REQUEST);
	}

	_onRestoreAfterUnreasonableRequestCanceled(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.RESTORE_AFTER_UNREASONABLE_REQUEST_CANCELED);
	}

	_onSitOutRequired(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_SIT_OUT_REQUIRED);
		APP.logger.i_pushDebug(`Game. SitOut required.`);
	}

	_onMidRoundCompensateSWRequired(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.MID_ROUND_COMPENSATE_SW_REQUIRED, {compensateSpecialWeapons: event.compensateSpecialWeapons, totalReturnedSpecialWeapons: event.totalReturnedSpecialWeapons, roomId: event.roomId});
	}
	
	_onRoundResultReturnSWResponse(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ROUND_RESULT_RETURN_SW_RESPONSE, {weaponsReturned: event.weaponsReturned, weaponSurplus: event.weaponSurplus});
	}

	_onRoundResultReturnSWDialogValidate(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ROUND_RESULT_RETURN_SW_DIALOG_SHOW_VALIDATE);
	}

	_onRoundResultScreenDeactivated(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ROUND_RESULT_SCREEN_DEACTIVATED);
	}

	_onForceSitOutRequired(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.EVENT_ON_FORCE_SIT_OUT_REQUIRED, {roomId: event.roomId});
	}

	_onNotEnoughMoneyDialogRequired(event)
	{
		let data = {dialogType: event.dialogType};

		if (event.specBalance !== undefined)
		{
			data.specBalance = event.specBalance;
		}

		if (event.hasUnrespondedShots !== undefined)
		{
			data.hasUnrespondedShots = event.hasUnrespondedShots;
		}

		if (event.hasDelayedShots !== undefined)
		{
			data.hasDelayedShots = event.hasDelayedShots;
		}

		if (event.hasUnparsedShotResponse !== undefined)
		{
			data.hasUnparsedShotResponse = event.hasUnparsedShotResponse;
		}

		if (event.hasAwardedFreeSW !== undefined)
		{
			data.hasAwardedFreeSW = event.hasAwardedFreeSW;
		}
		
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.NOT_ENOUGH_MONEY_DIALOG_REQUIRED, data);
	}

	_onSWPurchaseLimitExceededDialogRequired()
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_REQUIRED);
	}

	_onRoundResultScreenActivated(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ROUND_RESULT_ACTIVATED);
	}

	//override
	_initProfilingController()
	{
		return new GameProfilingController();
	}

	pause()
	{
		this.setTickerPaused(true, "focus");
	}

	resume(data)
	{
		//do not resume if the ticker was not paused
		if (!this.tickerAllowed)
		{
			this.setTickerPaused(false, "focus", data);
		}
	}

	onTickerPaused()
	{
		super.onTickerPaused.call(this);
		if (this.currentWindow && this.currentWindow.onRoomPaused)
		{
			this.currentWindow.onRoomPaused();
		}
	}

	onTickerResumed(data = null)
	{
		super.onTickerResumed.call(this, data);
		if (this.currentWindow && this.currentWindow.updateRoomInfo)
		{
			this.currentWindow.updateRoomInfo(data);
		}
	}

	isGamePlayInProgress()
	{
		return this.currentWindow && this.currentWindow.isGamePlayInProgress();
	}

	tick(e)
	{
		super.tick(e);
		if (this.currentWindow && this.currentWindow.tick)
		{
			this.currentWindow.tick(e.delta, e.realDelta);
			this.emit("tick", {delta: e.delta}); //it needs for pixi particle emitter used in InstantKill effects (endless smoke in particular)
		}

		if (this._fTickHandle_arr && this._fTickHandle_arr.length)
		{
			let lHandle_fn = this._fTickHandle_arr.pop();
			lHandle_fn.call();
		}
	}

	//PENDING OPERATION...
	get pendingOperationController()
	{
		return this._fPendingOperationController_lpoc || (this._fPendingOperationController_lpoc = this._initPendingOperationController());
	}

	updateRoundTime(roundStartTime,roundEndTime)
    {
        this.emit(Game.EVENT_ON_ROUND_TIME_UPDATE, {roundStartTime:roundStartTime, roundEndTime:roundEndTime});
    }

	_initPendingOperationController()
	{
		let l_lpoc = new GamePendingOperationController();
		this._fPendingOperationController_lpoc = l_lpoc;

		l_lpoc.i_init();

		return l_lpoc;
	}
	//...PENDING OPERATION
}

export default Game;