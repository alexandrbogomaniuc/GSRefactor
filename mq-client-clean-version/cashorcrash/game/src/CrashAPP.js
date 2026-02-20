import Application from '../../../common/PIXI/src/dgphoenix/unified/controller/main/Application';
import GameLayout from './layout/GameLayout';
import {DOM_LAYERS} from './layout/GameLayout';
import GameSoundsController from './controller/sounds/GameSoundsController';
import SoundsBackgoundLoadingController from './controller/sounds/SoundsBackgoundLoadingController';
import SoundSettingsController from './controller/sounds/SoundSettingsController';
import Sprite from '../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import BrowserSupportController from '../../../common/PIXI/src/dgphoenix/unified/controller/preloading/BrowserSupportController';
import CustomerspecController from '../../../common/PIXI/src/dgphoenix/unified/controller/preloading/CustomerspecController';
import SecondaryScreenController from './controller/uis/custom/secondary/SecondaryScreenController';
import PaytableScreenController from './controller/uis/custom/secondary/paytable/PaytableScreenController';
import DialogsController from './controller/uis/custom/dialogs/DialogsController';
import DialogController from './controller/uis/custom/dialogs/DialogController';
import GameWebSocketInteractionController from './controller/interaction/server/GameWebSocketInteractionController';
import GameJSEnvironmentInteractionController from './controller/interaction/js/GameJSEnvironmentInteractionController';
import RedirectionController from './controller/interaction/browser/redirection/RedirectionController';
import VueApplicationController from './vue/VueApplicationController';
import KeyboardControlProxy from '../../../common/PIXI/src/dgphoenix/unified/controller/interaction/keyboard/KeyboardControlProxy';
import GameProfilingController from './controller/profiling/GameProfilingController';
import GameDebuggingController from './controller/debug/GameDebuggingController';
import GameErrorHandlingController from './controller/error/GameErrorHandlingController';
import SubloadingController from './controller/subloading/SubloadingController';
import DialogsView from './view/uis/custom/dialogs/DialogsView';
import CurrencyInfo from '../../../common/PIXI/src/dgphoenix/gunified/model/currency/CurrencyInfo';
import MTimeLine from '../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import GameScreenView from './view/main/GameScreenView';
import GameController from './controller/main/GameController';
import BattlegroundGameController from './controller/main/BattlegroundGameController';
import TripleMaxBlastModeController from './controller/main/TripleMaxBlastModeController';
import PendingOperationController from './controller/gameplay/PendingOperationController';
import CustomCurrencyInfo from './model/currency/CustomCurrencyInfo';
import { APP } from '../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

/**
 * Crash game application class.
 * @class
 * @augments Application
 */
class CrashAPP extends Application
{
	static get EVENT_ON_GAME_STARTED() 						{return "onGameStarted";}
	static get EVENT_ON_GAME_ASSETS_LOADING_ERROR() 		{return "onGameAssetsLoadingError";}
	static get EVENT_ON_GAME_PRELOADER_REMOVED()			{return "EVENT_ON_GAME_PRELOADER_REMOVED";}
	static get EVENT_ON_CURRENCY_INFO_UPDATED()				{return "EVENT_ON_CURRENCY_INFO_UPDATED";}
	static get EVENT_ON_PRELOADER_SOUNDS_READY()			{return "EVENT_ON_PRELOADER_SOUNDS_READY";}

	static get EVENT_ON_OFFLINE()							{return "EVENT_ON_OFFLINE";}
	static get EVENT_ON_ONLINE_RESTORED()					{return "EVENT_ON_ONLINE_RESTORED";}

