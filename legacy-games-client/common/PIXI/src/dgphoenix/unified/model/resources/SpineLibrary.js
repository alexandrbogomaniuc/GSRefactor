import { APP } from '../../controller/main/globals';
import SpineLoader from '../../controller/interaction/resources/loaders/SpineLoader';
import Queue from '../../controller/interaction/resources/loaders/Queue';
import SpineSprite from '../../view/base/display/SpineSprite';

var ATLAS_SCALE = null;

/**
 * @class
 * @classdesc Spine animations library
 */
class SpineLibrary {

	/**
	 * @param path Relative path to spine configs folder
	 * @param {Array} [list]
	 */
	constructor(path = APP.contentPathURLsProvider.spinePath, list = []) {

		this.path = path;
		this.items = new Map();
		this.loaded = false;

		this.textureAutoSufix = true;

		this.addItems(list);
	}

	/** Add items to spine library. */
	addItems(list=[]) {
		for(let spine of list) {
			this.items.set(spine.name, {
				name: spine.name,
				jsonScale: spine.jsonScale,
				textureScale: spine.textureScale,
				assets: spine.assets,
				data: null
			});
		}	
	}

	/**
	 * Creates loader queue.
	 */
	createLoaderQueue(list) {

		this.addItems(list);

		let queue = new Queue();
		for (let [name] of this.items) {
			let ldr = new SpineLoader(this.path + "/" + name);
			ldr._spineName = name;
			ldr._spineJsonScale = this.items.get(name).jsonScale;
			ldr._spineTextureScale = this.items.get(name).textureScale;
			ldr._assets = this.items.get(name).assets;
			queue.add(ldr);
		}

		queue.on('fileload', this.prepareSpineDataItems, this);

		return queue;
	}

	detectAtlasScale() {
		if(ATLAS_SCALE) return ATLAS_SCALE;

		var max = -1;

		for(let scale of APP.config.scales) {
			if(scale > max) max = scale;
		}

		return max;
	}

	/** Sets spine data to items. */
	prepareSpineDataItems(e) {
		
		let assets = e.item._assets;
		let ignoreAtlasAssetName = true;
		if (!assets)
		{
			assets = [e.item._spineName];
			ignoreAtlasAssetName = false;
		}

		for(let i = 0; i < assets.length; ++i) {
			let atlas = e.item.data.atlas;
			let config = e.item.data.config;
			let name = assets[i];
			let spineTextureScale = e.item._spineTextureScale;
			let spineJsonScale = e.item._spineJsonScale;
			this.prepareSpineData(atlas, config, name, ignoreAtlasAssetName, spineTextureScale, spineJsonScale);
		}
	}

	/** Sets spine data to item. */
	prepareSpineData(atlas, config, name, ignoreAtlasAssetName = false, spineTextureScale = 1, spineJsonScale = 1) {
		
		let scale = APP.layout.bitmapScale;
		let texScale = -1;

		let texturesList = [];

		let spineAtlas = new PIXI.spine.core.TextureAtlas(atlas, (line, callback) => {

			let assetName = name;
			if (!ignoreAtlasAssetName)
			{
				let parts = line.split(".");
				parts.pop();
				assetName = parts.join(".");

				if(this.textureAutoSufix) {
					parts = name.split("/");
					if(parts.length > 1) {
						parts.pop();
						assetName = parts.join("/") + "/" + assetName;
					}
				}
			}

			let tex = APP.library.getAsset(assetName).baseTexture;

			if(texScale < 0) {
				texScale = this.detectAtlasScale(atlas, tex);
			}

			tex.resolution = texScale / spineTextureScale;
			texturesList.push(tex);

			callback(tex);
		});

		//for(let tex of texturesList) tex.resolution = scale;
		
		let spineJsonParser = new PIXI.spine.core.SkeletonJson(new PIXI.spine.core.AtlasAttachmentLoader(spineAtlas));
		spineJsonParser.scale = spineJsonScale;
		let skeletonData = spineJsonParser.readSkeletonData(config);


		this.items.set(name, {
			name: name,
			data: skeletonData
		});
	}

	/**
	 * Returns Spine config by name
	 * @param {String} name
	 */
	getData(name) {
		let item = this.items.get(name);
		if(!item) {
			throw Error("Spine data " + name + " not found");
		}

		return item.data;
	}

	/**
	 * Creates SpineSprite by name
	 * @param {String} name
	 * @returns {SpineSprite}
	 */
	getSprite(name) {
		return new SpineSprite(this.getData(name), name);
	}

	static get atlasScale() {
		return ATLAS_SCALE;
	}

	static set atlasScale(val) {
		ATLAS_SCALE = val;
	}
}

export default SpineLibrary;