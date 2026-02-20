import SimpleUIInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import { SUBLOADING_ASSETS_TYPES } from '../../config/Constants';

class SubloadingInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

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
}

export default SubloadingInfo;