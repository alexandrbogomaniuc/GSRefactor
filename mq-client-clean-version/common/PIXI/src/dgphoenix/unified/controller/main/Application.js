import * as GLOBALS from './globals';

import EventDispatcher from '../events/EventDispatcher';

import Fonts from '../../model/resources/Fonts';
import AssetsLibrary from '../../model/resources/AssetsLibrary';

import KeyboardControlProxy from '../interaction/keyboard/KeyboardControlProxy';
import ContentPathURLsProvider from '../../model/resources/ContentPathURLsProvider';
import APPParamsController from '../custom/APPParamsController';
import ProfilingController from '../profiling/ProfilingController';
import SanitizerController from '../sanitizer/SanitizerController';

import DOMLayout, { DOM_LAYERS, ORIENTATION } from '../../view/layout/DOMLayout';

import Stage from '../../view/base/display/Stage';
import Ticker from '../time/Ticker';
import Timer from '../time/Timer';

import ExternalAPI from './ExternalAPI';
import { Utils } from '../../model/Utils';
import CurrencyInfo from '../../../gunified/model/currency/CurrencyInfo';
import CommonAssetsController from '../preloading/CommonAssetsController';
import LoggingController from '../interaction/server/LoggingController';

let INSTANCE = null;

/**
 * @class
 * @inheritDoc
 * @classdesc Base Application class
 */
class Application extends EventDispatcher {

	static get EVENT_ON_WEBGL_CONTEXT_LOST() 					{return "EVENT_ON_WEBGL_CONTEXT_LOST";}
	static get EVENT_ON_TICK_TIME() 							{return "EVENT_ON_TICK_TIME";}
	static get EVENT_ON_SOUND_CONTEXT_NEED_TO_BE_CHECKED() 		{return "EVENT_ON_SOUND_CONTEXT_NEED_TO_BE_CHECKED";}
	static get EVENT_ON_ROOM_PAUSED()							{return 'onTickerPaused';}
	static get EVENT_ON_ROOM_RESUMED()							{return 'onTickerResumed';}
	static get EVENT_ON_APPLICATION_FATAL_ERROR() 				{return "EVENT_ON_APPLICATION_FATAL_ERROR";}
	static get EVENT_ON_CLOSE_GAME_SESSION()						{return "EVENT_ON_LEAVE_GAME_START";}
	static get EVENT_IS_LOBBY_APP()								{return "EVENT_IS_LOBBY_APP";}

	/**
	 * Singleton
	 * @returns {Application}
	 */
	static getInstance() {
		if (!GLOBALS.APP) {
			throw new Error('You must create application instance first.');
		}
		return GLOBALS.APP;
	}

	get screenWidth()
	{
		return this.config.size.width;
	}

	get screenHeight()
	{
		return this.config.size.height;
	}

	get commonAssetsController()
	{
		return this._fCommonAssetsController_cac;
	}

