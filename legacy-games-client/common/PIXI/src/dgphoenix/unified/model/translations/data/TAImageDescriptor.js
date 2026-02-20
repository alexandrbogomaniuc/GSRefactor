/**
 * Translatable asset image descriptor.
 * @class
 */
class TAImageDescriptor
{
	/**
	 * @constructor
	 * @param {string} aImgURL_str Image url.
	 */
	constructor(aImgURL_str)
	{
		this._imageUrl = undefined;

		this._initTAImageDescriptor(aImgURL_str);
	}

	/**
	 * Image url.
	 * @type {string}
	 */
	get url ()
	{
		return this._imageUrl;
	}

	/**
	 * Update image url.
	 * @param {string} aImgURL_str Image url.
	 */
	updateURL (aImgURL_str)
	{
		this._imageUrl = aImgURL_str;
	}

	_initTAImageDescriptor(aImgURL_str)
	{
		this._imageUrl = aImgURL_str;
	}
}

export default TAImageDescriptor