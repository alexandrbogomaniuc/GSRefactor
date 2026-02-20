import SimpleInfo from '../base/SimpleInfo';

/**
 * @class
 * @classdesc Base info class for SimpleUIController.
 */
class SimpleUIInfo extends SimpleInfo
{
	//IL INTERFACE...
	/**
	 * Is corresponding UI visible or not.
	 * @type {boolean}
	 */
	get visible()
	{
		return this._fIsVisible_bl;
	}

	/**
	 * Sets visibility state for corresponding UI.
	 * @param {boolean} aValue_bl
	 */
	set visible(aValue_bl)
	{
		this._fIsVisible_bl = !!aValue_bl;
	}

	/** Destroy info instance. */
	destroy()
	{
		super.destroy();
	}
	//...IL INTERFACE

	//IL CONSTRUCTION...
	constructor(aOptId_obj, aOptParentInfo_usi)
	{
		super(aOptId_obj, aOptParentInfo_usi);

		//IL IMPLEMENTATION...
		this._fIsVisible_bl = true;
	}
	//...IL CONSTRUCTION

	//ILI INIT...
	//...ILI INIT
}

export default SimpleUIInfo;