import GUGameBaseDialogController from './GUGameBaseDialogController';
import { APP } from '../../../../../../../unified/controller/main/globals';
import GUSLobbyExternalCommunicator from '../../../../../external/GUSLobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../../external/GUSExternalCommunicator';
import GUSLobbyApplication from '../../../../../main/GUSLobbyApplication';

class GUGamePendingOperationFailedDialogController extends GUGameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED () { return GUGameBaseDialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return GUGameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGUGamePendingOperationFailedDialogController();
	}

	_initGUGamePendingOperationFailedDialogController()
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
			APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}
	}

	_onLobbyStarted(event)
	{
		this._startHandleGameErrors();
	}

	_startHandleGameErrors()
	{
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
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
			case GAME_MESSAGES.ON_PENDING_OPERATION_FAILED_DIALOG_REQUIRED:
				this.info.retryType = event.data.type;
				this.__activateDialog();
				break;
		}
	}
}

export default GUGamePendingOperationFailedDialogController