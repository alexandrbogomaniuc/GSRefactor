import SimpleController from '../base/SimpleController';
import ErrorHandlingInfo from '../../model/error/ErrorHandlingInfo';
import { APP } from '../main/globals';

/**
 * @class
 * @classdesc Base runtime error handling controller.
 * @extends SimpleController
 */
class ErrorHandlingController extends SimpleController {

	constructor(aOptInfo_si)
	{
		super(aOptInfo_si || new ErrorHandlingInfo());

		this._fOldErrorHandler_func = null;
	}

	__init() {
		super.__init();

		if (!APP.isErrorHandlingMode) return;

		this._fOldErrorHandler_func = window.onerror; // if for example it's been declared somewhere like <body onerror=...
		window.onerror = this._onError.bind(this);
	}

	_onError(aErrorMsg_str, aUrl_str, aLineNumber_int, aColumnNumber_int, aErrorObject_obj) {
		if (this._fOldErrorHandler_func)
		{
			this._fOldErrorHandler_func(...arguments);
		}
		APP.logger.i_pushError(`Error handle. ${aErrorMsg_str}. ${JSON.stringify(aErrorObject_obj)}`);
	}
}

export default ErrorHandlingController;