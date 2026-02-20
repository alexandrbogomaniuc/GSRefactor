import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import LobbyAPP from '../../../../../LobbyAPP';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import {GAME_MESSAGES} from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';

class RoomNotFoundDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initRoomNotFoundDialogController();
	}

	_initRoomNotFoundDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().roomNotFoundDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived.bind(this));
	}

	//VALIDATION...
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

			let messageId = "TADialogMessageRoomNotFound";
			if (this.info.roomSelectionErrorCode === LobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN)
			{
				messageId = "TADialogMessageRoomNotOpen";
			}
			else if (
						APP.isCAFMode
						&& (
								this.info.roomSelectionErrorCode === LobbyWebSocketInteractionController.ERROR_CODES.TOO_MANY_OBSERVERS
								|| this.info.roomSelectionErrorCode === LobbyWebSocketInteractionController.ERROR_CODES.TOO_MANY_PLAYER
							)
					)
			{
				messageId = "TADialogCAFMessageRoomIsFull";
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

	_onLobbyServerConnectionOpened(event)
	{
		this.__deactivateDialog();
	}

	_onLobbyServerConnectionClosed(event)
	{
		this.__deactivateDialog();
	}
	
	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		
		switch (serverData.code) 
		{
			case LobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_FOUND:
			case LobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN:
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
				if (LobbyWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					this._handleGameGeneralError(event.data.errorCode);
				}
				break;
		}
	}

	_handleGameGeneralError(errorCode)
	{
		let supported_codes = LobbyWebSocketInteractionController.ERROR_CODES;
		switch(errorCode)
		{
			case supported_codes.TOO_MANY_OBSERVERS:
			case supported_codes.TOO_MANY_PLAYER:
				if (!APP.isCAFMode)
				{
					break;
				}
			case supported_codes.NOT_CONFIRM_BUYIN:
			case supported_codes.ROOM_NOT_OPEN:
				if (APP.isBattlegroundGame && errorCode === supported_codes.ROOM_NOT_OPEN)
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

export default RoomNotFoundDialogController