/**
 * Environment features. Collection of constants
 * @module features
 */

//-----------------------------------------------------------------------------
// Operation Systems
//-----------------------------------------------------------------------------

/**
 * Android OS
 * @type {boolean}
 * @constant
 */
const ANDROID = !!navigator.userAgent.match(/Android/i);

/**
 * Windows Phone OS
 * @type {boolean}
 * @constant
 */
const WINDOWS_PHONE = !!navigator.userAgent.match(/Windows Phone/i);

/**
 * iOS
 * @type {boolean}
 * @constant
 */
const IOS = !WINDOWS_PHONE && (!!navigator.userAgent.match(/(iPad|iPhone|iPod)/i) || isIPAD());

function isIPAD ()
{
	if (navigator.userAgent.indexOf('Macintosh') > -1) {
		try {
			document.createEvent("TouchEvent");
			return true;
		} catch (e) {}
	}
}

const IOS_VERSION =
(function ()
{
	if (!IOS)
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
})();

const IPAD = !!navigator.userAgent.match(/iPad/i) || isIPAD();

/**
 * Blackberry
 * @type {boolean}
 * @constant
 */
const BLACKBERRY = !WINDOWS_PHONE && !!navigator.userAgent.match(/(Blackberry)/i);

/**
 * webOS
 * @type {boolean}
 * @constant
 */
const WEBOS = !WINDOWS_PHONE && !!navigator.userAgent.match(/(webOS)/i);

//-----------------------------------------------------------------------------
// Browsers
//-----------------------------------------------------------------------------

// Chrome
/**
 * Chrome browser
 * @type {boolean}
 * @constant
 */
const CHROME = !!navigator.userAgent.match(/Chrome/i)
	&& (!ANDROID || (parseInt((/Chrome\/([0-9]+)/.exec(navigator.appVersion) || 0)[1], 10) || 0) >= 22);

/**
 * PlayFree browser
 * @type {boolean}
 */
const PLAY_FREE_BROWSER = !!navigator.userAgent.match(/PlayFreeBrowser/i);

/**
 * Firefox browser
 * @type {boolean}
 * @constant
 */
const FIREFOX = navigator.userAgent.match(/Firefox/i);

const FIREFOX_IOS = navigator.userAgent.match(/FxiOS/i);

/**
 * Internet Explorer
 * @type {boolean}
 */
const IE = !!navigator.userAgent.match(/MSIE/i) || !!navigator.userAgent.match(/Trident/i) || navigator.appName == "Microsoft Internet Explorer";

/**
 * Edge
 * @type {boolean}
 */
const EDGE = !!navigator.userAgent.match(/Edge/i);

//-----------------------------------------------------------------------------
// Touch
//-----------------------------------------------------------------------------
/**
 * Touch or mouse pointer is handling. This value is not constant and toggles in runtime for hybrid devices on mouse or touch activity!
 * @type {boolean}
 */
let TOUCH_SCREEN = (ANDROID || WINDOWS_PHONE || IOS || BLACKBERRY || WEBOS)
	|| ('ontouchstart' in window)
	|| (navigator.MaxTouchPoints > 0)
	|| (navigator.msMaxTouchPoints > 0);


/**
 * Device is mobile
 * @type {boolean}
 * @constant
 */
const MOBILE = (ANDROID || WINDOWS_PHONE || IOS || BLACKBERRY || WEBOS);

if(!MOBILE) {
	window.addEventListener('mousemove', () => TOUCH_SCREEN = false, true);
	window.addEventListener('touchstart', () => TOUCH_SCREEN = true, true);
}

const TOUCH_EVENTS = {
	START: WINDOWS_PHONE ? "MSPointerDown" : "touchstart",
	MOVE: WINDOWS_PHONE ? "MSPointerMove" : "touchmove",
	END: WINDOWS_PHONE ? "MSPointerUp" : "touchend"
};

//-----------------------------------------------------------------------------
// Audio
//-----------------------------------------------------------------------------

const MP3_SUPPORT = (document.createElement('audio').canPlayType('audio/mpeg') != "");
const WEBAUDIO_SUPPORT = !!((window.AudioContext || window.webkitAudioContext) && (!MOBILE || !FIREFOX));

//-----------------------------------------------------------------------------
// Device info hacks
//-----------------------------------------------------------------------------

/**
 * Device is Iphone 4
 * @type {boolean}
 * @boolean
 */
const IPHONE4 = (MOBILE && navigator.userAgent.indexOf('iPhone') >= 0 && window.devicePixelRatio == 2);
/**
 * Some stock android browsers need hacks to work properly.
 * @type {boolean}
 * @constant
 */
const BROKEN_ANDROID = MOBILE && ANDROID && !CHROME && !FIREFOX;
/**
 * Some devices need performance hacks to work properly.
 * @type {boolean}
 * @constant
 */
const SLOW_DEVICE = (BROKEN_ANDROID && navigator.userAgent.toLowerCase().indexOf("sm-t310") >= 0)
	|| (MOBILE && ANDROID && FIREFOX && navigator.userAgent.toLowerCase().indexOf("sm-t310") >= 0);

// window.addEventListener('touchmove', (e) => {
//     e.preventDefault();
//     return false;
// });

export {
	ANDROID,
	IOS,
	IOS_VERSION,
	IPAD,
	WINDOWS_PHONE,
	BLACKBERRY,
	WEBOS,
	CHROME,
	PLAY_FREE_BROWSER,
	FIREFOX,
	FIREFOX_IOS,
	IE,
	EDGE,
	TOUCH_SCREEN,
	TOUCH_EVENTS,
	MP3_SUPPORT,
	WEBAUDIO_SUPPORT,
	MOBILE,
	IPHONE4,
	BROKEN_ANDROID,
	SLOW_DEVICE
}