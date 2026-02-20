import SimpleController from '../base/SimpleController';
import ProfilingInfo from '../../model/profiling/ProfilingInfo';
import { APP } from '../main/globals';

/**
 * @class
 * @extends SimpleController
 * @classdesc Base controller to specify application profiles.
 */
class ProfilingController extends SimpleController {

	constructor(aInfo_pi = null)
	{
		let lInfo_pi = aInfo_pi ? aInfo_pi : new ProfilingInfo();
		super (lInfo_pi);
	}

	//override
	init(aProfilingObj_obj) 
	{
		super.init();

		this.info.profiles = aProfilingObj_obj;
	}
}

export default ProfilingController;