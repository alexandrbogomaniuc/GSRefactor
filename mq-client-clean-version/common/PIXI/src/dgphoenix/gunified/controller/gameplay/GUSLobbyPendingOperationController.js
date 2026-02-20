import GUPendingOperationController from './GUPendingOperationController';
import GUSLobbyPendingOperationInfo from '../../model/gameplay/GUSLobbyPendingOperationInfo';
import { APP } from '../../../unified/controller/main/globals';
import GUSLobbyExternalCommunicator from '../external/GUSLobbyExternalCommunicator';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../external/GUSExternalCommunicator';
import { SERVER_MESSAGES } from '../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';
import WebSocketInteractionController from '../../../unified/controller/interaction/server/WebSocketInteractionController';
import GUSPseudoGameWebSocketInteractionController from '../interaction/server/GUSPseudoGameWebSocketInteractionController';
import GUDialogController from '../uis/custom/dialogs/GUDialogController';

class GUSLobbyPendingOperationController extends GUPendingOperationController
{
	static get EVENT_ON_ROOM_PENDING_OPERATION_STATUS_UPDATED() 				{return "EVENT_ON_ROOM_PENDING_OPERATION_STATUS_UPDATED";}

	constructor(aOptInfo_poi)
	{
		super(aOptInfo_poi || new GUSLobbyPendingOperationInfo);
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		this.info.isPendingOperationHandlingSupported = true;

		this._fIsRoomConnectionOpened_bl = false;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (this.info.isPendingOperationHandlingSupported)
		{
			APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameExternalMessageReceived, this);
			APP.pseudoGamewebSocketInteractionController.on(GUSPseudoGameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onPseudoGameServerErrorMessage, this);

			let forceSitOutDialogController = APP.dialogsController.forceSitOutDialogController;
			forceSitOutDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onForceSitOutDialogControllerDialogRequestConfirmed, this);
		}
	}

	_onGameExternalMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.PENDING_OPERATION_STATUS_UPDATED:
				this.info.isRoomPendingOperationInProgress = event.data.inProgress;
				this.emit(GUSLobbyPendingOperationController.EVENT_ON_ROOM_PENDING_OPERATION_STATUS_UPDATED);

				if (this.info.isRoomPendingOperationProgressStatusDefined)
				{
					if (this.info.isRoomPendingOperationInProgress)
					{
						this.info.isRoomPendingOperationInProgress = true;
						this._startPendingOperation(true);
					}
					else
					{
						this.info.isRoomPendingOperationInProgress = false;
						this._completePendingOperation(true);
					}
				}
				break;

			case GAME_MESSAGES.PENDING_OPERATION_TRACKING_STATUS_UPDATED:
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
			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
				this._fIsRoomConnectionOpened_bl = true;
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
				this._fIsRoomConnectionOpened_bl = false;
				break;
		}
	}

	_onServerMessage(event)
	{	
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.ENTER_LOBBY_RESPONSE:
				if (!this.info.isPendingOperationProgressStatusDefined)
				{
					this._requestPendingOperationStatus();
				}
				break;

			default:
				super._onServerMessage(event);
				break;
		}
	}

	_onPseudoGameServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		if (serverData.code === WebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION)
		{
			this._startPendingOperation();

			this.__turnOperationStatusTrackingOn();
		}
	}

	_onServerConnectionOpened(event)
	{
		if (
				this.info.isPendingOperationInProgress && this.info.isOperationStatusTrackingOn
				|| !this._fIsRoomConnectionOpened_bl
			)
		{
			this.__resetPendingOperationIfPossible();
		}
	}

	_onServerConnectionClosed(event)
	{
		if (
				this.info.isPendingOperationInProgress && this.info.isOperationStatusTrackingOn
				|| !this._fIsRoomConnectionOpened_bl
			)
		{
			this.__resetPendingOperationIfPossible();
		}
	}

	_onForceSitOutDialogControllerDialogRequestConfirmed()
	{
		if (!APP.lobbyScreen.resitOutInProcess)
		{
			this.__resetPendingOperationIfPossible();

			if (!this.info.isPendingOperationProgressStatusDefined)
			{
				this._requestPendingOperationStatus();
			}
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
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.PENDING_OPERATION_STATUS_UPDATED, {inProgress: this.info.isPendingOperationInProgress});
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
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.PENDING_OPERATION_STATUS_UPDATED, {inProgress: this.info.isPendingOperationInProgress});
		}

		this.__turnOperationStatusTrackingOff();
	}

	__turnOperationStatusTrackingOn(aIsSyncFromLobby_bl=false)
	{
		let lPreviousIsOperationStatusTrackingOn_bl = this.info.isOperationStatusTrackingOn;

		super.__turnOperationStatusTrackingOn();

		if (!aIsSyncFromLobby_bl && this.info.isOperationStatusTrackingOn && !lPreviousIsOperationStatusTrackingOn_bl)
		{
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.PENDING_OPERATION_TRACKING_STATUS_UPDATED, {inProgress: this.info.isOperationStatusTrackingOn});
		}
	}

	__turnOperationStatusTrackingOff(aIsSyncFromLobby_bl=false)
	{
		let lPreviousIsOperationStatusTrackingOn_bl = this.info.isOperationStatusTrackingOn;

		super.__turnOperationStatusTrackingOff();

		if (!aIsSyncFromLobby_bl && !this.info.isOperationStatusTrackingOn && !!lPreviousIsOperationStatusTrackingOn_bl)
		{
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.PENDING_OPERATION_TRACKING_STATUS_UPDATED, {inProgress: this.info.isOperationStatusTrackingOn});
		}
	}

	__resetPendingOperation()
	{
		super.__resetPendingOperation();

		this.__turnOperationStatusTrackingOff();
	}
}

export default GUSLobbyPendingOperationController;