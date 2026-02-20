import AJAXLoader from './AJAXLoader';
import JSONLoader from './JSONLoader';
import QueueItem from './QueueItem';
import Queue from './Queue';

/**
 * @class
 * @inheritDoc
 * @classdesc Spine animations loader
 */
class SpineLoader extends QueueItem {
	/**
	 * @constructor
	 * @param {String} path
	 */
	constructor(path) {
		super(path);
	}

	prepareData(src) {
		return null;
	}

	prepareKey(src) {
		return src;
	}

	load(cache = true) 
	{
		super.load(cache);
		if (!this.complete) {

			let loader = new Queue();

			let config = null;
			let atlas = null;

			let dataLoader = new JSONLoader(this.name + ".json");
			dataLoader.once("complete", e => {config = e.target.data});

			let atlasLoader = new AJAXLoader(this.name + ".atlas");
			atlasLoader.once("complete", e => {atlas = e.target.data});

			loader.add(
				dataLoader,
				atlasLoader
			);

			loader.once("complete", (e) => {

				this.data = {
					config: config,
					atlas: atlas
				};

				this.completeLoad(cache);
			});

			loader.once("error", (e) => {
				this._dispatchError();
			})

			loader.load();
		}
	}
}

export default SpineLoader;