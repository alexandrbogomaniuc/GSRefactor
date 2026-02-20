import ProfilingController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/profiling/ProfilingController';
import GameProfilingInfo from '../../model/profiling/GameProfilingInfo';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ProfilingInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo';

class GameProfilingController extends ProfilingController {

	static get EVENT_ON_PROFILES_READY()		{ return "EVENT_ON_PROFILES_READY" };

	constructor()
	{
		super(new GameProfilingInfo());
	}

	//override
	init(aProfilingObj_obj) 
	{
		super.init(aProfilingObj_obj);

		this.emit(GameProfilingController.EVENT_ON_PROFILES_READY);
	}

	//add your own getters/functions, specific to the game level
}

export default GameProfilingController;