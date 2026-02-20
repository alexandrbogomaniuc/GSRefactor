"use strict";

(function ()
{
	var ANDROID_BROWSER = "Android Browser",
		BLACKBERRY = "BlackBerry",
		CHROME = "Chrome",
		CHROME_ANDROID = "Chrome for Android",
		CHROME_IOS = "Chrome for iOS",
		EDGE = "Edge",
		EDGE_IOS = "Edge for iOS",
		FIREFOX = "Firefox",
		FIREFOX_ANDROID = "Firefox for Android",
		FIREFOX_IOS = "Firefox for iOS",
		INTERNET_EXPLORER = "Internet Explorer",
		INTERNET_EXPLORER_MOBILE = "IE Mobile",
		OPERA = "Opera",
		OPERA_MINI = "Opera Mini",
		OPERA_MOBILE = "Opera Mobile",
		SAMSUNG_BROWSER = "Samsung Internet",
		SAFARI = "Safari",
		UC_BROWSER = "UC Browser",
		WEB_VIEW_ANDROID = "WebView on Android",
		YANDEX_BROWSER = "Yandex Browser",
		WEB_VIEW_ANDROID_CHROME = "WebView on Android Chrome String",

		OS_WINDOWS = "Windows",
		OS_WINDOWS_CE = "Windows CE",
		OS_WINDOWS_PHONE = "Windows Phone",
		OS_MAC = "Mac OS",
		OS_IOS = "iOS",
		OS_LINUX = "Linux",
		OS_ANDROID = "Android",
		OS_BLACKBERRY = "BlackBerry",

		SUPPORTED_BROWSERS = {},
		SUPPORTED_OSES = {},
		AVS = "all versions supported";

	SUPPORTED_BROWSERS[CHROME] = "43.0";
	SUPPORTED_BROWSERS[CHROME_IOS] = "43.0";
	SUPPORTED_BROWSERS[CHROME_ANDROID] = "41.0";
	SUPPORTED_BROWSERS[EDGE] = "12.0";
	SUPPORTED_BROWSERS[EDGE_IOS] = "12.0";
	SUPPORTED_BROWSERS[FIREFOX] = "39.0";
	SUPPORTED_BROWSERS[FIREFOX_IOS] = "8.0";
	SUPPORTED_BROWSERS[FIREFOX_ANDROID] = "39.0";
	SUPPORTED_BROWSERS[INTERNET_EXPLORER] = "11.0";
	SUPPORTED_BROWSERS[INTERNET_EXPLORER_MOBILE] = "10.0";
	SUPPORTED_BROWSERS[OPERA] = "32.0";
	SUPPORTED_BROWSERS[OPERA_MINI] = "0.0";
	SUPPORTED_BROWSERS[OPERA_MOBILE] = "0.0";
	SUPPORTED_BROWSERS[SAFARI] = "9.0";
	SUPPORTED_BROWSERS[WEB_VIEW_ANDROID] = "4.0";
	SUPPORTED_BROWSERS[WEB_VIEW_ANDROID_CHROME] = "67.0";

	SUPPORTED_OSES[OS_WINDOWS] = AVS;
	SUPPORTED_OSES[OS_WINDOWS_PHONE] = "10.0";
	SUPPORTED_OSES[OS_MAC] = AVS;
	SUPPORTED_OSES[OS_IOS] = "8.0";
	SUPPORTED_OSES[OS_LINUX] = AVS;
	SUPPORTED_OSES[OS_ANDROID] = "5.0";

	var MESSAGE_DEFAULT = "The platform is not supported by the application.";
	var MESSAGE_TRIDENT = "Your browser does not support all features found within this game. Sounds and other functions may not occur as expected.";
	var MESSAGE_UPGRADE = "Please consider upgrading to the latest version of Chrome, Edge or Firefox browsers."
	var MESSAGE_WEBGL_NOT_SUPPORTED = "Your browser does not support all features found within this game. Sounds and other functions may not occur as expected. Please ensure that your hardware acceleration is turned on for the optimum experience.";
	var MESSAGE_NOT_SUPPORTED = "Your browser is not supported. Some features may not occur as expected.";
	var MESSAGE_NOT_SUPPORTED_RECOMMEND_CHROME_ANDROID = "Your browser is not supported, some features may not work as expected. We recommend using Chrome for Android.";
	var MESSAGE_NOT_SUPPORTED_RECOMMEND_IOS_SAFARI = "Your browser is not supported, some features may not work as expected. We recommend using Safari for IOS.";
	var MESSAGE_NOT_SUPPORTED_RECOMMEND_CHROME_DESKTOP = "Your browser is not supported, some features may not work as expected. We recommend using Chrome.";
	
	var userAgent = navigator.userAgent;
	var isSupported = false, isMobile = false, isSoundEnabled = false, browserName, browserVersion, webViewVersion, osName, osVersion, temp, tempWebView;

	var isWebGLSupported = function ()
	{
		var contextOptions = { stencil: true, failIfMajorPerformanceCaveat: true };

		try {
			if (!window.WebGLRenderingContext) {
				return false;
			}

			var canvas = document.createElement('canvas');
			var gl = canvas.getContext('webgl', contextOptions) || canvas.getContext('experimental-webgl', contextOptions);

			var success = !!(gl && gl.getContextAttributes().stencil);

			if (gl) {
				var loseContext = gl.getExtension('WEBGL_lose_context');

				if (loseContext) {
					loseContext.loseContext();
				}
			}

			gl = null;

			return success;
		} catch (e) {
			return false;
		}
	}

	//BROWSER NAME
	if (userAgentHas("Trident") || userAgentHas("MSIE"))
	{
		browserName = userAgentHas("Mobile") ? INTERNET_EXPLORER_MOBILE : INTERNET_EXPLORER;
	}
	if (userAgentHas("Firefox") && !userAgentHas("Seamonkey"))
	{
		browserName = userAgentHas("Android") ? FIREFOX_ANDROID : FIREFOX;
	}
	if (userAgentHas("Safari") && !userAgentHas("Chrome") && !userAgentHas("Chromium") && !userAgentHas("Android"))
	{
		browserName = userAgentHas("CriOS") ? CHROME_IOS : (userAgentHas("FxiOS") ? FIREFOX_IOS : SAFARI);
	}
	if (userAgentHas("Chrome"))
	{
		if (userAgentMatch(/\bChrome\/[.0-9]* Mobile\b/))
		{
			if (userAgentMatch(/\bVersion\/\d+\.\d+\b/) || userAgentMatch(/\bwv\b/))
			{
				browserName = WEB_VIEW_ANDROID;
			}
			else
			{
				browserName = CHROME_ANDROID;
			}
		}
		else
		{
			browserName = CHROME;
		}
	}
	if (userAgentHas("Android") && !userAgentHas("Chrome") && !userAgentHas("Chromium") && !userAgentHas("Trident") && !userAgentHas("Firefox"))
	{
		browserName = ANDROID_BROWSER;
	}
	if (userAgentHas("Edg"))
	{
		browserName = userAgentHas("EdgiOS") ? EDGE_IOS : EDGE;
	}
	if (userAgentHas("UCBrowser"))
	{
		browserName = UC_BROWSER;
	}
	if (userAgentHas("SamsungBrowser"))
	{
		browserName = SAMSUNG_BROWSER;
	}
	if (userAgentHas("OPR/") || userAgentHas("Opera") || userAgentHas("OPiOS"))
	{
		if (userAgentHas("Opera Mini") || userAgentHas("OPiOS"))
		{
			browserName = OPERA_MINI;
		}
		else if (userAgentHas("Opera Mobi") || userAgentHas("Opera Tablet") || userAgentHas("Mobile"))
		{
			browserName = OPERA_MOBILE;
		}
		else
		{
			browserName = OPERA;
		}
	}
	if (userAgentHas("BB10") || userAgentHas("PlayBook") || userAgentHas("BlackBerry"))
	{
		browserName = BLACKBERRY;
	}
	if (userAgentHas("YaBrowser"))
	{
		browserName = YANDEX_BROWSER;
	}

	//BROWSER VERSION
	switch (browserName)
	{
		case WEB_VIEW_ANDROID:
			temp = userAgentMatch(/Version\/((\d+\.)+\d+)/);
			tempWebView = userAgentMatch(/Chrome\/((\d+\.)+\d+)/);
			break;
		case CHROME:
		case CHROME_ANDROID:
			temp = userAgentMatch(/Chrome\/((\d+\.)+\d+)/);
			break;
		case CHROME_IOS:
			temp = userAgentMatch(/CriOS\/((\d+\.)+\d+)/);
			break;
		case FIREFOX:
		case FIREFOX_ANDROID:
			temp = userAgentMatch(/Firefox\/((\d+\.)+\d+)/);
			break;
		case FIREFOX_IOS:
			temp = userAgentMatch(/FxiOS\/((\d+\.)+\d+)/);
			break;
		case EDGE:
		case INTERNET_EXPLORER:
		case INTERNET_EXPLORER_MOBILE:
			if (userAgentHas("Edge"))
			{
				temp = userAgentMatch(/Edge\/((\d+\.)+\d+)/);
			}
			else if (userAgentHas("EdgA"))
			{
				temp = userAgentMatch(/EdgA\/((\d+\.)+\d+)/);
			}
			else if (userAgentHas("Edg"))
			{
				temp = userAgentMatch(/Edg\/((\d+\.)+\d+)/);
			}
			else if (userAgentHas("rv:11"))
			{
				temp = userAgentMatch(/rv:((\d+\.)+\d+)/);
			}
			else if (userAgentHas("MSIE"))
			{
				temp = userAgentMatch(/MSIE\ ((\d+\.)+\d+)/);
			}
			break;
		case EDGE_IOS:
			temp = userAgentMatch(/EdgiOS\/((\d+\.)+\d+)/);
			break;
		case SAFARI:
		case ANDROID_BROWSER:
			temp = userAgentMatch(/Version\/((\d+\.)+\d+)/);
			break;
		case UC_BROWSER:
			temp = userAgentMatch(/UCBrowser\/((\d+\.)+\d+)/);
			break;
		case SAMSUNG_BROWSER:
			temp = userAgentMatch(/SamsungBrowser\/((\d+\.)+\d+)/);
			break;
		case OPERA_MINI:
			if (userAgentHas("OPiOS"))
			{
				temp = userAgentMatch(/OPiOS\/((\d+\.)+\d+)/);
			}
			else
			{
				temp = userAgentMatch(/Opera Mini\/((\d+\.)+\d+)/);
			}
			break;
		case OPERA:
		case OPERA_MOBILE:
			if (userAgentMatch(/OPR/))
			{
				temp = userAgentMatch(/OPR\/((\d+\.)+\d+)/);
			}
			else if (userAgentMatch(/Version/))
			{
				temp = userAgentMatch(/Version\/((\d+\.)+\d+)/);
			}
			else
			{
				temp = userAgentMatch(/Opera\/((\d+\.)+\d+)/);
			}
			break;
		case BLACKBERRY:
			temp = userAgentMatch(/Version\/((\d+\.)+\d+)/);
			break;
		case YANDEX_BROWSER:
			temp = userAgentMatch(/YaBrowser\/((\d+\.)+\d+)/);
			break;
		default:
			break;
	}
	if (temp && temp[1])
	{
		browserVersion = temp[1];
	}

	if (tempWebView && tempWebView[1])
	{
		webViewVersion = tempWebView[1];
	}
	
	tempWebView = undefined;
	temp = undefined;

	//OS NAME
	if (userAgentHas("Windows"))
	{
		osName = userAgentHas("Windows Phone") ? OS_WINDOWS_PHONE : (userAgentHas("Windows CE") ? OS_WINDOWS_CE : OS_WINDOWS);
	}
	if (userAgentHas("OS X") && !userAgentHas("Android"))
	{
		osName = OS_MAC;
	}
	if (userAgentHas("Linux"))
	{
		osName = OS_LINUX;
	}
	if (userAgentHas("like Mac OS X"))
	{
		osName = OS_IOS;
	}
	if ((userAgentHas("Android") || userAgentHas("Adr")) && !userAgentHas("Windows Phone"))
	{
		osName = OS_ANDROID;
	}
	if (userAgentHas("BB10") || userAgentHas("RIM Tablet OS") || userAgentHas("BlackBerry"))
	{
		osName = OS_BLACKBERRY;
	}

	//OS VERSION
	switch (osName)
	{
		case OS_WINDOWS:
			/*
				3.1.1 - Windows 3.11
				4.0 - Windows 95
				4.1 - Windows 98
				4.9 - Windows ME
				5.0 - Windows 2000
				5.0.1 - Windows 2000 SP1
				5.1 - Windows XP
				5.2 - Windows Server 2003
				6.0 - Windows Vista
				6.1 - Windows 7
				6.2 - Windows 8
				6.3 - Windows 8.1
				10.0 - Windows 10
			*/
			if (userAgentHas("Win16"))
			{
				osVersion = "3.1.1";
			}
			else if (userAgentHas("Windows 95"))
			{
				osVersion = "4.0";
			}
			else if (userAgentHas("Windows 98"))
			{
				osVersion = userAgentHas("Windows 98; Win 9x 4.90") ? "4.9" : "4.1";
			}
			else
			{
				temp = userAgentMatch(/Win(?:dows)?(?: Phone)?[\ _]?(?:(?:NT|9x)\ )?((?:(\d+\.)*\d+)|XP|ME|CE)\b/);
			}
			break;
		case OS_WINDOWS_CE:
			temp = userAgentMatch(/Windows CE ((\d+[._])+\d+)\b/);
			break;
		case OS_WINDOWS_PHONE:
			if (userAgentHas("Windows Phone OS"))
			{
				temp = userAgentMatch(/Windows Phone OS ((\d+[._])+\d+)\b/);
			}
			else
			{
				temp = userAgentMatch(/Windows Phone ((\d+[._])+\d+)\b/);
			}
			break;
		case OS_MAC:
			temp = userAgentMatch(/OS\ X\ ((\d+[._])+\d+)\b/);
			break;
		case OS_IOS:
			temp = userAgentMatch(/OS\ ((\d+[._])+\d+)\ like\ Mac\ OS\ X/);
			break;
		case OS_ANDROID:
			temp = userAgentMatch(/(?:Android|Adr)\ (\d+([._]\d+)*)/);
			break;
		case OS_BLACKBERRY:
			if (userAgentHas("Tablet OS"))
			{
				temp = userAgentMatch(/RIM Tablet OS ((\d+\.)+\d+)/);
			}
			else
			{
				temp = userAgentMatch(/Version\/((\d+\.)+\d+)/);
			}
			break;
		case OS_LINUX: // linux user agent strings do not usually include the version
		default:
			break;
	}

	if (temp && temp[1])
	{
		temp[1] = temp[1].replace(/_/g, ".");
		osVersion = temp[1];
	}
	temp = undefined;



	//MOBILE
	var android = function ()
	{
		return !windows() && userAgentHas('Android');
	};

	var androidPhone = function ()
	{
		return android() && userAgentHas('Mobile');
	};

	var iphone = function ()
	{
		return !windows() && userAgentHas('iPhone');
	};

	var windows = function ()
	{
		return userAgentHas('Windows');
	};

	var windowsPhone = function ()
	{
		return windows() && userAgentHas('Phone');
	};

	var blackberryPhone = function ()
	{
		return blackberry() && !userAgentHas('Tablet');
	};

	var blackberry = function ()
	{
		return userAgentHas('blackberry') || userAgentHas('bb10') || userAgentHas('rim');
	};

	var fxos = function ()
	{
		return (userAgentHas('(Mobile;') || userAgentHas('(Tablet;')) && userAgentHas('; rv:');
	};

	var fxosPhone = function ()
	{
		return fxos() && userAgentHas('Mobile');
	};

	var meego = function ()
	{
		return userAgentHas('Meego');
	};

	var ipad = function ()
	{
		if (userAgent.indexOf('iPad') > -1) {
			return true;
		}

		if (userAgent.indexOf('Macintosh') > -1) {
			try {
				document.createEvent("TouchEvent");
				return true;
			} catch (e) {}
		}

		return false;
	};

	var androidTablet = function ()
	{
		return android() && !userAgentHas('Mobile');
	};

	var blackberryTablet = function ()
	{
		return blackberry() && userAgentHas('Tablet');
	};

	var windowsTablet = function ()
	{
		return windows() && (userAgentHas('touch') && !windowsPhone());
	};

	var fxosTablet = function ()
	{
		return fxos() && userAgentHas('Tablet');
	};

	var mobile = function ()
	{
		return androidPhone() || iphone() || windowsPhone() || blackberryPhone() || fxosPhone() || meego();
	};

	var tablet = function ()
	{
		return ipad() || androidTablet() || blackberryTablet() || windowsTablet() || fxosTablet();
	};

	//VALIDATION
	var browserReqVersion = browserName ? SUPPORTED_BROWSERS[browserName] : undefined,
		osReqVersion = osName ? SUPPORTED_OSES[osName] : undefined;

	if (checkVersion(osVersion, osReqVersion) && checkVersion(browserVersion, browserReqVersion) && checkWebViewVersionIfRequired(browserName, webViewVersion))
	{
		var alertMessage = undefined;

		if (!isWebGLSupported())
		{
			alertMessage = MESSAGE_WEBGL_NOT_SUPPORTED;
		}
		else
		{
			if (browserName === INTERNET_EXPLORER)
			{
				alertMessage = MESSAGE_TRIDENT + " " + MESSAGE_UPGRADE;
			}
			else if (browserName === INTERNET_EXPLORER_MOBILE)
			{
				alertMessage = MESSAGE_TRIDENT;
			}
		}

		if (alertMessage !== undefined)
		{
			alert(alertMessage);
		}

		isSoundEnabled = (browserName !== INTERNET_EXPLORER && browserName !== INTERNET_EXPLORER_MOBILE);
		isSupported = true;
	}
	else
	{
		if(!isSupportedBrowser(browserName, SUPPORTED_BROWSERS) || browserName == WEB_VIEW_ANDROID)
		{
			if (isMobile)
			{
				if (osName == OS_ANDROID)
				{
					alert(MESSAGE_NOT_SUPPORTED_RECOMMEND_CHROME_ANDROID);
				}
				else if (osName == OS_IOS)
				{
					alert(MESSAGE_NOT_SUPPORTED_RECOMMEND_IOS_SAFARI);
				}
				else
				{
					alert(MESSAGE_NOT_SUPPORTED);
				}
			}
			else if (osName == OS_WINDOWS || osName == OS_MAC || osName == OS_LINUX)
			{
				alert(MESSAGE_NOT_SUPPORTED_RECOMMEND_CHROME_DESKTOP);
			}
			else
			{
				alert(MESSAGE_NOT_SUPPORTED);
			}

			isSupported = true;
		}
		else
		{
			alert(MESSAGE_DEFAULT + "\n" + userAgent);
		}
	}

	if (mobile() || tablet())
	{
		isMobile = true;
	}

	//UTILS
	function isSupportedBrowser(browserName, SUPPORTED_BROWSERS)
	{
		for (var key in SUPPORTED_BROWSERS)
		{
			if (browserName === key) return true;
		}
		return false;
	}

	function userAgentHas(txt)
	{
		if (!userAgent) return false;
		return ~userAgent.indexOf(txt);
	}

	function userAgentMatch(reg)
	{
		if (!userAgent) return false;
		return userAgent.match(reg);
	}

	function checkVersion(current, required)
	{
		if (required === AVS)
		{
			return true;
		}
		else if (current !== undefined && required !== undefined)
		{
			var splitCurrent = current.split('.'),
				splitRequired = required.split('.'),
				compareDepth = splitRequired.length,
				cur, req, i;

			for (i = 0; i < compareDepth; i++)
			{
				cur = +splitCurrent[i] || 0;
				req = +splitRequired[i];
				if (cur !== req || i === compareDepth - 1)
				{
					return cur >= req;
				}
			}
		}
		return false;
	}

	function checkWebViewVersionIfRequired(aBrowserName, current)
	{
		if (aBrowserName != WEB_VIEW_ANDROID)
		{
			return true;
		}
		
		var webViewReqVersion = SUPPORTED_BROWSERS[WEB_VIEW_ANDROID_CHROME];

		if (webViewReqVersion === AVS)
		{
			return true;
		}
		else if (current !== undefined && webViewReqVersion !== undefined)
		{
			var splitCurrent = current.split('.'),
				splitRequired = webViewReqVersion.split('.'),
				compareDepth = splitRequired.length,
				cur, req, i;

			for (i = 0; i < compareDepth; i++)
			{
				cur = +splitCurrent[i] || 0;
				req = +splitRequired[i];
				if (cur !== req || i === compareDepth - 1)
				{
					return cur >= req;
				}
			}
		}
		return false;
	}

	

	window.getPlatformInfo = function ()
	{
		return {
			name: browserName,
			version: browserVersion,
			webViewverson: webViewVersion,
			minVersion: browserReqVersion,
			supported: isSupported,
			mobile: isMobile,
			soundEnabled: isSoundEnabled,
			os: {
				name: osName,
				version: osVersion,
				minVersion: osReqVersion
			},
			ua: userAgent
		}
	}

	window.isWebGLSupported = isWebGLSupported();
})();