import CookieStorage from './CookieStorage';

/**
 * @class
 * @classdesc Save data to LocalStorage or (if not supported by the browser) to Cookie
 * @inheritDoc
 * @extends CookieStorage
 */
class LocalStorage extends CookieStorage {

	constructor(key) {
		super(key);
	}

	/**
	 * Is local storage available or not.
	 * @type {boolean}
	 */
	get available() {
		return !!(window && window.localStorage);
	}

	get async () {
		return false;
	}

	/** Get data from local storage. */
	load(callback = null) {

		// fallback to cookies for browsers without localStorage support
		if (!this.available) {
			return super.load(callback);
		}

		let data = window.localStorage.getItem(this.key);
		if (callback instanceof Function) {
			callback(data);
		}
		return data;
	}

	/** Save data to local storage. */
	save(data, callback = null) {

		// fallback to cookies for browsers without localStorage support
		if (!this.available) {
			return super.save(data, callback);
		}

		window.localStorage.setItem(this.key, data);
		if (callback instanceof Function) {
			callback();
		}
	}
}

export default LocalStorage;