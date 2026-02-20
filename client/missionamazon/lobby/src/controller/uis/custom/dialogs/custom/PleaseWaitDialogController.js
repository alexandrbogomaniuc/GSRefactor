import DialogController from '../DialogController';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../external/LobbyExternalCommunicator';
import {WAIT_MESSAGE_TYPES, WAIT_EVENT_TYPES} from '../../../../../model/uis/custom/dialogs/custom/PleaseWaitDialogInfo';
import DialogsInfo from '../../../../../model/uis/custom/dialogs/DialogsInfo';
import {SERVER_MESSAGES, CLIENT_MESSAGES} from '../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';

class PleaseWaitDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return DialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return DialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().pleaseWaitDialogView;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
		APP.webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_GAME_CLIENT_SENT_MESSAGE, this._onClientSentMessage, this);
		APP.webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		APP.webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		APP.webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);
	}

	//VALIDATION...
	__validateViewLevel()
	{
		super.__validateViewLevel();

		var view = this.__fView_uo;

		view.setEmptyMode();

		view.validateSpinner();
	}
	//...VALIDATION

	_onClientSentMessage(aData_obj)
	{
		switch (aData_obj.class)
		{
			case CLIENT_MESSAGES.GET_BATTLEGROUND_START_URL:
				this._activatePendingDialog(`${WAIT_MESSAGE_TYPES.GET_BATTLEGROUND_START_URL}:${aData_obj.rid}`, WAIT_EVENT_TYPES.LOBBY, 2000);
				break;
		}
	}

	_onServerMessage(aEvent_obj)
	{
		let lServerMessageData_obj = aEvent_obj.messageData;
		let lRequestData_obj = aEvent_obj.requestData;

		switch (lServerMessageData_obj.class)
		{
			case SERVER_MESSAGES.GET_BATTLEGROUND_START_URL_RESPONSE:
			case SERVER_MESSAGES.GET_PRIVATE_BATTLEGROUND_START_URL_RESPONSE:
			case SERVER_MESSAGES.GET_START_GAME_URL_RESPONSE:
				if (lRequestData_obj && lRequestData_obj.rid > 0)
				{
					this._removeDialogForEvent(`${WAIT_MESSAGE_TYPES.GET_BATTLEGROUND_START_URL}:${lRequestData_obj.rid}`);
				}
				break;
		}
	}

	_onServerErrorMessage(event)
	{
		let requestData = event.requestData;
		if (requestData && requestData.rid > -1)
		{
			switch (requestData.class)
			{
				case CLIENT_MESSAGES.GET_BATTLEGROUND_START_URL:
					this._removeDialogForEvent(`${WAIT_MESSAGE_TYPES.GET_BATTLEGROUND_START_URL}:${requestData.rid}`);
					break;
				
			}
		}
	}

	_onLobbyServerConnectionOpened(aEvent_obj)
	{
		this._removeAllLobbyPendingEvents();
	}

	_onLobbyServerConnectionClosed(aEvent_obj)
	{
		this._removeAllLobbyPendingEvents();
	}

	_onGameMessageReceived(aEvent_obj)
	{
		switch (aEvent_obj.type)
		{
			case GAME_MESSAGES.ON_PLEASE_WAIT_DIALOG_APPEAR_REQUIRED:
				for (let lMessage_obj in aEvent_obj.data)
				{
					this._activatePendingDialog(lMessage_obj, WAIT_EVENT_TYPES.ROOM, aEvent_obj.data[lMessage_obj]);
				}
				break;
			case GAME_MESSAGES.ON_PLEASE_WAIT_DIALOG_DISAPPEAR_REQUIRED:
				this._removeDialogForEvent(aEvent_obj.data.eventType);
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
			case GAME_MESSAGES.ROOM_CLOSED:
				// close the dialog and remove all timers, because we won't get the messsage anyway
				this._removeAllRoomPendingEvents();
				break;
		}
	}

	__activateDialog()
	{
		super.__activateDialog();
	}

	__deactivateDialog()
	{
		super.__deactivateDialog();
	}

	_activatePendingDialog(aEvent_str, aEventType_str, aOptTime_int=0)
	{
		this._removeDialogForEvent(aEvent_str);

		APP.logger.i_pushDebug(`PleaseWaitDialogController, _activatePendingDialog: ${aEvent_str}, delay to activate: ${aOptTime_int}`);

		if (aOptTime_int > 0)
		{
			let lTimerId_num = setTimeout(this._onTimeToActivateDialog.bind(this, aEvent_str), aOptTime_int);
			this.info.i_addEventTimer(aEvent_str, aEventType_str, lTimerId_num);
		}
		else
		{
			this.info.i_addEventTimer(aEvent_str, aEventType_str, -1);
			this._onTimeToActivateDialog(aEvent_str);
		}
	}

	_onTimeToActivateDialog(aEvent_str)
	{
		APP.logger.i_pushWarning(`PleaseWaitDialogController. Dialog activated. Type: ${aEvent_str}`);

		this.__activateDialog();
	}

	_removeDialogForEvent(aEvent_str)
	{
		if (!this.info.i_doesTimerExist(aEvent_str))
		{
			return;
		}

		let lTimer_int = this.info.i_getTimerByEvent(aEvent_str);
		if (lTimer_int >= 0)
		{
			clearTimeout(lTimer_int);
		}
		this.info.i_removeEventTimer(aEvent_str);

		APP.logger.i_pushDebug(`PleaseWaitDialogController, _removeDialogForEvent: ${aEvent_str}, is rest timer list empty: ${this.info.isTimerListEmpty}`);

		if (this.info.isTimerListEmpty)
		{
			this.__deactivateDialog();
		}
	}

	_removeAllRoomPendingEvents()
	{
		for (let lEvent_str of this.info.i_getAllEvents(WAIT_EVENT_TYPES.ROOM))
		{
			this._removeDialogForEvent(lEvent_str);
		}
	}

	_removeAllLobbyPendingEvents()
	{
		for (let lEvent_str of this.info.i_getAllEvents(WAIT_EVENT_TYPES.LOBBY))
		{
			this._removeDialogForEvent(lEvent_str);
		}
	}

	destroy()
	{
		super.destroy();
	}
}

export default PleaseWaitDialogController;