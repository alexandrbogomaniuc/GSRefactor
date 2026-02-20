// TODO: Autodetect preferred data storage.

let DATA = new Map();

/**
 * @class
 * @classdesc Data storage.
 * @example <caption>Example of implementing your own data storage logic</caption>
 * const API_URL = 'http://example.com/api';
 * class MyRemoteStorage extends BaseStorage {
 *
 *     get async() {
 *         return true;
 *     }
 *
 *     load(callback = null) {
 *         let url = `${API_URL}?key=${this.key}`;
 *         get(url, function(data){
 *             callback(data);
 *         });
 *     }
 *
 *     save(data, callback = null) {
 *         let url = `${API_URL}?key=${this.key}&data=${data}`;
 *         post(url, function(data){
 *             callback(data);
 *         });
 *     }
 * }
 *
 * APP.storage = new MyRemoteStorage(dataKey);
 */
class BaseStorage {

	/**
	 * Base class of data storage. Keeps data in local variable.
	 * In real project use {CookieStorage}, {LocalStorage}
	 *
	 * @constructor
	 * @param {string} key Data key in storage
	 */
	constructor(key) {
		this.key = key;
	}

	/**
	 * @member {boolean} available Availability of storage
	 * @readonly
	 */
	get available() {
		return true;
	}

	/**
	 * @member {boolean} async Specifies whether the developer can receive data synchronously using this store, or should use the callback function
	 * @readonly
	 */
	get async() {
		return false;
	}

	/**
	 * Get data by storage key
	 * @param {function} [callback=null] For asynchronous loading. How the parameter gets the saved data.
	 * @returns {string} Serialized data (synchronous storage only)
	 */
	load(callback = null) {
		let data = DATA.get(this.key);
		if (callback instanceof Function) {
			callback(data);
		}
		return data;
	}

	/**
	 * Saving data with a specified storage key
	 * @param {string} data Data serialized to a string.
	 * @param {function} [callback=false] Called after the data has been saved.
	 */
	save(data, callback = null) {
		DATA.set(this.key, data);
		if (callback instanceof Function) {
			callback();
		}
	}
}

export default BaseStorage;