	constructor(...args)
	{
		super(...args);

		this._forcedState = null;

		this._gameAppStarted = false;

		this._fGameScreenView_gsv = null;

		this.appParamsInfo.backgroundLoadingAllowed && (this._fSubloadingController_sc = new SubloadingController());

		this._fSecondaryScreenController_ssc = new SecondaryScreenController();
		this._fSoundSettingsController_ssc = new SoundSettingsController();
		this._fSoundsController_lsc = new GameSoundsController();

		this._fBrowserSupportController_bsc = new BrowserSupportController();
		this._fCustomerspecController_cc = new CustomerspecController();

		this._fSoundsBgLoadingController_ssblc = null;
		this.soundsLoadingInitiated = false;

		this._fGameDebuggingController_ldc = null;
		this._fGameErrorHandlingController_lehc = null;

		this._fTripleMaxBlastModeController_tmbmc = new TripleMaxBlastModeController();

		this._initVueApplicationController();
		this._initDialogsController();
		this._initErrorHandlingController();
		this._initJSEnvironmentInteractionController();
		this._initRedirectionController();
		this._initGameDebuggingController();

		this._generateGameController();
		
		this.appClientServerTime = Date.now();
		this._isOnline = true;
		this._roundId = 0;
	}

	_generateGameController()
	{
		if (this._fGameController_gc)
		{
			throw new Error ("GameController instance already exists!")
		}

		if (this.isBattlegroundGame)
		{
			this._fGameController_gc = new BattlegroundGameController();
		}
		else
		{
			this._fGameController_gc = new GameController();
		}
	}


	set forcedState(aStrin_val)
	{
		this._forcedState = aStrin_val;
	}

	get forcedState()
	{
		return this._forcedState;
	}

	//INIT...
	onApplicationReady()
	{
		this._fBrowserSupportController_bsc.init();
		this._fBrowserSupportController_bsc.on(BrowserSupportController.i_EVENT_BROWSER_SUPPORT_CONFIRMED, () => {this.emit("browserConfirmed")});

		this._fCustomerspecController_cc.init();

		this._fTripleMaxBlastModeController_tmbmc.init();

		this._fSoundSettingsController_ssc.init();
		this._fSoundSettingsController_ssc.once(SoundSettingsController.EVENT_ON_SOUND_SETTINGS_CHANGED, () => {this.emit(CrashAPP.EVENT_ON_SOUND_CONTEXT_NEED_TO_BE_CHECKED)});

		let soundSettingsInfo = this._fSoundSettingsController_ssc.i_getInfo();
		this._fSoundsController_lsc.init(this.config.audio.stereo, soundSettingsInfo.soundsLoadingAvailable,
																	soundSettingsInfo.soundsMuted,
																	soundSettingsInfo.i_getFxSoundsVolume(),
																	soundSettingsInfo.i_getBgSoundsVolume());

		this._fDialogsController_dsc.init();

		super.onApplicationReady();
	}

	//override
	onLoaderUIReady()
	{
		super.onLoaderUIReady();
		this._fDialogsController_dsc.initView(this.dialogsStage.view);
	}

	run()
	{
		super.run();

		this.initWebSocket();

		this._fGameController_gc.init();
		
		this._startHandlingServerMessages();

		this._fSecondaryScreenController_ssc.init();
		this._fSecondaryScreenController_ssc.initView();

		this._jsEnvironmentInteractionController.init();
		this._redirectionController.init();

		this.vueApplicationController.on(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onTimeToShowVueLayer, this);
		this.vueApplicationController.on(VueApplicationController.EVENT_TIME_TO_HIDE_VUE_LAYER, this._onTimeToHideVueLayer, this);

		let lPendingOperationController_poc = this.gameController.gameplayController.pendingOperationController;
		lPendingOperationController_poc.on(PendingOperationController.EVENT_ON_FATAL_PENDING_OPERATION_OCCURED, this._onFatalPendingOperationOccured, this);

		this.layout.fitLayout(true);

		this._gameAppStarted = true;
		this.emit(CrashAPP.EVENT_ON_GAME_STARTED);
	}

	/**
	 * Handles losing of access to the network by browser.
	 */
	handleOffline()
	{
		console.log("handle offline");
		this.emit(CrashAPP.EVENT_ON_OFFLINE);
		this._isOnline = false;
	}

