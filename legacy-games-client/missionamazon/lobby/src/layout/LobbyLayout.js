import DOMLayout, {DOM_LAYERS} from '../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';
import EventDispatcher from '../../../../common/PIXI/src/dgphoenix/unified/controller/events/EventDispatcher';
import CommonPanelController from '../controller/uis/custom/commonpanel/CommonPanelController';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import * as FEATURES from '../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';

class GameLayer extends EventDispatcher 
{
	static get EVENT_ON_GAME_FRAME_READY()
	{
		return 'onFrameReady';
	}

	static create(container, gameUrl, isIPhone) 
	{
		let gameFrame = container.appendChild(document.createElement('iframe'));
		gameFrame.className = 'app-game-frame';
		gameFrame.style.position = 'absolute';
		gameFrame.style.width = "100%";
		gameFrame.style.height = "100%";
		gameFrame.style.border = "none";
		gameFrame.style.backgroundColor = "black";
		gameFrame.src = gameUrl;
		
		return new GameLayer(gameFrame);
	}

	constructor(gameFrame) 
	{
		super();

		this._fVueScreenNode_obj = null;
		this.gameFrame = gameFrame;

		this.gameFrame.onload = this.onGameFrameReady.bind(this);
	}

	get gameUrl()
	{
		return this.gameFrame ? this.gameFrame.src : null;
	}

	onGameFrameReady()
	{
		this.emit(GameLayer.EVENT_ON_GAME_FRAME_READY);
	}
}


/** @ignore */
class LobbyLayout extends DOMLayout 
{
	static get EVENT_ON_GAME_READY()
	{
		return 'onGameReady';
	}

	constructor(container, size = {width: 960, height: 540}, margin = {}, noCheckOrientation=false) 
	{
		DOM_LAYERS.GAMES = 'games';
		DOM_LAYERS.COMMON_PANEL = "commonPanel";
		DOM_LAYERS.TUTORIAL = "tutorial";
		DOM_LAYERS.DIALOGS = 'dialogs';
		DOM_LAYERS.FULLSCREEN_NOTIFICATION = "fullscreenNote";

		super(container, size, margin, noCheckOrientation);

		this._fGames_arr = [];

		this._fShowGamesScreenTimeoutID_num = null;

		this._visibilityBasedLayers = [DOM_LAYERS.COMMON_PANEL];
		if (FEATURES.FIREFOX)
		{
			this._visibilityBasedLayers.push(DOM_LAYERS.GAMES);
		}

		this.hideCommonPanel();
		this.hideTutorialLayer();
	}

	get initialLayers()
	{
		let layers = [];

		layers.push(DOM_LAYERS.GAMES);
		layers.push(DOM_LAYERS.APP_SCREENS);
		layers.push(DOM_LAYERS.VUE_SCREEN);
		layers.push(DOM_LAYERS.PRELOADER);
		layers.push(DOM_LAYERS.SPINNER);
		layers.push(DOM_LAYERS.DIALOGS);
		layers.push(DOM_LAYERS.COMMON_PANEL);
		layers.push(DOM_LAYERS.TUTORIAL);
		
		layers.push(DOM_LAYERS.STATUS_BAR);
		layers.push(DOM_LAYERS.ORIENTATION_CHANGE);
		layers.push(DOM_LAYERS.FULLSCREEN_NOTIFICATION);
		layers.push(DOM_LAYERS.HTML_OVERLAY);

		return layers;
	}

	get isMobile()
	{
		if (this._fIsMobilePlatform_bl === undefined)
		{
			let lPlatformParams_obj = window["getPlatformInfo"] ? window["getPlatformInfo"].apply(window) || {} : {};
			if (lPlatformParams_obj["mobile"])
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

	get isLobbyLayoutVisible()
	{
		return (this.getLayer(DOM_LAYERS.APP_SCREENS).style.display === 'block');
	}

	get isGamesLayoutVisible()
	{
		let visibilityBasedLayers = this._visibilityBasedLayers || [];
		if (visibilityBasedLayers.indexOf(DOM_LAYERS.GAMES) >= 0)
		{
			return (this.getLayer(DOM_LAYERS.GAMES).style.visibility === 'visible');
		}

		return (this.getLayer(DOM_LAYERS.GAMES).style.display === 'block');
	}


	get htmlOverlayNode()
	{
		return this._fHtmlOverlayNode_html;
	}


	showHtmlElement(aElement_html)
	{
		this.clearHtmlOverlay();
		this._fHtmlOverlayNode_html.appendChild(aElement_html);
	}

	clearHtmlOverlay()
	{
		this._fHtmlOverlayNode_html.innerHTML = "";
	}

	get vueScreenNode()
	{
		return this._fVueScreenNode_obj;
	}

	showPrePreloader() 
	{
		super.showPrePreloader();

		this.changeLayerDisplayStatus(DOM_LAYERS.GAMES, false);
		this.changeLayerDisplayStatus(DOM_LAYERS.SPINNER, true);

		this.hideDialogsScreen();
		this.hideVueLayer();
	}

	showPreloader()
	{
		super.showPreloader();

		this.changeLayerDisplayStatus(DOM_LAYERS.GAMES, false);
	}

	hidePrePreloader()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.SPINNER, false);
	}