	/**
	 * @constructor
	 * @param {Object} config
	 * @param {string} external
	 * @param {string} package
	 * @param {string} version
	 * @extends EventDispatcher
	 * @inheritDoc
	 */
	constructor(CONFIG, external, PACKAGE, VERSION)
	{
		if (GLOBALS.APP) {
			return GLOBALS.APP;
		}

		super();
		this._blockRequests = false;
		this._isLobbyApp = false;
		this._criticalAnimationInProgress = false;

		/**
		 * Unique app id
		 * @type {string}
		 */
		this.id = PACKAGE.name;

		/**
		 * app name
		 * @type {string}
		 */
		this.name = PACKAGE.description;

		/**
		 * app version
		 * @type {string}
		 */
		this.version = VERSION.version;

		/**
		 * last build date
		 * @type {string}
		 */
		this.date = VERSION.date;

		this.isApplicationFatalErrorOccured = false;

		this.sanitizerController = new SanitizerController();

		this.contentPathURLsProvider = new ContentPathURLsProvider();

		this.keyboardControlProxy = new KeyboardControlProxy();

		/**
		 * Application initial window params
		 * @type {APPParamsController}
		 */
		this.appParamsController = new APPParamsController();
		this.appParamsInfo = this.appParamsController.info;

		/**
		 * Profiling Controller
		 * @type {string}
		 * @private
		 */
		this._fProfilingController_pc = this._initProfilingController();
		this._fProfilingInfo_pi = this._fProfilingController_pc.info;

		/**
		 * Controller for assets from common folder
		 * @type {CommonAssetsController}
		 * @private
		 */
		this._fCommonAssetsController_cac = this._initCommonAssetsController();

		/**
		 * Configuration
		 * @type {Object}
		 */
		this.config = CONFIG || {};
		this.config.landscapeSize = { width: this.config.size.width, height: this.config.size.height }

		/**
		 * App ticker
		 * @type {Ticker}
		 */
		this.ticker = Ticker;

		/**
		 * Indicators of ticker pause state
		 */
		this.tickerPausedStateDelayed = {};
		this.tickerPausedState = {};
		this.tickerAllowed = true;

		/**
		 * Assets (images) library
		 * @type {AssetsLibrary}
		 */
		this.library = null;

		/**
		 * Opentype fonts
		 * @type {Fonts}
		 */
		this.fonts = new Fonts();

		/**
		 * DOM
		 * @type {DOMLayout}
		 */
		this.layout = null;
		
		/**
		 * ExternalAPI
		 * @type {ExternalAPI}
		 */
		this.external = external || new ExternalAPI;

		/**
		 * Logging controller
		 * @type {LoggingController}
		 */
		this.logger = new LoggingController();
		this.appParamsInfo.clientLogLevel && this.logger.i_setNewLoggingLevel(this.appParamsInfo.clientLogLevel);

		/**
		 * Data storage
		 * @type {LocalStorage}
		 */
		this.storage = null;

		/**
		 * Array of stages
		 * @type {Array.Stage}
		 */
		this.stages = [];

		/**
		 * Set of get params
		 * @type {Object}
		 * @example 
		 * {
		 * 		LANG: "en",
		 * 		BANKID: 271
		 * }
		 */
		this.urlBasedParams = this.sanitizerController.sanitizer(Utils.parseGet());

		/**
		 * Indicators of fps meter
		 */
		this._fps = 0;
		this._lastFPS = 60;
		this._fpsClearTime = 0;
		
		/**
		 * App folder absolute url
		 * @type {Object}
		 */
		this.folderURL = this.applicationFolderURL;

		/**
		 * Indicates whether app loader is ready ot not
		 * @type {boolean}
		 * @private
		 */
		this._loaderReady = false;

		/**
		 * App socket controller
		 * @type {WebSocketInteractionController}
		 * @private
		 */
		this._webSocketInteractionController = null;

		GLOBALS.setApplication(this);

		if (this.__isCurrencyInfoSupported())
		{
			/**
			 * Currency info
			 * @type {CurrencyInfo}
			 * @private
			 */
			this._fCurrencyInfo_ci = this.__generateCurrencyInfo();
		}

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 107) //+
	// 	{
			// this.handleShowWindow();
	// 	}

	// 	if (keyCode.keyCode == 109) //-
	// 	{
			// this.handleHideWindow();
	// 	}		
	// }
	//...DEBUG

	/**
	 * Initialization of application
	 * @param {*} container 
	 */
	init(container)
	{
		this.ticker.on("tick", this.tick, this);

		this.createLayout(container);

		this.detectBitmapScale();
		this.library = new AssetsLibrary();

		this._addStages();

		if (this.layout.isPortraitOrientation && this.layout.isPortraitModeSupported)
		{
			this.handleOrientationChange( { orientation: this.layout.orientation, locked: this.layout.isScreenOrientationLocked } );
		}
		
		if (this.layout.autoFit)
		{
			this.layout.fitLayout(true);
		}

		this.layout.showPrePreloader();

		this.external.init(this.onApplicationReady.bind(this));
	}

	//CURRENCY INFO...
	/**
	 * Indicates if application supports CurrencyInfo
	 * @returns {boolean}
	 */
	__isCurrencyInfoSupported()
	{
		return false;
	}

	__generateCurrencyInfo()
	{
		return new CurrencyInfo();
	}

	/**
	 * Currency Info
	 * @returns {CurrencyInfo}
	 */
	get currencyInfo()
	{
		return this._fCurrencyInfo_ci;
	}
	//...CURRENCY INFO

	_initProfilingController()
	{
		return new ProfilingController();
	}

	//COMMON ASSETS...
	_initCommonAssetsController()
	{
		let l_casc = this.__provideCommonAssetsControllerInstance();
		l_casc.init();

		return l_casc;
	}