	/**
	 * Handles gaining of access to the network by browser.
	 */
	handleOnline()
	{
		console.log("handle online");
		this.emit(CrashAPP.EVENT_ON_ONLINE_RESTORED);
		this._isOnline = true;
	}

	/**
	 * Main container for game actions.
	 * @type {Sprite}
	 */
	get gameScreenView()
	{
		return this._fGameScreenView_gsv;
	}

	get isOnline(){
		return this._isOnline;
	}

	set isOnline(aValue_bull)
	{
		this._isOnline = aValue_bull;
	}

	set roundId(aValue_int)
	{
		this._roundId = aValue_int;
	}

	get roundId()
	{
		return this._roundId;
	}

	_switchPreloaderToApplicationScreen()
	{
		this.layout.hidePreloader();

		this.removePreloader();

		this.emit(CrashAPP.EVENT_ON_GAME_PRELOADER_REMOVED);
	}

	
	get goToHomeParams()
	{
		return {sid:APP.urlBasedParams.SID, privateRoomID:APP.appParamsInfo.privateRoomId};
	}

	removePreloader()
	{
		this.dialogsController.view.parent.removeChild(this.dialogsController.view);

		super.removePreloader();
	}

	//override
	_addStages()
	{
		super._addStages();
		this.dialogsStage = this.addStage(DOM_LAYERS.DIALOGS, undefined, PIXI.RENDERER_TYPE.CANVAS);

		let margin = this.config.margin;
		let preloaderWidth_num = this.config.size.width + (margin.left || 0) + (margin.right || 0);
		let preloaderHeight_num = this.config.size.height  + (margin.top || 0) + (margin.bottom || 0);
		if (this.isMobile)
		{
			this.fullscreenNoteStage = this.addStage(DOM_LAYERS.FULLSCREEN_NOTIFICATION, {width:preloaderWidth_num, height:preloaderHeight_num}, PIXI.RENDERER_TYPE.CANVAS);
		}

		this._fGameScreenView_gsv = this.stage.view.addChild(new GameScreenView);
	}

	//override
	get applicationFolderURL()
	{
		let appFolderURL = this.appParamsInfo.gamePath;
		if (!appFolderURL)
		{
			appFolderURL = super.applicationFolderURL;
		}
		return appFolderURL;
	}

	//override
	get _unnecessaryPreloaderAssets()
	{
		return ['preloader/preloader_assets',
					'preloader/tips/tips_1', 'preloader/tips/tips_2', 'preloader/tips/tips_3',
					'preloader/click_continue', 'preloader/click_continue_portrait',
					'TAPreloaderBrand', 'TAPreloaderTip1', 'TAPreloaderTip2', 'TAPreloaderTip2', 'TALoadingHeaderBattleground', 'TALoadingHeader'];
	}

	get domLayoutClass()
	{
		return GameLayout;
	}

	//@override
	get isErrorHandlingMode()
	{
		//DEBUG...
		//return !this.isDebugMode;
		//...DEBUG
		return this.appParamsInfo.errorHandlingAllowed;
	}

	/**
	 * Checks if application launched on mobile device.
	 * @returns {boolean}
	 */
	get isMobile()
	{
		return this.layout.isMobile;
	}

	/**
	 * Indicates whether application is run or not.
	 * @type {boolean}
	 */
	get gameAppStarted()
	{
		return this._gameAppStarted;
	}

	//override
	resize(width, height)
	{
		super.resize(width, height);

		this.dialogsStage.resize(width, height);
		this.fullscreenNoteStage && this.fullscreenNoteStage.resize(width, height);
	}
	//...INIT

