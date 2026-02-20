import ColorDescriptor from '../../color/ColorDescriptor';

/**
 * Text shadow descriptor.
 * @class
 */
class TextShadowDescriptor
{
	/**
	 * @constructor
	 * @param {ColorDescriptor} aColor_obj - Shadow color.
	 * @param {Number} aOptBlurRadius_num - Shadow blur radius.
	 * @param {Number} aOptDistance_num - Shadow distance from object.
	 * @param {Number} aOptAngle_num - Shadow angle.
	 * @param {Number} aOptAlpha_num - Shadow alpha.
	 */
	constructor (aColor_obj, aOptBlurRadius_num, aOptDistance_num, aOptAngle_num, aOptAlpha_num)
	{
		this._color = null;
		this._blurRadius = undefined;
		this._shadowDistance = undefined;
		this._shadowAngle = false;
		this._shadowAlpha = undefined;

		this._initTextShadowDescriptor(aColor_obj, aOptBlurRadius_num, aOptDistance_num, aOptAngle_num, aOptAlpha_num);
	}

	/** Shadow color
	 * @type {ColorDescriptor}
	 */
	get color ()
	{
		return this._color;
	}

	/** Shadow blur radius. */
	get blurRadius()
	{
		return this._blurRadius;
	}

	/** Shadow distance from object. */
	get shadowDistance()
	{
		return this._shadowDistance;
	}

	/** Shadow angle. */
	get shadowAngle()
	{
		return this._shadowAngle;
	}

	/** Is shadow alpha defined or not. */
	get isShadowAlphaDefined()
	{
		return this._shadowAlpha !== undefined;
	}

	/** Shadow alpha. */
	get shadowAlpha()
	{
		return this._shadowAlpha;
	}

	/** Clone shadow descriptor instance. */
	clone ()
	{
		return new TextShadowDescriptor(this._color.clone(), this._blurRadius, this._shadowDistance, this._shadowAngle, this._shadowAlpha);
	}

	
	_initTextShadowDescriptor(aColor_obj, aOptBlurRadius_num, aOptShadowDistance_num, aOptAngle_num, aOptAlpha_num)
	{
		this._color = (aColor_obj instanceof ColorDescriptor) ? aColor_obj.clone() : new ColorDescriptor(aColor_obj);
		this._blurRadius = aOptBlurRadius_num;
		this._shadowDistance = aOptShadowDistance_num;
		this._shadowAngle = aOptAngle_num;
		this._shadowAlpha = aOptAlpha_num;
	}
}

export default TextShadowDescriptor