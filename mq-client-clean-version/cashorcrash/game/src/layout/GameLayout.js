import DOMLayout, {DOM_LAYERS} from '../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';
import EventDispatcher from '../../../../common/PIXI/src/dgphoenix/unified/controller/events/EventDispatcher';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import * as FEATURES from '../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';
import { GAME_VIEW_SETTINGS } from '../view/main/GameBaseView';

/** @ignore */
class GameLayout extends DOMLayout 
{
	constructor(container, size = {width: 960, height: 540}, margin = {}, noCheckOrientation=false) 
	{
		DOM_LAYERS.FULLSCREEN_NOTIFICATION = "fullscreenNote";
		DOM_LAYERS.DIALOGS = 'dialogs';

		super(container, size, margin, noCheckOrientation);

		this._visibilityBasedLayers = [];
	}

	get initialLayers()
	{
		let layers = [];

		layers.push(DOM_LAYERS.APP_SCREENS);
		layers.push(DOM_LAYERS.VUE_SCREEN);
		layers.push(DOM_LAYERS.PRELOADER);
		layers.push(DOM_LAYERS.SPINNER);
		layers.push(DOM_LAYERS.DIALOGS);
		
		layers.push(DOM_LAYERS.STATUS_BAR);
		layers.push(DOM_LAYERS.ORIENTATION_CHANGE);
		layers.push(DOM_LAYERS.FULLSCREEN_NOTIFICATION);
		layers.push(DOM_LAYERS.HTML_OVERLAY);

		return layers;
	}

	get htmlOverlayNode()
	{
		return this._fHtmlOverlayNode_html;
	}

	addMobileListeners()
	{
		super.addMobileListeners();
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

		this.changeLayerDisplayStatus(DOM_LAYERS.SPINNER, true);

		this.hideDialogsScreen();
		this.hideVueLayer();
	}

	hidePrePreloader()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.SPINNER, false);
	}

	showScreens()
	{
		super.showScreens();

		this.changeLayerDisplayStatus(DOM_LAYERS.PRELOADER, true);
	}

	hidePreloader()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.PRELOADER, false);
	}

	hideAppScreensLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, false);
	}

	showAppScreensLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, true);
	}

	showFullscreenNotificationScreen()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.FULLSCREEN_NOTIFICATION, true);
	}

	hideFullscreenNotificationScreen()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.FULLSCREEN_NOTIFICATION, false);
	}

	showVueLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.VUE_SCREEN, true);
	}

	hideVueLayer()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.VUE_SCREEN, false);
	}

	showDialogsScreen()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.DIALOGS, true);
	}

	hideDialogsScreen()
	{
		this.changeLayerDisplayStatus(DOM_LAYERS.DIALOGS, false);
	}

	get paytableHeadLayer()
	{
		return this.getLayer(DOM_LAYERS.VUE_SCREEN);
	}

	_resizeLayers(appRect)
	{
		super._resizeLayers(appRect);

		let lAppRectWithoutBottomPanel = 	{	
												left: appRect.left, 
												top: appRect.top,
												width: appRect.width, 
												height: appRect.height - (GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT || 0)*this.scale
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

		this._resizeLayerNode(this.getLayer(DOM_LAYERS.VUE_SCREEN), lAppRectWithoutBottomPanel);
																
		this._resizeLayerNode(this.getLayer(DOM_LAYERS.DIALOGS), lAppRectWithoutBottomPanel);

		this._resizeLayerNode(this._getLayerNode(DOM_LAYERS.DIALOGS), 	{
																			width: lAppRectWithoutBottomPanel.width,
																			height: lAppRectWithoutBottomPanel.height
																		});
	}

	addLayer(name, container = this.container) 
	{
		let node = super.addLayer(name, container);

		if (name == DOM_LAYERS.VUE_SCREEN)
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

		return screen;
	}
}
export { DOM_LAYERS };
export default GameLayout;