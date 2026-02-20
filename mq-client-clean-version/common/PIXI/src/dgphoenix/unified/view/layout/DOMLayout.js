import EventDispatcher from '../../controller/events/EventDispatcher';

import { ALIGN, APP, VALIGN } from '../../controller/main/globals';
import * as FEATURES from './features';

import { bindEvent, unbindEvent, preventEvent, getPointerCoordinates } from './helpers';

import {P2L} from './images';

import hotkeys from '../../lib/hotkeys.js';

let WINDOW_IS_HIDDEN = false;

let staticWindowRect = null; // TODO: refactor this

/**
 * Device orientation
 * @type {{LANDSCAPE: number, PORTRAIT: number}}
 * @property {Number} LANDSCAPE landscape
 * @property {Number} PORTRAIT portrait
 * @constant
 */
const ORIENTATION = {LANDSCAPE: "landscape", PORTRAIT: "portrait"};

const DOM_LAYERS = {
	STATUS_BAR: "status-bar",
	PRELOADER: "preloader",
	APP_SCREENS: "screens",
	SPINNER: "preparepreloader",
	ORIENTATION_CHANGE: "p2l",
	VUE_SCREEN: "vue-screen",
	HTML_OVERLAY: "html-overlay"
}

/**
 * @class
 */
class CanvasLayer {

	static create(container) {
		let canvas = container.appendChild(document.createElement('canvas'));
		canvas.className = 'app-screen';
		canvas.style.position = 'absolute';
		return new CanvasLayer(canvas);
	}

	constructor(canvas) {
		this.canvas = canvas;
	}

	destroy()
	{
		this.canvas = null;
	}
}

var SCALE = 1;

/**
 * @class
 * @inheritDoc
 * @extends EventDispatcher
 * @classdesc Base class for DOM layout manipulations
 */
class DOMLayout extends EventDispatcher {

	static get EVENT_ON_ORIENTATION_CHANGED()			{ return 'orientationchange'; }

	/**
	 * @constructor
	 * @param {HTMLElement} container
	 * @param {{width:number, height:number}} size
	 * @param {boolean} autoCheckOrientation
	 */
	constructor(container, size = {width: 960, height: 540}, margin = {}, noCheckOrientation=false) 
	{
		super();

		this._fIsAutoFitMode_bl = false;
		this._fIsFitLayoutRequired_bl = false;
		this._fIsForcedFitLayoutRequired_bl = false;

		this._fMobileHideAddressBarTimeout_int = null;
		this._fAutoFitLayoutTimer_int = null;

		this._fNewIosBarMargin_int = null;

		this.size = size;
		this.noCheckOrientation = !!noCheckOrientation;

		/** @ignore */
		this.calculatedSize = {
			width: size.width,
			height: size.height,
			screenWidth: size.width,
			screenHeight: size.height,
			scale: 1
		};

		/** @ignore */
		this.bitmapScale = 1;

		/**
		 * Cache orientation value
		 * @private
		 */
		this._lastOrientation = null;

		/**
		 * Cache viewport value
		 * @type {{width: number, height: number}}
		 * @private
		 */
		this._lastViewport = {width: null, height: null};

		/**
		 * Align in container
		 * @type {{horizontal: string, vertical: string}}
		 */
		this.align = {
			horizontal: ALIGN.CENTER,
			vertical: VALIGN.MIDDLE
		};

		/**
		 * Margin. Array of numbers (like in CSS) or a number
		 * @type {number[]}
		 */
		this.margin = [margin.top || 0, margin.right || 0, margin.bottom || 0, margin.left || 0];

		/**
		 * Array of HTMLCanvasElement
		 * @type {Map}
		 */
		this.screens = [];

		container = ((container instanceof HTMLElement) && container) || document.querySelector(container);
		if (!(container instanceof HTMLElement)) {
			throw new Error('Invalid layout container');
		}

		if (container.parentNode === document.body) {
			container.parentNode.style.overflow = 'hidden';
		}

		/**
		 * Main application container
		 * @type {HTMLElement}
		 */
		this.container = container;
		this.container.className = 'app-layout';
		Object.assign(this.container.style, {
			position: 'absolute',
			overflow: 'hidden'
		});

		this.addLayers.apply(this, this.initialLayers);

		this.initP2L({
			css: {
				display: 'none'
			}
		});

		// Hack for wrong window size reported by browser
		this._domMarkElement = null;
		if (window.parent == window && FEATURES.ANDROID && FEATURES.FIREFOX) {
			this._domMarkElement = this.container.appendChild(document.createElement("div"));
			Object.assign(this._domMarkElement.style, {
				position: "fixed",
				right: "0px",
				bottom: "0px",
				width: "2px",
				height: "2px",
				background: "transparent",
				zIndex: "100000"
			});
		}

		hotkeys('z,x', this.onStatusBarHotkeyPressed.bind(this));

		this.fitLayout(true);
		this._mobileRaiseUpLayout();
		this.autoFit = true;

		this._autoFill = false;

		//DEBUG...
		/*let l_html = this.testText = window.document.createElement('div');
		l_html.style.color = "#5c5c5c";
		l_html.style.position = "absolute";
		l_html.style.left = "0px";
		l_html.style.top = "0px";
		l_html.style.width = "350px";
		l_html.style.height = "20px";
		l_html.style["z-index"] = "990";
		l_html.style["text-align"] = "center";
		l_html.style["font-family"] = "calibri";
		l_html.style["font-size"] = "10px";

		l_html.innerText = "";

		document.body.appendChild(l_html);*/
		//...DEBUG

		return this;
	}

