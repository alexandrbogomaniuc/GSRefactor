/**
 * Translatable asset text descriptor.
 * @class
 */
class TATextDescriptor
{
	static get LINE_SPACING_AUTO () { return "auto" }
	static get LETTER_SPACING_AUTO () { return 0; }
	static get WIDTH_AUTO () { return "auto" }
	static get HEIGHT_AUTO () { return "auto" }
	static get HEIGHT_EXPLICIT () { return "explicit" }
	static get PADDING_DEFAULT () { return 0; }
	static get ALPHA_AUTO () { return 1; }
	
	/**
	 * @constructor
	 * @param {object} aAssetContent_obj 
	 * @param {TextFontDescriptor} aFontDescriptor_utfd 
	 * @param {TextShadowsDescriptor} aOptShadowsDescriptor_utshsd 
	 * @param {string|number} aOptWidth_obj 
	 * @param {string|number} aOptHeight_obj 
	 * @param {boolean} aOptAutoWrapMode_bl 
	 * @param {string|number} aOptLineSpacing_obj 
	 * @param {number} aOptPadding_obj 
	 * @param {number} aOptLetterSpacing_obj 
	 * @param {number} aOptAlpha_obj 
	 */
	constructor(aAssetContent_obj, aFontDescriptor_utfd, aOptShadowsDescriptor_utshsd, aOptWidth_obj, aOptHeight_obj, aOptAutoWrapMode_bl, aOptLineSpacing_obj, aOptPadding_obj, aOptLetterSpacing_obj, aOptAlpha_obj)
	{
		this._content = null;
		this._fontDescriptor = null;
		this._shadowsDescriptor = null;
		this._width = undefined;
		this._height = undefined;
		this._autoWrapMode = false;
		this._lineSpacing = null;
		this._padding = undefined;
		this._letterSpacing = null;
		this._alpha = null;

		this._initTATextDescriptor(aAssetContent_obj, aFontDescriptor_utfd, aOptShadowsDescriptor_utshsd, aOptWidth_obj, aOptHeight_obj, aOptAutoWrapMode_bl, aOptLineSpacing_obj, aOptPadding_obj, aOptLetterSpacing_obj, aOptAlpha_obj);
	}

	/**
	 * Asset text.
	 * @type {string}
	 */
	get text ()
	{
		return this._content.text;
	}
	
	/**
	 * Asset content.
	 * @type {object}
	 */
	get content ()
	{
		return this._content;
	}

	//LETTERSPACING...
	/**
	 * Text letter spacing.
	 * @type {number}
	 */
	get letterSpacing()
	{
		return this._letterSpacing;
	}
	//...LETTERSPACING

	//LINESPACING...
	/**
	 * Text line spacing.
	 * @type {number|string}
	 */
	get lineSpacing ()
	{
		return this._lineSpacing;
	}

	/**
	 * Is line spacing type auto.
	 */
	get isAutoLineSpacing ()
	{
		return this._lineSpacing === TATextDescriptor.LINE_SPACING_AUTO;
	}
	//...LINESPACING

	//ALPHA...
	get alpha ()
	{
		return this._alpha;
	}
	//...ALPHA

	//PADDING...
	/**
	 * Padding.
	 * @type {number}
	 */
	get padding ()
	{
		return this._padding;
	}
	//...PADDING

	/**
	 * Text font descriptor.
	 * @type {TextFontDescriptor}
	 */
	get fontDescriptor ()
	{
		return this._fontDescriptor;
	}

	/**
	 * Text shadows descriptor.
	 * @type {TextShadowsDescriptor}
	 */
	get shadowsDescriptor ()
	{
		return this._shadowsDescriptor;
	}

	//TEXT WIDTH...
	/**
	 * Text width.
	 * @type {string|number}
	 */
	get width ()
	{
		return this._width;
	}

	/**
	 * Is text auto width mode.
	 * @type {boolean}
	 */
	get isAutoWidthMode ()
	{
		return this._width === TATextDescriptor.WIDTH_AUTO;
	}
	//...TEXT WIDTH

	//TEXT HEIGHT...
	/**
	 * Text height.
	 * @type {string|number}
	 */
	get height ()
	{
		return this._height;
	}

	/**
	 * Is text auto height mode.
	 * @type {boolean}
	 */
	get isAutoHeightMode ()
	{
		return this._height === TATextDescriptor.HEIGHT_AUTO;
	}

	/**
	 * Is text explicit height mode.
	 * @type {boolean}
	 */
	get isExplicitHeightMode ()
	{
		return this._height === TATextDescriptor.HEIGHT_EXPLICIT;
	}
	//...TEXT HEIGHT

	/**
	 * Is text auto wrap.
	 * @type {boolean}
	 */
	get isAutoWrapMode ()
	{
		return this._autoWrapMode;
	}
	
	/**
	 * Override text descriptor params.
	 * @param {TATextDescriptor} aOverride_utatd Descriptor with new parameters.
	 */
	merge (aOverride_utatd)
	{
		this._merge(aOverride_utatd);
	}

	_initTATextDescriptor(aAssetContent_obj, aFontDescriptor_utfd, aOptShadowsDescriptor_utshsd, aOptWidth_obj, aOptHeight_obj, aOptAutoWrapMode_bl, aOptLineSpacing_obj, aOptPadding_obj, aOptLetterSpacing_obj, aOptAlpha_obj)
	{
		this._content = aAssetContent_obj;
		this._fontDescriptor = aFontDescriptor_utfd;
		this._shadowsDescriptor = aOptShadowsDescriptor_utshsd;
		this._width = aOptWidth_obj ? aOptWidth_obj : TATextDescriptor.WIDTH_AUTO;
		this._height = aOptHeight_obj ? aOptHeight_obj : TATextDescriptor.HEIGHT_AUTO;
		this._autoWrapMode = Boolean(aOptAutoWrapMode_bl);
		this._lineSpacing = aOptLineSpacing_obj === undefined ? TATextDescriptor.LINE_SPACING_AUTO : aOptLineSpacing_obj;
		this._padding = aOptPadding_obj === undefined ? TATextDescriptor.PADDING_DEFAULT : aOptPadding_obj;
		this._letterSpacing = aOptLetterSpacing_obj === undefined ? TATextDescriptor.LETTER_SPACING_AUTO : aOptLetterSpacing_obj;
		this._alpha = aOptAlpha_obj === undefined ? TATextDescriptor.ALPHA_AUTO : aOptAlpha_obj;
	}

	_merge (aOverride_utatd)
	{
		if (aOverride_utatd.content.override)
		{
			this._content = aOverride_utatd.content;
		}

		this._fontDescriptor = aOverride_utatd.fontDescriptor;
		this._shadowsDescriptor = aOverride_utatd.shadowsDescriptor;
		this._width = aOverride_utatd.width;
		this._height = aOverride_utatd.height;
		this._autoWrapMode = aOverride_utatd.isAutoWrapMode;
		this._lineSpacing = aOverride_utatd.lineSpacing;
		this._letterSpacing = aOverride_utatd.letterSpacing;
	}
}

export default TATextDescriptor