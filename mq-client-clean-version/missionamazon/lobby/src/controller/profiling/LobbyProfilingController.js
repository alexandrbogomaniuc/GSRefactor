import ProfilingController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/profiling/ProfilingController';
import LobbyProfilingInfo from '../../model/profiling/LobbyProfilingInfo';

class LobbyProfilingController extends ProfilingController {

	constructor()
	{		
		super(new LobbyProfilingInfo());
	}

	//add your own getters/functions, specific to the lobby level
}

export default LobbyProfilingController;