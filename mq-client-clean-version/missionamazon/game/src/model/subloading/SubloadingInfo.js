import SimpleUIInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class SubloadingInfo extends SimpleUIInfo {
	constructor() {
		super();

		this._fMapsLoaded_obj = {};
		this._fMapsLoading_obj = {};
		this._fLoadingInProgress_bl = false;
	}

	i_isMapLoaded(aMapId_int)
	{
		if (this._fMapsLoaded_obj[aMapId_int])
		{
			return true;
		}
		return false;
	}

	i_setMapLoaded(aMapId_int)
	{
		this._fMapsLoading_obj[aMapId_int] = false;
		this._fMapsLoaded_obj[aMapId_int] = true;
	}

	i_isMapLoading(aMapId_int)
	{
		if (this._fMapsLoading_obj[aMapId_int])
		{
			return true;
		}
		return false;
	}

	i_setMapLoading(aMapId_int)
	{
		this._fMapsLoading_obj[aMapId_int] = true;
	}

	get loadingInProgress()
	{
		return this._fLoadingInProgress_bl;
	}

	set loadingInProgress(aValue_bl)
	{
		this._fLoadingInProgress_bl = aValue_bl;
	}

	destroy()
	{
		this._fMapsLoaded_obj = null;
		this._fMapsLoading_obj = null;

		super.destroy();
	}
}

export default SubloadingInfo;