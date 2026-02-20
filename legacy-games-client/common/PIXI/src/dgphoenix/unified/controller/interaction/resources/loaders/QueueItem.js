import EventDispatcher from '../../../events/EventDispatcher';
import { CACHE, generateAbsoluteURL } from '../../../main/globals';

/**
 * @class
 * @inheritDoc
 * @extends EventDispatcher
 * @classdesc An item of loader Queue
 */
class QueueItem extends EventDispatcher {

	constructor(...params) {
		super();

		/** @ignore */
		this.data = this.prepareData(...params);

		/** @ignore */
		this.key = this.prepareKey(...params);

		/** @ignore */
		this.name = this.key; // TODO: API

		/**
		 * Function that modifies data before finish
		 * @type {Function}
		 */
		this.parser = null;

		/** @ignore */
		this._complete = false;

		/** @ignore */
		this._inProgress = false;

		/** @ignore */
		this._status = null;
		/** @ignore */
		this._statusMessage = null;
	}

	destructor()
	{
		super.destructor();
		this.data = null;
	}

	generateAbsoluteURL(src)
	{
		return generateAbsoluteURL(src);
	}

	/**
	 * @type {ProgressInfo}
	 * @readonly
	 */
	get progressInfo() {
		let c = this._complete ? 1 : 0;
		return {total: 1, complete: c, progress: c};
	}

	/**
	 * @type {boolean}
	 */
	get complete() {
		return !!this._complete;
	}

	//noinspection JSAnnotator
	set complete(val) {
		if (this._complete) return; // only once
		this._complete = !!val;
		if (this._complete) {
			// for loaders compatibility
			this.emit('progress', this.progressInfo);

			this.inProgress = false;
			
			if (this._status == 'error') {
				this._dispatchError();
			}

			if (this._status == 'success') {
				this._dispatchSuccess();
			}

			this._dispatchComplete();
		}
	}

	get inProgress() 
	{
		return !!this._inProgress;
	}

	set inProgress(val) 
	{
		this._inProgress = val;
	}

	_dispatchComplete(){
		/**
		 * @event QueueItem#complete
		 * @property {String} status - Completion status [success|error]
		 * @property {String} message - Status message [OK|ErrorMessage]
		 */
		this.emit('complete', {status: this._status, message: this._statusMessage});
	}

	_dispatchSuccess(){
		/**
		 * @event QueueItem#success
		 */
		this.emit('success', {status: this._status, message: this._statusMessage});
	}

	_dispatchError(){
		/**
		 * @event QueueItem#error
		 */
		this.emit('error', {status: this._status, message: this._statusMessage, key: this.key});
	}

	prepareData(...params) {
		return params.shift();
	}

	/**
	 * @param {...*} params
	 * @returns {*} Unique data key, for example URL.
	 * @ignore
	 */
	prepareKey(...params) {
		return params.shift();
	}

	parseData() {
		if (this.parser instanceof Function) {
			try {
				this.data = this.parser(this.data);
			}
			catch (e) {
				this._status = 'error';
				this._statusMessage = `Error parsing data ${this.key}: ${e.message}`;
				this.data = null;
			}
		}
	}

	cached(key = this.key){
		return CACHE.get(key);
	}

	cache(key = this.key){
		if (this.data) {
			CACHE.put(key, this.data, this.name);
		}
		else {
			CACHE.remove(key);
		}
	}

	/**
	 * Start load
	 * @param {Boolean} cache - Use cached data or not
	 */
	load(cache = true) {
		if (cache) {
			let data = this.cached();
			if (data) {
				this.data = data;
				this.completeLoad(cache);
			}
		}

		this.inProgress = true;
	}

	completeLoad(cache = true){
		if (this._status == 'error')
		{
			this.complete = true;
			return;
		}
		this.parseData();
		if (cache) {
			this.cache();
		}
		this._status = this._status || 'success';
		this._statusMessage = this._statusMessage || (this._status == 'success' ? 'OK' : 'Unknown error');
		this.complete = true;
	}

	/**
	 * Repeat loading. It's required to remove resource from cache manually if it was cached previously (cache clearing is not handled via this method).
	 */
	reload() { // not tested well!
		this._complete = false;
		this._status = null;
		this._statusMessage = null;
		this.load(false);
	}
}

export default QueueItem;