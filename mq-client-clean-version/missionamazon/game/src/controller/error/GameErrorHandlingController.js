import ErrorHandlingController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/error/ErrorHandlingController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { GAME_MESSAGES } from '../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSGameExternalCommunicator';

class GameErrorHandlingController extends ErrorHandlingController {

	//override
	_onError(aErrorMsg_str, aUrl_str, aLineNumber_int, aColumnNumber_int, aErrorObject_obj) {
		let lData_obj = {
			errorMessage: 	aErrorMsg_str,
			url: 			aUrl_str,
			lineNumber: 	aLineNumber_int,
			columnNumber: 	aColumnNumber_int,
			errorObject: 	aErrorObject_obj,
			errorStack: 	aErrorObject_obj.stack
		};
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.RUNTIME_ERROR, lData_obj);
		return false;
	}

}

export default GameErrorHandlingController;