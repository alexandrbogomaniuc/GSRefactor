import { Utils } from '../../Utils';

/**
 * Scale descriptor.
 * 
 * @class
 */
class ScaleDescriptor
{
	/** Scale mode AUTO_SCALE: scale object if it is out of bounds. */
	static get AUTO_SCALE ()		{ return "auto"; }

	/** Scale mode NO_SCALE: don't scale object without depending on object bounds. */
	static get NO_SCALE ()			{ return "no_scale"; }

	/** Scale mode EXACT_FIT_SCALE: scale object to exactly match bounds. */
	static get EXACT_FIT_SCALE ()	{ return "exact_fit"; }

	static get _SUPPORTED_PREDEFINED_SCALES () { return [ScaleDescriptor.AUTO_SCALE, ScaleDescriptor.NO_SCALE, ScaleDescriptor.EXACT_FIT_SCALE] };
	
	/**
	 * @constructor
	 * @param {(string|number)} aOptXScale_obj - X scale type.
	 * @param {(string|number)} aOptYScale_obj - Y scale type.
	 * @param {boolean} aOptPreventDistortion_bl - Is distorion allowed or not.
	 */
	constructor(aOptXScale_obj, aOptYScale_obj, aOptPreventDistortion_bl)
	{
		this._xScale = undefined;
		this._yScale = undefined;
		this._preventDistortion = false;

		this._initScaleDescriptor(aOptXScale_obj, aOptYScale_obj, aOptPreventDistortion_bl);
	}

	/** Is X scale type AUTO_SCALE or not. */
	isXAutoScaleMode ()
	{
		return this._xScale === ScaleDescriptor.AUTO_SCALE;
	}

	/** Is Y scale type AUTO_SCALE or not. */
	isYAutoScaleMode ()
	{
		return this._yScale === ScaleDescriptor.AUTO_SCALE;
	}

	/** Is X scale type NO_SCALE or not. */
	isXNoScaleMode ()
	{
		return this._xScale === ScaleDescriptor.NO_SCALE;
	}

	/** Is Y scale type NO_SCALE or not. */
	isYNoScaleMode ()
	{
		return this._yScale === ScaleDescriptor.NO_SCALE;
	}

	/** Is X scale type EXACT_FIT_SCALE or not. */
	isXExactFitScaleMode ()
	{
		return this._xScale === ScaleDescriptor.EXACT_FIT_SCALE;
	}

	/** Is Y scale type EXACT_FIT_SCALE or not. */
	isYExactFitScaleMode ()
	{
		return this._yScale === ScaleDescriptor.EXACT_FIT_SCALE;
	}

	/** Is scale distortion allowed or not. If allowed - object can be scaled to different values in x and y-axes.*/
	isDistortionAllowed ()
	{
		return !this._preventDistortion;
	}

	/** X scale type */
	getXScale ()
	{
		return this._xScale;
	}

	/** Y scale type */
	getYScale ()
	{
		return this._yScale;
	}
	
	_initScaleDescriptor(aOptXScale_obj, aOptYScale_obj, aOptPreventDistortion_bl)
	{
		if (aOptXScale_obj === undefined)
		{
			//default X scale
			this._xScale = ScaleDescriptor.NO_SCALE;
		}
		else if (
					ScaleDescriptor._SUPPORTED_PREDEFINED_SCALES.indexOf(aOptXScale_obj) >= 0
					|| (Utils.isNumber(aOptXScale_obj) && aOptXScale_obj >= 0)
				)
		{
			this._xScale = aOptXScale_obj;
		}
		else
		{
			throw new Error(`Invalid XS: '${aOptXScale_obj}'; `);
		}

		if (aOptYScale_obj === undefined)
		{
			//default Y scale
			this._yScale = ScaleDescriptor.NO_SCALE;
		}
		else if (
					ScaleDescriptor._SUPPORTED_PREDEFINED_SCALES.indexOf(aOptYScale_obj) >= 0
					|| (Utils.isNumber(aOptYScale_obj) && aOptYScale_obj >= 0)
				)
		{
			this._yScale = aOptYScale_obj;
		}
		else
		{
			throw new Error(`Invalid YS: '${aOptYScale_obj}'; `);
		}
		
		this._preventDistortion = Boolean(aOptPreventDistortion_bl);
	}
}

export default ScaleDescriptor;