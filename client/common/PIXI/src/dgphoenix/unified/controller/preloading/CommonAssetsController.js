import SimpleController from '../base/SimpleController';
import CommonAssetsInfo from '../../model/preloading/CommonAssetsInfo';
import { createLoader } from '../interaction/resources/loaders';
import { APP } from '../main/globals';

class CommonAssetsController extends SimpleController
{
	static get EVENT_ON_READY() 		{return "EVENT_ON_READY";}

	prepareForAssetsLoading()
	{
		this._prepareForAssetsLoading();
	}

	constructor(aOptInfo_si)
	{
		super(aOptInfo_si || new CommonAssetsInfo());
	}

	_prepareForAssetsLoading()
	{
		this._loadCommonAssetsVersionDescriptor();
	}

	_loadCommonAssetsVersionDescriptor()
	{
		var ap = APP;
		let lVersionUrl_str = APP.contentPathURLsProvider.commonRootResourcesPath + "/version.json?version=" + this.info.commonAssetsVersion;
		
		let lLoader = createLoader(lVersionUrl_str);
		lLoader.once('complete', this._onVersionLoadingCompleted, this);
		lLoader.once('error', this._onVersionLoadingError, this);

		lLoader.load();
	}

	_onVersionLoadingCompleted(event)
	{
		try
		{
			let l_obj = event.target.data;
			let lVersion_str = l_obj.version;

			this.info.updateVersion(lVersion_str);

			this.emit(CommonAssetsController.EVENT_ON_READY);
		}
		catch(err)
		{
			throw new Error("Common assets version descriptor parsing error");
		}
	}

	_onVersionLoadingError(event)
	{
		throw new Error("Common assets version descriptor loading error");
	}

}

export default CommonAssetsController