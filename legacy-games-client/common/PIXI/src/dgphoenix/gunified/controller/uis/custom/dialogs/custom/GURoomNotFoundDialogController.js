import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import { GAME_MESSAGES } from '../../../../external/GUSExternalCommunicator';
import GUSLobbyExternalCommunicator from '../../../../external/GUSLobbyExternalCommunicator';

class GURoomNotFoundDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initRoomNotFoundDialogController();
	}

	_initRoomNotFoundDialogController()
	{	
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().roomNotFoundDialogView;
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived.bind(this));
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

			let messageId = "TADialogMessageRoomNotFound";
			if (this.info.roomSelectionErrorCode === GUSLobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN)
			{
				messageId = "TADialogMessageRoomNotOpen";
			}
			
			view.setMessage(messageId);
			view.setOkMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.__deactivateDialog();
	}

	_onLobbyServerConnectionOpened()
	{
		this.__deactivateDialog();
	}

	_onLobbyServerConnectionClosed()
	{
		this.__deactivateDialog();
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;

		switch (serverData.code) 
		{
			case GUSLobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_FOUND:
			case GUSLobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN:
				if (requestData && requestData.rid >= 0)
				{
					this.info.roomSelectionErrorCode = serverData.code;
					this.__activateDialog();
				}
				break;
		}
	}

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
		}
	}

	_handleGameGeneralError(errorCode)
	{
		let supported_codes = GUSLobbyWebSocketInteractionController.ERROR_CODES;
		switch (errorCode)
		{
			case supported_codes.ROOM_NOT_OPEN:
				if (APP.isBattlegroundGame)
				{
					// Dialog TADialogMessageRoomNotOpen should be opened only in SP version
					break;
				}
				this.info.roomSelectionErrorCode = errorCode;
				this.__activateDialog();
				break;
		}
	}

}

export default GURoomNotFoundDialogController