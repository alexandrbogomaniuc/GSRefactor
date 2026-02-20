import GUSubloadingInfo, { SUBLOADING_ASSETS_TYPES } from './GUSubloadingInfo';

class GUSLobbySubloadingInfo extends GUSubloadingInfo
{
	constructor()
	{
		super();
	}

	__fillSubloadingAssetTypes()
	{
		SUBLOADING_ASSETS_TYPES.profile = "profile";
		SUBLOADING_ASSETS_TYPES.paytable = "paytable";
	}
}

export default GUSubloadingInfo;