	/** Array of layers that will be added automatically with DOMLayout creation.*/
	get initialLayers()
	{
		return [DOM_LAYERS.APP_SCREENS, DOM_LAYERS.PRELOADER, DOM_LAYERS.SPINNER, DOM_LAYERS.STATUS_BAR, DOM_LAYERS.ORIENTATION_CHANGE];
	}

	get autoFill() {
		return this._autoFill;
	}

	set autoFill(val) {
		this._autoFill = !!val;
		this.fitLayout(true);
	}

	/**
	 * @member {boolean} windowVisible Indicates whether the window that contains layout is visible or not
	 * @readonly
	 */
	get windowVisible() {
		return !WINDOW_IS_HIDDEN;
	}

	/**
	 * Layout scale
	 * @type {number}
	 */
	get scale() {
		return SCALE;
	}

	/**
	 * Set layout scale.
	 * @param {number} val
	 */
	set scale(val) { // TODO: apply scale, useful for manual scaling
		SCALE = val;
	}

	/**
	 * Detect scales of bitmaps.
	 * @param {number[]} scales 
	 * @returns {number}
	 */
	detectBitmapScale(scales = [1]) {
		var scale = 1;

		scales = scales.slice(0);
		scales.sort();

		if((!FEATURES.IOS || FEATURES.IPHONE4) && scales.length > 1) {
			var ratio = window.devicePixelRatio || 1;

			ratio = 1;
			var sw = screen.width * ratio;
			var sh = screen.height * ratio;
			var w, h;

			if(this.size.width > this.size.height) {
				w = Math.max(sw, sh);
				h = Math.min(sw, sh);
			}
			else {
				w = Math.min(sw, sh);
				h = Math.max(sw, sh);
			}

			var test = [];
			for(var sc of scales) {
				test.push({
					scale: sc,
					coef: ((this.size.width * sc) / sw) * ((this.size.height * sc) / sh)
				});
			}

			var min = Number.MAX_VALUE;
			scale = scales[0];

			for(sc of test) {
				var diff = Math.abs(sc.coef - 1);
				if(diff < min) {
					min = diff;
					scale = sc.scale;
				}
			}
		}
		else scale = scales.pop();

		this.bitmapScale = scale;
		return scale;
	}

	/**
	 * Add listeners actual for mobile devices.
	 */
	addMobileListeners()
	{
		this._handleWindowVisibility();

		window.addEventListener("orientationchange", this._onDeviceOrientationChanged.bind(this));
	}

	_onDeviceOrientationChanged(e)
	{
		this.fitLayout(true);
	}

