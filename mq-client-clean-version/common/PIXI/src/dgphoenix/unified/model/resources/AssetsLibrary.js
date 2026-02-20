import { APP } from '../../controller/main/globals';
import ImageLoader from '../../controller/interaction/resources/loaders/ImageLoader';
import Queue from '../../controller/interaction/resources/loaders/Queue';
import Sprite from '../../view/base/display/Sprite';
import Asset from './Asset';
import ProfilingInfo from '../profiling/ProfilingInfo';
import { Utils } from '../Utils';
import { AtlasSprite } from '../../view/base/display';

function src2name(src) {
	var name = src.split('/');
	name = name.pop();
	name = name.split('.');
	name = name.shift() + '';
	return name;
}

/**
 * @class
 * @classdesc Library of bitmaps
 */
class AssetsLibrary {

	/**
	 * @constructor
	 * @param {String} [path] Relative path to images
	 * @param {float} [scale] Used scale
	 * @param {Array} [assets] Optional array of assets descriptors
	 */
	constructor(path = APP.contentPathURLsProvider.assetsPath, scale = APP.layout.bitmapScale, assets = []) {

		this.path = path;

		this.scale = scale;

		this.items = new Map();

		this.loaded = false;

		this.spriteClass = Sprite;

		this.atlasTextures = new Map();
        this.atlasKeys = new Map();

		this.addAssets(assets);
	}

	/**
	 * Creates images loader queue
	 * @return {Queue}
	 */
	createLoaderQueue(assets, aOptIsCommonAssets_bl=false) {
		let lAddedAssets_arr = this.addAssets(assets);

		if (!lAddedAssets_arr || !lAddedAssets_arr.length)
		{
			return null;
		}

		let self = this;
		let queue = new Queue();
		for (let asset of lAddedAssets_arr)
		{
			if (asset.ready)
			{
				continue;
			}

			if (aOptIsCommonAssets_bl)
			{
				asset.src = APP.commonAssetsController.info.generateAssetAbsoluteURL(asset.src);
			}

			let ldr = new ImageLoader(asset.src);
			ldr.name = asset.name || null;
			// console.log("** added loader for ", ldr.name)
			queue.add(ldr);
		}

		queue.on('fileload', (e)=> {
			let asset = this.items.get(e.item.name);
			// console.log("asset fileload", e.item.name, e.item.data);
			asset.bitmap = e.item.data;
		});

		return queue;
	}

	/**
	 * Adds a set of image assets
	 * @param {Array} data Array of assets descriptors
	 * @returns {void}
	 */
	addAssets(data) {
		if (typeof data == 'undefined') return;
		if (typeof data != 'object') return;

		let lAddedAssets_arr = [];

		for (let item of data) {
			item.noscale = ( typeof item.noscale == 'undefined') ? false : item.noscale;
			if (!item.noscale) item.src = '%APP.layout.bitmapScale%/' + item.src;
			let lAsset_a = this.addAsset(item);

			lAsset_a && lAddedAssets_arr.push(lAsset_a);
		}

		return lAddedAssets_arr;
	}

	/**
	 * Adds an asset to the queue
	 * @param {String} src
	 * @param {String} [name] Asset name
	 * @param {Number} [w] Width for 1x scale
	 * @param {Number} [h] Height for 1x scale
	 * @param {Number} [f=1] Frames amount (vertically)
	 * @param {Number} [l=1] Layers amount (horizontally)
	 * @returns {Asset}
	 */
	addAsset(src, name = undefined, w = undefined, h = undefined, f = 1, l = 1) {
		if (!src) return null;

		if (src.isMobile !== undefined)
		{
			if (APP.isMobile && !src.isMobile) return null;
			if (!APP.isMobile && src.isMobile) return null;
		}
		if (src.vfx !== undefined)
		{
			//check vfx
			if ( APP.profilingController.info.i_isProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE, src.vfx) )
			{
				return null;
			}
		}

		let spriteClass = null, properties = null, quality = this.scale;
		let createTexture = src.createTexture != undefined ? src.createTexture : true;
		if (typeof src == 'object' && (arguments.length == 1)) {
			name = src.name || src2name(src.src);
			w = ~~src.width || 0;
			h = ~~src.height || 0;
			f = ~~src.frames || 1;
			l = ~~src.layers || 1;
			spriteClass = src.spriteClass || null;
			properties = src.properties || null;
			quality = src.quality || this.scale;
			src = src.src;
		}

		src = src.replace('%APP.layout.bitmapScale%', '%PATH%/' + quality);
		src = src.replace('%PATH%', this.path);

		var asset = new Asset(name, src, w, h, f, l, createTexture);
		asset.spriteClass = spriteClass;
		asset.scale = quality;

		// extend asset with custom properties
		if (properties) {
			for (var prop in properties) {
				if (typeof asset[prop] == 'undefined') asset[prop] = properties[prop];
			}
		}

