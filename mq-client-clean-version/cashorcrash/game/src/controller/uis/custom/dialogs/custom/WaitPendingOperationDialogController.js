import DialogController from '../DialogController';
import PendingOperationController from '../../../../gameplay/PendingOperationController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';

class WaitPendingOperationDialogController extends DialogController
{
	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initWaitPendingOperationDialogController();
	}

	_initWaitPendingOperationDialogController()
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

		let lPendingOperationController_poc = this._fPendingOperationController_poc = APP.gameController.gameplayController.pendingOperationController;
		lPendingOperationController_poc.on(PendingOperationController.EVENT_ON_PENDING_OPERATION_STARTTED, this._onPendingOperationStarted, this);
		lPendingOperationController_poc.on(PendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);
		lPendingOperationController_poc.on(PendingOperationController.EVENT_ON_PENDING_OPERATION_RESET, this._onPendingOperationReset, this);
		lPendingOperationController_poc.on(PendingOperationController.EVENT_ON_FATAL_PENDING_OPERATION_OCCURED, this._onFatalPendingOperationOccured, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
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

			let lMsg_str = this._fPendingOperationController_poc.info.isFatalPendingOperation ? "TADialogMessageCriticalErrorPendingTransaction" : "TADialogMessageWaitPendingOperation";
			
			view.setMessage(lMsg_str);
			view.setEmptyMode();
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

	_onPendingOperationReset(aEvent_evt)
	{
		this.__deactivateDialog();
	}

	_onGameServerConnectionClosed(aEvent_evt)
	{
		this.__deactivateDialog();
	}

	_onFatalPendingOperationOccured(aEvent_evt)
	{
		this.__activateDialog();
	}

	__activateDialog(aEvent_evt)
	{
		super.__activateDialog(aEvent_evt);
		APP.logger.i_pushWarning(`WaitPendingOperationDialogController. Dialog Activated. Event - ${aEvent_evt}`);
	}
}

export default WaitPendingOperationDialogController