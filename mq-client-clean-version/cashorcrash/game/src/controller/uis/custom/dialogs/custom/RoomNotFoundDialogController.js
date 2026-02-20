import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';

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
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	//DEBUG...
	get debugMessages()
	{
		return ["TADialogMessageRoomNotFound", "TADialogMessageRoomNotOpen"];
	}
	//...DEBUG

	__validateViewLevel ()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;

			let messageId = "TADialogMessageRoomNotFound";
			if (this.info.roomSelectionErrorCode === GameWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN)
			{
				messageId = "TADialogMessageRoomNotOpen";
			}

			//DEBUG...
			if (this.curDebugMessage !== undefined)
			{
				messageId = this.curDebugMessage;
			}
			//...DEBUG
			
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
			case GameWebSocketInteractionController.ERROR_CODES.ROOM_NOT_FOUND:
			case GameWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN:
				if (requestData && requestData.rid >= 0)
				{
					this.info.roomSelectionErrorCode = serverData.code;
					this.__activateDialog();
				}
				break;
		}
	}
}

export default RoomNotFoundDialogController