	__provideCommonAssetsControllerInstance()
	{
		return new CommonAssetsController();
	}
	//...COMMON ASSETS

	//BATTLEGROUND...
	/**
	 * Indicates if the game launched in Battleground mode
	 * @returns {boolean} Returns true if game is launched in Battleground mode
	 */
	get isBattlegroundGame()
	{
		//DEBUG...
		if(!this.appParamsInfo.isBattlegroundGame)
		{
			switch(this.appParamsInfo.gameId)
			{
				case 856:
					return true;
			}
		}
		//...DEBUG

		return this.appParamsInfo.isBattlegroundGame;
	}

	/**
	 * Indicates if the game launched in CAF mode
	 * @returns {Boolean} Returns true if game is launched in CAF mode
	 */
	get isCAFMode()
	{
		 /*TODO [os]: debug...*/
		// return true;
		/*TODO [os]: ...debug*/
		return this.appParamsInfo.privateRoomId ? true : false;
	}
	//...BATTLEGROUND

	/**
	 * Profilling controller instance
	 */
	get profilingController()
	{
		return this._fProfilingController_pc;
	}

	/**
	 * Indicates whether application expects to use Pixi Heaven library or not
	 * @default [false]
	 */
	get isHeavenSpineUsageAllowed()
	{
		return false;
	}

	/**
	 * Indicates whether browser supports Pixi Heaven library or not
	 * @default [false]
	 */
	get isPixiHeavenLibrarySupported()
	{
		return (navigator.userAgent.indexOf('Edge') < 0 && navigator.userAgent.indexOf('MSIE') < 0 && navigator.userAgent.indexOf('Trident') < 0
			&& window.isWebGLSupported !== false)
	}

	/**
	 * Application preloader view
	 * @type {LoaderUI}
	 */
	get loaderUI()
	{
		return this._loaderUI || (this._loaderUI = this.initLoderUI());
	}

	initLoderUI(aLoaderQueue_sq)
	{
		let loaderUI = new this.external.LoaderUI(this.layout);
		return loaderUI;
	}

	_addStages()
	{
		let margin = this.config.margin;
		let preloaderWidth_num = this.config.size.width + (margin.left || 0) + (margin.right || 0);
		let preloaderHeight_num = this.config.size.height  + (margin.top || 0) + (margin.bottom || 0);

		this.addStage(DOM_LAYERS.PRELOADER, {width:preloaderWidth_num, height:preloaderHeight_num}, PIXI.RENDERER_TYPE.WEBGL);
		this.addStage(DOM_LAYERS.APP_SCREENS);
	}

	/**
	 * Absolute application folder url
	 * @type {string}
	 */
	get applicationFolderURL()
	{
		let appLocation = window.location;

		let appFolderURL = appLocation.protocol + "//" + appLocation.host + appLocation.pathname;
		let lastSlashPos = appFolderURL.lastIndexOf("/");

		appFolderURL = appFolderURL.substring(0, lastSlashPos+1);

		return appFolderURL;
	}

	initSoundsBackgroundLoading()
	{
	}

	set loaderReady (val)
	{
		if (this._loaderReady === val)
		{
			return;
		}


		this._loaderReady = val;

		if (this._loaderReady)
		{
			this.onLoaderUIReady();

		}
	}

	get loaderReady()
	{
		return _loaderReady;
	}

	get isAutoFireMode()
	{
		return false;
	}

	get freeMoneySign()
	{
		if(this.applicationFolderURL.indexOf("maxquest")>-1)
		{
			return "QC"; 
		}
		return "PM";
	}

	get isDebugMode()
	{
		return this.urlBasedParams.DEBUG === "1" || this.urlBasedParams.DEBUG === "true";
	}

	get isDebugsrmsg()
	{
		return this.urlBasedParams.DEBUGSRMSG === "1" || this.urlBasedParams.DEBUGSRMSG === "true";
	}

	/**
	 * Indicates whether application supports error notifications or not.
	 * If `true` - Runtime Error Dialog will be shown by thrown exception.
	 * @type {boolean}
	 * @default [false]
	 */
	get isErrorHandlingMode()
	{
		return this.urlBasedParams.ERROR_HANDLING === "1" || this.urlBasedParams.ERROR_HANDLING === "true";
	}

	onLoaderUIReady()
	{
	}