	/**
	 * Tick app stages
	 * @param {TickerInfo} e
	 * @override
	 */
	tick(e)
	{
		this._updateClientServerTime();

		if (this.tickerAllowed)
		{
			super.tick(e);
			MTimeLine.forceTick(e.delta);
		}

		let activeLayers = this.layout.activeLayersNames;

		if (activeLayers.indexOf(DOM_LAYERS.DIALOGS) >= 0)
		{
			this.dialogsStage.tick(e.delta);
		}

		if (this.fullscreenNoteStage && activeLayers.indexOf(DOM_LAYERS.FULLSCREEN_NOTIFICATION) >= 0)
		{
			this.fullscreenNoteStage.tick(e.delta);
		}
	}

	/**
	 * Update client-server time at the moment.
	 * @private
	 */
	_updateClientServerTime()
	{
		let lCurClientTime_int = Date.now();
		let wsController = this._webSocketInteractionController;

		if (!wsController || !wsController.info.isLastServerMessageTimeDefined)
		{
			this.appClientServerTime = lCurClientTime_int;
		}
		else
		{
			this.appClientServerTime = wsController.info.lastServerMessageTime + (lCurClientTime_int - wsController.info.lastServerMessageApplyTime);
		}
	}

	//override
	_initProfilingController()
	{
		return new GameProfilingController();
	}

	/**
	 * @type {TripleMaxBlastModeController}
	 */
	get tripleMaxBlastModeController()
	{
		return this._fTripleMaxBlastModeController_tmbmc;
	}

	/**
	 * @type {GameController|BattlegroundGameController}
	 */
	get gameController()
	{
		return this._fGameController_gc;
	}

	get minimalCafPlayersReady(){
		return this.gameController.gameplayController.roomController.info.minSeats;
	}

	get isCAFRoomManager(){
		return this.gameController.gameplayController.gamePlayersController.info.isCAFRoomManager;

	}

	get ownerNickname(){
		return this.gameController.gameplayController.info.gamePlayersInfo.observerId;
	}

	get isKicked(){
		return this.gameController.gameplayController.roomController.info.isKickedOutOfTheRoom;
		//return true;
	}

	get isCAFRoomManagerDefined()
	{
		return this.isCAFRoomManager !== undefined;
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

	/**
	 * @type {SubloadingController}
	 */
	get subloadingController()
	{
		return this._fSubloadingController_sc;
	}

	/**
	 * @type {SecondaryScreenController}
	 */
	get secondaryScreenController()
	{
		return this._fSecondaryScreenController_ssc;
	}

	/**
	 * @type {SoundSettingsController}
	 */
	get soundSettingsController()
	{
		return this._fSoundSettingsController_ssc;
	}

	/**
	 * @type {GameSoundsController}
	 */
	get soundsController()
	{
		return this._fSoundsController_lsc;
	}

	/**
	 * @type {BrowserSupportController}
	 */
	get browserSupportController()
	{
		return this._fBrowserSupportController_bsc;
	}

	/**
	 * @type {CustomerspecController}
	 */
	get customerspecController()
	{
		return this._fCustomerspecController_cc;
	}

	/**
	 * @type {DialogsController}
	 */
	get dialogsController()
	{
		return this._fDialogsController_dsc;
	}

	/**
	 * @type {GameJSEnvironmentInteractionController}
	 */
	get jsEnvironmentInteractionController()
	{
		return this._jsEnvironmentInteractionController;
	}

	/**
	 * @type {RedirectionController}
	 */
	get redirectionController()
	{
		return this._redirectionController;
	}

	/**
	 * @type {VueApplicationController}
	 */
	get vueApplicationController()
	{
		return this._fVueApplicationController_vac;
	}

	initSoundsBackgroundLoading()
	{
		this.soundsBgLoadingController.init();
	}

	/**
	 * @type {SoundsBackgoundLoadingController}
	 */
	get soundsBgLoadingController()
	{
		let lSoundsBgLoadingController_ssblc = this._fSoundsBgLoadingController_ssblc;
		if (!lSoundsBgLoadingController_ssblc)
		{
			lSoundsBgLoadingController_ssblc = new SoundsBackgoundLoadingController();
		}

		return lSoundsBgLoadingController_ssblc;
	}

	//SOCKET...
	_generateWebSocketInteractionInstance()
	{
		return new GameWebSocketInteractionController();
	}

	_startHandlingServerMessages()
	{
		let wsInteractionController = this.webSocketInteractionController;
		
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ENTER_GAME_MESSAGE, this._onServerEnterGameMessage, this);
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		if (GameWebSocketInteractionController.isFatalError(errorType))
		{
			this.__activateApplicationFatalError();
		}
	}

