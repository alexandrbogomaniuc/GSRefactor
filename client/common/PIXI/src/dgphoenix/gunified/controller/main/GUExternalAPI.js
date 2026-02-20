import ExternalAPI from '../../../unified/controller/main/ExternalAPI';
import StatusBarUI from '../../view/layout/StatusBarUI';

class GUExternalAPI extends ExternalAPI
{
	constructor(config)
	{
		super(config);
	}

	get StatusBarUI()
	{
		return StatusBarUI;
	}
}

export default GUExternalAPI;