import Application from '../../../unified/controller/main/Application';
import GUSCurrencyInfo from '../../model/currency/GUSCurrencyInfo';
import Sprite from '../../../unified/view/base/display/Sprite';
import GUSLobbySubloadingController from '../subloading/GUSLobbySubloadingController';
import GUSLobbySecondaryScreenController from '../uis/custom/secondary/GUSLobbySecondaryScreenController';
import GUSLobbySoundSettingsController from '../sounds/GUSLobbySoundSettingsController';
import SoundsController from '../../../unified/controller/sounds/SoundsController';
import BrowserSupportController from '../../../unified/controller/preloading/BrowserSupportController';
import CustomerspecController from '../../../unified/controller/preloading/CustomerspecController';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from '../external/GUSLobbyExternalCommunicator';
import GUSLobbySoundsBackgoundLoadingController from '../sounds/GUSLobbySoundsBackgoundLoadingController';
import GUSLobbyDebuggingController from '../debug/GUSLobbyDebuggingController';
import GUSLobbyErrorHandlingController from '../error/GUSLobbyErrorHandlingController';
import GUSLobbyVueApplicationController from '../vue/GUSLobbyVueApplicationController';
import GUSLobbyDialogsController from '../uis/custom/dialogs/GUSLobbyDialogsController';
import GUSLobbyTournamentModeController from '../custom/tournament/GUSLobbyTournamentModeController';
import GUSLobbyJSEnvironmentInteractionController from '../interaction/js/GUSLobbyJSEnvironmentInteractionController';
import GUSLobbyRedirectionController from '../interaction/browser/redirection/GUSLobbyRedirectionController';
import GUSLobbyBonusController from '../uis/custom/bonus/GUSLobbyBonusController';
import GUSLobbyFRBController from '../custom/frb/GUSLobbyFRBController';
import GUSLobbyCommonPanelController from '../uis/custom/commonpanel/GUSLobbyCommonPanelController';
import GUSLobbyTooltipsController from '../uis/custom/tooltips/GUSLobbyTooltipsController';
import GUSLobbyTutorialController from '../uis/custom/tutorial/GUSLobbyTutorialController';
import GUSLobbyScreen from '../../view/main/GUSLobbyScreen';
import GUSLobbyProfilingController from '../profiling/GUSLobbyProfilingController';
import GUSLobbyButtonsController from '../uis/custom/lobby_buttons/GUSLobbyButtonsController';
import GUSLobbyGameLauncher from './GUSLobbyGameLauncher';
import GUSLobbyLayout, { DOM_LAYERS } from '../../view/layout/GUSLobbyLayout';
import GUSLobbyPaytableScreenController from '../uis/custom/secondary/paytable/GUSLobbyPaytableScreenController';
import GUSLobbyWebSocketInteractionController from '../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyPlayerController from '../custom/GUSLobbyPlayerController';
import GUSLobbyStateController from '../state/GUSLobbyStateController';
import GUDialogController from '../uis/custom/dialogs/GUDialogController';
import KeyboardControlProxy from '../../../unified/controller/interaction/keyboard/KeyboardControlProxy';
import GUSLobbyCommonAssetsController from '../preloading/GUSLobbyCommonAssetsController';
import GUSLobbyBattlegroundController from '../custom/battleground/GUSLobbyBattlegroundController';
import GUSLobbyDialogsView from '../../view/uis/dialogs/GUSLobbyDialogsView';
import { APP } from '../../../unified/controller/main/globals';
import GUSLobbyPendingOperationController from '../gameplay/GUSLobbyPendingOperationController';

class GUSLobbyApplication extends Application
{
	static get EVENT_ON_LOBBY_STARTED() 					{return "onLobbyStarted";}
	static get EVENT_ON_SOUND_SETTINGS_CHANGED() 			{return "onSoundSettingsChanged";}
	static get EVENT_ON_PLAYER_INFO_UPDATED() 				{return "onPlayerInfoUpdated";}
	static get EVENT_ON_LOBBY_ASSETS_LOADING_ERROR() 		{return "onLobbyAssetsLoadingError";}
	static get EVENT_ON_LOBBY_RESTART_REQUIRED() 			{return "onLobbyRestartRequird"; }
	static get EVENT_ON_LOBBY_REFRESH_BALANCE_REQUIRED() 	{return "EVENT_ON_LOBBY_REFRESH_BALANCE_REQUIRED";}
	static get EVENT_ON_LOBBY_SCREEN_SHOWED()				{return "EVENT_ON_LOBBY_SCREEN_SHOWED";}
	static get EVENT_ON_LOBBY_UI_HIDDED() 					{return "EVENT_ON_LOBBY_UI_HIDDED";}
	static get EVENT_ON_LOBBY_UI_SHOWN() 					{return "EVENT_ON_LOBBY_UI_SHOWN";}
	static get EVENT_ON_SOME_BONUS_STATE_CHANGED() 			{return "EVENT_ON_SOME_BONUS_STATE_CHANGED";}
	static get EVENT_ON_ROOM_CLOSED() 						{return "onRoomClosed";}

	static get EVENT_ON_PROFILE_BUTTON_CLICKED()			{return GUSLobbyCommonPanelController.EVENT_EDIT_PROFILE_BUTTON_CLICKED;}
	static get EVENT_ON_SETTINGS_BUTTON_CLICKED()			{return GUSLobbyCommonPanelController.EVENT_SETTINGS_BUTTON_CLICKED;}
	static get EVENT_ON_INFO_BUTTON_CLICKED() 				{return GUSLobbyCommonPanelController.EVENT_INFO_BUTTON_CLICKED;}

	static get EVENT_ON_UPDATE_BATTLEGROUND_MODE()			{return "EVENT_ON_UPDATE_BATTLEGROUND_MODE";}

	static get EVENT_ON_OFFLINE()								{return "EVENT_ON_OFFLINE";}
	static get EVENT_ON_ONLINE_RESTORED()						{return "EVENT_ON_ONLINE_RESTORED";}

	constructor(...args)
	{
		super(...args);

		this._fIsMobilePlatform_bl = undefined;
		this._lobbyAppStarted = false;
		this.soundsLoadingInitiated = false;
		this._fBuyInFuncName_str = undefined;

		this._fLobbyScreen_sprt = null;
		this._fLobbyWaitScreen_sprt = null;

		this._pseudoGameWebSocketInteractionController = null;
		this._fSoundsBgLoadingController_ssblc = null;
		this._fLobbyDebuggingController_ldc = null;
		this._fLobbyErrorHandlingController_lehc = null;
		this._fVueApplicationController_vac = null;
		this._fTournamentModeController_tmc = null;
		this._jsEnvironmentInteractionController = null;
		this._redirectionController = null;
		this._fLobbyBonusController_lbc = null;
		this._fFRBController_frbc = null;
		this._fLobbyButtonsController_lbc = null;
		this._fBattlegroundController_bc = null;

		this.__generateContainers();

		if (this.__isSubloadingRequired)
		{
			this._fSubloadingController_sc = this.__provideSubloadingControllerInstance();
		};

		this._fSecondaryScreenController_ssc = this.__provideSecondaryScreenControllerInstance();
		this._fSoundSettingsController_ssc = this.__provideSoundSettingsControllerInstance();
		this._fSoundsController_lsc = this.__provideSoundsControllerInstance();

		this._fBrowserSupportController_bsc = this.__provideBrowserSupportControllerInstance();
		this._fCustomerspecController_cc = this.__provideCustomerspecControllerInstance();

		this._fLobbyStateController_lsc = this.__provideLobbyStateControllerInstance();
		this._fLobbyPlayerController_lpc = this.__provideLobbyPlayerControllerInstance();

		this.externalCommunicator = this.__provideExternalCommunicatorInstance();

		this._initVueApplicationController();
		this._initDialogsController();
		this._initErrorHandlingController();
		this._initTournamentModeController();
		this._initJSEnvironmentInteractionController();
		this._initRedirectionController();
		this._initLobbyBonusController();
		this._initFRBController();
		this._initCommonPanelController();
		
		if (this.isTutorialSupported)
		{
			this._initTutorialController();
		}
		else
		{
			this._initTooltipsController();
		}

		this._initPendingOperationController();

		this._initLobbyDebuggingController();
		this._initBattlegroundController();
	}

