import { Utils } from '../../../Utils';
import LGFillDescriptor from '../../color/gradient/LGFillDescriptor'
import ColorDescriptor from '../../color/ColorDescriptor'
import * as FEATURES from '../../../../view/layout/features';


let quotedFontNameMode_bl = FEATURES.IE || FEATURES.EDGE;

/**
 * Text font descriptor.
 * @class
 */
class TextFontDescriptor
{
	static get DEFAULT_FONT_NAME () { return 'Arial' }
	static get DEFAULT_FONT_SIZE () { return 12 }
	static get DEFAULT_FONT_STYLE () { return "normal" }

	/**
	 * @constructor
	 * @param {string} aOptFontName_str - Font name.
	 * @param {number} aOptFontSize_num - Font size.
	 * @param {(LGFillDescriptor|ColorDescriptor)} aOptColor_obj - Font color descriptor.
	 * @param {boolean} aOptBold_bl - Use font weight "bold" or not.
	 * @param {StrokeDescriptor} aOptStroke_obj - Font stroke descriptor.
	 * @param {*} aOptFontStyle_str - Font style (not used for now).
	 */
	constructor(aOptFontName_str, aOptFontSize_num, aOptColor_obj, aOptBold_bl, aOptStroke_obj, aOptFontStyle_str)
	{
		this._fontName = undefined;
		this._fontSize = undefined;
		this._color = null;
		this._bold = false;
		this._stroke = null;

		this._initTextFontDescriptor(aOptFontName_str, aOptFontSize_num, aOptColor_obj, aOptBold_bl, aOptStroke_obj, aOptFontStyle_str);
	}

	/** Font name. */
	get fontName ()
	{
		return this._fontName;
	}

	/** Font size. */
	get fontSize ()
	{
		return this._fontSize;
	}

	/** Font color. */
	get color ()
	{
		return this._color;
	}

	/** Font stroke. */
	get stroke ()
	{
		return this._stroke;
	}

	/** Is font weight "bold" used or not. */
	get isBold ()
	{
		return this._bold;
	}

	toCSSString ()
	{
		var lOptionalFontNameQuote_str = quotedFontNameMode_bl ? "'" : "";
		
		return (this._bold ? "bold " : "")
				+ this._fontSize.toFixed(2 /*greater precision seems to be not required*/) + "px "
				+ lOptionalFontNameQuote_str + this._fontName + lOptionalFontNameQuote_str;
	}

	_initTextFontDescriptor(aOptFontName_str, aOptFontSize_num, aOptColor_obj, aOptBold_bl, aOptStroke_obj, aOptFontStyle_str)
	{
		var lFontName_str;
		if (aOptFontName_str === undefined)
		{
			lFontName_str = TextFontDescriptor.DEFAULT_FONT_NAME;
		}
		else if (Utils.isString(aOptFontName_str))
		{
			lFontName_str = aOptFontName_str;
		}
		else
		{
			throw new Error ('Invalid FN type: ' + typeof (aOptFontName_str));
		}
		this._fontName = lFontName_str;

		var lFontSize_num;
		if (aOptFontSize_num === undefined)
		{
			lFontSize_num = TextFontDescriptor.DEFAULT_FONT_SIZE;
		}
		else if (
					Utils.isNumber(aOptFontSize_num)
					&& aOptFontSize_num > 0 //zero font size is not allowed as well
				)
		{
			lFontSize_num = aOptFontSize_num;
		}
		else
		{
			throw new Error ('Invalid size: ' + aOptFontSize_num);
		}
		this._fontSize = lFontSize_num;

		if (aOptColor_obj instanceof LGFillDescriptor)
		{
			this._color = aOptColor_obj;
		}
		else
		{
			this._color = aOptColor_obj === undefined ? new ColorDescriptor(0xFFFFFF) : new ColorDescriptor(aOptColor_obj);
		}

		this._bold = Boolean(aOptBold_bl);

		if (aOptStroke_obj !== undefined)
		{
			this._stroke = aOptStroke_obj;
		}
	}

}

export default TextFontDescriptor