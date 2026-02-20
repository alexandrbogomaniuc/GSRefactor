import ErrorHandlingController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/error/ErrorHandlingController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyExternalCommunicator from '../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';

class LobbyErrorHandlingController extends ErrorHandlingController {

    static get i_EVENT_ON_RUNTIME_ERROR() { return 'i_EVENT_ON_RUNTIME_ERROR'; }

    __initControlLevel() {
        super.__initControlLevel();

        APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

    }

    _onGameMessageReceived(aEvent_obj) {
        if (aEvent_obj.type === GAME_MESSAGES.RUNTIME_ERROR) {
            if (!aEvent_obj.data.errorObject.stack) {
                aEvent_obj.data.errorObject.stack = aEvent_obj.data.errorStack; //stack is lost after postMessage
            }
            this._onError(aEvent_obj.data.errorMessage,
                aEvent_obj.data.url,
                aEvent_obj.data.lineNumber,
                aEvent_obj.data.columnNumber,
                aEvent_obj.data.errorObject,
                false /*error happened not in the lobby*/
            );
        }
    }

    _onError(aErrorMsg_str, aUrl_str, aLineNumber_int, aColumnNumber_int, aErrorObject_obj, aIsLobby_bl = true) {
        super._onError(aErrorMsg_str, aUrl_str, aLineNumber_int, aColumnNumber_int, aErrorObject_obj);

        //EvalError :: Creates an instance representing an error that occurs regarding the global function eval().
        //InternalError :: Creates an instance representing an error that occurs when an internal error in the JavaScript engine is thrown. E.g. "too much recursion". (! not standartized)
        //RangeError :: Creates an instance representing an error that occurs when a numeric variable or parameter is outside of its valid range.
        //ReferenceError :: Creates an instance representing an error that occurs when de-referencing an invalid reference.
        //SyntaxError ::  Creates an instance representing a syntax error.
        //TypeError :: Creates an instance representing an error that occurs when a variable or parameter is not of a valid type.
        //URIError :: Creates an instance representing an error that occurs when encodeURI() or decodeURI() are passed invalid parameters.

        let lStackTrace_str = "\n" + (aErrorObject_obj.stack || "");

        let lLocation_str = aIsLobby_bl ? "Lobby" : "Game";
        let lErrorInfo_str = `Runtime error happenned in ${lLocation_str}! ${aErrorMsg_str}${lStackTrace_str}`;

        this.emit(LobbyErrorHandlingController.i_EVENT_ON_RUNTIME_ERROR, {message: lErrorInfo_str});

        return false;

    }
}

export default LobbyErrorHandlingController;