	/**
	 * Application initialization completed.
	 */
	onApplicationReady()
	{
		this.preLoader = new this.external.PreLoaderUI(this.layout);
		this.statusBar = new this.external.StatusBarUI(this.layout, this.name, this.version, this.date);
		this.layout.on("statusbardisplayed", this.onStatusBarDisplayed, this);

		/**
		 * End of application initialization
		 * @event Application#ready
		 */
		this.emit("ready");
	}

	goToHome()
	{
		setTimeout(()=>{
			let lAppParams = this.appParamsInfo;
			if (lAppParams.homeFuncNameDefined)
			{
				let lHomeFuncName_str = lAppParams.homeFuncName;
				if (lHomeFuncName_str)
				{
					this._callWindowMethod(lHomeFuncName_str);
				}else{
					console.warn("Home function was not properly provided by wrapper");
				}
			}else{
				console.warn("Home function was not provided at all by wrapper");
			}
		},200);
		
		this.emit(Application.EVENT_ON_CLOSE_GAME_SESSION);
		this._blockRequests = true;
	}

	_callWindowMethod(aMethodName_str, aParams_obj_arr) {
		var lRet_obj;
		try {
			lRet_obj = window[aMethodName_str].apply(window, aParams_obj_arr);
		}
		catch (a_obj) {
			throw new Error(`An error occured while trying to call JS Environment method: METHOD NAME = ${aMethodName_str}; PARAMS = ${aParams_obj_arr}`);
		}
		return lRet_obj;
	}

	detectBitmapScale() {
		this.layout.detectBitmapScale(this.config.scales);
	}

	createLayout(container) {
		this.layout = new this.domLayoutClass(container, this.config.size, this.config.margin, this.config.noCheckOrientation);

		this.layout.addMobileListeners();
		if (!this.layout.windowVisible)
		{
			this.handleHideWindow();
		}

		this.layout.on(DOMLayout.EVENT_ON_ORIENTATION_CHANGED, this.handleOrientationChange, this);
		this.layout.on("hidewindow", this.handleHideWindow, this);
		this.layout.on("showwindow", this.handleShowWindow, this);

		this.layout.autoFit = true;

		return this.layout;
	}

	get domLayoutClass()
	{
		return DOMLayout;
	}

	handleOrientationChange(e)
	{
		let lDefaultLandscapeSize = this.config.landscapeSize;
		if (e.orientation == ORIENTATION.PORTRAIT && !e.locked)
		{
			this.resize(lDefaultLandscapeSize.height, lDefaultLandscapeSize.width);
		}
		else
		{
			this.resize(lDefaultLandscapeSize.width, lDefaultLandscapeSize.height);
		}
		
		if (this.stage)
		{
			this.setTickerPaused(e.locked, "orientation");
		}
	}

	handleHideWindow(e)
	{
		if(this._criticalAnimationInProgress) return;
		this.setTickerPaused(true, "visibility");
	}

	handleShowWindow(e)
	{
		this.setTickerPaused(false, "visibility");
	}

	setTickerPaused(state, owner = null, data = null, delay = 0)
	{
		if (delay > 0)
		{
			this.tickerPausedStateDelayed[owner] && this.tickerPausedStateDelayed[owner].destructor();
			this.tickerPausedStateDelayed[owner] = new Timer((e) => {
				this.tickerPausedStateDelayed[owner] && this.tickerPausedStateDelayed[owner].destructor();
				this.tickerPausedStateDelayed[owner] = null;
				this.setTickerPaused(state, owner, data);
			}, delay);
			return;
		}

		if (owner)
		{
			this.tickerPausedState[owner] = state;
		}

		this.tickerAllowed = this.calcUnpauseState(this.getTickerPauseExceptions());
		let soundsAllowed = this.calcUnpauseState(this.getSoundsPauseExceptions());

		if (state)
		{
			if (!this.tickerAllowed)
			{
				this.ticker.stop();
			}

			this.onTickerPaused();

			if (!soundsAllowed)
			{
				this.onSoundsPaused();
			}
		}
		else
		{
			if (this.tickerAllowed)
			{
				this.ticker.start();
				
				this.onTickerResumed(data);
			}

			if (soundsAllowed)
			{
				this.onSoundsResumed();
			}
		}
	}

	getTickerPauseExceptions()
	{
		return [];
	}

	getSoundsPauseExceptions()
	{
		return [];
	}

