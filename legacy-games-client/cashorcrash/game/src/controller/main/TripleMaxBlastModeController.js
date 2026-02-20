import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import TripleMaxBlastModeInfo from '../../model/main/TripleMaxBlastModeInfo';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class TripleMaxBlastModeController extends SimpleController
{
	constructor(aOptInfo_si)
	{
		super(aOptInfo_si || new TripleMaxBlastModeInfo());
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		if (APP.appParamsInfo.gameId === 875)
		{
			this.info.isTripleMaxBlastMode = true;
		}
		else
		{
			let lModeParamValue_str = APP.appParamsInfo.getParamValue(TripleMaxBlastModeInfo.PARAM_IS_TRIPLE_MAX_BLAST_MODE) || "false";
			this.info.isTripleMaxBlastMode = lModeParamValue_str.toLowerCase() === "true" || lModeParamValue_str.toLowerCase() === "1";
		}
	}

	__initControlLevel()
	{
		super.__initControlLevel();
	}
}

export default TripleMaxBlastModeController;