import Queue from "./Queue";

/**
 * @class
 * @inheritDoc
 * @extends Queue
 * @classdesc Sync loaders queue. Start loading next item when previous one is loaded
 */
class SyncQueue extends Queue {
	
	constructor(...items) {
		super(...items);
	}

	loadNextItem() 
	{
		let item = this.items.values().next();
		if(item && !item.done) 
		{
			item.value.once('complete', this.onSyncQueueItemLoaded, this);
			item.value.load();
		}
		this._handleProgress();
	}

	onSyncQueueItemLoaded()
	{
		this.loadNextItem();
	}

	processQueueLoading()
	{
		// no actions required
	}

	/**
	 * Start load
	 * @param {Boolean} [cache=true] - Use resources cache or not
	 */
	load(cache = true) 
	{
		this.loadNextItem();
	}
}

export default SyncQueue;