import SimpleInfo from '../base/SimpleInfo';
import { APP } from '../../controller/main/globals';

export const COMMON_ASSET_TYPES = {
	DIALOGS: "DIALOGS"
}

/**
 * Common assets info.
 * @class
 * @extends SimpleInfo
 */
class CommonAssetsInfo extends SimpleInfo
{
	constructor(aOptId_obj, aOptParentInfo_usi)
	{
		super(aOptId_obj, aOptParentInfo_usi);

		this._fCommonAssetsVersion_str = Date.now() + "" + Math.random();
	}

	/**
	 * Version of assets in "common" folder.
	 * @type {string}
	 */
	get commonAssetsVersion()
	{
		return this._fCommonAssetsVersion_str;
	}

	/**
	 * Set common assets version.
	 * @param {string} aVersion_str 
	 */
	updateVersion(aVersion_str)
	{
		this._fCommonAssetsVersion_str = aVersion_str;
	}

	/**
	 * Path "common" folder.
	 */
	get commonAssetsFolderPath()
	{
		return APP.appParamsInfo.commonPathForActionGames;
	}

	/**
	 * Generate absolute URL for asset.
	 * @param {string} aCommonAssetPath_str 
	 * @returns {string}
	 */
	generateAssetAbsoluteURL(aCommonAssetPath_str)
	{
		let lCommonAssetPath_str = aCommonAssetPath_str;
		if (lCommonAssetPath_str.indexOf(this.commonAssetsFolderPath) < 0)
		{
			lCommonAssetPath_str = this.commonAssetsFolderPath + lCommonAssetPath_str;
		}

		if (lCommonAssetPath_str.indexOf(this.commonAssetsVersion) < 0)
		{
			lCommonAssetPath_str = lCommonAssetPath_str += "?version=" + this.commonAssetsVersion;
		}

		return lCommonAssetPath_str;
	}
}

export default CommonAssetsInfo