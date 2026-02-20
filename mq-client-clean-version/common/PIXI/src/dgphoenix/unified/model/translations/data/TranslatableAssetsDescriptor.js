/**
 * Translatable assets descriptor.
 * @class
 */
class TranslatableAssetsDescriptor
{
	constructor()
	{
		this._fAssetsDescriptors_utad_arr = null;
		this._fAssetsDescriptors_utad_obj = null;

		this._initTranslatableAssetsDescriptor();
	}

	/**
	 * Add asset descriptor.
	 * @param {TranslatableAssetDescriptor} a_utad 
	 */
	addAssetDescriptor (a_utad)
	{
		var lAssetId_str = a_utad.assetId;
		if (this._fAssetsDescriptors_utad_obj[lAssetId_str])
		{
			throw new Error("Cannot add asset descriptor due to one already exists: ASSET ID = " + lAssetId_str);
		}
		this._fAssetsDescriptors_utad_arr.push(a_utad);
		this._fAssetsDescriptors_utad_obj[lAssetId_str] = a_utad;
	}

	/**
	 * Get translatable asset descriptor by index.
	 * @param {number} aIntId_int 
	 * @returns {TranslatableAssetDescriptor} 
	 */
	getAssetDescriptorByIntId (aIntId_int)
	{
		return this._fAssetsDescriptors_utad_arr[aIntId_int];
	}

	/**
	 * Get translatable asset descriptor by asset id.
	 * @param {string} aAssetId_str 
	 * @param {boolean} aDontThrowIfAssetDoesntExist_bl 
	 * @returns {boolean}
	 */
	getAssetDescriptor (aAssetId_str, aDontThrowIfAssetDoesntExist_bl)
	{
		var lAssetDescriptor_utad = this._fAssetsDescriptors_utad_obj[aAssetId_str];
		if (!lAssetDescriptor_utad && !aDontThrowIfAssetDoesntExist_bl)
		{
			throw new Error("Asset descriptor does not exist: ASSET ID = " + aAssetId_str);
		}
		return lAssetDescriptor_utad; //can return undefined value: this is not correct according to reference types concept for which null should be used instead but that is legacy specific that is actually in use by environment so it should be kept for external compatibility
	}

	/**
	 * Registered assets amount.
	 */
	get assetsCount ()
	{
		return this._fAssetsDescriptors_utad_arr.length;
	}

	_initTranslatableAssetsDescriptor()
	{
		this._fAssetsDescriptors_utad_arr = new Array();
		this._fAssetsDescriptors_utad_obj = new Object();
	}
}

export default TranslatableAssetsDescriptor