	parsePageParams()
	{
		if (this.appParamsInfo.buyInFuncName)
		{
			this._fBuyInFuncName_str = this.appParamsInfo.buyInFuncName;
		}
	}
	
	handleOffline()
	{
		this.emit(GUSLobbyApplication.EVENT_ON_OFFLINE);
	}

	handleOnline()
	{
		this.emit(GUSLobbyApplication.EVENT_ON_ONLINE_RESTORED);
	}

	//COMMON ASSETS...
	__provideCommonAssetsControllerInstance()
	{
		return new GUSLobbyCommonAssetsController();
	}
	//...COMMON ASSETS

	//RUN...
	run()
	{
		super.run();

		this._runLobby();

		this._lobbyAppStarted = true;
		this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED);
	}

	_runLobby()
	{
		this.parsePageParams();

		this.initWebSocket();
		this._startHandlingServerMessages();

		this._fSecondaryScreenController_ssc.init(this._fSecondaryContainer_sprt);
		this._fSecondaryScreenController_ssc.on(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this.onSecondaryScreenDeactivated, this);
		this._fSecondaryScreenController_ssc.on(GUSLobbySecondaryScreenController.EVENT_SCREEN_ACTIVATED, this.onSecondaryScreenActivated, this);

		this._fSecondaryScreenController_ssc.initView();

		if (this.isMobile)
		{
			let lPaytableScreenController_psc = this._fSecondaryScreenController_ssc.paytableScreenController;
			lPaytableScreenController_psc.on(GUSLobbyPaytableScreenController.EVENT_ON_SCREEN_SHOW, this._onMobilePaytableActivated, this);
			lPaytableScreenController_psc.on(GUSLobbyPaytableScreenController.EVENT_ON_SCREEN_HIDE, this._onMobilePaytableDeactivated, this);
		}

		this._fCommonPanelController_cpc.init();
		this._fCommonPanelController_cpc.initView(this.commonPanelStages);

		this.stage.view.addChild(this._fLobbyContainer_sprt);
		this.stage.view.addChild(this._fSecondaryContainer_sprt);

		this._fSecondaryContainer_sprt.visible = false;

		if (this.isTutorialSupported)
		{
			this.tutorialController.i_initViewOnStage(this._fTutorialStage_s);
		}

		this._jsEnvironmentInteractionController.init();
		this._redirectionController.init();

		this.startLobby();

		this.vueApplicationController.on(GUSLobbyVueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onTimeToShowVueLayer, this);
		this.vueApplicationController.on(GUSLobbyVueApplicationController.EVENT_TIME_TO_HIDE_VUE_LAYER, this._onTimeToHideVueLayer, this);

		this.on("contextmenu", this._onContextMenu, this);
	}

	get lobbyAppStarted()
	{
		return this._lobbyAppStarted;
	}
	//...RUN

	get isMobile()
	{
		if (this._fIsMobilePlatform_bl === undefined)
		{
			let lPlatformParams_obj = window["getPlatformInfo"] ? window["getPlatformInfo"].apply(window) || {} : {};
			if (
					lPlatformParams_obj["mobile"]
					|| (
							(
								lPlatformParams_obj["name"] === "Safari"
								|| navigator.platform === 'MacIntel'
							)
							&& navigator.maxTouchPoints > 0
						)
				)
			{
				this._fIsMobilePlatform_bl = lPlatformParams_obj["mobile"];
			}
			else
			{
				this._fIsMobilePlatform_bl = false;
			}
		}
		return this._fIsMobilePlatform_bl;
	}

	//LOBBY CONTAINERS...
	__generateContainers()
	{
		this._fLobbyContainer_sprt = this.__generateLobbyContainer();
		this._fSecondaryContainer_sprt = this.__generateSecondaryContainer();
	}

	__generateLobbyContainer()
	{
		return new Sprite();
	}

	__generateSecondaryContainer()
	{
		return new Sprite();
	}

	//WAIT SCREEN...
	get lobbyWaitScreen()
	{
		return this._fLobbyWaitScreen_sprt || (this._fLobbyWaitScreen_sprt = this._initLobbyWaitScreen());
	}

	_initLobbyWaitScreen()
	{
		return this._fLobbyContainer_sprt.addChild(this.__generateWaitScreenContainer());
	}

	__generateWaitScreenContainer()
	{
		return new Sprite();
	}
	//...WAIT SCREEN
	//LOBBY CONTAINERS...

	//CURRENCY INFO...
	__isCurrencyInfoSupported()
	{
		return true;
	}

	__generateCurrencyInfo()
	{
		return new GUSCurrencyInfo();
	}
	//...CURRENCY INFO

	//SUBLOADING...
	get __isSubloadingRequired()
	{
		return this.appParamsInfo.backgroundLoadingAllowed;
	}

	__provideSubloadingControllerInstance()
	{
		return new GUSLobbySubloadingController();
	}

	get subloadingController()
	{
		return this._fSubloadingController_sc;
	}
	//...SUBLOADING

	//SECONDARY SCREEN...
	get secondaryScreenController()
	{
		return this._fSecondaryScreenController_ssc;
	}

	__provideSecondaryScreenControllerInstance()
	{
		return new GUSLobbySecondaryScreenController();
	}
	//...SECONDARY SCREEN

	//SOUND SETTINGS...
	__provideSoundSettingsControllerInstance()
	{
		return new GUSLobbySoundSettingsController();
	}

	get soundSettingsController()
	{
		return this._fSoundSettingsController_ssc;
	}
	//...SOUND SETTINGS

	//SOUNDS...
	__provideSoundsControllerInstance()
	{
		return new SoundsController();
	}

	get soundsController()
	{
		return this._fSoundsController_lsc;
	}
	//...SOUNDS

	//BROWSER SUPPORT...
	__provideBrowserSupportControllerInstance()
	{
		return new BrowserSupportController();
	}

	get browserSupportController()
	{
		return this._fBrowserSupportController_bsc;
	}
	//...BROWSER SUPPORT

	//CUSTOMER SPEC...
	__provideCustomerspecControllerInstance()
	{
		return new CustomerspecController();
	}

	get customerspecController()
	{
		return this._fCustomerspecController_cc;
	}
	//...CUSTOMER SPEC

	//LOBBY STATE...
	__provideLobbyStateControllerInstance()
	{
		return new GUSLobbyStateController();
	}

	get lobbyStateController()
	{
		return this._fLobbyStateController_lsc;
	}
	//...LOBBY STATE

	//LOBBY PLAYER...
	__provideLobbyPlayerControllerInstance()
	{
		return new GUSLobbyPlayerController();
	}

	get playerController()
	{
		return this._fLobbyPlayerController_lpc;
	}
	//...LOBBY PLAYER

	//WEB SOCKET...
	get pseudoGamewebSocketInteractionController()
	{
		return this._pseudoGameWebSocketInteractionController || (this._pseudoGameWebSocketInteractionController = this.__providePseudoGameWebSocketInteractionControllerInstance());
	}

	__providePseudoGameWebSocketInteractionControllerInstance()
	{
		return new GUSPseudoGameWebSocketInteractionController();
	}
	//...WEB SOCKET

	//EXTERNAL COMMUNICATOR...
	__provideExternalCommunicatorInstance()
	{
		return new GUSLobbyExternalCommunicator();
	}
	//...EXTERNAL COMMUNICATOR

	//SOUNDS BACKGROUND LOADING...
	initSoundsBackgroundLoading()
	{
		this.soundsBgLoadingController.init();
	}

	get soundsBgLoadingController()
	{
		let lSoundsBgLoadingController_ssblc = this._fSoundsBgLoadingController_ssblc;
		if (!lSoundsBgLoadingController_ssblc)
		{
			lSoundsBgLoadingController_ssblc = this.__provideSoundsBackgoundLoadingControllerInstance();
		}

		return lSoundsBgLoadingController_ssblc;
	}

	__provideSoundsBackgoundLoadingControllerInstance()
	{
		return new GUSLobbySoundsBackgoundLoadingController();
	}
	//...SOUNDS BACKGROUND LOADING

	//DEBUG...
	get lobbyDebuggingController()
	{
		return this._fLobbyDebuggingController_ldc || (this._fLobbyDebuggingController_ldc = this._initLobbyDebuggingController());
	}

	_initLobbyDebuggingController()
	{
		let l_ldc = this.__provideLobbyDebuggingControllerInstance();
		this._fLobbyDebuggingController_ldc = l_ldc;

		l_ldc.i_init();

		return l_ldc;
	}

	__provideLobbyDebuggingControllerInstance()
	{
		return new GUSLobbyDebuggingController();
	}
	//...DEBUG

	//ERROR HANDLING...
	get errorHandlingController()
	{
		return this._fLobbyErrorHandlingController_lehc || (this._fLobbyErrorHandlingController_lehc = this._initErrorHandlingController());
	}

	_initErrorHandlingController()
	{
		let l_lehc = this.__provideLobbyErrorHandlingControllerInstance();
		this._fLobbyErrorHandlingController_lehc = l_lehc;

		l_lehc.i_init();

		return l_lehc;
	}

	__provideLobbyErrorHandlingControllerInstance()
	{
		return new GUSLobbyErrorHandlingController();
	}
	//...ERROR HANDLING

	//VUE...
	_initVueApplicationController()
	{
		this._fVueApplicationController_vac = this.__provideVueApplicationControllerInstance();
		this._fVueApplicationController_vac.i_init();
	}

	get vueApplicationController()
	{
		return this._fVueApplicationController_vac;
	}

	__provideVueApplicationControllerInstance()
	{
		return new GUSLobbyVueApplicationController();
	}
	//...VUE

	//DIALOGS...
	_initDialogsController()
	{
		let dialogsController = this._fDialogsController_dsc = this.__provideDialogsControllerInstance();
		dialogsController.on(GUSLobbyDialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		dialogsController.on(GUSLobbyDialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
	}

	__provideDialogsControllerInstance()
	{
		return new GUSLobbyDialogsController();
	}

	get dialogsController()
	{
		return this._fDialogsController_dsc;
	}
	//...DIALOGS

	//TOURNAMENT...
	_initTournamentModeController()
	{
		this._fTournamentModeController_tmc = this.__provideTournamentModeControllerInstance();
		this._fTournamentModeController_tmc.i_init();
	}

	__provideTournamentModeControllerInstance()
	{
		return new GUSLobbyTournamentModeController();
	}

	get tournamentModeController()
	{
		return this._fTournamentModeController_tmc;
	}
	//...TOURNAMENT

	//JS ENVIRONMENT...
	_initJSEnvironmentInteractionController()
	{
		this._jsEnvironmentInteractionController = this.__provideJSEnvironmentInteractionControllerInstance();
	}

	__provideJSEnvironmentInteractionControllerInstance()
	{
		return new GUSLobbyJSEnvironmentInteractionController();
	}

	get jsEnvironmentInteractionController()
	{
		return this._jsEnvironmentInteractionController;
	}
	//...JS ENVIRONMENT

	//REDIRECTION...
	_initRedirectionController()
	{
		this._redirectionController = this.__provideRedirectionControllerInstance();
	}

	__provideRedirectionControllerInstance()
	{
		return new GUSLobbyRedirectionController();
	}

	get redirectionController()
	{
		return this._redirectionController;
	}
	//...REDIRECTION

	//BONUS...
	_initLobbyBonusController()
	{
		this._fLobbyBonusController_lbc = this.__provideLobbyBonusControllerInstance();
		this._fLobbyBonusController_lbc.i_init();

		this._fLobbyBonusController_lbc.on(GUSLobbyBonusController.EVENT_BONUS_RESTART_REQUIRED, this._onBonusRestartRequired, this);
		this._fLobbyBonusController_lbc.on(GUSLobbyBonusController.EVENT_ON_BONUS_STATE_CHANGED, this._onBonusStateChanged, this);
	}

	__provideLobbyBonusControllerInstance()
	{
		return new GUSLobbyBonusController();
	}

	get lobbyBonusController()
	{
		return this._fLobbyBonusController_lbc;
	}

	_onBonusRestartRequired(event)
	{
	}

	_onBonusRestartRequired()
	{
		if (!this.lobbyScreen.visible) // we are in the room
		{
			let lIsRoomRestartRequired_bl = this._fLobbyBonusController_lbc.info.isRoomRestartRequired;
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BONUS_RESTART_REQUIRED, {restartRoom: lIsRoomRestartRequired_bl});
			if (lIsRoomRestartRequired_bl)
			{
				this._fIsRestartLobbyRequired_bl = true; // will restart after room is closed
			}
		}
		else
		{
			this._restartLobby();
		}
	}

	_onBonusStateChanged(event)
	{
		this._onSomeBonusStateChanged();
	}
	//...BONUS

	//FRB...
	_initFRBController()
	{
		this._fFRBController_frbc = this.__provideLobbyFRBControllerInstance();
		this._fFRBController_frbc.i_init();

		this._fFRBController_frbc.on(GUSLobbyFRBController.EVENT_FRB_RESTART_REQUIRED, this._onFRBRestartRequired, this);
		this._fFRBController_frbc.on(GUSLobbyFRBController.EVENT_ON_FRB_STATE_CHANGED, this._onFRBStateChanged, this);
	}

	__provideLobbyFRBControllerInstance()
	{
		return new GUSLobbyFRBController();
	}

	get FRBController()
	{
		return this._fFRBController_frbc;
	}

	_onFRBRestartRequired(event)
	{
		if (!this.lobbyScreen.visible) // we are in the room
		{
			let lIsRoomRestartRequired_bl = this._fFRBController_frbc.info.isRoomRestartRequired;
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FRB_RESTART_REQUIRED, {restartRoom: lIsRoomRestartRequired_bl});
			if (lIsRoomRestartRequired_bl)
			{
				this._fIsRestartLobbyRequired_bl = event.noLobbyRestart ? false : true; // will restart after room is closed
			}
		}
		else
		{
			this._restartLobby();
		}
	}

	_onFRBStateChanged(event)
	{
		this._onSomeBonusStateChanged();
	}
	//...FRB

	//COMMON_PANEL...
	_initCommonPanelController()
	{
		this._fCommonPanelController_cpc = this.__provideCommonPanelControllerInstance();

		this._fCommonPanelController_cpc.on(GUSLobbyCommonPanelController.EVENT_TIME_SYNC_REQUEST, this._onCommonPanelTimeSync, this);
		this._fCommonPanelController_cpc.on(GUSLobbyCommonPanelController.EVENT_REFRESH_BALANCE_REQUEST, this._onCommonPanelRefreshBalance, this);

		this._fCommonPanelController_cpc.on(GUSLobbyCommonPanelController.EVENT_EDIT_PROFILE_BUTTON_CLICKED, this._onCommonPanelEditProfileClicked, this);
		this._fCommonPanelController_cpc.on(GUSLobbyCommonPanelController.EVENT_SETTINGS_BUTTON_CLICKED, this._onCommonPanelSettingsClicked, this);
		this._fCommonPanelController_cpc.on(GUSLobbyCommonPanelController.EVENT_BACK_TO_LOBBY_BUTTON_CLICKED, this._onCommonPanelBackToLobbyClicked, this);
		this._fCommonPanelController_cpc.on(GUSLobbyCommonPanelController.EVENT_FIRE_SETTINGS_BUTTON_CLICKED, this._onCommonPanelFireSettingsClicked, this);
		this._fCommonPanelController_cpc.on(GUSLobbyCommonPanelController.EVENT_INFO_BUTTON_CLICKED, this._onCommonPanelInfoClicked, this);

		this._fCommonPanelController_cpc.on(GUSLobbyCommonPanelController.EVENT_GROUP_BUTTONS_CHANGED, this._onCommonPanelGroupButtonsChanged, this);
		this._fCommonPanelController_cpc.on(GUSLobbyCommonPanelController.EVENT_COUNT_GROUP_BUTTONS_CHANGED, this._onCommonPanelCountGroupButtonsChanged, this);
	}

	__provideCommonPanelControllerInstance()
	{
		return new GUSLobbyCommonPanelController();
	}

	get commonPanelController()
	{
		return this._fCommonPanelController_cpc;
	}

	_onCommonPanelTimeSync(event)
	{
		this.lobbyScreen.onGetLobbyTimeRequired();
	}

	_onCommonPanelRefreshBalance(event)
	{
		//BATTLEGROUND...
		//two types of balance in BTG:
		// 1 - fixed BTG round balance
		// 2 - real player balance
		//Show real player balance over btg buy in confirmation dialog
		if (this.isBattlegroundGame)
		{
			if (
					!this.isBattlegroundRoomMode ||
					this.dialogsController.battlegroundBuyInConfirmationDialogController.info.isActive
				)
			{
				this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_REFRESH_BALANCE_REQUIRED);
			}
			else
			{
				this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.GAME_REFRESH_BALANCE_REQUIRED);
			}

			return;
		}
		//...BATTLEGROUND

		if (!this.lobbyScreen.visible)
		{
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.GAME_REFRESH_BALANCE_REQUIRED);
		}
		else
		{
			this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_REFRESH_BALANCE_REQUIRED);
		}
	}

	_onCommonPanelEditProfileClicked()
	{
		this._validateScreenState();

		this._fSecondaryContainer_sprt.visible = true;
		this.emit(GUSLobbyApplication.EVENT_ON_PROFILE_BUTTON_CLICKED);
	}

	_onCommonPanelSettingsClicked()
	{
		this._validateScreenState();

		this._fSecondaryContainer_sprt.visible = true;
		this.emit(GUSLobbyApplication.EVENT_ON_SETTINGS_BUTTON_CLICKED);
	}

	_onCommonPanelBackToLobbyClicked()
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BACK_TO_LOBBY);

		this._fSecondaryContainer_sprt.visible = false;

		this._onTimeToHideTutorial(); //just in case if it is opened
	}

	_onCommonPanelFireSettingsClicked()
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FIRE_SETTINGS_CLICKED);
	}

	_onCommonPanelGroupButtonsChanged(aEvent_obj)
	{
		if (aEvent_obj.visible)
		{
			this.layout.showMobileCommonGroupButtonsLayer();
		}
		else
		{
			this.layout.hideMobileCommonGroupButtonsLayer();
		}
	}

	_onCommonPanelCountGroupButtonsChanged(aEvent_obj)
	{
		let lButtonsAmount = aEvent_obj.buttonsAmount;
		this.layout.resizeMobileCommonGroupButtonsLayer(lButtonsAmount);
	}
	//...COMMON_PANEL

	//TOOLTIPS...
	_initTooltipsController()
	{
		let l_ltc = this.__provideLobbyTooltipsControllerInstance();
		this._fTooltipsController_ltc = l_ltc;

		l_ltc.i_init();

		return l_ltc;
	}

	__provideLobbyTooltipsControllerInstance()
	{
		return new GUSLobbyTooltipsController();
	}

	get tooltipsController()
	{
		return this._fTooltipsController_ltc || (this._fTooltipsController_ltc = this._initTooltipsController());
	}
	//...TOOLTIPS

	//TUTORIAL...
	_initTutorialController()
	{
		let l_tc = this.__provideLobbyTutorialControllerInstance();
		l_tc.on(GUSLobbyTutorialController.EVENT_ON_TIME_TO_HIDE_TUTORIAL_LAYER, this._onTimeToHideTutorial, this);
		l_tc.on(GUSLobbyTutorialController.EVENT_ON_TIME_TO_SHOW_TUTORIAL_LAYER, this._onTimeToShowTutorial, this);
		this._fTutorialController_tc = l_tc;

		l_tc.i_init();

		return l_tc;
	}

	__provideLobbyTutorialControllerInstance()
	{
		return new GUSLobbyTutorialController(null, null);
	}

	get tutorialController()
	{
		return this._fTutorialController_tc || (this._fTutorialController_tc = this._initTutorialController());
	}

	_onTimeToShowTutorial()
	{
		this.layout.showTutorialLayer();
	}

	_onTimeToHideTutorial()
	{
		this.layout.hideTutorialLayer();
	}
	//...TUTORIAL

	//RESOURCES...
	get applicationFolderURL()
	{
		let appFolderURL = this.appParamsInfo.lobbyPath;
		if (!appFolderURL)
		{
			appFolderURL = super.applicationFolderURL;
		}
		return appFolderURL;
	}

	handleAssetsLoadingError(errorKey, errorMessage)
	{
		this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_ASSETS_LOADING_ERROR, {key:errorKey, message:errorMessage});

		this.preLoader.stopSpinnerUpdate();
		this.layout.hidePrePreloader();

		let lLobbyScreen_sprt = this._fLobbyScreen_sprt;
		if (lLobbyScreen_sprt)
		{
			lLobbyScreen_sprt.clearInfo();
			lLobbyScreen_sprt.hideLobby();
		}

		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.LOBBY_LOADING_ERROR);
		APP.logger.i_pushError(`Lobby. Assets Loading Error! ${errorKey}: ${errorMessage} `);
	}
	//...RESOURCES

	get buyInFuncName()
	{
		return this._fBuyInFuncName_str;
	}

	get buyInFuncDefined()
	{
		return this._fBuyInFuncName_str !== undefined;
	}

	//LOBBY SCREEN...
	get lobbyScreen ()
	{
		return this._fLobbyScreen_sprt || (this._fLobbyScreen_sprt = this._initLobbyScreen());
	}

	_initLobbyScreen()
	{
		let l_sprt = this.__provideLobbyScreenInstance();
		l_sprt.init();
		
		this._fLobbyContainer_sprt.addChild(l_sprt);

		l_sprt.on(GUSLobbyScreen.EVENT_ON_LOBBY_PROFILE_CLICKED, this.onLobbyScreenProfileClicked, this);
		l_sprt.on(GUSLobbyScreen.EVENT_ON_LOBBY_BACK_TO_GAME, this.onBackToGame, this);
		l_sprt.on(GUSLobbyScreen.EVENT_ON_PSEUDO_GAME_URL_READY, this.onPseudoGameUrlReady, this);

		return l_sprt;
	}

	__provideLobbyScreenInstance()
	{
		return new GUSLobbyScreen();
	}
	//...LOBBY SCREEN

	//PROFILLING...
	_initProfilingController()
	{
		return new GUSLobbyProfilingController();
	}
	//...PROFILLING

	//BUTTONS...
	get lobbyButtonsConstroller()
	{
		return this._fLobbyButtonsController_lbc || this._initLobbyButtonsController();
	}

	_initLobbyButtonsController()
	{
		const l_lbc = this._fLobbyButtonsController_lbc = this.__provideLobbyButtonsControllerInstance();
		return l_lbc;
	}

	__provideLobbyButtonsControllerInstance()
	{
		return new GUSLobbyButtonsController();
	}
	//...BUTTONS

	//GAME LAUNCHER...
	get gameLauncher()
	{
		return this._fGameLauncher_gl;
	}

	initGameLauncher()
	{
		this._fGameLauncher_gl = this.__provideGameLauncherInstance();
		this._fGameLauncher_gl.once(GUSLobbyGameLauncher.EVENT_ON_GAME_ADDED, this._onGameLayerAdded, this);
	}

	__provideGameLauncherInstance()
	{
		return new GUSLobbyGameLauncher(this._fLobbyContainer_sprt, this.stage.view);
	}

	_onGameLayerAdded(event)
	{
		this.externalCommunicator.addTargetWindow(event.gameFrame.contentWindow);
	}
	//...GAME LAUNCHER

	//LAYOUT...
	get domLayoutClass()
	{
		return GUSLobbyLayout;
	}

	get isTutorialSupported()
	{
		return false;
	}
	//...LAYOUT

	//GAME COMMUNICATOR...
	initGameCommunicator()
	{
		this.externalCommunicator.subscribeMessagesReceive();

		this.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this.onGameMessageReceived, this);
	}

	onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.PLAYER_INFO_UPDATED:
				this.emit(GUSLobbyApplication.EVENT_ON_PLAYER_INFO_UPDATED, event.data);
				break;
			case GAME_MESSAGES.CONTEXT_MENU:
				this.emit("gamecontextmenu");
				break;
			case GAME_MESSAGES.ROOM_CLOSED:
				this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_REFRESH_BALANCE_REQUIRED);
				this.emit(GUSLobbyApplication.EVENT_ON_ROOM_CLOSED);

				this._tryToRestartLobbyIfNeeded();
				break;
			case GAME_MESSAGES.GAME_START_CLICKED:
				this.emit(GUSLobbyApplication.EVENT_ON_SOUND_CONTEXT_NEED_TO_BE_CHECKED);
				break;

			case GAME_MESSAGES.GAME_LOADING_ERROR:
				this.lobbyScreen.visible = false;
				this.lobbyScreen.hideLobby();
				break;
		}
	}
	//...GAME COMMUNICATOR

	// BATTLEGROUND...
	get isBattlegroundRoomMode()
	{
		let l_bc = this._fBattlegroundController_bc;
		return APP.isBattlegroundGame
				&& !!l_bc
				&& l_bc.info.isConfirmedBuyInCostDefined
				&& l_bc.info.isBattlegroundGameStarted
				&& !this.lobbyScreen.visible;
	}

	get battlegroundController()
	{
		return this._fBattlegroundController_bc || (this._fBattlegroundController_bc = this._initBattlegroundController());
	}

	_initBattlegroundController()
	{
		let l_bc = this.__provideLobbyBattlegroundControllerInstance();
		this._fBattlegroundController_bc = l_bc;

		l_bc.on(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NEED_UPDATE_MODE_ON_GAME_LEVEL, this._needUpdateBattlegroundModeOnGameLevel, this);

		l_bc.i_init();

		return l_bc;
	}

	__provideLobbyBattlegroundControllerInstance()
	{
		return new GUSLobbyBattlegroundController();
	}

	_needUpdateBattlegroundModeOnGameLevel(event)
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.UPDATE_BATTLEGROUND_MODE, {
			isConfirmBuyinDialogExpectedOnLastHand: event.isConfirmBuyinDialogExpectedOnLastHand
		});

		if (this.battlegroundController.info.isBattlegroundGameStarted)
		{
			this.emit(GUSLobbyApplication.EVENT_ON_UPDATE_BATTLEGROUND_MODE);
		}
	}
	// ...BATTLEGROUND

	_addStages()
	{
		super._addStages();

		this.dialogsStage = this.addStage(DOM_LAYERS.DIALOGS, undefined, PIXI.RENDERER_TYPE.CANVAS);
		
		this.commonPanelStages = [];
		let layersParams_obj_arr = GUSLobbyCommonPanelController.getCommonPanelLayers(this.isMobile);
		for (var i = 0; i < layersParams_obj_arr.length; i++)
		{
			let layerId_int = layersParams_obj_arr[i].id;
			this.commonPanelStages[layerId_int] = this.addStage(DOM_LAYERS.COMMON_PANEL + "_" + layerId_int, layersParams_obj_arr[i], PIXI.RENDERER_TYPE.CANVAS);
		}

		let margin = this.config.margin;
		let preloaderWidth_num = this.config.size.width + (margin.left || 0) + (margin.right || 0);
		let preloaderHeight_num = this.config.size.height  + (margin.top || 0) + (margin.bottom || 0);
		if (this.isMobile)
		{
			this.fullscreenNoteStage = this.addStage(DOM_LAYERS.FULLSCREEN_NOTIFICATION, {width:preloaderWidth_num, height:preloaderHeight_num}, PIXI.RENDERER_TYPE.CANVAS);
		}

		if (this.isTutorialSupported)
		{
			let lTutorialHeigth_num = this.isMobile ? this.config.size.height : this.config.size.height + 20;
			this._fTutorialStage_s = this.addStage(DOM_LAYERS.TUTORIAL, {width: this.config.size.width, height: lTutorialHeigth_num}, PIXI.RENDERER_TYPE.WEBGL);
		}
	}

	//@override
	get isErrorHandlingMode()
	{
		//DEBUG...
		//return !this.isDebugMode;
		//...DEBUG
		return this.appParamsInfo.errorHandlingAllowed;
	}

	_onWebglContextLost(event)
	{
		super._onWebglContextLost(event);

		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.WEBGL_CONTEXT_LOST);
	}

	_onContextMenu(event)
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.CONTEXT_MENU);
	}

	restartLobby()
	{
		this._restartLobby();
	}

	_onSomeBonusStateChanged()
	{
		this.emit(GUSLobbyApplication.EVENT_ON_SOME_BONUS_STATE_CHANGED);
	}

	_generateWebSocketInteractionInstance()
	{
		return new GUSLobbyWebSocketInteractionController();
	}

	_startHandlingServerMessages()
	{
		let wsInteractionController = this.webSocketInteractionController;
		wsInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
	}

	_onServerErrorMessage(event)
	{
		this._onTimeToHideTutorial();

		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED, {errorType: event.errorType, errorCode: event.messageData.code});
		APP.logger.i_pushError(`Internal error! ${JSON.stringify(event.messageData)}`);
	}

	onApplicationReady()
	{
		this._fBrowserSupportController_bsc.init();
		this._fBrowserSupportController_bsc.on(BrowserSupportController.i_EVENT_BROWSER_SUPPORT_CONFIRMED, () => {this.emit("browserConfirmed")});

		this._fCustomerspecController_cc.init();

		this._fSoundSettingsController_ssc.init();
		this._fSoundSettingsController_ssc.on(GUSLobbySoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED, this.onSoundVolumeChanged, this);

		this.once(GUSLobbyApplication.EVENT_ON_SOUND_SETTINGS_CHANGED, () => {this.emit(GUSLobbyApplication.EVENT_ON_SOUND_CONTEXT_NEED_TO_BE_CHECKED)});

		let soundSettingsInfo = this._fSoundSettingsController_ssc.i_getInfo();
		this._fSoundsController_lsc.init(
			this.config.audio.stereo,
			soundSettingsInfo.soundsLoadingAvailable,
			soundSettingsInfo.soundsMuted,
			soundSettingsInfo.i_getFxSoundsVolume(),
			soundSettingsInfo.i_getBgSoundsVolume()
		);

		this._fLobbyPlayerController_lpc.init();
		this._fLobbyPlayerController_lpc.on(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this.playerInfoUpdated, this);

		this._fLobbyStateController_lsc.init();
		this._fLobbyStateController_lsc.on(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this.onLobbyScreenVisibilityChanged, this);
		this._fLobbyStateController_lsc.on(GUSLobbyStateController.EVENT_ON_SECONDARY_SCREEN_STATE_CHANGE, this.onSecondaryScreenStateChange, this);

		this._fDialogsController_dsc.init();

		let nemDialogController = this._fDialogsController_dsc.gameNEMDialogController;
		nemDialogController.on(GUDialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED, this._onNEMDialogCustomButtonClicked, this);
		nemDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onGameNEMDialogRequestConfirmed, this);

		let forceSitOutDialogController = this._fDialogsController_dsc.forceSitOutDialogController;
		forceSitOutDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onForceSitOutDialogControllerDialogRequestConfirmed, this);

		let midRoundCompensateSWExitDialogController = this._fDialogsController_dsc.midRoundCompensateSWExitDialogController;
		midRoundCompensateSWExitDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onMidRoundCompensateSWExitDialogRequestConfirmed, this);

		let midRoundExitDialogController = this._fDialogsController_dsc.midRoundExitDialogController;
		midRoundExitDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onMidRoundExitDialogRequestConfirmed, this);

		this.keyboardControlProxy.on(KeyboardControlProxy.i_EVENT_BUTTON_CLICKED, this._onKeyboardButtonClicked, this);

		let gameBuyAmmoFailedDialogController = this._fDialogsController_dsc.gameBuyAmmoFailedDialogController;
		gameBuyAmmoFailedDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onBuyAmmoFailedDialogRequestConfirmed, this);
		gameBuyAmmoFailedDialogController.on(GUDialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED, this._onBuyAmmoFailedDialogExitBtnClicked, this);

		let gameRebuyDialogController = this._fDialogsController_dsc.gameRebuyDialogController;
		gameRebuyDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onGameRebuyDialogRequestConfirmed, this);
		gameRebuyDialogController.on(GUDialogController.EVENT_DIALOG_PRESENTED, this._onGameRebuyDialogPresented, this);		

		let gameNEMForRoomDialogController = this._fDialogsController_dsc.gameNEMForRoomDialogController;
		gameNEMForRoomDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onGameNEMForRoomDialogRequestConfirmed, this);

		let lGamePendingOperationFailedDialogController = this._fDialogsController_dsc.gamePendingOperationFailedDialogController;
		lGamePendingOperationFailedDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onPendingOperationFailedFailedDialogRequestConfirmed, this);

		super.onApplicationReady();
	}

	get __preloaderBgMusicAssetName()
	{
		return undefined;
	}

	playPreloaderSounds()
	{
		this.soundsController.playPreloaderSounds();
	}

	playBackgroundSoundName()
	{
		if (!this.__defaultBackgroundSoundName)
		{
			return;
		}

		this.soundsController.play(this.__defaultBackgroundSoundName);
		this.soundsController.setFadeMultiplier(this.__defaultBackgroundSoundName, 0);

		if (this.soundsController.info.soundsMuted)
		{
			this.on(GUSLobbyApplication.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);
		}
		else
		{
			this.soundsController.setVolumeSmoothly(this.__defaultBackgroundSoundName, 1, 5000, null);
		}
	}

	_onSoundSettingsChanged(event)
	{
		if (!event.muted)
		{
			this.off(GUSLobbyApplication.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);

			this.soundsController.setVolumeSmoothly(this.__preloaderBgMusicAssetName, 1, 5000, null);
		}
	}

	resize(width, height)
	{
		super.resize(width, height);
		this.dialogsStage.resize(width, height);
	}

	onLoaderUIReady()
	{
		super.onLoaderUIReady();
		this._fDialogsController_dsc.initView(this.dialogsStage.view);
	}

	startLobby()
	{
		this.lobbyScreen.visible = false;
		this.webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		this.initGameCommunicator();
		this.initGameLauncher();
	}

	_onServerEnterLobbyMessage(event)
	{
		if (this.isPreloaderActive)
		{
			//DO NOT SHOW LOBBY WHEN BATTLEGROUND INSTANT START REQUIRED...
			if (this.battlegroundController.info.isStartOnEnterLobbyInProgress())
			{
				if (!this.battlegroundController.info.isStartGameLevelWasInitiated)
				{
					this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_SCREEN_SHOWED);
				}
			}
			//...DO NOT SHOW LOBBY WHEN BATTLEGROUND INSTANT START REQUIRED
			else
			{
				this._switchPreloaderToLobbyScreen();
			}
		}
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.ENTER_LOBBY_MESSAGE_RECEIVED);
	}

	_switchPreloaderToLobbyScreen()
	{
		this.lobbyScreen.visible = true;
		this.layout.hidePreloader();
		this.layout.showCommonPanel();

		this.removePreloader();

		this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_SCREEN_SHOWED);
	}

	_tryToRestartLobbyIfNeeded()
	{
		let l_poi = APP.pendingOperationController.info;
		if (
				this._fIsRestartLobbyRequired_bl
				&& !l_poi.isPendingOperationStatusCheckInProgress
			)
		{
			this._restartLobby();
		}
	}

	_restartLobby()
	{
		this._fIsRestartLobbyRequired_bl = false;

		this.lobbyScreen.clearInfo();
		this.lobbyScreen.hideLobby();
		this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_RESTART_REQUIRED);
	}

	_onNEMDialogCustomButtonClicked()
	{
		if (!this.lobbyScreen.visible)
		{
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.GAME_REFRESH_BALANCE_REQUIRED);
		}
		else
		{
			this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_REFRESH_BALANCE_REQUIRED);
		}
	}

	_onGameNEMDialogRequestConfirmed()
	{
		let nemDialogController = this._fDialogsController_dsc.gameNEMDialogController;
		let nemDialogInfo = nemDialogController.info;
		
		if (nemDialogInfo.isBonusMode && nemDialogInfo.isBonusModeNEMForRoom)
		{
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.GAME_NEM_FOR_ROOM_REQUEST_CONFIRMED);
		}
	}

	_onMidRoundCompensateSWExitDialogRequestConfirmed()
	{
		if (!this.lobbyScreen.resitOutInProcess && !this.FRBController.info.isActivated)
		{
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.MID_ROUND_COMPENSATE_SW_EXIT_CONFIRMED);
		}
	}

	_onForceSitOutDialogControllerDialogRequestConfirmed()
	{
		if (!this.lobbyScreen.resitOutInProcess)
		{
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FORCE_SIT_OUT_EXIT_CONFIRMED);
			this._fIsRestartLobbyRequired_bl = true;
		}
	}

	_onMidRoundExitDialogRequestConfirmed()
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.MID_ROUND_EXIT_CONFIRMED);
	}

	_onBuyAmmoFailedDialogRequestConfirmed()
	{
		let gameBuyAmmoFailedDialogInfo = this._fDialogsController_dsc.gameBuyAmmoFailedDialogController.info;

		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BUY_AMMO_FAILED_RETRY_CONFIRMED, {type: gameBuyAmmoFailedDialogInfo.retryType});
	}

	_onBuyAmmoFailedDialogExitBtnClicked()
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BUY_AMMO_FAILED_EXIT_ROOM_REQUESTED);
	}

	_onGameRebuyDialogRequestConfirmed()
	{
		let lTournamentModeinfo_tmi = this.tournamentModeController.info;	

		if (
				lTournamentModeinfo_tmi.isTournamentMode
				&& lTournamentModeinfo_tmi.rebuyAllowed
				&& !lTournamentModeinfo_tmi.isRebuyLimitExceeded
			)
		{
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.TOURNAMENT_REBUY_CONFIRMED);
		}
	}

	_onGameRebuyDialogPresented()
	{
		let lTournamentModeinfo_tmi = this.tournamentModeController.info;		
		let lGameRebuyDialogController_grdc = this._fDialogsController_dsc.gameRebuyDialogController;
		
		if (
				lTournamentModeinfo_tmi.isTournamentMode
				&& !lGameRebuyDialogController_grdc.keepDlg
				&& (lTournamentModeinfo_tmi.isRebuyLimitExceeded || !lTournamentModeinfo_tmi.rebuyAllowed)
			)
		{
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.TOURNAMENT_COMEPLETED_WITH_NO_REBUYS);
		}
	}

	_onGameNEMForRoomDialogRequestConfirmed()
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.GAME_NEM_FOR_ROOM_REQUEST_CONFIRMED);
	}

	_onDialogActivated(event)
	{
		this.hideLobbyScreen();
		this.layout.showDialogsScreen();
		this.dialogsStage.render();
		this.setTickerPaused(false, "focus");

		this._fLobbyScreen_sprt && !GUSLobbyDialogsView.isDialogViewBlurForbidden(event.dialogId) && this._fLobbyScreen_sprt.showBlur();

		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.DIALOG_ACTIVATED, {dialogId: event.dialogId});
	}

	_onDialogDeactivated(event)
	{
		let dialogsController = this._fDialogsController_dsc;
		if (!dialogsController.info.hasActiveDialog)
		{
			this.dialogsStage.render();
			this.layout.hideDialogsScreen();
			this._fLobbyScreen_sprt && this._fLobbyScreen_sprt.hideBlur();

			if (this.secondaryScreenController.isSecondaryScreenActive)
			{
				this._fSecondaryContainer_sprt.visible = true;
			}
		}
		this._fLobbyScreen_sprt && this._fLobbyScreen_sprt.hideBlur();
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.DIALOG_DEACTIVATED, {dialogId: event.dialogId});
	}

	_onKeyboardButtonClicked(event)
	{
		this.externalCommunicator.sendExternalMessage(GAME_MESSAGES.KEYBOARD_BUTTON_CLICKED, {code: event.code});
	}

	clearPauseTimeout()
	{
		clearTimeout(this._fPauseTimeout_int);
		this._fPauseTimeout_int = undefined;
	}

	onBackToGame(event)
	{
		this.clearPauseTimeout();
		this._fPauseTimeout_int = setTimeout(this.pause.bind(this), 100);
		if (event)
		{
			let lParams_obj = {};
			lParams_obj.startGameUrl = event.startGameUrl ? event.startGameUrl : undefined;
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BACK_TO_GAME, lParams_obj);
		}
		else
		{
			this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BACK_TO_GAME);
		}
		this.dispatchSoundSettingsMessages();

		this.layout.showGamesScreen(300);
	}

	onPseudoGameUrlReady(event)
	{
		this.pseudoGamewebSocketInteractionController.setParams(event.pseudoGameParams);
	}

	onLobbyScreenVisibilityChanged(data)
	{
		this.lobbyScreen.onScreenVisibleChange(data.visible);
	}

	onSecondaryScreenStateChange(data)
	{
		this.lobbyScreen.onSecondaryScreenStateChange(data.state);
	}

	playerInfoUpdated(event)
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.PLAYER_INFO_UPDATED, {data: event.data});
	}

	_onPendingOperationFailedFailedDialogRequestConfirmed()
	{
		let gamePendingOperationFailedFailedDialogInfo = this._fDialogsController_dsc.gamePendingOperationFailedDialogController.info;
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.PENDING_OPERATION_FAILED_RETRY_CONFIRMED, {type: gamePendingOperationFailedFailedDialogInfo.retryType});
	}

	pause()
	{
		let hasActiveDialog = this.dialogsController.info.hasActiveDialog;

		this._fSecondaryContainer_sprt.visible = false;
		this.hideLobbyScreenUI();
		this.forceRendering(); //force rendering application before pause
		let pauseFocusTicker = !hasActiveDialog;
		this.setTickerPaused(pauseFocusTicker , "focus");
	}

	resume()
	{
		this.clearPauseTimeout();

		this.setTickerPaused(false, "focus");
		this.showLobbyScreenUI();
	}

	getTickerPauseExceptions()
	{
		return ["focus"];
	}

	getSoundsPauseExceptions()
	{
		return ["focus"];
	}

	hideLobbyScreenUI()
	{
		this.lobbyScreen.visible = false;
		this.lobbyScreen.hideLobby();

		if (!this.isMobile || !this._fSecondaryScreenController_ssc._paytableScreenController.isActiveScreen)
		{
			this.layout.showGamesLayer();
		}

		this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_UI_HIDDED);
	}

	showLobbyScreenUI()
	{
		this.lobbyScreen.visible = true;
		if (!this.lobbyScreen.isRoomLasthand)
		{
			this.lobbyScreen.showLobby();
		}
		this.layout.hideGamesLayer();

		this.emit(GUSLobbyApplication.EVENT_ON_LOBBY_UI_SHOWN);
	}

	hideLobbyScreen()
	{
		if (this._fSecondaryContainer_sprt.visible)
		{
			this._fSecondaryContainer_sprt.visible = false;
			this.setTickerPaused(true, "focus");
		}
	}

	_onMobilePaytableActivated()
	{
		this._hideGamesLayer();
		this.layout.hideAppScreensLayer();
	}

	_onMobilePaytableDeactivated()
	{
		this._showGamesLayerSuspicion();
		this.layout.showAppScreensLayer();
	}

	_showGamesLayerSuspicion()
	{
		if (!this.lobbyScreen.visible)
		{
			this.layout.showGamesLayer();
		}
	}

	_hideGamesLayer()
	{
		this.layout.hideGamesLayer();
	}

	onSecondaryScreenActivated()
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.SECONDARY_SCREEN_ACTIVATED);
	}

	onSecondaryScreenDeactivated()
	{
		if (!this.lobbyScreen.visible)
		{
			this.onBackToGame();
		}
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.SECONDARY_SCREEN_DEACTIVATED);
	}

	onLobbyScreenProfileClicked()
	{
		this._fSecondaryContainer_sprt.visible = true;
		this.emit(GUSLobbyApplication.EVENT_ON_PROFILE_BUTTON_CLICKED);
	}

	onSoundVolumeChanged()
	{
		this.dispatchSoundSettingsMessages();
	}

	_onCommonPanelInfoClicked()
	{
		this._validateScreenState();

		this._fSecondaryContainer_sprt.visible = true;
		this.emit(GUSLobbyApplication.EVENT_ON_INFO_BUTTON_CLICKED);
	}

	_onTimeToShowVueLayer()
	{
		this.layout.showVueLayer();
	}

	_onTimeToHideVueLayer()
	{
		this.layout.hideVueLayer();
	}

	_validateScreenState()
	{
		if (!this.lobbyStateController.info.lobbyScreenVisible)
		{
			this.setTickerPaused(false, "focus");
			this._fSecondaryContainer_sprt.visible = true;
			this.layout.coverGamesScreenByLobby();
		}
	}

	dispatchSoundSettingsMessages()
	{
		let lSoundSettingsInfo_ssi = this._fSoundSettingsController_ssc.i_getInfo();
		let lSoundSettings_obj = {
			"fxVolume": 	lSoundSettingsInfo_ssi.i_getFxSoundsVolume(),
			"musicVolume": 	lSoundSettingsInfo_ssi.i_getBgSoundsVolume(),
			"muted":		lSoundSettingsInfo_ssi.soundsMuted
		};

		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.SOUND_SETTINGS_CHANGED, lSoundSettings_obj);
		this.emit(GUSLobbyApplication.EVENT_ON_SOUND_SETTINGS_CHANGED, lSoundSettings_obj);
	}

	dispatchInitialMessages()
	{
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.PROFILES_VALUES_INITIALIZATION, this.profilingController.info.profiles);

		let lGameSettings_obj = {
			"weaponsSavingAllowed" : this.appParamsInfo.weaponsSavingAllowed
		}
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.GAME_SETTINGS_INITIALIZATION, lGameSettings_obj);
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.WEBGL_STATE_INFO, window.isWebGLSupported );

		let lSoundSettingsInfo_ssi = this._fSoundSettingsController_ssc.i_getInfo();
		let lSoundSettings_obj = {
			"fxVolume": 	lSoundSettingsInfo_ssi.i_getFxSoundsVolume(),
			"musicVolume": 	lSoundSettingsInfo_ssi.i_getBgSoundsVolume(),
			"loadingAvailable": lSoundSettingsInfo_ssi.soundsLoadingAvailable,
			"lobbySoundsLoadingOccurred": this.soundsLoadingInitiated
		};
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.SOUND_SETTINGS_CHANGED, lSoundSettings_obj);
		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.PLAYER_INFO_UPDATED, {data: this._fLobbyPlayerController_lpc.info.playerInfo});

		this.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.TOURNAMENT_STATE_CHANGED, {tournamentState: this.tournamentModeController.info.tournamentState});
	}

	tick(e)
	{
		let activeLayers = this.layout.activeLayersNames;

		if (this.tickerAllowed && !this.tickerPausedState["focus"])
		{
			super.tick(e);

			if (activeLayers.indexOf(DOM_LAYERS.DIALOGS) >= 0)
			{
				this.dialogsStage.tick(this.delta);
			}
		}
		else
		{
			this.layout.tick();
		}

		if (activeLayers.indexOf(DOM_LAYERS.COMMON_PANEL) >= 0)
		{
			for (var i = 0; i < this.commonPanelStages.length; i++)
			{
				this.commonPanelStages[i] && this.commonPanelStages[i].tick(this.delta);
			}
		}

		if (this.fullscreenNoteStage && activeLayers.indexOf(DOM_LAYERS.FULLSCREEN_NOTIFICATION) >= 0)
		{
			this.fullscreenNoteStage.tick(this.delta);
		}

		if (this._fTutorialStage_s)
		{
			this._fTutorialStage_s.tick(this.delta)
		}
	}

	//PENDING OPERATION...
	get pendingOperationController()
	{
		return this._fPendingOperationController_lpoc || (this._fPendingOperationController_lpoc = this._initPendingOperationController());
	}

	_initPendingOperationController()
	{
		let l_lpoc = this._providePendingOperationControllerInstance();
		l_lpoc.on(GUSLobbyPendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);

		this._fPendingOperationController_lpoc = l_lpoc;

		l_lpoc.i_init();

		return l_lpoc;
	}

	_providePendingOperationControllerInstance()
	{
		return new GUSLobbyPendingOperationController();
	}

	_onPendingOperationCompleted(aEvent_e)
	{
		this._tryToRestartLobbyIfNeeded();
	}
	//...PENDING OPERATION
}

export default GUSLobbyApplication;