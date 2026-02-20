/**
 * Linear gradient fill descriptor.
 * @class
 */
class LGFillDescriptor
{
	static get REPEAT_TYPE_AREA() { return 'area'}
	static get REPEAT_TYPE_LINES() { return 'lines'}

	constructor(aPointFrom, aPointTo)
	{
		this._pointFrom = null;
		this._pointTo = null;
		this._keyColor_arr = new Array();
		this._repeatType = LGFillDescriptor.REPEAT_TYPE_AREA;

		this._initLGFillDescriptor(aPointFrom, aPointTo);
	}

	/**
	 * Add key color to gradiant fill.
	 * @param {KeyColorDescriptor} aKeyColor_ukc 
	 */
	addKeyColor (aKeyColor_ukc)
	{
		this._keyColor_arr.push(aKeyColor_ukc);
	}

	/**
	 * Get key color descriptor.
	 * @param {number} aIndex_int - Color index.
	 * @returns {KeyColorDescriptor}
	 */
	getKeyColor (aIndex_int)
	{
		return this._keyColor_arr[aIndex_int];
	}

	/**
	 * Total key colors amount.
	 * @type {number}
	 * @readonly
	 */
	get keysCount ()
	{
		return this._keyColor_arr.length;
	}

	/**
	 * Gradient fill start point.
	 * @type {PIXI.Point}
	 * @readonly
	 */
	get pointFrom ()
	{
		return this._pointFrom;
	}

	/**
	 * Gradient fill end point.
	 * @type {PIXI.Point}
	 * @readonly
	 */
	get pointTo ()
	{
		return this._pointTo;
	}

	/** Set gradient repeat type. */
	set repeatType (aRepeatType_str)
	{
		this._repeatType = aRepeatType_str;
	}

	/** Is gradient repeat type REPEAT_TYPE_LINES or not. */
	get isRepeatTypeLines ()
	{
		return this._repeatType === LGFillDescriptor.REPEAT_TYPE_LINES;
	}

	/** Is gradient repeat type REPEAT_TYPE_AREA or not. */
	get isRepeatTypeArea ()
	{
		return this._repeatType === LGFillDescriptor.REPEAT_TYPE_AREA;
	}

	_initLGFillDescriptor(aPointFrom, aPointTo)
	{
		this._pointFrom = aPointFrom;
		this._pointTo = aPointTo;		
	}
}

export default LGFillDescriptor