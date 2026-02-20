import ColorDescriptor from './ColorDescriptor';

/**
 * Stroke descriptor.
 * @class
 */
class StrokeDescriptor
{
	/**
	 * @constructor
	 * @param {ColorDescriptor} colorDescriptor - Stroke color.
	 * @param {number} tickness - Stroke tickness.
	 */
	constructor(colorDescriptor, tickness)
	{
		this._colorDescriptor = null;
		this._tickness = undefined;

		this._initStrokeDescriptor(colorDescriptor, tickness);
	}

	/**
	 * Stroke color.
	 * @type {ColorDescriptor}
	 */
	get color ()
	{
		return this._colorDescriptor;
	}

	/** 
	 * Stroke tickness.
	 * @type {number}
	 */
	get tickness ()
	{
		return this._tickness;
	}

	_initStrokeDescriptor(colorDescriptor, tickness)
	{
		this._colorDescriptor = colorDescriptor || new ColorDescriptor(0xFFFFFF);
		this._tickness = tickness || 0;
	}
}

export default StrokeDescriptor