		this.items.set(name, asset);
		return asset;
	}

	removeAsset(keyOrAsset, destroyBaseTexture=false)
	{
		let key;
		let asset;
		
		if (keyOrAsset instanceof Asset)
		{
			key = keyOrAsset.name;
			asset = keyOrAsset;
		}
		else
		{
			key = keyOrAsset;
			asset = this.items.get(key);
		}

		if (!(asset instanceof Asset))
		{
			return;
		}

		if (!!destroyBaseTexture && !!asset.baseTexture)
		{
			let baseTex = asset.baseTexture;
			baseTex.destroy();
		}

		asset.bitmap = null;
		asset.baseTexture = null;

		if (this.items.has(key))
		{
			this.items.delete(key);
		}
		
	}

	destroyAssets(assetNames)
	{
		if (!assetNames || !assetNames.length)
		{
			return;
		}

		for (let i=0; i<assetNames.length; i++)
		{
			let assetName = assetNames[i];
			this.removeAsset(assetName, true);
		}
	}

	/**
	 * Adds new asset to the queue
	 * @param {Object} obj
	 * @returns {Asset}
	 */
	addObject(obj) {
		// objects are always scaled and size is 1x
		var asset = this.addAsset('%APP.layout.bitmapScale%/' + obj.image, obj.name, obj.width * this.scale, obj.height * this.scale, obj.frames, obj.layers);
		if (asset) asset.object = obj;
		return asset;
	}

	/**
	 * Get asset by name
	 * @param {String} name Asset name
	 * @param {Boolean} [checkLoad=true] Indicates if ready asset required
	 * @returns {Asset}
	 */
	getAsset(name, checkLoad = true) {
		let asset = this.items.get(name);

		if (!asset || !asset.bitmap) {
			
			if (!asset)
			{
				console.log("error asset", name);
			}
			else
			{
				console.log("error asset.bitmap", name);
			}
			throw new Error(`Trying to get undefined asset "${name}"`);
		}

		if (checkLoad && !asset.ready) {
			throw new Error(`Trying to get asset "${name}" before it has been loaded`);
		}

		return asset;
	}

	/**
	 * Get asset Sprite
	 * Ima @param {String} namege name
	 * @param {Object} [params] Start values of Sprite properties (ex {x:10, y:20, opacity:0.5, onclick:myOnClick})
	 * @param {Function} [spriteClass=Sprite] Sprite class constructor: `Sprite` by default.
	 * @returns {Sprite}
	 */
	getSprite(name, params = null, spriteClass = null) {
		var mc = null, asset = null;
		try {
			asset = this.getAsset(name, true);
		}
		catch (e) {
			asset = new Asset();
		}

		spriteClass = spriteClass || asset.spriteClass || this.spriteClass || Sprite;

		if (typeof spriteClass == "string") {
			if (window[spriteClass]) spriteClass = window[spriteClass];
			else spriteClass = eval(spriteClass);
		}

		if (spriteClass.create && (typeof spriteClass.create == 'function')) {
			mc = spriteClass.create(asset, this);
		}
		else {
			// TODO: remove default sprite constructor usage, or refactor constructor parameters
			mc = new spriteClass(asset.bitmap, asset.width, asset.height, asset.frames, asset.layers);
		}

		if (params && (typeof params == 'object')) {
			// override sprite defaults
			Object.assign(mc, params);
		}

		        if(!mc)
        {
            console.log("trying to get sprite from atlas " + name);
            this.getSpriteFromAtlas(name)
        }

        return mc;
    }

	/**
     * Get asset Sprite
     * Ima @param {String}  name in atlas file
     * @returns {Sprite}
     */
    getSpriteFromAtlas(aName_str) 
    {
        let key = null;
        if(this.atlasKeys.has(aName_str)){
            key = this.atlasKeys.get(aName_str);
        }else{
            const map1 = new Array([...this.atlasTextures].filter(([k, v]) => aName_str.indexOf(k)>-1));  
            key = map1[0][0][0]; 
            this.atlasKeys.set(aName_str, key);
        }
        
        const parentTexture = this.atlasTextures.get(key);
        const mc = new Sprite(); 
        mc.texture =  Utils.getTexture(parentTexture, aName_str);
		return mc;
	}

	registerAtlas(file, config)
    {
        
        const keyArray = file.split("/");
        keyArray.pop();
        const key = keyArray.join("/");
        if(this.atlasTextures.has(key)) return;
        const texture = AtlasSprite.getFrames(APP.library.getAsset(file), config, "");
        this.atlasTextures.set(key, texture);
    }
	
	/**
	 * Returns bitmap with required scale and size.
	 * @param {String} name Image name
	 * @returns {Image}
	 */
	getBitmap(name) {
		try {
			var asset = this.getAsset(name, true);
			return asset.bitmap;
		}
		catch (e) {
			return null;
		}
	}
}

export default AssetsLibrary;