	showScreens()
	{
		super.showScreens();

		this.changeLayerDisplayStatus(DOM_LAYERS.PRELOADER, true);
		this.changeLayerDisplayStatus(DOM_LAYERS.GAMES, false);
	}

	hidePreloader()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.PRELOADER, false);
	}

	showCommonPanel()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.COMMON_PANEL, true);
	}

	hideCommonPanel()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.COMMON_PANEL, false);
	}

	addGamesScreen()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.SPINNER, false);
		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, true);
		this.changeLayerDisplayStatus(DOM_LAYERS.GAMES, true);
	}

	showGamesScreen(delay)
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.PRELOADER, false);
		if (!delay)
		{
			this.showGamesScreenNow();
		}
		else
		{
			let self = this;
			
			this._clearShowGamesScreenTimeout();
			this._fShowGamesScreenTimeoutID_num = setTimeout(function(){self.showGamesScreenNow()}, delay);
		}
	}

	_clearShowGamesScreenTimeout()
	{
		if (this._fShowGamesScreenTimeoutID_num !== null)
		{
			clearTimeout(this._fShowGamesScreenTimeoutID_num);
			this._fShowGamesScreenTimeoutID_num = null;
		}
	}	

	showGamesScreenNow()
	{
		this._clearShowGamesScreenTimeout();

		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, false);
		this.changeLayerDisplayStatus(DOM_LAYERS.STATUS_BAR, false);
	}

	showMobileCommonGroupButtonsLayer()
	{
		let layerName = DOM_LAYERS.COMMON_PANEL + "_" + CommonPanelController.LAYER_ID_MOBILE_BUTTONS;
		this.changeLayerDisplayStatus(layerName, true);
	}

	hideMobileCommonGroupButtonsLayer()
	{
		let layerName = DOM_LAYERS.COMMON_PANEL + "_" + CommonPanelController.LAYER_ID_MOBILE_BUTTONS;
		this.changeLayerDisplayStatus(layerName, false);
	}

	coverGamesScreenByLobby()
	{
		this._clearShowGamesScreenTimeout();
		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, true);
		this.changeLayerDisplayStatus(DOM_LAYERS.STATUS_BAR, false);
	}

	hideGamesLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.GAMES, false);
	}

	showGamesLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.GAMES, true);
	}

	hideAppScreensLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, false);
	}

	showAppScreensLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, true);
	}

	showDialogsScreen()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.DIALOGS, true);
		this.changeLayerDisplayStatus(DOM_LAYERS.TUTORIAL, false);
	}

	hideDialogsScreen()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.DIALOGS, false);
	}

	showFullscreenNotificationScreen()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.FULLSCREEN_NOTIFICATION, true);
	}

	hideFullscreenNotificationScreen()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.FULLSCREEN_NOTIFICATION, false);
	}

	showTutorialLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.TUTORIAL, true);
	}

	hideTutorialLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.TUTORIAL, false);
	}

	showVueLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.VUE_SCREEN, true);
	}

	hideVueLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.VUE_SCREEN, false);
	}

	addGame(gameUrl) 
	{
		let lGame_gl = this.getGame(gameUrl);
		let wrapper = this.container.querySelector(`.app-layer-${DOM_LAYERS.GAMES}`);
		
		if (lGame_gl !== null)
		{
			wrapper.appendChild(lGame_gl.gameFrame);
			this.onGameReady();
		}
		else
		{
			lGame_gl = GameLayer.create(wrapper, gameUrl);
			lGame_gl.once(GameLayer.EVENT_ON_GAME_FRAME_READY, this.onGameReady.bind(this));
			this._fGames_arr.push(lGame_gl);

			this.fitLayout(true);
		}
		return lGame_gl;
	}
	
	onGameReady()
	{
		this.emit(LobbyLayout.EVENT_ON_GAME_READY);
	}

	getGame(gameUrl)
	{
		let lGame_gl;
		for (let i=0; i<this._fGames_arr.length; i++)
		{
			lGame_gl = this._fGames_arr[i];
			if (lGame_gl.gameUrl === gameUrl)
			{
				return lGame_gl;
			}
		}

		return null;
	}

	_resizeLayers(appRect)
	{
		super._resizeLayers(appRect);
		this._resizeLayerNode(this._getLayerNode(DOM_LAYERS.DIALOGS), appRect);

		this._resizeLayerNode(this.getLayer(DOM_LAYERS.GAMES), {left: 0, top: 0, width: this.viewport.width, height: this.viewport.height});

		let node = this.getLayer(DOM_LAYERS.COMMON_PANEL);
		if (node)
		{
			let commonPanelBaseHeight = CommonPanelController.getCommonPanelSize(this.isMobile).height;
			let commonPanelTop = appRect.top;
			let bottomMargin = 0;
			if (!this.isMobile)
			{
				bottomMargin = isNaN(this.margin) ? this.margin[2] || 0 : +this.margin;
			}
			let commonPanelHeight = Math.floor((commonPanelBaseHeight-bottomMargin)*this.scale);
			commonPanelTop = commonPanelTop + appRect.height - commonPanelHeight;

			Object.assign(node.style, {
				left: appRect.left + "px",
				top: commonPanelTop + "px",
				width: appRect.width + "px",
				height: Math.floor(commonPanelBaseHeight*this.scale ) + "px"
			});
			this._resizeLayerNode(this.getLayer(DOM_LAYERS.VUE_SCREEN), {left: appRect.left, top: appRect.top,width: appRect.width, height: appRect.height - commonPanelHeight + 10});

		}

		let fullscreenNode = this.getLayer(DOM_LAYERS.FULLSCREEN_NOTIFICATION);
		if (fullscreenNode)
		{
			let [marginTop, marginRight=marginTop, marginBottom=marginTop, marginLeft=marginRight] = this.margin;
			let preloaderRect = {
				left: appRect.left,
				top: appRect.top,
				width: appRect.width + this.scale * (marginLeft + marginRight),
				height: appRect.height + this.scale * (marginTop + marginBottom)
			}
			this._resizeLayerNode(this._getLayerNode(DOM_LAYERS.FULLSCREEN_NOTIFICATION), preloaderRect);
		}

		let tutorialNode = this.getLayer(DOM_LAYERS.TUTORIAL);
		if (tutorialNode)
		{
			let [marginTop, marginRight=marginTop, marginBottom=marginTop, marginLeft=marginRight] = this.margin;
			let rect = {
				left: appRect.left,
				top: appRect.top,
				width: appRect.width,
			}
			this._resizeLayerNode(this._getLayerNode(DOM_LAYERS.TUTORIAL), rect);
		}
	}

	addLayer(name, container = this.container) 
	{
		let node = super.addLayer(name, container);
		if (name === DOM_LAYERS.COMMON_PANEL)
		{
			Object.assign(node.style, {pointerEvents:'none', visibility:'visible'});

			let layersParams_obj_arr = CommonPanelController.getCommonPanelLayers(this.isMobile);
			let commonPanelSize_obj = CommonPanelController.getCommonPanelSize(this.isMobile);
			for (var i = 0; i < layersParams_obj_arr.length; i++)
			{
				let params = layersParams_obj_arr[i];
				let subNode = super.addLayer(DOM_LAYERS.COMMON_PANEL + "_" + params.id, node);
				Object.assign(subNode.style, {left: `${(params.left || 0)/commonPanelSize_obj.width*100}%`,
											  top: `${(params.top || 0)/commonPanelSize_obj.height*100}%`,
											  width: `${params.width ? params.width/commonPanelSize_obj.width*100 : 100}%`,
											  height: `${params.height ? params.height/commonPanelSize_obj.height*100 : 100}%`,
											  pointerEvents: params.pointerEvents || 'inherit'});
			}
		}
		
		else if (name == DOM_LAYERS.VUE_SCREEN)
		{
			this._fVueScreenNode_obj = node;
		}
		else if (name === DOM_LAYERS.HTML_OVERLAY)
		{
			this._fHtmlOverlayNode_html = node;
		}

		Object.assign(node.style, {"z-index": this.initialLayers.indexOf(name)});

		return node;
	}

	changeLayerDisplayStatus(layerName, visible)
	{
		visible = Boolean(visible);

		let layer = this.getLayer(layerName);

		if (!layer)
		{
			return;	
		}

		let visibilityBasedLayers = this._visibilityBasedLayers || [];
		if (visibilityBasedLayers.indexOf(layerName) >= 0)
		{
			layer.style.visibility = visible ? 'visible' : 'hidden';
		}
		else
		{
			super.changeLayerDisplayStatus(layerName, visible);
		}
	}

	addScreen(wrapperName) 
	{
		let screen = super.addScreen.call(this, wrapperName);
		if ( ~wrapperName.indexOf(DOM_LAYERS.COMMON_PANEL) )
		{
			let node = screen.canvas;
			if (node)
			{
				Object.assign(node.style, {width:'100%', height:'100%'});
			}
		}
		return screen;
	}
}
export { DOM_LAYERS };
export default LobbyLayout;