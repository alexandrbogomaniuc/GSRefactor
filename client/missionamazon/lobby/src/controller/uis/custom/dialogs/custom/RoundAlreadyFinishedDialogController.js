import DialogController from "../DialogController"
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';

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
		}

		view.customButton.setBackToLobbyCaption();

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

	_onDialogCustomButtonClicked(event){

		APP.goToHome();
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

export default RoundAlreadyFinishedDialogController