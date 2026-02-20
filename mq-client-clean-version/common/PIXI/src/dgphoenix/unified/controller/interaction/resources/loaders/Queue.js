import EventDispatcher from '../../../events/EventDispatcher';

/**
 * @class
 * @inheritDoc
 * @extends EventEmitter
 * @classdesc Queue of loads
 */

let _loadingStopped = false;

class Queue extends EventDispatcher {

	constructor(...items) {
		super();
		this.items = new Set();
		this.data = new Set();
		this._complete = false;
		this._loadingErrorOccured = false;
		
		this.add(...items);

		this._concurrency = Queue.DEFAULT_CONCURRENCY;
		this._workers = 0;
		this._useCache = true;
	}

	static get DEFAULT_CONCURRENCY()
	{
		return 10;
	}

	set concurrency(val)
	{
		this._concurrency = val < 0 ? Queue.DEFAULT_CONCURRENCY : val;
	}

	/**
	 * @type {ProgressInfo}
	 * @readonly
	 */
	get progressInfo() 
	{
		let summary = {total: 0, complete: 0, progress: 0};
		for (let item of this.items) 
		{
			let itemProgressInfo = item.progressInfo;
			if (itemProgressInfo === undefined)
			{
				return undefined;
			}
			summary.total += itemProgressInfo.total;
			summary.complete += itemProgressInfo.complete;
		}

		for (let item of this.data) 
		{
			let {total, complete, progress} = item.progressInfo;
			summary.total += total;
			summary.complete += complete;
		}
		summary.progress = summary.total == 0 ? 1 : summary.complete / summary.total;
		
		return summary;
	}

	/**
	 * @type {Boolean}
	 * @readonly
	 */
	get complete() {
		return this._complete;
	}

	//noinspection JSAnnotator
	set complete(val) {
		if (!this._complete) {
			this._complete = !!val;
			if (this._complete) {
				this._dispatchComplete();
			}
		}
	}

	/**
	 * Add elements to load
	 * @param items to add
	 */
	add(...items) {
		for (let item of items) {
			item.once('complete', this._handleProgress, this);
			item.once('error', this._handleError, this);

			if (item instanceof Queue) {
				item.on('fileload', e => this._dispatchFileLoad(e.item));
				item.on('progress', this._handleProgress, this);
			}
			this.items.add(item);
		}
	}

	/**
	 * Combine queues
	 * @param {Queue} queue
	 * @param {Boolean} [clear=true] clear source queue or not
	 */
	merge(queue, clear = true) {
		this.items = new Set([...this.items, ...queue.items]);
		this.data = new Set([...this.data, ...queue.data]);
		if (clear) {
			queue.clear();
		}
	}

	/**
	 * Completely clear the Queue
	 */
	clear() {
		this.items.clear();
		this.data.clear();
		this._complete = false;
		this._workers = 0;
		this._useCache = true;
	}

	/**
	 * Start load
	 * @param {Boolean} [cache=true] - Use resources cache or not
	 */
	load(cache = true) 
	{
		this._useCache = cache;
		this._workers = 0;
		this.processQueueLoading();
	}

	stopLoading()
	{
		_loadingStopped = true;
	}

	processQueueLoading()
	{
		if (this.items.size == 0) 
		{
			this.complete = true;
			return;
		}
		
		if (this._workers >= this._concurrency)
		{
			return;
		}

		for (let item of this.items) {
			if (this._workers >= this._concurrency)
			{
				break;
			}

			if (item.inProgress)
			{
				continue;
			}

			this._workers++;
			item.load(this._useCache);
		}
	}

	_dispatchProgress()
	{
		/**
		 * @event Queue#progress
		 * @property {number} progress Completion progress 0..1
		 * @property {number} complete Items completed
		 * @property {number} total Items total
		 */
		this.emit('progress', this.progressInfo);
	}

	_dispatchFileLoad(item) 
	{
		/**
		 * @event Queue#fileload
		 * @property {QueueItem} item
		 */
		this.emit('fileload', {item: item});
	}

	_dispatchComplete()
	{
		/**
		 * @event Queue#complete
		 * @property {*} data Loaded data (depends on loader type)
		 */
		this.emit('complete');
	}

	_flushLoadedItems(item)
	{

		this.items.delete(item);
		this.data.add(item);
		this._workers--;
				
		if (!(item instanceof Queue)) 
		{
			this._dispatchFileLoad(item);   
		}
	}

	_handleProgress(e) 
	{
		if (e && e.target.complete)
		{
			this._flushLoadedItems(e.target);
		}
		
		this._dispatchProgress();
		
		if (!this._loadingErrorOccured && !_loadingStopped)
		{
			this.processQueueLoading();
		}
	}

	_handleError(e)
	{
		this._loadingErrorOccured = true;
		console.log("Queue _handleError", e.key, e.message);
		this.emit('error', {key: e.key, message: e.message});
	}

}

export default Queue;