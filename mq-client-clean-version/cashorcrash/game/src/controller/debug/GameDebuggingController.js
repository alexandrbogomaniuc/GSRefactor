import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

/**
 * Can be used for debug purposes.
 * Subscribe to events from the environment and implement required handlers.
 * 
 * @class
 * @extends SimpleController
 * @inheritdoc
 */
class GameDebuggingController extends SimpleController {

	constructor()
	{
		super();		
	}

	//override
	__init()
	{
		super.__init();
	}


}

export default GameDebuggingController;