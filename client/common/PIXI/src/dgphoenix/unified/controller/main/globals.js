import { WINDOWS_PHONE, MOBILE, ANDROID, CHROME, FIREFOX } from '../../view/layout/features';

if(WINDOWS_PHONE || (MOBILE && ANDROID && !CHROME && !FIREFOX)) {
	let lastTime = Date.now();

	window.requestAnimationFrame = function (callback) {
		if (typeof callback !== 'function') {
			throw new TypeError(callback + 'is not a function');
		}

		var currentTime = Date.now(), delay = 16 + lastTime - currentTime;

		if (delay < 0) delay = 0;

		lastTime = currentTime;

		return setTimeout(function () {
			lastTime = Date.now();
			callback(lastTime);
		}, delay);
	};
}

/**
 * Global variables and constants.
 * @module globals
 */

/**
 * Application instance
 * @type {Application}
 */
let APP = null;

/**
 * Sets the app instance
 * @param {Application} app
 */
function setApplication(app){
	APP = app;
}

/**
 * Returns absolute url for provided relative path
 * @param {string} src 
 * @returns {string}
 */
function generateAbsoluteURL(src)
{
	let abslouteURL = src;
	
	let lIsCommonAssetsFolderUrlIncluded_bl = src.indexOf(APP.commonAssetsController.info.commonAssetsFolderPath) >= 0;
	let lIsAppAssetsFolderUrlIncluded_bl = src.indexOf(APP.folderURL) >= 0;
	
	if (!lIsAppAssetsFolderUrlIncluded_bl && !lIsCommonAssetsFolderUrlIncluded_bl)
	{
		abslouteURL = APP.folderURL + src;
	}

	if (lIsCommonAssetsFolderUrlIncluded_bl)
	{
		let lCommonVersion_str = APP.commonAssetsController.info.commonAssetsVersion;
		if (src.indexOf(lCommonVersion_str) < 0)
		{
			abslouteURL += `?version=${lCommonVersion_str}`;
		}
	}
	else
	{
		let version = APP.version;
		if (version && src.indexOf(version) < 0)
		{
			abslouteURL += `?version=${version}`;
		}
	}

	return abslouteURL;
}

/**
 * Horizontal align
 * @type {{LEFT: number, CENTER: number, RIGHT: number, JUSTIFY: number}}
 * @property {Number} LEFT
 * @property {Number} CENTER
 * @property {Number} RIGHT
 * @property {Number} JUSTIFY
 * @constant
 */
const ALIGN = {
	LEFT: -1,
	CENTER: 0,
	RIGHT: 1,
	JUSTIFY: 2
};

/**
 * Vertical align
 * @type {{TOP: number, MIDDLE: number, BOTTOM: number}}
 * @property {Number} TOP
 * @property {Number} MIDDLE
 * @property {Number} BOTTOM
 * @constant
 */
const VALIGN = {
	TOP: -1,
	MIDDLE: 0,
	BOTTOM: 1
};

import * as CACHE from '../../model/storage/cache';

export { CACHE };
export { ALIGN, VALIGN };
export { APP, setApplication, generateAbsoluteURL };

//----------------------------------------------------------------------------------------------------------------------
if (!Array.from) Array.from = function(arg){
	var res = [];
	for (var entry of arg) res.push(entry);
	return res;
}