	_handleWindowVisibility()
	{
		let props = {hidden: null, visibilityChange: null};
		if (undefined !== document.hidden) {
			props.hidden = "hidden";
			props.visibilityChange = "visibilitychange";
		}
		else if (undefined !== document.mozHidden) {
			props.hidden = "mozHidden";
			props.visibilityChange = "mozvisibilitychange";
		}
		else if (undefined !== document.msHidden) {
			props.hidden = "msHidden";
			props.visibilityChange = "msvisibilitychange";
		}
		else if (undefined !== document.webkitHidden) {
			props.hidden = "webkitHidden";
			props.visibilityChange = "webkitvisibilitychange";
		}

		if (props.hidden) {
			WINDOW_IS_HIDDEN = document[props.hidden];
		}
		if (props.visibilityChange) {
			document.addEventListener(props.visibilityChange, () => {

				WINDOW_IS_HIDDEN = document[props.hidden];

				if (WINDOW_IS_HIDDEN) {

					clearTimeout(this._fMobileHideAddressBarTimeout_int);
					/**
					 * Current window lost focus
					 * @event DOMLayout#hidewindow
					 */
					this.emit("hidewindow");
				}
				else {
					/**
					 * Current window has focus
					 * @event DOMLayout#showwindow
					 */
					this.checkOrientation();
					this.emit("showwindow");
				}
			}, false);
		}
	}

	/**
	 * Add layout layers
	 * @param  {...any} names - Array of names.
	 */
	addLayers(...names) {
		for (let name of names) {
			this.addLayer(name);
		}
	}

	get _isIOS()
	{
		let useragent = navigator.userAgent.toLowerCase();

		let isIOS = !!navigator.platform && /iPad|iPhone|iPod/.test(navigator.platform);

		if (useragent.indexOf('iPad') > -1) {
			isIOS = true;
		}

		if (useragent.indexOf('macintosh') > -1) {
			try {
				document.createEvent("TouchEvent");
				isIOS = true;
			} catch (e) {}
		}

		return isIOS;
	}

	/**
	 * Add layout layer.
	 * @param {string} name - Layer name.
	 * @param {HTMLElement} container - Container to add layer in.
	 * @returns {HTMLElement}
	 */
	addLayer(name, container = this.container) {
		let node = container.appendChild(document.createElement('div'));
		node.className = `app-layout-layer app-layer-${name}`;

		let position = 'absolute';

		if (name.indexOf("commonPanel_") != -1)
		{
			Object.assign(node.style, {zIndex: 1});
		}

		if (this._isIOS)
		{
			if (!~name.indexOf("commonPanel_"))
			{
				position = 'fixed';
			}
		}

		Object.assign(node.style, {
			position: position,
			left: '0px', right: '0px', top: '0px', bottom: '0px'
		});
		if (name === DOM_LAYERS.STATUS_BAR)
		{
			Object.assign(node.style, {display:'none', pointerEvents: 'auto', bottom: 'auto', right: 'auto'});
		}
		return node;
	}

	/**
	 * Remove layout layer.
	 * @param {string} layerName - Layer name.
	 */
	removeLayer(layerName)
	{
		let layer = this.getLayer(layerName);
		if (layer)
		{
			while (layer.firstChild) 
			{
				layer.removeChild(layer.firstChild);
			}
			layer.parentNode && layer.parentNode.removeChild(layer);
		}
	}

	/**
	 * Get layer by name.
	 * @param {string} name - Layer name.
	 * @returns {HTMLElement}
	 */
	getLayer(name) {
		return this.container.querySelector(`.app-layer-${name}`);
	}

	/**
	 * Names of layers that are active (displayed) at the moment.
	 * @type {string[]}
	 */
	get activeLayersNames()
	{
		var activeLayers = [];
		var layer;
		for (let layerName in DOM_LAYERS) 
		{
			layer = this.getLayer(DOM_LAYERS[layerName]);
			if (layer && (layer.style.display != 'none'))
			{
				activeLayers.push(DOM_LAYERS[layerName]);
			}
		}

		return activeLayers;
	}

	/**
	 * Add screen (create canvas) for stage.
	 * @param {*} wrapperName 
	 * @returns {CanvasLayer}
	 */
	addScreen(wrapperName) 
	{
		let wrapper = this.container.querySelector(`.app-layer-${wrapperName}`);
		let screen = CanvasLayer.create(wrapper);

		this.screens.push(screen);
		
		return screen;
	}

	/**
	 * Remove (and destroy) screen from layout.
	 * @param {*} screen 
	 */
	removeScreen(screen)
	{
		if (!screen)
		{
			return;
		}

		screen.destroy();
		
		if (!this.screens || !this.screens.length)
		{
			return;
		}

		let index = this.screens.indexOf(screen);
		if (index >= 0)
		{
			this.screens.splice(index, 1);
		}
	}

