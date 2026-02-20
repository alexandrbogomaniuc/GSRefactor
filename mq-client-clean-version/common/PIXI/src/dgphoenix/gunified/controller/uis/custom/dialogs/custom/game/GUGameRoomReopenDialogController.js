import GUGameBaseDialogController from './GUGameBaseDialogController';
import { APP } from '../../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyExternalCommunicator from '../../../../../external/GUSLobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../../external/GUSExternalCommunicator';

class GUGameRoomReopenDialogController extends GUGameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUGameBaseDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUGameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGameRoomReopenDialogController();
	}

	_initGameRoomReopenDialogController()
	{
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameRoomReopenDialogView;
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived.bind(this));

		let roomNotFoundDialogController = APP.dialogsController.roomNotFoundDialogController;
		roomNotFoundDialogController.on(GUGameBaseDialogController.EVENT_DIALOG_ACTIVATED, this._onRoomNotFoundDialogActivated, this);
	}

	//VALIDATION...
	__validateModelLevel()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;

			view.setMessage("TADialogMessageReconnect");
			view.setEmptyMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (GUSLobbyWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					this._handleGameGeneralError(event.data.errorCode);
				}
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
				this.__deactivateDialog();
				break;
			case GAME_MESSAGES.RESTORED_AFTER_UNREASONABLE_REQUEST:
			case GAME_MESSAGES.RESTORE_AFTER_UNREASONABLE_REQUEST_CANCELED:
				this.__deactivateDialog();
				break;
		}
	}

	_handleGameGeneralError(errorCode)
	{
		let supported_codes = GUSLobbyWebSocketInteractionController.ERROR_CODES;
		switch (errorCode)
		{
			case supported_codes.ROOM_NOT_FOUND:
			case supported_codes.ROOM_MOVED:
			case supported_codes.NOT_SEATER:
				this.__activateDialog();
				break;
		}
	}

	_onRoomNotFoundDialogActivated()
	{
		this.__deactivateDialog();
	}
}

export default GUGameRoomReopenDialogController