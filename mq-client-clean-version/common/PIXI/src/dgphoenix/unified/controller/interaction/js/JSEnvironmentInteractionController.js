import SimpleController from '../../base/SimpleController';
import Application from '../../main/Application';
import { APP } from '../../main/globals';
import WebSocketInteractionController from '../server/WebSocketInteractionController';

/**
 * @class
 * @classdesc Base class for javascript environment interaction. Use to call window methods.
 * @extends SimpleController 
 */
class JSEnvironmentInteractionController extends SimpleController {
	static get EVENT_WAITING_FOR_REDIRECTION() { return "onWaitingForRedirection"; }
	static get EVENT_ON_GAME_SESSION_FINISHED() { return "EVENT_ON_GAME_SESSION_FINISHED" };

	constructor(optInfo) {
		super(optInfo);
	}

	/**
	 * Calls specified window method
	 * @param {String} aWindowMethodName_str - Window method name
	 * @param {Object[]} aOptParams_obj_arr - Argunemts for window method
	 */
	callWindowMethod(aWindowMethodName_str, aOptParams_obj_arr) {
		this.__callWindowMethod(aWindowMethodName_str, aOptParams_obj_arr);
	}

	__initControlLevel() {

		APP.on(Application.EVENT_IS_LOBBY_APP, this._registerCloseGameSessionListener, this);
		super.__initControlLevel();
	}

	_registerCloseGameSessionListener(event)
	{
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.off(WebSocketInteractionController.EVENT_ON_GAME_SESSION_FINISHED, this._onGameSessionFinished, this);
		webSocketInteractionController.on(WebSocketInteractionController.EVENT_ON_GAME_SESSION_FINISHED, this._onGameSessionFinished, this);
	}

	_onGameSessionFinished(event)
	{
		let lAppParams = APP.appParamsInfo;
		if (lAppParams.homeFuncNameDefined)
		{
			let lHomeFuncName_str = lAppParams.homeFuncName;
			if (lHomeFuncName_str)
			{
				this.__callWindowMethod(lHomeFuncName_str);
			}
		}
	}

	__callWindowMethod(aMethodName_str, aParams_obj_arr) {
		var lRet_obj;
		try {
			lRet_obj = window[aMethodName_str].apply(window, aParams_obj_arr);
		}
		catch (a_obj) {
			throw new Error(`An error occured while trying to call JS Environment method: METHOD NAME = ${aMethodName_str}; PARAMS = ${aParams_obj_arr}`);
		}
		return lRet_obj;
	}
}

export default JSEnvironmentInteractionController