/**
 * @module cache
 */

import { parseURL } from '../interaction/server/helpers';

// some magic here
// removes /images/XX/
// removes /sounds/
let _urlMagic = new RegExp('^/?(sounds|images/([0-9\.]+))/', 'i');
function key2name(key) {
	let url = parseURL(key);
	let name = url.pathname;
	if (url.extension.length == 0) {
		return name;
	}
	name = name.replace(_urlMagic, '');
	name = name.replace(new RegExp('\.' + url.extension + '$', 'i'), '');
	return name;
}

// URL - data
let DATA = new Map();
// name - URL
let NAME_MAP = new Map();

/**
 * Maps asset name to file URL
 * @param {String} url
 * @param {String} [name] Autogenerate from URL if ommited
 */
function map(url, name = null) {
	if (!DATA.has(url)) {
		console.warn(`CACHE: Cannot map "${name}" to "${url}". Item not cached.`);
		return;
	}

	if (name === null) {
		name = key2name(url);
	}

	if (NAME_MAP.has(name) && NAME_MAP.get(name) !== url) {
		console.warn(`CACHE: Name "${name}" remapped to "${url}". Old value is "${NAME_MAP.get(name)}")`);
	}

	NAME_MAP.set(name, url);
}

/**
 * Puts data in cache
 * @param {String} key
 * @param {*} item
 * @param {String} [name] Alias to get data more convenient
 */
function put(key, item, name = null) {
	DATA.set(key, item);
	map(key, name);
}

/**
 * Returns cached data by name or by URL
 * @param {String} key Name or URL
 * @return {*}
 */
function get(key) {
	key = NAME_MAP.has(key) ? NAME_MAP.get(key) : key;
	return DATA.get(key);
}

/**
 * Removes cached data by URL or by name
 * @param {String} key
 */
function remove(key) {
	let url = NAME_MAP.has(key) ? NAME_MAP.get(key) : key;
	DATA.delete(url);
	for (let [name, val] of NAME_MAP) {
		if (url == val) NAME_MAP.delete(name);
	}
}

/**
 * Clear cache
 * @param {String|RagExp} pattern Provide pattern to clear only matching names and URLs
 */
function clear(pattern = null){
	if (pattern) {
		if (typeof pattern == 'string') {
			pattern = new RegExp(pattern);
		}
		// Clear matching URLs (from NAME_MAP too)
		for (let key of DATA.keys()) {
			if (pattern.test(key)) remove(key);
		}
		// Clear matching aliases
		for (let key of NAME_MAP.keys()) {
			if (pattern.test(key)) remove(key);
		}
	}
	else {
		DATA.clear();
		NAME_MAP.clear();
	}
}

export { DATA, NAME_MAP }; // XXX: exported for debug only. No public access tho these items!
export { map, put, get, remove, clear };
