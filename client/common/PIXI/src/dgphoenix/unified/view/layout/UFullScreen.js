import EventDispatcher from '../../controller/events/EventDispatcher';
import Tween from '../../controller/animation/Tween';
import { APP } from '../../controller/main/globals';
import * as FEATURES from './features';
import DOMLayout from './DOMLayout';

/**
 * Notification that offers to switch to fullscreen mode.
 * @class
 * @augments EventDispatcher
 */
class UFullScreen extends EventDispatcher
{
	static get SUPPORT_FULLSCREEN_API()		{return "fullscreenAPI"	};
	static get SUPPORT_MASK_FULLSCREEN()	{return "maskFullscreen"};
	static get SUPPORT_NONE()				{return "none"			};

	static get EVENT_HIDE_NOTE()			{return "hideNote"		};
	static get EVENT_SHOW_NOTE()			{return "showNote"		};

	static get SPECS_SAFARI()
	{
		return [
			[320,	480,	39,	0,	"iPhone 4"				],
			[320,	568,	39,	0,	"iPhone 5 or 5s"		],
			[375,	667,	39,	0,	"iPhone 6"				],
			[414,	736,	40,	0,	"iPhone 6 plus"			],
			[375,	667,	40,	0,	"iPhone 6 plus (Zoomed)"],
			[375,	812,	60,	0,	"iPhone 8. 11"			],
			[375,	812,	61,	20,	"iPhoneX"				],
			[375,	812,	44,	19,	"iPhone 11 Pro"			],
			[414,	896,	44,	19,	"iPhone 11 Pro Max"		],
			[390,	844,	47,	19,	"iPhone 12, 13"			],
			[428,	926,	47,	19,	"iPhone 13 Pro Max"		],
			[390,	844,	59,	19,	"iPhone 14"				],
			[393 ,	852,	59,	19,	"iPhone 14 Pro"			],
			[430,	932,	59,	19,	"iPhone 14 Pro Max"		],
			[768,	1024,	39,	39,	"iPad 2"				],
			[768,	1024,	39,	39,	"iPad Air or Retina"	],
			[834,	1194,	43,	43,	"iPad Pro"				],
			[820,	1180,	43,	43,	"iPad Air 4/5"			]
		];
	}
	static get SPECS_CHROME()
	{
		return [
			[320,	480,	20,	20,	"iPhone 4"				],
			[320,	568,	20,	20,	"iPhone 5 or 5s"		],
			[375,	667,	20,	20,	"iPhone 6"				],
			[414,	736,	20,	20,	"iPhone 6 plus"			],
			[375,	667,	20,	20,	"iPhone 6 plus (Zoomed)"],
			[375,	812,	60,	0,	"iPhone 8. 11"			],
			[375,	812,	64,	0,	"iPhoneX"				],
			[375,	812,	44,	19,	"iPhone 11 Pro"			],
			[414,	896,	44,	19,	"iPhone 11 Pro Max"		],
			[390,	844,	47,	19,	"iPhone 12, 13"			],
			[428,	926,	47,	19,	"iPhone 13 Pro Max"		],
			[390,	844,	59,	19,	"iPhone 14"				],
			[393 ,	852,	59,	19,	"iPhone 14 Pro"			],
			[430,	932,	59,	19,	"iPhone 14 Pro Max"		],
			[768,	1024,	20,	20,	"iPad 2"				],
			[768,	1024,	20,	20,	"iPad Air or Retina"	],
			[1024,	1366,	20,	20,	"iPad Pro"				],
			[820,	1180,	43,	43,	"iPad Air 4/5"			]
		];
	}
	static get SPECS_FIREFOX()
	{
		return [
			[320,	480,	20,	20,	"iPhone 4"				],
			[320,	568,	20,	20,	"iPhone 5 or 5s"		],
			[375,	667,	20,	0,	"iPhone 6"				],
			[414,	736,	20, 0,	"iPhone 6 plus"			],
			[375,	667,	20,	0,	"iPhone 6 plus (Zoomed)"],
			[375,	812,	60,	0,	"iPhone 8. 11"			],
			[375,	812,	64,	0,	"iPhoneX"				],
			[375,	812,	44,	19,	"iPhone 11 Pro"			],
			[414,	896,	44,	19,	"iPhone 11 Pro Max"		],
			[390,	844,	47,	19,	"iPhone 12, 13"			],
			[428,	926,	47,	19,	"iPhone 13 Pro Max"		],
			[390,	844,	59,	19,	"iPhone 14"				],
			[393 ,	852,	59,	19,	"iPhone 14 Pro"			],
			[430,	932,	59,	19,	"iPhone 14 Pro Max"		],
			[768,	1024,	20,	20,	"iPad 2"				],
			[768,	1024,	20,	20,	"iPad Air or Retina"	],
			[1024,	1366,	20,	20,	"iPad Pro"				],
			[820,	1180,	43,	43,	"iPad Air 4/5"			]
		];
	}

