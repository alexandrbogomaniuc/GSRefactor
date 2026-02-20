import DialogController from '../../../../../controller/uis/custom/dialogs/DialogController';
import LobbyErrorHandlingController from '../../../../../controller/error/LobbyErrorHandlingController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class RuntimeErrorDialogController extends DialogController {

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initRuntimeErrorDialogController();
	}

	_initRuntimeErrorDialogController()
	{

	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().runtimeErrorDialogView;
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let lobbyErrorHandlingController = APP.errorHandlingController;
		lobbyErrorHandlingController.on(LobbyErrorHandlingController.i_EVENT_ON_RUNTIME_ERROR, this._onRuntimeError, this);
	}

	//VALIDATION...
	__validateViewLevel ()
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

export default RuntimeErrorDialogController;