	_onServerEnterGameMessage(event)
	{
		this.currencyInfo.i_setCurrencyId(event.messageData.currency.code);
		this.emit(CrashAPP.EVENT_ON_CURRENCY_INFO_UPDATED);

		if (this.isPreloaderActive)
		{
			this._switchPreloaderToApplicationScreen();
		}
	}
	//...SOCKET

	playPreloaderSounds()
	{
		this.emit(CrashAPP.EVENT_ON_PRELOADER_SOUNDS_READY);
	}
	
	handleAssetsLoadingError(errorKey, errorMessage)
	{
		this.emit(CrashAPP.EVENT_ON_GAME_ASSETS_LOADING_ERROR, {key:errorKey, message:errorMessage});

		this.preLoader.stopSpinnerUpdate();
		this.layout.hidePrePreloader();

		/*TODO [os]:  hide game view ??? */
	}

	//DEBUG...
	/**
	 * @type {GameDebuggingController}
	 */
	get gameDebuggingController()
	{
		return this._fGameDebuggingController_ldc || (this._fGameDebuggingController_ldc = this._initGameDebuggingController());
	}

	_initGameDebuggingController()
	{
		let l_ldc = new GameDebuggingController();
		this._fGameDebuggingController_ldc = l_ldc;

		l_ldc.i_init();

		return l_ldc;
	}
	//...DEBUG

	//ERROR HANDLING...
	/**
	 * @type {GameErrorHandlingController}
	 */
	get errorHandlingController()
	{
		return this._fGameErrorHandlingController_lehc || (this._fGameErrorHandlingController_lehc = this._initErrorHandlingController());
	}

	_initErrorHandlingController()
	{
		let l_lehc = new GameErrorHandlingController();
		this._fGameErrorHandlingController_lehc = l_lehc;

		l_lehc.i_init();

		return l_lehc;
	}
	//...ERROR HANDLING

	//DIALOGS...
	_initDialogsController()
	{
		let dialogsController = this._fDialogsController_dsc = new DialogsController();
		dialogsController.on(DialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		dialogsController.on(DialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
		return dialogsController;
	}

	_onDialogActivated(event)
	{
		this.layout.showDialogsScreen();
		this.dialogsStage.render();
		this.setTickerPaused(false, "focus");
	}

	_onDialogDeactivated(event)
	{
		let dialogsController = this._fDialogsController_dsc;
		if (!dialogsController.info.hasActiveDialog)
		{
			this.dialogsStage.render();
			this.layout.hideDialogsScreen();
		}
	}
	//...DIALOGS

	//VUE...
	_initVueApplicationController()
	{
		this._fVueApplicationController_vac = new VueApplicationController();
		this._fVueApplicationController_vac.i_init();
	}

	_onTimeToShowVueLayer(event)
	{
		this.layout.showVueLayer();
	}

	_onTimeToHideVueLayer(event)
	{
		this.layout.hideVueLayer();
	}
	//...VUE

	_initJSEnvironmentInteractionController()
	{
		this._jsEnvironmentInteractionController = new GameJSEnvironmentInteractionController();
	}

	_initRedirectionController()
	{
		this._redirectionController = new RedirectionController();
	}

	_onFatalPendingOperationOccured(aEvent_evt)
	{
		this.__activateApplicationFatalError();
	}
}

export default CrashAPP;