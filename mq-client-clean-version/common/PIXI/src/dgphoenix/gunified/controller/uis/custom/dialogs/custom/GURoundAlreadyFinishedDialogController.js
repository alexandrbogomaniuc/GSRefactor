import GUDialogController from '../GUDialogController'
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';

class GURoundAlreadyFinishedDialogController extends GUDialogController
{
	static get EVENT_DIALOG_ACTIVATED()		{return GUDialogController.EVENT_DIALOG_ACTIVATED}

	constructor(aOptInfo_usuii, aOptParentController_usc)
	{
		super(aOptInfo_usuii, undefined, aOptParentController_usc);
	}

	__init ()
	{
		super.__init();
	}
	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().roundAlreadyFinishedDialogView;
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
	}

	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;

			let messageId = "TADialogMessageRoundAlreadyFinished";

			view.setMessage(messageId);
			view.setCustomMode();
		}

		view.customButton.setBackToLobbyCaption();

		super.__validateViewLevel();
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		
		switch (serverData.code) 
		{
			case GUSLobbyWebSocketInteractionController.ERROR_CODES.ROUND_ALREADY_FINISHED:
				this.__activateDialog();
				break;
		}
	}

	__onDialogCustomButtonClicked()
	{
		APP.goToHome();
	}
}

export default GURoundAlreadyFinishedDialogController