	calcUnpauseState(aOptExceptions_arr = [])
	{
		let allowed = true;
		for (let owner in this.tickerPausedState)
		{
			if (this.tickerPausedState[owner] === true && aOptExceptions_arr.indexOf(owner) < 0)
			{
				allowed = false;
			}
		}
		return allowed;
	}

	onTickerPaused()
	{
		
		if (!this.tickerAllowed)
		{
			this.layout.resetOrientation();
			this.layout.autoFit = false;
		}
		this._tickerPausedAt_dt = Date.now();
		this.emit(Application.EVENT_ON_ROOM_PAUSED);
	}

	onTickerResumed(data = null)
	{
		if (this.tickerAllowed && !this.layout.isLastOrientationDefined)
		{
			this.layout.fitLayout(true);
			this.layout.autoFit = true;
		}
		this._tickerResumedAt_dt = Date.now();
		this.emit(Application.EVENT_ON_ROOM_RESUMED);
	}

	get tickerResumedDif()
	{
		return this._tickerResumedAt_dt - this._tickerPausedAt_dt;
	}

	onSoundsPaused()
	{
		this.emit("onSoundsPaused");
	}

	onSoundsResumed()
	{
		this.emit("onSoundsResumed")
	}

	/**
	 * Application size update
	 * @param {number} width
	 * @param {number} height
	 */
	resize(width, height) {
		this.config.size.width = width;
		this.config.size.height = height;

		for(var stage of this.stages) {
			stage.resize(width, height);
		}

		this.preloaderStage && this.preloaderStage.resize(width, height);

		this.layout.size.width = width;
		this.layout.size.height = height;
		
		this.layout.fitLayout(true);
	}

	/**
	 * Adds new stage
	 * @param {string} wrapperName Stage DOM container 
	 * @param {Object} aSize_obj Stage size
	 * @param {number} aTypeId_int Stage canvas renderer type
	 * @returns {Stage}
	 */
	addStage(wrapperName=DOM_LAYERS.APP_SCREENS, aSize_obj = null, aTypeId_int = PIXI.RENDERER_TYPE.UNKNOWN)
	{
		let lConfig_obj = Utils.clone(this.config);
		if (aSize_obj)
		{
			if (aSize_obj.width)
			{
				lConfig_obj.size.width = aSize_obj.width;
			}
			if (aSize_obj.height)
			{
				lConfig_obj.size.height = aSize_obj.height;
			}
		}

		let stage = new Stage(this.layout, lConfig_obj, this.layout.bitmapScale, aTypeId_int, wrapperName);
		stage.on(Stage.EVENT_ON_WEBGL_CONTEXT_LOST, this._onWebglContextLost, this);
		stage.on(Stage.EVENT_ON_WEBGL_CONTEXT_RESTORED, this._onWebglContextRestored, this);

		if (wrapperName===DOM_LAYERS.APP_SCREENS)
		{
			this.stages.push(stage);
		}
		else if (wrapperName === DOM_LAYERS.PRELOADER)
		{
			this.preloaderStage = stage;
		}

		return stage;
	}

	/**
	 * Webgl context lost handler
	 * @param {Object} event 
	 */
	_onWebglContextLost(event)
	{
		this.emit(Application.EVENT_ON_WEBGL_CONTEXT_LOST);

		this.__activateApplicationFatalError();
	}

	/**
	 * Webgl context restore handler
	 * @param {Object} event 
	 */
	_onWebglContextRestored(event)
	{
		// not handled for now,
		// use this info to handle:
		// https://developer.mozilla.org/ru/docs/Web/API/WebGLRenderingContext/isContextLost
		// https://www.khronos.org/webgl/wiki/HandlingContextLost
	}

	__activateApplicationFatalError()
	{
		if (this.isApplicationFatalErrorOccured)
		{
			return;
		}
		
		this.isApplicationFatalErrorOccured = true;
		this.emit(Application.EVENT_ON_APPLICATION_FATAL_ERROR);
	}

	/**
	 * Indicates whether preloader stage active or not.
	 * Preloader stage is removed when app runs.
	 */
	get isPreloaderActive()
	{
		return this.preloaderStage !== null;
	}


	isLobbyApp()
	{
		this.emit(Application.EVENT_IS_LOBBY_APP);
	}

	/**
	 * Destroy app preloader
	 */
	removePreloader()
	{
		if (!this.preloaderStage)
		{
			return;
		}

		this.layout.removeLayer(DOM_LAYERS.PRELOADER);
		this.loaderUI.destructor();

		this.preloaderStage.destroy();
		this.preloaderStage = null;

		this.library.destroyAssets(this._unnecessaryPreloaderAssets);
	}

