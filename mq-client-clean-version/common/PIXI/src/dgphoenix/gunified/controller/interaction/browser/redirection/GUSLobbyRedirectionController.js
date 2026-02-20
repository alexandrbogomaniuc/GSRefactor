import SimpleController from '../../../../../unified/controller/base/SimpleController';
import { APP } from '../../../../../unified/controller/main/globals';
import GUSLobbyJSEnvironmentInteractionController from '../../js/GUSLobbyJSEnvironmentInteractionController';

class GUSLobbyRedirectionController extends SimpleController
{
	static get EVENT_REDIRECTION() { return "onRedirection" }

	//IL CONSTRUCTION...
	constructor()
	{
		super();
	}
	//...IL CONSTRUCTION

	//IL INTERFACE...
	//...IL INTERFACE

	//ILI INIT...

	__initControlLevel()
	{
		super.__initControlLevel();

		let jsEnvironmentInteractionController = APP.jsEnvironmentInteractionController;
		jsEnvironmentInteractionController.on(GUSLobbyJSEnvironmentInteractionController.EVENT_WAITING_FOR_REDIRECTION, this._onWaitingForExternalRedirection, this);
	}
	//...ILI INIT

	_onWaitingForExternalRedirection()
	{
		this._onRedirection();
	}

	_onRedirection()
	{
		this.emit(GUSLobbyRedirectionController.EVENT_REDIRECTION);
	}
}

export default GUSLobbyRedirectionController;