	/**
	 * Show fullscreen notificatiuon if supported.
	 * @param {PIXI.Container} aFullscreenNoteLayer_sprt
	 * @param {HTMLElement} aStageLayout_obj
	 */
	tryToEnable(aFullscreenNoteLayer_sprt, aStageLayout_obj)
	{
		this._tryToEnableFullscreen(aFullscreenNoteLayer_sprt, aStageLayout_obj);
	}

	/**
	 * Indicates whether fullscreen notification is supported or not.
	 * @returns {boolean}
	 */
	get isFullScreenSupported()
	{
		return this.isFullScreenMethodDefined && this._fSupportedFullscreenMethod_str !== UFullScreen.SUPPORT_NONE;
	}

	/**
	 * Indicates whether availability of fullscreen notification is already checked or not.
	 * @returns {boolean}
	 */
	get isFullScreenMethodDefined()
	{
		return this._fSupportedFullscreenMethod_str !== null;
	}

	constructor()
	{
		super();

		this._fFullscreenAPIFunctions_obj = null;
		this._fFullscreenAPIEvents_obj = null;

		this._fFullscreenMask_de = null;
		this._fTreadmill_de = null;
		this._fBindedRaiseTreadmillHeight_fn = undefined;
		this._fFullscreenNoteLayer_sprt = null;
		this._fStageLayout_obj = null;

		this._fStartPoint_obj = null;
		this._fEndPoint_obj = null;
		this._fGestureBounds_obj = null;
		this._fCrossHitArea_obj = null;
		this._fCrossHitAreaYOffset = 0;

		this._fNotificationCross_sprt = null;
		this._fNotificationFinger_sprt = null;

		this._fSupportedFullscreenMethod_str = null;

		this._fGestureParsingActive_bln = false;

		this._fTransparentBack_pg = null;
		this._fPortraitTransparentBack_pg = null;

		this._fTouchEvents_obj = {
			pointerdown: "",
			pointerup: ""
		};

		if(!!window.PointerEvent)
		{
			this._fTouchEvents_obj.pointerdown = "pointerdown";
			this._fTouchEvents_obj.pointerup = "pointerup";
		}
		else
		{
			this._fTouchEvents_obj.pointerdown = "touchstart";
			this._fTouchEvents_obj.pointerup = "touchend";
		}

		this._fBindedOnPointerDown_fn = this._onPointerDown.bind(this);
		this._fBindedOnPointerUp_fn = this._onPointerUp.bind(this);

		//DEBUG...
		/*let l_html = this.sizeText = window.document.createElement('div');
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

		this._detectFullscreenAPIFunctions();
		this._detectFullscreenAPIEvents();
		this._identifySupportedFullscreenMethod();
	}

	_identifySupportedFullscreenMethod()
	{
		if (this._fullscreenAPIEnabled && !this._isIOS)
		{
			this._fSupportedFullscreenMethod_str = UFullScreen.SUPPORT_FULLSCREEN_API;
		}
		else if (this._isIOS && this._allowFullscreenByMaskForIOS)
		{
			this._fSupportedFullscreenMethod_str = UFullScreen.SUPPORT_MASK_FULLSCREEN;
		}
		else
		{
			this._fSupportedFullscreenMethod_str = UFullScreen.SUPPORT_NONE;
		}
	}

	_tryToEnableFullscreen(aFullscreenNoteLayer_sprt, aStageLayout_obj)
	{
		switch (this._fSupportedFullscreenMethod_str)
		{
			case UFullScreen.SUPPORT_FULLSCREEN_API:
				this._onFullscreenAllowed(aFullscreenNoteLayer_sprt, aStageLayout_obj);

				this._showFullscreenNotification();
				
			break;
			case UFullScreen.SUPPORT_MASK_FULLSCREEN:
				this._onFullscreenAllowed(aFullscreenNoteLayer_sprt, aStageLayout_obj);

				this._setupMaskFullScreen();
			break;
		}
	}

	_onFullscreenAllowed(aFullscreenNoteLayer_sprt, aStageLayout_obj)
	{
		this._fFullscreenNoteLayer_sprt = aFullscreenNoteLayer_sprt;
		this._fStageLayout_obj = aStageLayout_obj;

		this._setupFullscreenNotification();
		this._activateLineGestureParser();
		this._addResizeListener();
	}

	_activateLineGestureParser()
	{
		this._fGestureBounds_obj = {width: window.innerWidth * 0.35, height: window.innerHeight * 0.20};

		document.documentElement.addEventListener(this._fTouchEvents_obj.pointerdown, this._fBindedOnPointerDown_fn);
		document.documentElement.addEventListener(this._fTouchEvents_obj.pointerup, this._fBindedOnPointerUp_fn);

		this._fGestureParsingActive_bln = true;
	}

	_deactivateLineGestureParser()
	{
		document.documentElement.removeEventListener(this._fTouchEvents_obj.pointerdown, this._fBindedOnPointerDown_fn);
		document.documentElement.removeEventListener(this._fTouchEvents_obj.pointerup, this._fBindedOnPointerUp_fn);

		this._fGestureParsingActive_bln = false;
	}

	_onPointerDown(aEvent_obj)
	{
		let lTouch_obj = (aEvent_obj.changedTouches && aEvent_obj.changedTouches[0]) || aEvent_obj;
		this._fStartPoint_obj = {x: lTouch_obj.pageX, y: lTouch_obj.pageY};
	}

	_onPointerUp(aEvent_obj)
	{
		let lTouch_obj = (aEvent_obj.changedTouches && aEvent_obj.changedTouches[0]) || aEvent_obj;
		this._fEndPoint_obj = {x: lTouch_obj.pageX, y: lTouch_obj.pageY};

		this._parseNotificationCrossClick({x: lTouch_obj.pageX, y: lTouch_obj.pageY});

		if (this._fSupportedFullscreenMethod_str == UFullScreen.SUPPORT_FULLSCREEN_API)
		{
			this._parseLineGesture();
		}
	}

	//FULLSCREEN API...
	_parseLineGesture()
	{
		let lOffsetX_num = this._fStartPoint_obj.x - this._fEndPoint_obj.x;
		let lOffsetY_num = this._fStartPoint_obj.y - this._fEndPoint_obj.y;

		if (
				Math.abs(lOffsetX_num) < this._fGestureBounds_obj.width &&
				Math.abs(lOffsetY_num) > this._fGestureBounds_obj.height
			)
		{
			if (lOffsetY_num > 0)
			{
				this._activateFullScreenAPI();
			}
			else
			{
				this._deactivateFullScreenAPI();
			}
		}
	}

	_activateFullScreenAPI()
	{
		if (this._isFullscreenByAPI)
			return;

		let lRequest_fn = this._fFullscreenAPIFunctions_obj.requestFullscreen;
		let lElement_de = window.document.documentElement;

		if (lRequest_fn.indexOf('webkit') >= 0)
		{
			/*For security reasons, most keyboard inputs have been blocked in the fullscreen mode. However, in Google Chrome you can request keyboard support by calling the method with a flag:
			docElm.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
			This does not work in Safari, and the method won’t be called.*/
			let lIsKeyboardAllowed_bln = Boolean(typeof Element !== 'undefined' && 'ALLOW_KEYBOARD_INPUT' in Element);
			try
			{
				lElement_de[lRequest_fn](lIsKeyboardAllowed_bln && Element.ALLOW_KEYBOARD_INPUT);
			}
			catch (e)
			{
				lElement_de[lRequest_fn]();
			}
		}
		else
		{
			lElement_de[lRequest_fn]();
		}