	/**
	 * Array of preloader assets that can be removed (including textures) when app runs
	 * @protected
	 */
	get _unnecessaryPreloaderAssets()
	{
		return null;
	}

	get stage() {
		return this.stages[this.stages.length-1] || null;
	}

	set stage(stage) {

	}

	set criticalAnimationInProgress(a_valBool)
	{
		this._criticalAnimationInProgress = a_valBool;
	}


	getStage(ix) {
		return this.stages[ix] || null;
	}

	/**
	 * Define application profiles
	 * @param {Object} aProfilesObj_obj 
	 */
	i_onProfilesReady(aProfilesObj_obj) {
		this._fProfilingController_pc.init(aProfilesObj_obj);
	}

	/**
	 * Switch to preloader screen.
	 */
	showPreloader()
	{
		this.preLoader.stopSpinnerUpdate();
		this.layout.showPreloader();
	}

	playPreloaderSounds()
	{
	}

	playBackgroundSoundName()
	{
	}

	/**
	 * Run application when preloader completed.
	 */
	run() {
		this.layout.showScreens();
	}

	/**
	 * Init websocket and establish connection
	 */
	initWebSocket() {
		this.webSocketInteractionController.init();
	}

	get isWebSocketInteractionInitiated()
	{
		return this._webSocketInteractionController !== null;
	}


	get criticalAnimationInProgress()
	{
		return this._criticalAnimationInProgress;
	}

	get webSocketInteractionController()
	{
		return this._webSocketInteractionController || (this._webSocketInteractionController = this._initWebSocketInteraction());
	}

	get lastFps()
	{
		return this._lastFPS;
	}

	_initWebSocketInteraction()
	{
		let webSocketInteractionController = this._generateWebSocketInteractionInstance();

		return webSocketInteractionController;
	}

	_generateWebSocketInteractionInstance()
	{
		return null; // must be overridden
	}

	/**
	 * force rendering immediately.
	 */
	forceRendering()
	{
		this.tick({delta: 0});
	}

	/**
	 * Tick app stages
	 * @param {TickerInfo} e - Tick event
	 * @param {number} e.delta - Time elapsed in milliseconds from last tick to this tick capped by setting Ticker.MAX_DELTA.
	 * @param {number} e.realDelta - Time elapsed in milliseconds from last tick to this tick.
	 */
	tick(e) 
	{
		this.layout.tick();

		this.emit(Application.EVENT_ON_TICK_TIME, e);

		this._fpsClearTime += e.delta;
		if(this._fpsClearTime >= 1000)
		{
			this._fpsClearTime -= 1000;
			this._lastFPS = this._fps;
			this._fps = 0;

			this.__updateFPSDebug();
		}

		this._fps++;

		let activeLayers = this.layout.activeLayersNames;
		if (activeLayers.indexOf(DOM_LAYERS.PRELOADER) >= 0)
		{
			this.loaderReady = true;
			this.preloaderStage && this.preloaderStage.tick(e.delta);
		}
		else
		{
			for(let stage of this.stages)
			{
				stage.tick(e.delta);
			}
		}
	}

	__updateFPSDebug()
	{
		this.statusBar.updateStatusBar(false, this._lastFPS);
	}

	onStatusBarDisplayed()
	{
		this.statusBar.updateStatusBar(true, this._lastFPS/*this.isDebugMode ? this._lastFPS : false*/);
	}

	checkAudioContext()
	{
		this.emit(Application.EVENT_ON_SOUND_CONTEXT_NEED_TO_BE_CHECKED);
	}

	get areRequestsBlocked()
	{
		return this._blockRequests;
	}

	get isNewMatchMakingSupported()
	{
		return true;
	}

	get btgRoundDuration() {
		let duration = 0;
		switch (this.appParamsInfo.gameId) {
			case 867:
				duration = 60000;
				break;
			case 862:
				duration = 90000;
				break;
			case 856:
				duration = 90000;
				break;
		}

		return duration;
	}
	
	get isTestApp() {
		let appLocation = window.location;
		let appFolderURL = appLocation.protocol + "//" + appLocation.host + appLocation.pathname;
		return appFolderURL.indexOf("//test." > -1)
	}
}

export default Application;