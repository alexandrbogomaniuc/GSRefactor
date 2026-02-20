/**
 * Translation fonts descriptor.
 * @class
 */
class TranslationFontsDescriptor
{
	constructor()
	{
		this._fTranslationFontsDescriptors_utfd_arr = null;
		this._fTranslationFontsDescriptors_utfd_obj = null;

		this._initTranslationFontsDescriptor();
	}

	/**
	 * Add translation font descriptor.
	 * @param {TranslationFontDescriptor} a_utfd - Translation font descriptor.
	 */
	addFontDescriptor (a_utfd)
	{
		var lFontName_str = a_utfd.fontName;
		if (this._fTranslationFontsDescriptors_utfd_obj[lFontName_str])
		{
			throw new Error("Font name duplication: " + lFontName_str);
		}
		this._fTranslationFontsDescriptors_utfd_arr.push(a_utfd);
		this._fTranslationFontsDescriptors_utfd_obj[lFontName_str] = a_utfd;
	}

	/**
	 * Get translation font descriptor by index.
	 * @param {number} aIntId_int 
	 * @returns {TranslationFontDescriptor}
	 */
	getFontDescriptorByIntId (aIntId_int)
	{
		return this._fTranslationFontsDescriptors_utfd_arr[aIntId_int];
	}

	/**
	 * Translation font descriptors anount.
	 * @type {number}
	 */
	get fontsCount ()
	{
		return this._fTranslationFontsDescriptors_utfd_arr.length;
	}

	_initTranslationFontsDescriptor()
	{
		this._fTranslationFontsDescriptors_utfd_arr = [];
		this._fTranslationFontsDescriptors_utfd_obj = {};
	}
}

export default TranslationFontsDescriptor