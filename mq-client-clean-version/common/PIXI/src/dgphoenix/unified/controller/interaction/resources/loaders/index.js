/**
 * Utilities for different data types loaders
 * @module loaders
 */

/**
 * @typedef ProgressInfo
 * @property {number} total Total items
 * @property {number} complete Completed items
 * @property {number} progress Comletion progress 0-1
 */

import { parseURL } from '../../../../model/interaction/server/helpers';

import AJAXLoader from './AJAXLoader';
import JSONLoader from './JSONLoader';
import XMLLoader from './XMLLoader';
import HTMLLoader from './HTMLLoader';
import ImageLoader from './ImageLoader';
import SoundLoader from './SoundLoader';
import FontLoader from './FontLoader';

//----------------------------------------------------------------------------------------------------------------------
let DefaultLoader = AJAXLoader, LOADER_MAP = new Map();
function registerLoader(cls, ...exts) {
	for (let ext of exts) {
		LOADER_MAP.set(ext.toLowerCase(), cls);
	}
}

function detectLoader(url) {
	let ext = (parseURL(url).extension || '').toLowerCase();
	return LOADER_MAP.has(ext) && LOADER_MAP.get(ext) || DefaultLoader;
}

function createLoader(url, ...args) {
	let cls = detectLoader(url);
	return new cls(url, ...args);
}

// Built-in loaders
registerLoader(JSONLoader, 'json');
registerLoader(XMLLoader, 'xml');
registerLoader(HTMLLoader, 'html');
registerLoader(ImageLoader, 'png', 'jpg', 'jpeg', 'gif', 'bmp');
registerLoader(SoundLoader, 'wav', 'mp3', 'ogg', 'aac', 'm4a', 'mp4');
registerLoader(FontLoader, 'ttf', 'otf', 'woff');
//----------------------------------------------------------------------------------------------------------------------

export {
	registerLoader,
	detectLoader,
	createLoader
};