import {Utils} from '../../Utils'

/**
 * Translation font descriptor.
 * @class
 */
class TranslationFontDescriptor
{
	/**
	 * @constructor
	 * @param {string} aFontName_str - Font name.
	 * @param {string} aFontURL_str - Font url.
	 */
	constructor(aFontName_str, aFontURL_str)
	{
		this._fontName = null;
		this._fontURL = null;

		this._initTranslationFontDescriptor(aFontName_str, aFontURL_str);
	}

	/**
	 * Font name.
	 * @type {string}
	 */
	get fontName ()
	{
		return this._fontName;
	}

	/**
	 * Font url.
	 * @type {string}
	 */
	get url ()
	{
		return this._fontURL;
	}

	/**
	 * Update font url.
	 * @param {string} a_str - Font url.
	 */
	updateURL (a_str)
	{
		if (!(Utils.isString(a_str)))
		{
			throw new Error('Invalid argument value: ' + a_str);
		}

		this._fontURL = a_str;
	}

	_initTranslationFontDescriptor(aFontName_str, aFontURL_str)
	{
		this._fontName = aFontName_str;
		this._fontURL = aFontURL_str;
	}
}

export default TranslationFontDescriptor