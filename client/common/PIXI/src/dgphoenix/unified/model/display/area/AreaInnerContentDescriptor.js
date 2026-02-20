/**
 * Area content descriptor.
 * Describes a way of content placement inside area (bounds rectangle, scale, align).
 * @class
 */
class AreaInnerContentDescriptor
{
	constructor(aAreaDescriptor_ur, aContentAlignDescriptor_uad, aContentScaleDescriptor_usd)
	{
		this._areaDescriptor = null;
		this._contentAlignDescriptor = null;
		this._contentScaleDescriptor = null;

		this._initAreaInnerContentDescriptor(aAreaDescriptor_ur, aContentAlignDescriptor_uad, aContentScaleDescriptor_usd);
	}

	/**
	 * Area bounds descriptor (rectangle).
	 * @type {PIXI.Rectangle}
	 */
	get areaDescriptor()
	{
		return this._areaDescriptor;
	}

	/**
	 * Area scale descriptor.
	 * @type {ScaleDescriptor}
	 */
	get contentAlignDescriptor()
	{
		return this._contentAlignDescriptor;
	}

	/**
	 * Area scale descriptor.
	 * @type {ScaleDescriptor}
	 */
	get contentScaleDescriptor()
	{
		return this._contentScaleDescriptor;
	}

	_initAreaInnerContentDescriptor(aAreaDescriptor_ur, aContentAlignDescriptor_uad, aContentScaleDescriptor_usd)
	{
		this._areaDescriptor = aAreaDescriptor_ur;
		this._contentAlignDescriptor = aContentAlignDescriptor_uad;
		this._contentScaleDescriptor = aContentScaleDescriptor_usd;
	}
}

export default AreaInnerContentDescriptor;