/**
 * Key color descriptor for gradient fill.
 * @class
 */
class KeyColorDescriptor
{
	/**
	 * @constructor
	 * @param {number} aPosition_num - Position of color in gradient fill.
	 * @param {ColorDescriptor} aColor_uc - Color descriptor.
	 */
	constructor(aPosition_num, aColor_uc)
	{
		this._color = null;
		this._position = undefined;

		this._initKeyColorDescriptor(aPosition_num, aColor_uc);
	}

	/**
	 * Color position.
	 * @type {number}
	 */
	get position ()
	{
		return this._position;
	}

	/**
	 * Color descriptor.
	 * @type {ColorDescriptor}
	 */
	get color ()
	{
		return this._color;
	}

	_initKeyColorDescriptor(aPosition_num, aColor_uc)
	{
		this._color = aColor_uc;
		this._position = aPosition_num;
	}
}

export default KeyColorDescriptor