	/**
	 * Create view of  P2L screen (notification to change device orientation from portrait to landscape).
	 * @param {Object} param0 
	 */
	initP2L({image = P2L, color = 'rgba(255, 0, 0, 1)', css = {}} = {}) {
		let p2l = this.container.querySelector(`.app-layer-${DOM_LAYERS.ORIENTATION_CHANGE}`);
		if (!p2l) return;
		Object.assign(p2l.style, {
			backgroundColor: 'rgba(51, 51, 51, 1)',
			backgroundSize: '30%',
			backgroundImage: `url('${image}')`,
			backgroundRepeat: 'no-repeat',
			backgroundPosition: 'center center'
		});
		Object.assign(p2l.style, css || {});
	}

	/**
	 * @description Determin available window size
	 * @returns {Object} {"width": width, "height": height}
	 */
	get windowRect() {
		let rect = {
			width: document.documentElement.clientWidth,
			height: document.documentElement.clientHeight
		};

		if (FEATURES.IOS)
		{
			rect.width = window.innerWidth;
			rect.height = window.innerHeight;
		}

		if (this._domMarkElement) {
			rect.height = this._domMarkElement.offsetTop + this._domMarkElement.offsetHeight;
		}

		return rect;
	}

	/**
	 * Current application orientation
	 * @returns {String}
	 */
	get orientation() {
		var rect = FEATURES.MOBILE ? this.windowRect : this.viewport;
		return rect.width > rect.height ? ORIENTATION.LANDSCAPE : ORIENTATION.PORTRAIT;
	}

	/**
	 * Checks whether current orientaion is landscape.
	 * @returns {boolean}
	 */
	get isLandscapeOrientation()
	{
		return this.orientation === ORIENTATION.LANDSCAPE;
	}

	/**
	 * Checks whether current orientaion is portrait.
	 * @returns {boolean}
	 */
	get isPortraitOrientation()
	{
		return this.orientation === ORIENTATION.PORTRAIT;
	}

	/**
	 * Is autofit mode on or not. Autofit mode means that layout will automatically be resized on every resize of window.
	 * @returns {boolean}
	 */
	get autoFit()
	{
		return !!this._fIsAutoFitMode_bl;
	}

	/**
	 * Set autofit mode on or off.
	 * @param {val}
	 */
	set autoFit(val) {
		if (!!val === this.autoFit)
		{
			return;
		}

		this._fIsAutoFitMode_bl = val;

		clearTimeout(this._fAutoFitLayoutTimer_int);
		window.removeEventListener('resize', this._onWindowResizeEvt.bind(this), true);

		if (val)
		{
			this._startAutoFitTimeout();

			window.addEventListener('resize', this._onWindowResizeEvt.bind(this), true);
		}
	}

	_startAutoFitTimeout()
	{
		clearTimeout(this._fAutoFitLayoutTimer_int);
		this._fAutoFitLayoutTimer_int = setTimeout(this._onAutoFitTimeoutCompleted.bind(this), 500);
	}

	_onAutoFitTimeoutCompleted()
	{
		clearTimeout(this._fAutoFitLayoutTimer_int);
		this.fitLayout();

		this._startAutoFitTimeout();
	}

	_onWindowResizeEvt(event)
	{
		this.fitLayout(true)
	}

	/**
	 * Size of visible area to fit layout in.
	 * @returns {Object}
	 * @example {width: 100, height: 200}
	 */
	get viewport() {
		if (staticWindowRect) {
			return staticWindowRect;
		}
		
		let parent = this.container && this.container.parentNode;
		if (parent) {
			return (this.container === document.body || parent === document.body)
				? this.windowRect
				: { width: parent.clientWidth, height: parent.clientHeight };
		}
		return this.windowRect;
	}

	/**
	 * Set size of visible area.
	 * @param {Object} rect
	 */
	set viewport(rect) {
		staticWindowRect = rect;
	}

	/**
	 * Checks whether change orientation notification is active or not.
	 * @returns {boolean}
	 */
	get p2l() {
		let p2l = this.getLayer(DOM_LAYERS.ORIENTATION_CHANGE);
		return p2l && p2l.style.display !== 'none';
	}

