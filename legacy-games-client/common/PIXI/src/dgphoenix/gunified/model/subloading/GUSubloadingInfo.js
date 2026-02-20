import SimpleUIInfo from '../../../unified/model/uis/SimpleUIInfo';

export const SUBLOADING_ASSETS_TYPES = {};

class GUSubloadingInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this.__fillSubloadingAssetTypes();

		this.loadedTypes = {};
		this.loadingInProgressTypes = {};

		for (let assetsType in SUBLOADING_ASSETS_TYPES)
		{
			this.loadedTypes[assetsType] = false;
			this.loadingInProgressTypes[assetsType] = false;
		}
	}

	i_setLoaded(assetsType)
	{
		this.loadingInProgressTypes[assetsType] = false;
		this.loadedTypes[assetsType] = true;
	}

	i_isLoaded(assetsType)
	{
		return this.loadedTypes[assetsType];
	}

	i_setLoadingInProgress(assetsType)
	{
		this.loadingInProgressTypes[assetsType] = true;
	}

	i_isLoadingInProgress(assetsType)
	{
		return this.loadingInProgressTypes[assetsType];
	}

	__fillSubloadingAssetTypes()
	{
		// override in subclasses
	}
}

export default GUSubloadingInfo;