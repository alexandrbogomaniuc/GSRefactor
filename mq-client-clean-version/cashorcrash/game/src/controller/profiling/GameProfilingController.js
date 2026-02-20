import ProfilingController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/profiling/ProfilingController';
import GameProfilingInfo from '../../model/profiling/GameProfilingInfo';

class GameProfilingController extends ProfilingController {

	constructor()
	{		
		super(new GameProfilingInfo());
	}

	//add your own getters/functions, specific to the game level
}

export default GameProfilingController;