	/**
	 * Activate or deactivate change orientation notification.
	 * @param {boolean} val
	 */
	set p2l(val) {
		let p2l = this.getLayer(DOM_LAYERS.ORIENTATION_CHANGE);
		if (p2l) {
			p2l.style.display = val ? 'block' : 'none';
		}
	}

	/**
	 * Show specific layer while preloader assets are loading.
	 */
	showPrePreloader() {
		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, false);
		this.changeLayerDisplayStatus(DOM_LAYERS.PRELOADER, false);
		this.changeLayerDisplayStatus(DOM_LAYERS.SPINNER, false);
	}

	/**
	 * Show preloader layer.
	 */
	showPreloader() {
		this.changeLayerDisplayStatus(DOM_LAYERS.SPINNER, false);
		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, false);
		this.changeLayerDisplayStatus(DOM_LAYERS.PRELOADER, true);

		this.removeLayer(DOM_LAYERS.SPINNER);
	}

	/**
	 * Switch to application screen layer.
	 */
	showScreens() {
		this.changeLayerDisplayStatus(DOM_LAYERS.SPINNER, false);
		this.changeLayerDisplayStatus(DOM_LAYERS.PRELOADER, false);
		this.changeLayerDisplayStatus(DOM_LAYERS.APP_SCREENS, true);
	}

	/**
	 * Update visibility of layout layer.
	 * @param {number} layerName - Name of the layer.
	 * @param {boolean} visible - Target visibility of the layer.
	 * @protected
	 */
	changeLayerDisplayStatus(layerName, visible)
	{
		visible = Boolean(visible);

		let layer = this.getLayer(layerName);

		if (!layer)
		{
			return;	
		}

		layer.style.display = visible ? 'block' : 'none';
	}

	onStatusBarHotkeyPressed(event, handler) 
	{
		if (hotkeys.isPressed('z') && hotkeys.isPressed('x'))
		{
			let statusBarLayer = this.getLayer(DOM_LAYERS.STATUS_BAR);
			let statusBarDisplayed = statusBarLayer.style.display !== 'block';
			statusBarLayer.style.display = statusBarDisplayed ? 'block' : 'none';
			if (statusBarDisplayed)
			{
				this.emit("statusbardisplayed");
			}
		}
	}

	// returns rect which layout should be fitted in (with margins)
	_getLayoutRect(viewport)
	{
		let [marginTop, marginRight=marginTop, marginBottom=marginTop, marginLeft=marginRight] = this.margin;

		let layoutRect = {
			left: marginLeft * this.scale,
			top: marginTop * this.scale,
			width: viewport.width - (marginLeft + marginRight) * this.scale,
			height: viewport.height - (marginTop + marginBottom) * this.scale
		};
		return layoutRect;
	}

	_getLayoutScale(viewport)
	{
		let [marginTop, marginRight=marginTop, marginBottom=marginTop, marginLeft=marginRight] = this.margin;
		let nonScaledWidth_num = this.size.width + marginLeft + marginRight;
		let nonScaledHeight_num = this.size.height + marginTop;
		if (this._fNewIosBarMargin_int)
		{
			nonScaledHeight_num = nonScaledHeight_num + (marginBottom - this._fNewIosBarMargin_int/this.scale) + this._fNewIosBarMargin_int;
		}
		else
		{
			nonScaledHeight_num = nonScaledHeight_num + marginBottom;
		}
		return Math.min(viewport.width/nonScaledWidth_num, viewport.height/nonScaledHeight_num);
	}

	fitLayoutImmediately()
	{
		let viewport = this.viewport;
		let force = this._fIsForcedFitLayoutRequired_bl;

		this._fIsFitLayoutRequired_bl = false;
		this._fIsForcedFitLayoutRequired_bl = false;

		// viewport size not changed
		if (!force && this._lastViewport.width === viewport.width && this._lastViewport.height === viewport.height) {
			return;
		}

		Object.assign(this._lastViewport, viewport);
		this.checkOrientation(viewport);

		this.scale = this._getLayoutScale(viewport);
		this._mobileRaiseUpLayout();
		let layoutRect = this._getLayoutRect(viewport);

		let appRect = {
			left: layoutRect.left,
			top: layoutRect.top,
			width: Math.floor(this.size.width * this.scale),
			height: Math.floor(this.size.height * this.scale)
		};

		let {horizontal: alignHorizontal, vertical: alignVertical} = this.align;

		if (alignHorizontal == ALIGN.LEFT) alignHorizontal = 0;
		if (alignHorizontal == ALIGN.CENTER) alignHorizontal = 0.5;
		if (alignHorizontal == ALIGN.RIGHT) alignHorizontal = 1;
		appRect.left += !alignHorizontal || isNaN(alignHorizontal) ? 0 : Math.floor((layoutRect.width - appRect.width) * alignHorizontal);

		if (alignVertical == VALIGN.TOP) alignVertical = 0;
		if (alignVertical == VALIGN.MIDDLE) alignVertical = 0.5;
		if (alignVertical == VALIGN.BOTTOM) alignVertical = 1;
		appRect.top += !alignVertical || isNaN(alignVertical) ? 0 : Math.floor((layoutRect.height - appRect.height) * alignVertical);

		let screensRect = {
			width: this.size.width,
			height: this.size.height
		};

		if(this.autoFill) {
			if(appRect.width < layoutRect.width) {
				screensRect.width = Math.floor(screensRect.width * layoutRect.width / appRect.width);
			}

			if(appRect.height < layoutRect.height) {
				screensRect.height = Math.floor(screensRect.height * layoutRect.height / appRect.height);
			}

			appRect.left = 0;
			appRect.top = 0;
			appRect.width = layoutRect.width;
			appRect.height = layoutRect.height;
		}

		if (!this._isIOS)
		{
			Object.assign(this.container.style, {
				left: viewport.left + "px",
				top: viewport.top + "px",
				width: viewport.width + "px",
				height: viewport.height + "px"
			});
		}

		this._resizeLayers(appRect);

		let statusBarLayer = this.getLayer(DOM_LAYERS.STATUS_BAR);
		statusBarLayer.style.transformOrigin = 'left top';
		statusBarLayer.style.transform = `scale(${this.scale},${this.scale}) translate(${appRect.left/this.scale}px, ${appRect.top/this.scale}px)`;


		let htmlOverlayLayer = this.getLayer(DOM_LAYERS.HTML_OVERLAY);
		if(htmlOverlayLayer)
		{
			htmlOverlayLayer.style.transformOrigin = 'left top';
			htmlOverlayLayer.style.transform = `scale(${this.scale},${this.scale}) translate(${appRect.left/this.scale}px, ${appRect.top/this.scale}px)`;
			htmlOverlayLayer.style.width = "0px"
			htmlOverlayLayer.style.height = "0px"
		}

		this.calculatedSize = {
			width: appRect.width,
			height: appRect.height,
			screenWidth: screensRect.width,
			screenHeight: screensRect.height,
			scale: SCALE
		};

		/**
		 * Update window size
		 * @event DOMLayout#fitlayout
		 * @property {number} width 
		 * @property {number} height 
		 * @property {number} screenWidth Screen width
		 * @property {number} screenHeight Screen height
		 * @property {number} scale scale
		 */
		this.emit('fitlayout', this.calculatedSize);
	}

	/**
	 * Mark layout to fit the screen on the next tick.
	 * @param {boolean} [force=false] - Forced fit supposes fitting even if screen size was not changed.
	*/
	fitLayout(force = false)
	{
		this._fIsForcedFitLayoutRequired_bl = this._fIsForcedFitLayoutRequired_bl || force;
		this._fIsFitLayoutRequired_bl = true;
	}

	tick()
	{
		if (this._fIsFitLayoutRequired_bl)
		{
			this.fitLayoutImmediately();
		}
	}

	_mobileRaiseUpLayout()
	{
		let configMargin = [
			APP.config.margin.top || 0,
			APP.config.margin.right || 0,
			APP.config.margin.bottom || 0,
			APP.config.margin.left || 0
		];

		if (FEATURES.IOS && FEATURES.IOS_VERSION > '13')
		{
			this._fNewIosBarMargin_int = 15;

			if (this.isLandscapeOrientation || !this.isPortraitModeSupported)
			{
				this.margin[2] = configMargin[2] + this._fNewIosBarMargin_int/this.scale;
			}
			else
			{
				this.margin[2] = configMargin[2] + FEATURES.IPAD ? this._fNewIosBarMargin_int/this.scale : 0;
			}
		}
	}

	_getLayerNode(layerName)
	{
		return this.getLayer(layerName) ? this.getLayer(layerName).querySelector('.app-screen') : null;
	}

	_resizeLayers(appRect)
	{
		let screens = this.getLayer(DOM_LAYERS.APP_SCREENS).querySelectorAll('.app-screen');
		for (let node of screens) {
			this._resizeLayerNode(node, appRect)
		}

		let [marginTop, marginRight=marginTop, marginBottom=marginTop, marginLeft=marginRight] = this.margin;
		let preloaderRect = {
			left: appRect.left - this.scale * marginLeft,
			top: appRect.top - this.scale * marginTop,
			width: appRect.width + this.scale * (marginLeft + marginRight),
			height: appRect.height + this.scale * (marginTop + marginBottom + 1)
		}
		let targetRect = (FEATURES.IOS && FEATURES.IOS_VERSION > '13') ? appRect : preloaderRect;
		this._resizeLayerNode(this._getLayerNode(DOM_LAYERS.PRELOADER), targetRect);
	}

	/**
	 * Checks if application launched on mobile device.
	 * @returns {boolean}
	 */
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

	_resizeLayerNode(node, appRect)
	{
		if (node)
		{
			Object.assign(node.style, {
				left: appRect.left + "px",
				top: appRect.top + "px",
				width: appRect.width + "px",
				height: appRect.height + "px"
			});
		}
	}

	/**
	 * Check for correct device orientation
	 * @return {boolean} TRUE if orientation is wrong
	 */
	checkOrientation(viewport = this.viewport)
	{
		if (!FEATURES.MOBILE && !this.isPortraitModeSupported) return false;

		let viewportAspectRatio = viewport.width / viewport.height,
			appAspectRatio = this.size.width / this.size.height;

		if ((Infinity === viewportAspectRatio) || (Infinity === appAspectRatio)) return false; // no size
		if ((1 === viewportAspectRatio) || (1 === appAspectRatio)) return false; // square viewport or app always OK

		let lIsNewLandscapeAspectRatio_bl = viewportAspectRatio > 1;
		let lNewOrientation_str = lIsNewLandscapeAspectRatio_bl ? ORIENTATION.LANDSCAPE : ORIENTATION.PORTRAIT;

		if (this._lastOrientation === lNewOrientation_str) {
			return false;
		}

		this._lastOrientation = lNewOrientation_str;

		let wrong = ((appAspectRatio > 1) ^ lIsNewLandscapeAspectRatio_bl);

		if(!this.isPortraitModeSupported) {
			this.p2l = wrong; // TODO: configurable behavior - automatic P2L screen

			if (wrong) {
				/**
				 * Appeared wrong screen orientation message (p2l)
				 * @event DOMLayout#lockscreen
				 */
				this.emit("lockscreen");
			}
			else {
				/**
				 * Disappeared wrong screen orientation message (p2l)
				 * @event DOMLayout#unlockscreen
				 */
				this.emit("unlockscreen");
			}
		}
		else {
			this.p2l = false;
			wrong = false;
		}

		/**
		 * Changed device orientation event
		 * @event DOMLayout#EVENT_ON_ORIENTATION_CHANGED
		 * @param {boolean} locked Indicates whether screen is locked by wrong orientation message (p2l) or not
		 */

		this.emit(DOMLayout.EVENT_ON_ORIENTATION_CHANGED, {locked: wrong, orientation: lNewOrientation_str});

		return wrong;
	}

	/**
	 * Reset last applied layout orientation.
	 */
	resetOrientation()
	{
		this._lastOrientation = null;
	}

	/**
	 * Indicates whether last applied layout orientation defined or not.
	 * @type {boolean}
	 */
	get isLastOrientationDefined()
	{
		this._lastOrientation !== null;
	}

	/**
	 * Indicates whether portrait orientation supported or not.
	 * @type {boolean}
	 */
	get isPortraitModeSupported()
	{
		return !!this.noCheckOrientation;
	}

	/**
	 * Indicates whether screen is locked due to wrong orientation or not.
	 * @type {boolean}
	 */
	get isScreenOrientationLocked()
	{
		return this.isPortraitOrientation && !this.isPortraitModeSupported;
	}
}

export { ORIENTATION, DOM_LAYERS };
export default DOMLayout;