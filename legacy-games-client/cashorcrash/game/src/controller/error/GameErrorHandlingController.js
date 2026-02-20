import ErrorHandlingController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/error/ErrorHandlingController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

/**
 * Runtime error handling controller.
 * Fires GameErrorHandlingController#i_EVENT_ON_RUNTIME_ERROR.
 * 
 * @class
 * @extends ErrorHandlingController
 * @inheritdoc
 */
class GameErrorHandlingController extends ErrorHandlingController {

    /**
     * Runtime error event.
     *
     * @event GameErrorHandlingController#i_EVENT_ON_RUNTIME_ERROR
     * @type {object}
     * @property {string} message - Runtime error message.
     */
    static get i_EVENT_ON_RUNTIME_ERROR() { return 'i_EVENT_ON_RUNTIME_ERROR'; }

    __initControlLevel() {
        super.__initControlLevel();
    }

    _onError(aErrorMsg_str, aUrl_str, aLineNumber_int, aColumnNumber_int, aErrorObject_obj, aIsGame_bl = true) {
        super._onError(aErrorMsg_str, aUrl_str, aLineNumber_int, aColumnNumber_int, aErrorObject_obj);

        //EvalError :: Creates an instance representing an error that occurs regarding the global function eval().
        //InternalError :: Creates an instance representing an error that occurs when an internal error in the JavaScript engine is thrown. E.g. "too much recursion". (! not standartized)
        //RangeError :: Creates an instance representing an error that occurs when a numeric variable or parameter is outside of its valid range.
        //ReferenceError :: Creates an instance representing an error that occurs when de-referencing an invalid reference.
        //SyntaxError ::  Creates an instance representing a syntax error.
        //TypeError :: Creates an instance representing an error that occurs when a variable or parameter is not of a valid type.
        //URIError :: Creates an instance representing an error that occurs when encodeURI() or decodeURI() are passed invalid parameters.

        let lStackTrace_str = "\n" + (aErrorObject_obj.stack || "");

        let lLocation_str = aIsGame_bl ? "Game" : "Game";
        let lErrorInfo_str = `Runtime error happenned in ${lLocation_str}! ${aErrorMsg_str}${lStackTrace_str}`;

        this.emit(GameErrorHandlingController.i_EVENT_ON_RUNTIME_ERROR, {message: lErrorInfo_str});

        return false;

    }
}

export default GameErrorHandlingController;