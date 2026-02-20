import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyPendingOperationController from '../../../../gameplay/GUSLobbyPendingOperationController';
import { SERVER_MESSAGES } from '../../../../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';

class GUSWaitPendingOperationDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED () { return GUDialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initGUSWaitPendingOperationDialogController();
	}

	_initGUSWaitPendingOperationDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().waitPendingOperationDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let lPendingOperationController_poc = this._fPendingOperationController_poc = APP.pendingOperationController;
		this._fPendingOperationInfo_poi = this._fPendingOperationController_poc.info;

		if (lPendingOperationController_poc.info.isPendingOperationHandlingSupported)
		{
			lPendingOperationController_poc.on(GUSLobbyPendingOperationController.EVENT_ON_PENDING_OPERATION_STARTTED, this._onPendingOperationStarted, this);
			lPendingOperationController_poc.on(GUSLobbyPendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);
			lPendingOperationController_poc.on(GUSLobbyPendingOperationController.EVENT_ON_PENDING_OPERATION_RESET, this._onPendingOperationStatusReset, this);
			lPendingOperationController_poc.on(GUSLobbyPendingOperationController.EVENT_ON_ROOM_PENDING_OPERATION_STATUS_UPDATED, this._onRoomPendingOperationStatusUpdated, this);
		}
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

			view.setMessage("TADialogMessageWaitPendingOperation");
			view.setEmptyMode();

			if (
					!this._fPendingOperationInfo_poi.isPendingOperationProgressStatusDefined
					|| (!this._fPendingOperationInfo_poi.isPendingOperationInProgress && !this._fPendingOperationInfo_poi.isRoomPendingOperationProgressStatusDefined)
				)
			{
				this.view.hideMessage();
			}
			else
			{
				this.view.showMessage();
			}
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onPendingOperationStarted(aEvent_evt)
	{
		this.__activateDialog();
	}

	_onPendingOperationCompleted(aEvent_evt)
	{
		this.__deactivateDialog();
	}

	_onPendingOperationStatusReset()
	{
		this.__activateDialog();
	}

	_onRoomPendingOperationStatusUpdated()
	{
		if (this._fPendingOperationInfo_poi.isRoomPendingOperationProgressStatusDefined)
		{
			if (this._fPendingOperationInfo_poi.isRoomPendingOperationInProgress)
			{
				this.__activateDialog();
			}
			else
			{
				this.__deactivateDialog();
			}
		}
		else
		{
			if (APP.gameLauncher.isGamePreloaderReady && !APP.gameLauncher.isGameLoadingInProgress)
			{
				this.__activateDialog();
			}
		}
	}

	onGameMessageReceived(event)
	{
		let msgType = event.type;
		let msgData = event.data;

		switch (msgType)
		{
			case GAME_MESSAGES.GAME_STARTED:
				if (!this._fPendingOperationInfo_poi.isRoomPendingOperationProgressStatusDefined)
				{
					this.__activateDialog();
				}
				break;
		}
	}
}

export default GUSWaitPendingOperationDialogController