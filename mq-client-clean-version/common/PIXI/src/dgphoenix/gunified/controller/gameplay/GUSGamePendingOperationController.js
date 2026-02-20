import GUPendingOperationController from './GUPendingOperationController';
import GUSGamePendingOperationInfo from '../../model/gameplay/GUSGamePendingOperationInfo';
import { APP } from '../../../unified/controller/main/globals';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../external/GUSExternalCommunicator';
import GUSGameExternalCommunicator from '../external/GUSGameExternalCommunicator';
import WebSocketInteractionController from '../../../unified/controller/interaction/server/WebSocketInteractionController';
import { GAME_CLIENT_MESSAGES, SERVER_MESSAGES } from '../../../unified/model/interaction/server/WebSocketInteractionInfo';

class GUSGamePendingOperationController extends GUPendingOperationController
{
	constructor(aOptInfo_poi)
	{
		super(aOptInfo_poi || new GUSGamePendingOperationInfo);
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		this.info.isPendingOperationHandlingSupported = true;

		if (this.info.isPendingOperationHandlingSupported && (APP.urlBasedParams.ALREADY_SEAT_ROOM_ID >= 0))
		{
			this.__resetPendingOperationIfPossible();
		}
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.externalCommunicator.on(GUSGameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	// DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 103) //7
	// 	{
	// 		this._startPendingOperation();
	// 	}
	// }
	// ...DEBUG

	_onLobbyExternalMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case LOBBY_MESSAGES.PENDING_OPERATION_STATUS_UPDATED:
				if (event.data.inProgress)
				{
					this._startPendingOperation(true);
				}
				else
				{
					this._completePendingOperation(true);
				}
				break;
			case LOBBY_MESSAGES.PENDING_OPERATION_TRACKING_STATUS_UPDATED:
				if (event.data.inProgress)
				{
					this.__turnOperationStatusTrackingOff(true);
				}
				else
				{
					if (this.info.isPendingOperationInProgress)
					{
						this.__turnOperationStatusTrackingOn(true);
					}
				}
				
				break;
		}
	}

	_onServerMessage(event)
	{	
		let data = event.messageData;
		let lRequestData_obj = event.requestData;

		switch(data.class)
		{
			case SERVER_MESSAGES.OK:
				if (this.info.isPendingOperationInProgress && lRequestData_obj.class === GAME_CLIENT_MESSAGES.CLOSE_ROOM)
				{
					this.__turnOperationStatusTrackingOff();
				}
				break;

			default:
				super._onServerMessage(event);
				break;
		}
	}

	_onServerErrorMessage(event)
	{
		super._onServerErrorMessage(event);
		
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		switch (serverData.code)
		{
			case WebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION:
				this.__turnOperationStatusTrackingOn();
				break;

			case WebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE:
				if (APP.isBattlegroundGame)
				{
					this.__resetPendingOperationIfPossible();
				}
				break;
		}
	}

	_onServerConnectionClosed(event)
	{
		if (!event.wasClean)
		{
			this.__resetPendingOperationIfPossible();
		}
	}

	_startPendingOperation(aIsSyncFromLobby_bl=false)
	{
		let lPreviousPendingOperationInProgress_bl = this.info.isPendingOperationInProgress;

		super._startPendingOperation();

		if (
				!aIsSyncFromLobby_bl
				&& this.info.isPendingOperationInProgress
				&& !lPreviousPendingOperationInProgress_bl
			)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.PENDING_OPERATION_STATUS_UPDATED, {inProgress: this.info.isPendingOperationInProgress});
		}
	}

	_completePendingOperation(aIsSyncFromLobby_bl=false)
	{
		let lPreviousPendingOperationProgressStatusDefined_bl = this.info.isPendingOperationProgressStatusDefined;
		let lPreviousPendingOperationInProgress_bl = this.info.isPendingOperationInProgress;

		super._completePendingOperation();

		if (
				!aIsSyncFromLobby_bl
				&& !this.info.isPendingOperationInProgress
				&& (!!lPreviousPendingOperationInProgress_bl || !lPreviousPendingOperationProgressStatusDefined_bl)
			)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.PENDING_OPERATION_STATUS_UPDATED, {inProgress: this.info.isPendingOperationInProgress});
		}

		this.__turnOperationStatusTrackingOff();
	}

	__turnOperationStatusTrackingOn(aIsSyncFromLobby_bl=false)
	{
		let lPreviousIsOperationStatusTrackingOn_bl = this.info.isOperationStatusTrackingOn;

		super.__turnOperationStatusTrackingOn();

		if (!aIsSyncFromLobby_bl && this.info.isOperationStatusTrackingOn && !lPreviousIsOperationStatusTrackingOn_bl)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.PENDING_OPERATION_TRACKING_STATUS_UPDATED, {inProgress: this.info.isOperationStatusTrackingOn});
		}
	}

	__turnOperationStatusTrackingOff(aIsSyncFromLobby_bl=false)
	{
		let lPreviousIsOperationStatusTrackingOn_bl = this.info.isOperationStatusTrackingOn;

		super.__turnOperationStatusTrackingOff();

		if (!aIsSyncFromLobby_bl && !this.info.isOperationStatusTrackingOn && !!lPreviousIsOperationStatusTrackingOn_bl)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.PENDING_OPERATION_TRACKING_STATUS_UPDATED, {inProgress: this.info.isOperationStatusTrackingOn});
		}
	}

	__resetPendingOperation()
	{
		super.__resetPendingOperation();

		this.__turnOperationStatusTrackingOff();

		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.PENDING_OPERATION_STATUS_UPDATED, {inProgress: this.info.isPendingOperationInProgress});
	}
}

export default GUSGamePendingOperationController;