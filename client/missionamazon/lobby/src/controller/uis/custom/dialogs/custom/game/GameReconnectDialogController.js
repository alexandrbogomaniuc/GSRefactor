import GameBaseDialogController from './GameBaseDialogController';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyExternalCommunicator from '../../../../../../external/LobbyExternalCommunicator';
import {GAME_MESSAGES} from '../../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';

class GameReconnectDialogController extends GameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED () { return GameBaseDialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return GameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGameReconnectDialogController();
	}

	_initGameReconnectDialogController()
	{
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameReconnectDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

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
				if (LobbyWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					this._handleGameGeneralError(event.data.errorCode);
				}
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
				if (!event.data.wasClean)
				{
					this.__activateDialog();
				}
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
				this.__deactivateDialog();
				break;
		}
	}

	_handleGameGeneralError(errorCode)
	{
		let supported_codes = LobbyWebSocketInteractionController.ERROR_CODES;
		switch(errorCode)
		{
			case supported_codes.SERVER_SHUTDOWN:
				this.__activateDialog();
				break;
		}
	}
}

export default GameReconnectDialogController