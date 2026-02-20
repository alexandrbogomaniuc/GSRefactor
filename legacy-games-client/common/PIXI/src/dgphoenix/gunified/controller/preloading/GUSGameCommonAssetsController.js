import CommonAssetsController from '../../../unified/controller/preloading/CommonAssetsController';
import GUSGameCommonAssetsInfo from '../../model/preloading/GUSGameCommonAssetsInfo';

class GUSGameCommonAssetsController extends CommonAssetsController
{
	constructor(aOptInfo_si)
	{
		super(aOptInfo_si || new GUSGameCommonAssetsInfo());
	}
}

export default GUSGameCommonAssetsController