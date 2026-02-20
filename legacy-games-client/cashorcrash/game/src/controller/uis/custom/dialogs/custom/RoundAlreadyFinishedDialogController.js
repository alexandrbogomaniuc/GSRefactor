import DialogController from "../DialogController"
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';

class RoundAlreadyFinishedDialogController extends DialogController
{
    static get EVENT_DIALOG_ACTIVATED()		{return DialogController.EVENT_DIALOG_ACTIVATED};

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
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
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

			view.customButton.setBackToLobbyCaption();
		}
		
		super.__validateViewLevel();
	}

    _onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		
		switch (serverData.code) 
		{
			case LobbyWebSocketInteractionController.ERROR_CODES.ROUND_ALREADY_FINISHED:
				this.__activateDialog();
				break;
		}
	}

	__onDialogCustomButtonClicked(event)
	{
		//window.openMQBLobby();
		APP.goToHome();
	}
}

export default RoundAlreadyFinishedDialogController