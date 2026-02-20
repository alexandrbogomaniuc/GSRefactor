import SimpleController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameJSEnvironmentInteractionController from '../../js/GameJSEnvironmentInteractionController';

class RedirectionController extends SimpleController
{
	static get EVENT_REDIRECTION() {return "onRedirection"}

	//IL CONSTRUCTION...
	constructor(aOptInfo_ussi)
	{
		super();
		
		this._initRedirectionController();
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	//...IL INTERFACE

	//ILI INIT...
	_initRedirectionController()
	{
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let jsEnvironmentInteractionController = APP.jsEnvironmentInteractionController;
		jsEnvironmentInteractionController.on(GameJSEnvironmentInteractionController.EVENT_WAITING_FOR_REDIRECTION, this._onWaitingForExternalRedirection, this);
	}
	//...ILI INIT

	_onWaitingForExternalRedirection(event)
	{
		this._onRedirection();
	}

	_onRedirection()
	{
		this.emit(RedirectionController.EVENT_REDIRECTION);
	}
}

export default RedirectionController;