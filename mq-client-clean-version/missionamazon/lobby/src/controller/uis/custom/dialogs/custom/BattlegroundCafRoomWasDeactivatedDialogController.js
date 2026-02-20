import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import {GAME_MESSAGES} from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';

class BattlegroundCafRoomWasDeactivatedDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };
	static get EVENT_DIALOG_ACTIVATED()	{ return DialogController.EVENT_DIALOG_ACTIVATED };
	static get EVENT_DIALOG_DEACTIVATED() { return DialogController.EVENT_DIALOG_DEACTIVATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
	}

	__init ()
	{
		super.__init()

	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundCafRoomWasDeactivatedDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		// DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}
	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 68) //d
	// 	{
	// 		this.__activateDialog();
	// 	}
	// }
	//...DEBUG

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		
		switch (serverData.code)
		{
			case LobbyWebSocketInteractionController.ERROR_CODES.ROOM_WAS_DEACTIVATED:
					this.__activateDialog();
				break;
		}
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		let msgData = event.data;

		switch (msgType)
		{
			case GAME_MESSAGES.BATTLEGROUND_CAF_DEACTIVATED_ROOM_DELAYED_ERROR_TIME:
				this.__activateDialog();
				break;

			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (
						msgData.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.ROOM_WAS_DEACTIVATED
						&& LobbyWebSocketInteractionController.isFatalError(msgData.errorType)
					)
				{
					this.__activateDialog();
				}
				break;
		}
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var view = this.__fView_uo;

		//buttons configuration...
		view.setCancelMode();
		//...buttons configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{
		//wont work here because the room is already closed 
		//APP.goToHome();
		// call the method directly
		let lAppParams = APP.appParamsInfo;
		if (lAppParams.homeFuncNameDefined)
		{
			let lHomeFuncName_str = lAppParams.homeFuncName;
			if (lHomeFuncName_str)
			{
				this.__callWindowMethod(lHomeFuncName_str);
			}
		}
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

	__activateDialog()
	{
		if (!APP.isCAFMode)
		{
			return;
		}

		super.__activateDialog();
	}
}

export default BattlegroundCafRoomWasDeactivatedDialogController