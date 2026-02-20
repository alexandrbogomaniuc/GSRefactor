import { Utils } from '../../Utils';

/**
 * Color descriptor.
 * @class
 */
class ColorDescriptor
{
	/**
	 * @constructor
	 * @param {(ColorDescriptor|number|{r: number, g: number, c: number})} aColor_obj
	 * @param {number} aOptAlpha_num
	 * @param {boolean} aOptNormilizedAlphaMode_bl 
	 */
	constructor(aColor_obj, aOptAlpha_num, aOptNormilizedAlphaMode_bl)
	{
		this._rgbColor = 0;
		this._normilizedAlpha = undefined;

		this._initColorDescriptor(aColor_obj, aOptAlpha_num, aOptNormilizedAlphaMode_bl);
	}

	/** Set color */
	setColor (aColor_obj, aOptAlpha_num, aOptNormilizedAlphaMode_bl)
	{
		this._setColor(aColor_obj, aOptAlpha_num, aOptNormilizedAlphaMode_bl);
	}

	/** Alpha value from 0 to 1. */
	get normalizedAlpha ()
	{
		return this._normilizedAlpha;
	}

	/** Intensity of red (0 to 255) */
	getR ()
	{
		return this._rgbColor >> 16;
	}

	/** Intensity of green (0 to 255) */
	getG ()
	{
		return (this._rgbColor & 0xFF00) >> 8;
	}

	/** Intensity of blue (0 to 255) */
	getB ()
	{
		return this._rgbColor & 0xFF;
	}
	
	/**
	 * RGB color value (0 to 0xffffff).
	 * @returns {number}
	 */
	getRGB ()
	{
		return this._rgbColor;
	}

	/**
	 * Checks if RGB color is valid.
	 */
	get isValid ()
	{
		return this._rgbColor >= 0 && this._rgbColor <= 0xFFFFFF;
	}

	/**
	 * Conterts color to CSS fromat.
	 * @param {*} aPreventAlpha_bl 
	 * @returns {string}
	 */
	toCSSString (aPreventAlpha_bl)
	{
		return this._toCSSString(aPreventAlpha_bl);
	}

	/**
	 * Clone instance.
	 * @returns {ColorDescriptor}
	 */
	clone ()
	{
		return new ColorDescriptor(this._rgbColor, this._normilizedAlpha, true);
	}

	/**
	 * Checks if colors are equal.
	 * @param {ColorDescriptor} aColor_uc 
	 * @returns {boolean}
	 */
	isEqual (aColor_uc)
	{
		if (!(aColor_uc instanceof ColorDescriptor))
		{
			throw new Error (`Invalid color arg: ${aColor_uc}`);
		}
		
		return this._rgbColor === aColor_uc._rgbColor
				&& this._normilizedAlpha === aColor_uc._normilizedAlpha;
	}

	_initColorDescriptor(aColor_obj, aOptAlpha_num, aOptNormilizedAlphaMode_bl)
	{
		this._setColor(aColor_obj, aOptAlpha_num, aOptNormilizedAlphaMode_bl);
	}

	_setColor(aColor_obj, aOptAlpha_num, aOptNormilizedAlphaMode_bl)
	{
		var lColorArgAlphaMode_bl = false;
		if (aColor_obj instanceof ColorDescriptor)
		{
			this._rgbColor = aColor_obj._rgbColor;
			this._normilizedAlpha = aColor_obj._normilizedAlpha; //note: the alpha setting may be overrided by optional params specification
			lColorArgAlphaMode_bl = true;
		}
		else if (Utils.isNumber(aColor_obj) && aColor_obj >= 0 && aColor_obj <= 0xFFFFFF)
		{
			this._rgbColor = Math.round(aColor_obj);
		}
		else if (aColor_obj
					&& aColor_obj.r >= 0 && aColor_obj.r <= 255
					&& aColor_obj.g >= 0 && aColor_obj.g <= 255
					&& aColor_obj.b >= 0 && aColor_obj.b <= 255)
		{
			this._rgbColor = (aColor_obj.r << 16) | (aColor_obj.g << 8) | aColor_obj.b;
		}
		else if (aColor_obj !== undefined)
		{
			throw new Error(`Unsupported CArg: ${aColor_obj}`);
		}

		if (aOptAlpha_num !== undefined)
		{
			if (
					Utils.isNumber(aOptAlpha_num)
					&& aOptAlpha_num >= 0
					&&
					(
						(aOptAlpha_num <= 1 && aOptNormilizedAlphaMode_bl)
						|| (aOptAlpha_num <= 255 && !aOptNormilizedAlphaMode_bl)
					)
				)
			{
				this._normilizedAlpha = aOptNormilizedAlphaMode_bl ? aOptAlpha_num : aOptAlpha_num / 255;
			}
			else
			{
				throw new Error(`Unsupported AArg(normalized?): ${aOptAlpha_num}`);
			}
		}
		else if (!lColorArgAlphaMode_bl)
		{
			//if optional alpha arg value has not been specified and the value has not been received from the ColorDescriptor argument it should be resetted
			this._normilizedAlpha = undefined;
		}
	}

	_toCSSString (aPreventAlpha_bl)
	{
		if (!this.isValid)
		{
			return "";
		}
		var lCSSColor_str;
		var lNormilizedAlpha_num = this._normilizedAlpha;
		var lAlphaMode_bl = (lNormilizedAlpha_num !== undefined)
							&& !aPreventAlpha_bl;
		if (lAlphaMode_bl)
		{
			lCSSColor_str = "rgba(#r, #g, #b, #a)";
		}
		else
		{
			lCSSColor_str = "rgb(#r, #g, #b)"
		}

		var lRGBColor_int = this._rgbColor;
		var lR_int = this.getR();
		var lG_int = this.getG();
		var lB_int = this.getB();
		lCSSColor_str = lCSSColor_str.replace("#r", lR_int);
		lCSSColor_str = lCSSColor_str.replace("#g", lG_int);
		lCSSColor_str = lCSSColor_str.replace("#b", lB_int);
		if (lAlphaMode_bl)
		{
			lCSSColor_str = lCSSColor_str.replace("#a", lNormilizedAlpha_num.toFixed(3));
		}
		return lCSSColor_str;
	}

}

export default ColorDescriptor