import GUSCommonAssetsInfo from './GUSCommonAssetsInfo';
import COMMON_ROOM_PRELOADER_ASSETS from '../../config/s_room_preloader_assets.json';
import { APP } from '../../../unified/controller/main/globals';

class GUSGameCommonAssetsInfo extends GUSCommonAssetsInfo
{
	constructor()
	{
		super();
	}

	get roomPreloaderAssets()
	{
		return COMMON_ROOM_PRELOADER_ASSETS;
	}

	get commonAssetsFolderPath()
	{
		if (APP.isDebugMode)
		{
			return 'http://localhost:8081/' + "assets/_debug/common/";
		}

		return super.commonAssetsFolderPath;
	}

}

export default GUSGameCommonAssetsInfo