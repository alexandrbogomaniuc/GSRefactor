import { generateAbsoluteURL } from '../../controller/main/globals';

/**
 * @class
 * @classdesc Library asset {AssetsLibrary}
 */
class Asset {

	/**
	 * @constructor
	 * @param {String} name Unique asset name in the library
	 * @param {String} src URL of the image
	 * @param {Number} w width of one frame in the sprite at 1x scale
	 * @param {Number} h height of one frame in the sprite at 1x scale
	 * @param {Number} f amount of frames in the sprite (arranged vertically)
	 * @param {Number} l number of layers in the sprite (arranged horizontally)
	 * @param {Boolean} createTexture - whether to create a texture for loading into the GPU
	 */
	constructor(name, src, w = undefined, h = undefined, f = 1, l = 1, createTexture = true) {
		
		this.name = name + '';

		this.src = src + '';

		this.width = w;

		this.height = h;

		this.frames = f;

		this.layers = l;

		this.createTexture = createTexture;

		this._bitmap = null;

		this.baseTexture = null;

		/**
		 * @member {Boolean} ready Sets to TRUE when image loaded and ready to use
		 * @ignore
		 */
		this.ready = false;

		/**
		 * @member {Function} spriteClass Sprite constructor (by default {Sprite})
		 */
		this.spriteClass = null;

		this._scale = 1;
	}

	/**
	 * Asset bitmap.
	 */
	get bitmap() {
		return this._bitmap;
	}

	set bitmap(val) {
		this._bitmap = val;
		if (this.createTexture)
		{
			this.baseTexture = PIXI.BaseTexture.from(generateAbsoluteURL(this.src));
			this.baseTexture.setResolution(this._scale);
			this.baseTexture.update();
		}
		this.scale = this._scale; // update values

		if (!(this.width && this.height))
		{
			if (!this._detectSize()) return;
			this.frames = Math.max(1, ~~this.frames);
			this.layers = Math.max(1, ~~this.layers);
			this.width = Math.ceil((this.width / this.layers) / this._scale);
			this.height = Math.ceil((this.height / this.frames) / this._scale);
		}
		
		this.ready = !!this._bitmap;
	}

	/** Asset scale. */
	get scale() {
		return this._scale;
	}

	set scale(scale) {
		if (!scale || isNaN(scale)) scale = this._scale;
		this._scale = scale;
	}

	/**
	 * Determines the dimensions of the sprite either as specified, or tries automatically
	 * @ignore
	 */
	_detectSize() {
		if (!this.bitmap) return false;
		try {
			this.width = ~~this.width
				|| ~~this.bitmap.width
				|| ~~this.bitmap.naturalWidth
				|| 0;
			this.height = ~~this.height
				|| ~~this.bitmap.height
				|| ~~this.bitmap.naturalHeight
				|| 0;
		}
		catch (e) {}

		return (!isNaN(this.width) && !isNaN(this.height));
	}
}

export default Asset;