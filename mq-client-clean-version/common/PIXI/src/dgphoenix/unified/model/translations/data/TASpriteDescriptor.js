/**
 * Translatable asset sprite descriptor.
 * @class
 */
class TASpriteDescriptor
{
	/**
	 * @constructor
	 * @param {string} aAtlasConfigUrl_str Atlas configuration url.
	 * @param {string[]} aImgesURLs_str_arr Images urls.
	 * @param {string} aFramesPath_str Frames path.
	 * @param {number} aFramerate_num Framerate.
	 */
	constructor(aAtlasConfigUrl_str, aImgesURLs_str_arr, aFramesPath_str, aFramerate_num)
	{
		this._imagesUrls = [];
		this._altasConfigUrl = undefined;
		this._framesPath = "";
		this._atlasConfigs = null;
		this._framerate = undefined;

		this._initTASpriteDescriptor(aAtlasConfigUrl_str, aImgesURLs_str_arr, aFramesPath_str, aFramerate_num);
	}

	/**
	 * Images urls.
	 * @type {string[]}
	 */
	get imagesUrls ()
	{
		return this._imagesUrls;
	}

	/**
	 * Atlas configuration url.
	 * @type {string}
	 */
	get altasConfigUrl ()
	{
		return this._altasConfigUrl;
	}

	/**
	 * Frames path.
	 * @type {string}
	 */
	get framesPath ()
	{
		return this._framesPath;
	}

	/**
	 * Atlas configs.
	 * @type {JSON}
	 */
	get atlasConfigs()
	{
		return this._atlasConfigs;
	}

	/**
	 * Set atlas configs.
	 * @param {JSON} configs
	 */
	set atlasConfigs(configs)
	{
		this._atlasConfigs = configs;
	}

	/**
	 * Framerate.
	 * @type {number}
	 * @default 24
	 */
	get framerate ()
	{
		return this._framerate || 24;
	}

	/**
	 * Update atlas configuration url.
	 * @param {string} aUrl_str 
	 */
	updateAtlasConfigUrl(aUrl_str)
	{
		this._altasConfigUrl = aUrl_str;
	}

	/**
	 * Update image url.
	 * @param {string} aImgURL_str Image url.
	 * @param {number} aImageIndex_int Image index.
	 */
	updateImageURL (aImgURL_str, aImageIndex_int)
	{
		this._imagesUrls[aImageIndex_int] = aImgURL_str;
	}

	_initTASpriteDescriptor(aAtlasConfigUrl_str, aImgesURLs_str_arr, aFramesPath_str, aFramerate_num)
	{
		this._altasConfigUrl = aAtlasConfigUrl_str;
		this._imagesUrls = aImgesURLs_str_arr;
		this._framesPath = aFramesPath_str;
		this._framerate = aFramerate_num;
	}
}

export default TASpriteDescriptor