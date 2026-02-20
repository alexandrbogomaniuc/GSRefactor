import GUGameBaseDialogController from './GUGameBaseDialogController';
import { GAME_MESSAGES } from '../../../../../external/GUSExternalCommunicator';
import GUSLobbyExternalCommunicator from '../../../../../external/GUSLobbyExternalCommunicator';
import { APP } from '../../../../../../../unified/controller/main/globals';


class GUGamePleaseWaitDialogController extends GUGameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return DialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return DialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	_activatePendingDialog(aEvent_str, aOptTime_int=0)
	{
		this._removeDialogForEvent(aEvent_str);

		if (aOptTime_int > 0)
		{
			let lTimer_num = setTimeout(this._timeToActivateDialog.bind(this, aEvent_str), aOptTime_int);
			this.info.i_addEventTimer(aEvent_str, lTimer_num);
		}
		else
		{
			this._timeToActivateDialog(aEvent_str);
		}
	}

	_removeDialogForEvent(aEvent_str)
	{
		clearTimeout(this.info.i_getTimerByEvent(aEvent_str));
		this.info.i_removeEventTimer(aEvent_str);

		if (this.info.isTimerListEmpty)
		{
			this.__deactivateDialog();
		}
	}

	_removeAllTimers()
	{
		for (let lEvent_str of this.info.i_getAllEvents())
		{
			this._removeDialogForEvent(lEvent_str);
		}
		this.__deactivateDialog();
	}

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gamePleaseWaitDialogView;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
	}

	//VALIDATION...
	__validateViewLevel()
	{
		super.__validateViewLevel();
	}
	//...VALIDATION

	__activateDialog()
	{
		this.view && this.view.activateDialog();

		super.__activateDialog();
	}

	__deactivateDialog()
	{
		this.view && this.view.deactivateDialog();
		super.__deactivateDialog();
	}

	__onDialogOkButtonClicked()
	{
		super.__onDialogOkButtonClicked();
		this.__deactivateDialog();
	}

	_timeToActivateDialog(aEvent_str)
	{
		APP.logger.i_pushWarning(`GUGamePleaseWaitDialogController. Dialog activated. Type: ${aEvent_str}`);

		this.__activateDialog();
	}

	_onGameMessageReceived(aEvent_obj)
	{
		switch (aEvent_obj.type)
		{
			case GAME_MESSAGES.ON_PLEASE_WAIT_DIALOG_APPEAR_REQUIRED:
				for (let lMessage_obj in aEvent_obj.data)
				{
					this._activatePendingDialog(lMessage_obj, aEvent_obj.data[lMessage_obj]);
				}
				break;
			case GAME_MESSAGES.ON_PLEASE_WAIT_DIALOG_DISAPPEAR_REQUIRED:
				this._removeDialogForEvent(aEvent_obj.data.eventType);
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
			case GAME_MESSAGES.ROOM_CLOSED:
				// close the dialog and remove all timers, because we won't get the messsage anyway
				this._removeAllTimers();
				break;
		}
	}

	destroy()
	{
		APP.off(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._startHandleGameMessages, this, true);
		super.destroy();
	}
}

export default GUGamePleaseWaitDialogController;