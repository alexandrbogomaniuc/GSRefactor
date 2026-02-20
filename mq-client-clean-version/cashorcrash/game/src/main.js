import * as createjs from '../../../common/PIXI/src/dgphoenix/unified/lib/createjs-2015.11.26.corrected';
import UFullScreen from '../../../common/PIXI/src/dgphoenix/unified/view/layout/UFullScreen';
import CustomerspecController from '../../../common/PIXI/src/dgphoenix/unified/controller/preloading/CustomerspecController';

import XMLLoader from '../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/XMLLoader';
import Queue from '../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/Queue';
import { createLoader } from '../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders';
import ExternalAPI from '../../../common/PIXI/src/dgphoenix/unified/controller/main/ExternalAPI';
import I18 from '../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

import CrashAPP from './CrashAPP';

import PACKAGE from '../package.json';
import VERSION from '../version.json';
import CONFIG from './config/config.json';
import ASSETS from './config/assets.json';
import PAYTABLE_ASSETS from './config/paytable_assets.json';
import PRELOADER_ASSETS from './config/preloader_assets.json';
import {TRANSLATIONS_TYPES, APP_TYPES} from '../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/TranslationsQueue';
import LoaderUI from './external/ApplicationLoaderUI';

//----------------------------------------------------------------------------------------------------------------------
export default function (API = ExternalAPI, apiConfig = {}) {

	PIXI.settings.RENDER_OPTIONS.antialias = true;
	
	let external = new API(apiConfig);

	external.init(function (external) {

		//to prevent pinch zoom in iOS...
		window.addEventListener('touchmove', function (event) {
		  										if (event.scale !== 1) { event.preventDefault();}
											}, 
								{ passive: false });
		//... to prevent pinch zoom in iOS

		let preloaderQueue = null;
		let loaderUI = null;

		/*window.getPlatformInfo = function (){return {
			name: "Chrome for Android",version: "68.0",minVersion: "41.0",supported: true,
			mobile: true,soundEnabled: true,ua: navigator.userAgent,
			os: {name: "Android",version: "6.0",minVersion: "5.0"}}
		}*/

		var lPlatformInfo_obj = window.getPlatformInfo ? window.getPlatformInfo() : {};
		let lIsMobile_bln = lPlatformInfo_obj.mobile;
		
		if(lIsMobile_bln)
		{
			CONFIG.scales = [1];
			CONFIG.audio.stereo = false;
			CONFIG.margin.bottom = 0;
		}

		let APP = new CrashAPP(CONFIG, external, PACKAGE, VERSION);

		document.oncontextmenu = function(e) {APP.emit("contextmenu"); return false; }
		document.ondragstart = function() { return false }; // Prevent Internet Explorer’s Default Image Dragging Action

		if (lPlatformInfo_obj.os && lPlatformInfo_obj.os.name == "Mac OS" && lPlatformInfo_obj.name == "Safari")
		{
			window.onoffline = function ()
			{
				APP.handleOffline();
			}
			window.ononline = function ()
			{
				APP.handleOnline();
			}
		}

		APP.once('ready', applicationReady);
		APP.init(document.body);

		function applicationReady()
		{
			APP.customerspecController.once(CustomerspecController.EVENT_CUSTOMERSPEC_LOADED, onCustomerspecLoaded);
			APP.customerspecController.once(CustomerspecController.EVENT_CUSTOMERSPEC_LOAD_ERROR, onCustomerspecLoadError);

			APP.customerspecController.load();
		}

		function onCustomerspecLoaded()
		{
			onCustomerspecDescriptorReady();
		}

		function onCustomerspecLoadError()
		{
			onCustomerspecDescriptorReady();
		}

		function onCustomerspecDescriptorReady()
		{
			I18.init();

			loadScript(APP.applicationFolderURL + 'common_ue.js', APP.version, onCommonUeScriptLoaded.bind(this));
			
			hideFullscreenNote();
		}

		function hideFullscreenNote()
		{
			APP.layout.hideFullscreenNotificationScreen();
		}

		function showFullscreenNote()
		{
			APP.layout.showFullscreenNotificationScreen();
		}

		function loadPreloaderAssets()
		{
			let loader = new Queue();
			let translationsQueue = I18.createLoaderQueue
				(
					{
						type: TRANSLATIONS_TYPES.PRELOADER, 
						appType: APP_TYPES.GAME
					}
				);

			translationsQueue.once('complete', () => 
				{
					loader.add
					(
						I18.createAssetsLoaderQueue(translationsQueue)
					)
				});

			loader.add
			(
				translationsQueue,
				APP.library.createLoaderQueue(PRELOADER_ASSETS.images)
			);

			loader.once('error', onPreloaderAssetsLoadingError);
			loader.once('complete', onPreloaderAssetsLoaded);
			loader.load();
			return loader;
		}

		function onPreloaderAssetsLoadingError(e)
		{
			APP.handleAssetsLoadingError(e.key, e.message);
		}

		function onPreloaderAssetsLoaded()
		{
			tryToActivateGamePreloader();
		}

		function tryToActivateGamePreloader()
		{
			console.log("tryToActivateGamePreloader");
			if (!preloaderQueue || !preloaderQueue.complete)
			{
				return;
			}

			if (!APP.browserSupportController.info.confirmed)
			{
				APP.once("browserConfirmed", tryToActivateGamePreloader);
				return;
			}

			if (lIsMobile_bln)
			{
				let fullscreen = new UFullScreen();

				fullscreen.on(UFullScreen.EVENT_HIDE_NOTE, hideFullscreenNote.bind(this));
				fullscreen.on(UFullScreen.EVENT_SHOW_NOTE, showFullscreenNote.bind(this));

				fullscreen.tryToEnable(APP.fullscreenNoteStage.view, APP.fullscreenNoteStage.layout);
			}

			APP.showPreloader();
			loadGameAssets();
		}

		function loadScript(src, version, onLoadHandler)
		{
			var lScript_e = document.createElement('script');
			lScript_e.type = 'text/javascript';
			lScript_e.src = src + (version ? '?version=' + version : '');
			lScript_e.async = false;
			if (onLoadHandler)
			{
				lScript_e.onload = onLoadHandler;
			}
			document.head.appendChild(lScript_e);
		}

		function onCommonUeScriptLoaded(e)
		{
			let time = (new Date()).getTime();
			common_ue.startProfiling((aProfiles_obj) => {
				//DEBUG...
				//console.log("time taken for gathering profiles: " + ((new Date()).getTime() - time) + "ms");
				//...DEBUG

				APP.i_onProfilesReady(aProfiles_obj);

				preloaderQueue = loadPreloaderAssets();
				
			});
		}

		function loadGameAssets()
		{
			loaderUI = APP.loaderUI;
			loaderUI.createLayout();
			loaderUI.once('complete', preloadComplete);
			let loader = loaderUI.loader;

			if (APP.soundSettingsController.info.soundsMuted)
			{
				//don't load sounds before sounds on (https://jira.dgphoenix.com/browse/CRG-74 - 0019358: CRSP-74 MAKE GAME AUDIO OFF BY DEFAULT)
				APP.initSoundsBackgroundLoading();
			}
			else
			{
				APP.soundsLoadingInitiated = true;
				let preloaderSoundsQueue = APP.soundsController.createLoaderQueue(PRELOADER_ASSETS.sounds);
				preloaderSoundsQueue.once('error', onPreloaderSoundsAssetsLoadingError);
				preloaderSoundsQueue.once('complete', onPreloaderSoundsAssetsLoaded);

				loader.add
				(
					preloaderSoundsQueue,
					APP.soundsController.createLoaderQueue(ASSETS.sounds)
				)
			}

			let translationsQueue = I18.createLoaderQueue();
			translationsQueue.addTranslationDescriptor(TRANSLATIONS_TYPES.APP, APP_TYPES.GAME, false);

			if(!APP.appParamsInfo.backgroundLoadingAllowed)
			{
				translationsQueue.addTranslationDescriptor(TRANSLATIONS_TYPES.PAYTABLE, APP_TYPES.GAME, false);
			}

			translationsQueue.once('complete', () => {
				loader.add
				(
					I18.createAssetsLoaderQueue(translationsQueue)
				)
			});

			loader.add
			(	
				translationsQueue,
				APP.library.createLoaderQueue(ASSETS.images),
				APP.fonts.createLoaderQueue(ASSETS.fonts)
			);

			if (!APP.appParamsInfo.backgroundLoadingAllowed)
			{
				loader.add
				(
					APP.library.createLoaderQueue(PAYTABLE_ASSETS.images)
				);
			}

			loader.once('error', onGameAssetsLoadingError);
			loader.load();
		}

		function onPreloaderSoundsAssetsLoadingError(e)
		{
			loaderUI && loaderUI.loader.stopLoading();
			APP.handleAssetsLoadingError(e.key, e.message);
		}

		function onPreloaderSoundsAssetsLoaded(e)
		{
			APP.playPreloaderSounds();
		}

		function onGameAssetsLoadingError(e)
		{
			loaderUI.loader.stopLoading();
			APP.handleAssetsLoadingError(e.key, e.message);
		}

		function preloadComplete(e)
		{
			if (APP.isBattlegroundGame)
			{
				loaderUI.showClickToStart();
				loaderUI.once(LoaderUI.EVENT_ON_CLICK_TO_START_CLICKED, onPreloaderClickToStart);
			}
			else
			{
				APP.run();
			}
		}

		function onPreloaderClickToStart()
		{
			APP.browserSupportController.unlockContext();

			APP.run();
		}

	});

}
