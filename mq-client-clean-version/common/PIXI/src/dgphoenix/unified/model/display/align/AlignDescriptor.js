import SimpleXMLParser from '../../xml/SimpleXMLParser';
import { Utils } from '../../Utils';

/**
 * Align descriptor.
 * @class
 */
class AlignDescriptor
{
	static get LEFT ()			{ return "left"; }
	static get CENTER ()		{ return "center"; }
	static get RIGHT ()			{ return "right"; }
	static get TOP ()			{ return "top"; }
	static get MIDDLE ()		{ return "middle"; }
	static get BOTTOM ()		{ return "bottom"; }
	static get ALPHABETIC ()	{ return "alphabetic"; } //for text only

	static get _SUPPORTED_PREDEFINED_H_ALIGNS () { return [AlignDescriptor.LEFT, AlignDescriptor.CENTER, AlignDescriptor.RIGHT] };
	static get _SUPPORTED_PREDEFINED_V_ALIGNS () { return [AlignDescriptor.TOP, AlignDescriptor.MIDDLE, AlignDescriptor.BOTTOM, AlignDescriptor.ALPHABETIC] };

	constructor(aOptHAlign_obj, aOptVAlign_obj)
	{
		/**
		 * Vertical align type.
		 * @private
		 */
		this._hAlign = null;

		/**
		 * Hprizontal align type.
		 * @private
		 */
		this._vAlign = null;

		this._initAlignDescriptor(aOptHAlign_obj, aOptVAlign_obj);
	}

	/**
	 * Used to check if horizontal align is LEFT.
	 * @returns {boolean}
	 */
	isLeftAlign ()
	{
		return this._hAlign === AlignDescriptor.LEFT;
	}

	/**
	 * Used to check if horizontal align is RIGHT.
	 * @returns {boolean}
	 */
	isRightAlign ()
	{
		return this._hAlign === AlignDescriptor.RIGHT;
	}

	/**
	 * Used to check if horizontal align is CENTER.
	 * @returns {boolean}
	 */
	isCenterAlign ()
	{
		return this._hAlign === AlignDescriptor.CENTER;
	}

	/**
	 * Used to check if vertical align is TOP.
	 * @returns {boolean}
	 */
	isTopAlign ()
	{
		return this._vAlign === AlignDescriptor.TOP;
	}

	/**
	 * Used to check if vertical align is BOTTOM.
	 * @returns {boolean}
	 */
	isBottomAlign ()
	{
		return this._vAlign === AlignDescriptor.BOTTOM;
	}

	/**
	 * Used to check if vertical align is MIDDLE.
	 * @returns {boolean}
	 */
	isMiddleAlign ()
	{
		return this._vAlign === AlignDescriptor.MIDDLE;
	}

	/**
	 * Used to check if vertical align is ALPHABETIC.
	 * @returns {boolean}
	 */
	isAlphabeticAlign ()
	{
		return this._vAlign === AlignDescriptor.ALPHABETIC;
	}

	/**
	 * Current vertical align
	 * @type {string}
	 * @readonly
	 */
	get vAlign ()
	{
		return this._vAlign;
	}

	/**
	 * Current horizontal align
	 * @type {string}
	 * @readonly
	 */
	get hAlign ()
	{
		return this._hAlign;
	}

	_initAlignDescriptor(aOptHAlign_obj, aOptVAlign_obj)
	{
		var lHAlign_obj;
		if (aOptHAlign_obj === undefined)
		{
			//default H align
			lHAlign_obj = AlignDescriptor.CENTER;
		}
		else if (
					AlignDescriptor._SUPPORTED_PREDEFINED_H_ALIGNS.indexOf(aOptHAlign_obj) >= 0
					|| Utils.isNumber(aOptHAlign_obj)
				)
		{
			lHAlign_obj = aOptHAlign_obj;
		}
		else
		{
			throw new Error(`Invalid HA: '${aOptHAlign_obj}'; `);
		}
		this._hAlign = lHAlign_obj;


		var lVAlign_obj;
		if (aOptVAlign_obj === undefined)
		{
			//default V align
			lVAlign_obj = AlignDescriptor.MIDDLE;
		}
		else if (
					AlignDescriptor._SUPPORTED_PREDEFINED_V_ALIGNS.indexOf(aOptVAlign_obj) >= 0
					|| Utils.isNumber(aOptVAlign_obj)
				)
		{
			lVAlign_obj = aOptVAlign_obj;
		}
		else
		{
			throw new Error(`Invalid VA: '${aOptVAlign_obj}'; `);
		}
		this._vAlign = lVAlign_obj;
	}
}

export default AlignDescriptor;