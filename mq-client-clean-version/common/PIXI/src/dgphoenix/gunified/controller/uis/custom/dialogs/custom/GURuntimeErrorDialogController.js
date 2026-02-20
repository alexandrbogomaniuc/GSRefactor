import GUDialogController from '../GUDialogController';
import GUSLobbyErrorHandlingController from '../../../../error/GUSLobbyErrorHandlingController';
import { APP } from '../../../../../../unified/controller/main/globals';

/**
 * @class
 * @extends GUDialogController
 * @classdesc Dialog that appears when runtime error occures (if error handling mode is on)
 */
class GURuntimeErrorDialogController extends GUDialogController
{
	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initRuntimeErrorDialogController();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().runtimeErrorDialogView;
	}

	_initRuntimeErrorDialogController()
	{

	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let lobbyErrorHandlingController = APP.errorHandlingController;
		lobbyErrorHandlingController.on(GUSLobbyErrorHandlingController.i_EVENT_ON_RUNTIME_ERROR, this._onRuntimeError, this);
	}

	//VALIDATION...
	__validateViewLevel()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;

			let messageId = "TADialogMessageCriticalErrorInternal";

			view.setMessage(messageId, info.errorMessage);
			view.setCustomMode();

			view.customButton.setCopyCaption();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onRuntimeError(aEvent_obj)
	{
		this.info.errorMessage = aEvent_obj.message;
		this.__activateDialog();
	}

	__onDialogCustomButtonClicked(event)
	{
		this.view.copyToClibboard();

		super.__onDialogCustomButtonClicked(event);
	}
}

export default GURuntimeErrorDialogController;