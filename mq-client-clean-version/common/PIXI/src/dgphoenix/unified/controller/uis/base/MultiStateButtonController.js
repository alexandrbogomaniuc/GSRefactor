import SimpleUIController from './SimpleUIController';

/**
 * @class
 * Base controller for buttons that hase several visible states.
 * For example, Use for sound button with on/off states.
 */
class MultiStateButtonController extends SimpleUIController
{
	constructor(...args)
	{
		super(...args);

		this._initMultiStateButtonController();
	}

	_initMultiStateButtonController()
	{
	}
}

export default MultiStateButtonController