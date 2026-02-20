import GameBaseDialogController from './GameBaseDialogController';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyExternalCommunicator from '../../../../../../external/LobbyExternalCommunicator';
import {GAME_MESSAGES} from '../../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import LobbyAPP from '../../../../../../LobbyAPP';
import {GAME_CLIENT_MESSAGES} from '../../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';

class GamePendingOperationFailedDialogController extends GameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED () { return GameBaseDialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return GameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGamePendingOperationFailedDialogController();
	}

	_initGamePendingOperationFailedDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gamePendingOperationFailedDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		if (APP.lobbyAppStarted)
		{
			this._startHandleGameErrors();
		}
		else
		{
			APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}
	}

	_onLobbyStarted(event)
	{
		this._startHandleGameErrors();
	}

	_startHandleGameErrors()
	{
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
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

			view.setMessage("TADialogMessageCriticalErrorPendingTransaction");	
			view.setOkCancelMode();
			
			view.okButton.setRetryCaption();
			view.cancelButton.setCancelCaption();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{	
		super.__onDialogCancelButtonClicked(event);

		this.__deactivateDialog();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		
		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
				//this.__deactivateDialog();
				break;
			case GAME_MESSAGES.ON_PENDING_OPERATION_FAILED_DIALOG_REQUIRED:
				this.info.retryType = event.data.type;
				this.__activateDialog();
				break;
		}
	}
}

export default GamePendingOperationFailedDialogController