		if (!this._isPortraitModeSupported)
		{
			screen.lockOrientationUniversal = screen.lockOrientation || screen.mozLockOrientation || screen.msLockOrientation;

			let lLockOrientationMode_str = "landscape";
			if (screen.lockOrientationUniversal)
			{
				lLockOrientationMode_str += "-primary";
				screen.lockOrientationUniversal(lLockOrientationMode_str)
			}
			else if (screen.orientation && screen.orientation.lock)
				screen.orientation.lock(lLockOrientationMode_str);
		}
		
		this._hideFullscreenNotification();
	}

	_deactivateFullScreenAPI()
	{
		if (!this._isFullscreenByAPI)
			return;

		window.document[this._fFullscreenAPIFunctions_obj.exitFullscreen]();

		this._showFullscreenNotification();
	}

	_detectFullscreenAPIFunctions()
	{
		let lFnMap_arr_arr = [
			[
				'requestFullscreen',
				'exitFullscreen',
				'fullscreenElement',
				'fullscreenEnabled',
				'fullscreenchange',
				'fullscreenerror'
			],
			[
				'webkitRequestFullscreen',
				'webkitExitFullscreen',
				'webkitFullscreenElement',
				'webkitFullscreenEnabled',
				'webkitfullscreenchange',
				'webkitfullscreenerror'

			],
			[
				'webkitRequestFullScreen',
				'webkitCancelFullScreen',
				'webkitCurrentFullScreenElement',
				'webkitCancelFullScreen',
				'webkitfullscreenchange',
				'webkitfullscreenerror'

			],
			[
				'mozRequestFullScreen',
				'mozCancelFullScreen',
				'mozFullScreenElement',
				'mozFullScreenEnabled',
				'mozfullscreenchange',
				'mozfullscreenerror'
			],
			[
				'msRequestFullscreen',
				'msExitFullscreen',
				'msFullscreenElement',
				'msFullscreenEnabled',
				'MSFullscreenChange',
				'MSFullscreenError'
			]
		];

		let lAPIFunctions_obj = {};

		for (let i = 0; i < lFnMap_arr_arr.length; ++i)
		{
			let lVal_arr = lFnMap_arr_arr[i];
			if (lVal_arr && lVal_arr[1] in window.document)
			{
				for (let i = 0; i < lVal_arr.length; ++i)
				{
					lAPIFunctions_obj[lFnMap_arr_arr[0][i]] = lVal_arr[i];
				}
				break;
			}
		}
		this._fFullscreenAPIFunctions_obj = lAPIFunctions_obj;
	}

	_detectFullscreenAPIEvents()
	{
		this._fFullscreenAPIEvents_obj = {
			change: this._fFullscreenAPIFunctions_obj && this._fFullscreenAPIFunctions_obj.fullscreenchange,
			error: this._fFullscreenAPIFunctions_obj && this._fFullscreenAPIFunctions_obj.fullscreenerror
		};
	}
	//...FULLSCREEN API

	//FULLSCREEN NOTIFICATION...
	get __fingerAssetName()
	{
		//must be overridden
		return "preloader/finger";
	}

	get __crossAssetName()
	{
		//must be overridden
		return "preloader/cross";
	}
	
	_setupFullscreenNotification()
	{
		let lTransparentBack_pg = this._fTransparentBack_pg = this._fFullscreenNoteLayer_sprt.addChild(new PIXI.Graphics());
		lTransparentBack_pg.beginFill(0x000000, 0.3).drawRect(-520, -300, 1040, 600).endFill();

		let lPortraitTransparentBack_pg = this._fPortraitTransparentBack_pg = this._fFullscreenNoteLayer_sprt.addChild(new PIXI.Graphics());
		lPortraitTransparentBack_pg.beginFill(0x000000, 0.3).drawRect(-520, -500, 1040, 1040).endFill();
		lPortraitTransparentBack_pg.visible = false;
		let lNotificationFinger_sprt = this._fNotificationFinger_sprt = this._fFullscreenNoteLayer_sprt.addChild(APP.library.getSprite(this.__fingerAssetName));
		lNotificationFinger_sprt.position.set(0, 20);

		let lNotificationFingerAnim_tween = new Tween(lNotificationFinger_sprt, "y", 20, -120, 1500);
		lNotificationFingerAnim_tween.autoRewind = true;
		lNotificationFingerAnim_tween.play();

		let lNotificationCross_sprt = this._fNotificationCross_sprt = this._fFullscreenNoteLayer_sprt.addChild(APP.library.getSprite(this.__crossAssetName));
		lNotificationCross_sprt.position.set(440, -210);

		this._updateBounds();

		if (this._isPortraitModeSupported)
		{
			this._updateArea();

			APP.layout.on(DOMLayout.EVENT_ON_ORIENTATION_CHANGED, this._onAppOrientationChange, this);
		}
	}

	//ORIENTATION...
	_onAppOrientationChange(event)
	{
		this._updateArea();
	}

	_updateArea()
	{
		let lNotificationCross_sprt = this._fNotificationCross_sprt;
		let lNotificationFinger_sprt = this._fNotificationFinger_sprt;
		if (APP.layout.isPortraitOrientation)
		{
			lNotificationCross_sprt && lNotificationCross_sprt.position.set(230, -420);
			this._fTransparentBack_pg && (this._fTransparentBack_pg.visible = false);
			this._fPortraitTransparentBack_pg && (this._fPortraitTransparentBack_pg.visible = true);
		}
		else
		{
			lNotificationCross_sprt && lNotificationCross_sprt.position.set(440, -210);
			this._fPortraitTransparentBack_pg && (this._fPortraitTransparentBack_pg.visible = false);
			this._fTransparentBack_pg && (this._fTransparentBack_pg.visible = true);
		}
	}
	//...ORIENTATION

	_updateBounds()
	{
		let lClientWidth_num = document.documentElement.clientWidth;
		let lClientHeight_num = document.documentElement.clientHeight;

		let lCalculatedCrossSize_num = Math.min((lClientWidth_num / 1024), (lClientHeight_num / 704)) * 110;
		if (this._isPortraitModeSupported && APP.layout.isPortraitOrientation)
		{
			lCalculatedCrossSize_num = Math.min((lClientHeight_num / 1024), (lClientWidth_num / 704)) * 110;
		}

		let lCalculatedSize_obj = this._fStageLayout_obj.calculatedSize;
		let lStageOffset_obj = {
			y: (document.documentElement.clientHeight - lCalculatedSize_obj.height) / 2,
			x: (document.documentElement.clientWidth - lCalculatedSize_obj.width) / 2
		};

		if(this._isIOS)
		{
			lStageOffset_obj.y = (window.innerHeight - lCalculatedSize_obj.height) / 2;
		}

		this._fCrossHitArea_obj = {
			x: lClientWidth_num - lStageOffset_obj.x - lCalculatedCrossSize_num,
			y: lStageOffset_obj.y,
			width: lCalculatedCrossSize_num,
			height: lCalculatedCrossSize_num
		};

		if (this._fFullscreenMask_de)
		{
			this._fFullscreenMask_de.style.width = document.documentElement.clientWidth + "px";
			this._fFullscreenMask_de.style.height = this._fullscreenMaskHeight + "px";
		}

		if (this._fTreadmill_de)
		{
			this._fTreadmill_de.style.height = (2 * this._fullscreenMaskHeight).toString() + "px";
		}
	}

	_showFullscreenNotification()
	{
		if (this._isLandscape || this._isPortraitModeSupported)
		{
			this.emit(UFullScreen.EVENT_SHOW_NOTE);

			if (this._fullscreenAPIEnabled && !this._fGestureParsingActive_bln)
			{
				this._activateLineGestureParser();
			}
		}
	}

	_hideFullscreenNotification()
	{
		this.emit(UFullScreen.EVENT_HIDE_NOTE);

		if (this._fullscreenAPIEnabled && this._fGestureParsingActive_bln)
		{
			this._deactivateLineGestureParser();
		}
	}

	_parseNotificationCrossClick(aPoint_obj)
	{
		if (
				aPoint_obj.x > this._fCrossHitArea_obj.x &&
				aPoint_obj.x < this._fCrossHitArea_obj.x + this._fCrossHitArea_obj.width &&
				aPoint_obj.y > this._fCrossHitArea_obj.y + this._fCrossHitAreaYOffset &&
				aPoint_obj.y < this._fCrossHitArea_obj.y + this._fCrossHitArea_obj.height + this._fCrossHitAreaYOffset
			)
		{
			switch (this._fSupportedFullscreenMethod_str)
			{
				case UFullScreen.SUPPORT_FULLSCREEN_API:
					this._hideFullscreenNotification();
				break;
				case UFullScreen.SUPPORT_MASK_FULLSCREEN:
					this._deactivateMaskFullScreen();
				break;
			}
		}
	}
	//...FULLSCREEN NOTIFICATION

	//FULLSCREEN MASK...
	_setupMaskFullScreen()
	{
		document.body.style.position = "relative";

		let lFullscreenMask_de = document.createElement("div");
		lFullscreenMask_de.setAttribute("id", "fullscreenMask");
		lFullscreenMask_de.style.width = document.documentElement.clientWidth + "px";
		lFullscreenMask_de.style.height = this._fullscreenMaskHeight + "px";
		lFullscreenMask_de.style.position = "fixed";
		lFullscreenMask_de.style.zIndex = "999";
		lFullscreenMask_de.style.overflow = "hidden";
		lFullscreenMask_de.style.display = "none";
		this._fFullscreenMask_de = lFullscreenMask_de;

		document.body.insertBefore(lFullscreenMask_de, document.body.firstChild);

		let lTreadmill_de = document.createElement("div");
		lTreadmill_de.setAttribute("id", "scrollFullscreenMask");
		document.body.appendChild(lTreadmill_de);
	
		lTreadmill_de.style.visibility = "hidden";
		lTreadmill_de.style.position = "relative";
		lTreadmill_de.style.zIndex = "10";
		lTreadmill_de.style.left = "0px";
		lTreadmill_de.style.width = "1px";
		lTreadmill_de.style.height = (2 * this._fullscreenMaskHeight).toString() + "px";
		this._fTreadmill_de = lTreadmill_de;
		this._fBindedRaiseTreadmillHeight_fn = this._raiseTreadmillHeight.bind(this);

		this._activateMaskFullScreen();
	}

	_activateMaskFullScreen()
	{
		document.body.style.overflow = "visible";
		this._showFullscreenNotification();

		let lFullscreenMask_de = document.getElementById("fullscreenMask");
		lFullscreenMask_de.style.display = "block";
		window.scrollTo(0, 0);
		window.addEventListener("scroll", this._fBindedRaiseTreadmillHeight_fn);
	};

	_deactivateMaskFullScreen()
	{
		document.body.style.overflow = "hidden";
		this._hideFullscreenNotification();
		window.removeEventListener("scroll", this._fBindedRaiseTreadmillHeight_fn);
		let lFullscreenMask_de = document.getElementById("fullscreenMask");
		lFullscreenMask_de.style.display = "none";
	};

	_raiseTreadmillHeight()
	{
		let lTreadmill_de = document.getElementById("scrollFullscreenMask");
		let lTreadmillHeight = lTreadmill_de.offsetHeight;
		let lScrollPos = window.pageYOffset;
		let lWindowHeight = window.innerHeight;
		let lUserPos = lScrollPos + lWindowHeight;

		this._fCrossHitAreaYOffset = lScrollPos; // for "X"(hide) button working when scroll did not trigger a full screen
	   
		if(lUserPos >= lTreadmillHeight)
		{
			lTreadmill_de.style.height = (lTreadmillHeight + this._fullscreenMaskHeight).toString() + "px";
		}
	}
	//...FULLSCREEN MASK

	_addResizeListener()
	{
		window.addEventListener("resize", this._onResize.bind(this));
	}

	_onResize()
	{
		setTimeout(this._updateBounds.bind(this), 200);
		setTimeout(this._updateBounds.bind(this), 1000);

		switch (this._fSupportedFullscreenMethod_str)
		{
			case UFullScreen.SUPPORT_FULLSCREEN_API:
				if (this._isPortrait && !this._isPortraitModeSupported)
				{
					this._hideFullscreenNotification();
				}
				else if ((this._isLandscape || this._isPortraitModeSupported) && !this._isFullscreenByAPI)
				{
					this._showFullscreenNotification();
				}
			break;
			case UFullScreen.SUPPORT_MASK_FULLSCREEN:
				if (this._isPortrait && !this._isPortraitModeSupported)
				{
					this._deactivateMaskFullScreen();
				}
				else if (!this._isFullscreenByMask)
				{
					this._activateMaskFullScreen();
				}
				else
				{
					this._deactivateMaskFullScreen();
				}
			break;
		}
	}

	get _fullscreenMaskHeight()
	{
		return document.documentElement.clientHeight + 1;
	}

	get _isFullscreenForbiddenForIos()
	{
		return (this._isIOS && this._isIPhone && window.devicePixelRatio <= 2 && window.screen.height/window.screen.width <= 1.775) || window.top != window.self;
	}

	get _isFullscreenByAPI()
	{
		return Boolean(window.document[this._fFullscreenAPIFunctions_obj.fullscreenElement]);
	}

	get _isFullscreenByMask()
	{
		let lStatusBarHeight_num = 39;

		let lSpecs_arr = this._isSafari ?
			UFullScreen.SPECS_SAFARI :
			(this._isChrome ?
				UFullScreen.SPECS_CHROME :
				(this._isFirefox ?
					UFullScreen.SPECS_FIREFOX :
					null
				)
			);

		if (!lSpecs_arr || !lSpecs_arr.length)
		{
			return false;
		}

		for (let i = 0; i < lSpecs_arr.length; ++i)
		{
			let lSpec_arr = lSpecs_arr[i];

			if (lSpec_arr[0] == screen.width && lSpec_arr[1] == screen.height)
			{
				lStatusBarHeight_num = this._isPortrait ? lSpec_arr[2] : lSpec_arr[3];
				break;
			}
		}

		let lScreenH_num = this._isLandscape ? screen.width : screen.height;
		let lScreenW_num = this._isLandscape ? screen.height : screen.width;
		let lWindowH_num = window.innerHeight + lStatusBarHeight_num;
		let lMaxDelta_num = 20;

		// https://jira.dgphoenix.com/browse/MA-382 Problem with incorrect innerHeight on iPad Pro
		if (this._isIOS && !this._isIPhone && this._isPortrait)
		{
			// True inner height
			lWindowH_num = lScreenW_num * (window.innerHeight/window.innerWidth) + lStatusBarHeight_num;
		}
		
		return Math.abs(lScreenH_num - lWindowH_num) <= lMaxDelta_num;
	}

	get _isLandscape()
	{
		return !this._isPortrait;
	}

	get _isPortrait()
	{
		return (window.innerHeight > window.innerWidth);
	}

	get _isPortraitModeSupported()
	{
		return APP.layout.isPortraitModeSupported;
	}

	get _fullscreenAPIEnabled()
	{
		return (window.document[this._fFullscreenAPIFunctions_obj.fullscreenEnabled] && this._fFullscreenAPIFunctions_obj);
	}

	get _isIPhone()
	{
		return !!navigator.platform && /iPhone/.test(navigator.platform);
	}

	get _isIPod()
	{
		return !!navigator.platform && /iPod/.test(navigator.platform);
	}

	get _isIOS()
	{
		let lIsIOS_bln = !!navigator.platform && /iPad|iPhone|iPod/.test(navigator.platform);

		if(!lIsIOS_bln)
		{
			lIsIOS_bln = navigator.maxTouchPoints && navigator.maxTouchPoints > 2 && /MacIntel/.test(navigator.platform);
		}

		return lIsIOS_bln;
	}

	get iOSVersion()
	{
		if (!this._isIOS)
		{
			return null;
		}
		let useragent = navigator.userAgent;
		let regex = (useragent.indexOf('Macintosh') > -1) ? /Version\/((?:(?:\d+)(?:_|\.?))+)/ : /\([^\)]*OS ((?:(?:\d+)(?:_|\.?))+)[^\)]*\)/;
		let versionMatch = useragent.match(regex);
		let version = versionMatch[1]
		if (version.indexOf('_') > -1)
		{
			version = version.split('_').join('.');
		}

		return version;
	}

	get _allowFullscreenByMaskForIOS()
	{
		if (!this._isIOS)
		{
			return false;
		}
		if (this._isFullscreenForbiddenForIos)
		{
			return false;
		}
		if (FEATURES.IOS && !this._isIPod)
		{
			return true;
		}
		
		return false;
	}

	get _isSafari()
	{
		let lUseragent_ua = navigator.userAgent.toLowerCase();
		let lIsSafari_bln = lUseragent_ua.indexOf("safari") != -1 && lUseragent_ua.indexOf("crios") < 0 && lUseragent_ua.indexOf("fxios") < 0;

		return lIsSafari_bln;
	}

	get _isChrome()
	{
		let lUseragent_ua = navigator.userAgent.toLowerCase();
		let lIsChrome_bln = lUseragent_ua.indexOf("crios") != -1;

		return lIsChrome_bln;
	}

	get _isFirefox()
	{
		let lUseragent_ua = navigator.userAgent.toLowerCase();
		let lIsChrome_bln = lUseragent_ua.indexOf("fxios") != -1;

		return lIsChrome_bln;
	}
}

export default UFullScreen;