import SimpleInfo from '../base/SimpleInfo';
/**
 * @typedef {Object} BrandInfo
 * @property {boolean} enable - Should betsoft brand be displayed or not.
 * @property {number} priority - The priority of customer's brand setting.
 */

/**
 * Customer specification info.
 * @class
 * @extends SimpleInfo
 */
class CustomerspecInfo extends SimpleInfo
{
	static get TAG_FPS_INDICATOR()		{return "FPS_INDICATOR";}
	static get TAG_CURRENCY()			{return "CURRENCY";}
	static get TAG_AUTOPLAY()			{return "AUTOPLAY";}
	static get TAG_DOUBLEUP()			{return "DOUBLEUP";}
	static get TAG_BALANCE_FIELD()		{return "BALANCE_FIELD";}
	static get TAG_BRAND()				{return "BRAND";}
	static get TAG_CINEMATIC()			{return "CINEMATIC";}
	static get TAG_QUICKSPIN()			{return "QUICKSPIN";}

	static get ATTRIBUTE_ENABLE()		{return "ENABLE";}
	static get ATTRIBUTE_ENABLED()		{return "ENABLED";}
	static get ATTRIBUTE_PRIORITY()		{return "PRIORITY";}
	static get ATTRIBUTE_USE()			{return "USE";}

	constructor()
	{
		super();

		this._fBrand_obj = null;
	}

	set brand(aVal_obj)
	{
		this._fBrand_obj = {enable: aVal_obj.enable, priority: aVal_obj.priority};
	}

	/**
	 * Brand property.
	 * @type {BrandInfo}
	 */
	get brand()
	{
		return this._fBrand_obj;
	}

	destroy()
	{
		this._fBrandEnable_bln = null;

		super.destroy();
	}
}

export default CustomerspecInfo;