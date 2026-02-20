/**
 * @class
 * Base mvc model class.
 */
class SimpleInfo
{
	//IL CONSTRUCTION...
	constructor(aOptId_obj, aOptParentInfo_usi)
	{
		//IL IMPLEMENTATION...
		/** 
		 * Instance unique id. 
		 * @private
		 * */
		this._fId_obj = null;

		/**
		 * Link to parent info instance.
		 * @private
		 * */
		this._fParentInfo_usi = null;

		this._initUSimpleInfo(aOptId_obj, aOptParentInfo_usi);
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	i_getId()
	{
		return this._fId_obj;
	}

	i_getParentInfo()
	{
		return this._fParentInfo_usi;
	}

	destroy()
	{
		this._fId_obj = null;
		this._fParentInfo_usi = null;
	}
	//...IL INTERFACE

	//ILI INIT...
	_initUSimpleInfo(aOptId_obj, aOptParentInfo_usi)
	{
		this._fId_obj = aOptId_obj;
		this._fParentInfo_usi = aOptParentInfo_usi;
	}
	//...ILI INIT
}

export default SimpleInfo;