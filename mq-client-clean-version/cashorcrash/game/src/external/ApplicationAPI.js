import GUExternalAPI from '../../../../common/PIXI/src/dgphoenix/gunified/controller/main/GUExternalAPI';
import LoaderUI from './ApplicationLoaderUI';

class ApplicationAPI extends GUExternalAPI
{
	constructor(config)
	{
		super(config);
	}

	get LoaderUI() {
		return LoaderUI;
	}
}

export default ApplicationAPI;