import GUSCommonAssetsInfo from './GUSCommonAssetsInfo';
import COMMON_PRELOADER_ASSETS from '../../config/preloader_assets.json';
import COMMON_ASSETS from '../../config/assets.json';
import { APP } from '../../../unified/controller/main/globals';

class GUSLobbyCommonAssetsInfo extends GUSCommonAssetsInfo
{
	constructor()
	{
		super();
	}

	get preloaderAssets()
	{
		return COMMON_PRELOADER_ASSETS;
	}

	get assets()
	{
		return COMMON_ASSETS;
	}

	get commonAssetsFolderPath()
	{
		if (APP.isDebugMode)
		{
			return APP.appParamsInfo.lobbyPath + "assets/_debug/common/";
		}

		return super.commonAssetsFolderPath;
	}
}

export default GUSLobbyCommonAssetsInfo