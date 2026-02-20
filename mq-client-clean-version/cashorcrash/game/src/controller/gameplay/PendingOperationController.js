import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import PendingOperationInfo from '../../model/gameplay/PendingOperationInfo';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

const PENDING_OPERATION_REFRESH_INTERVAL = 500;

/**
 * Controls pending operation that can occur on server while processing bets or wins.
 * @class
 * @extends SimpleController
 * @inheritdoc
 */
class PendingOperationController extends SimpleController
{
	static get EVENT_ON_PENDING_OPERATION_STARTTED() 				{return "EVENT_ON_PENDING_OPERATION_STARTTED";}
	static get EVENT_ON_PENDING_OPERATION_COMPLETED() 				{return "EVENT_ON_PENDING_OPERATION_COMPLETED";}
	static get EVENT_ON_PENDING_OPERATION_RESET() 					{return "EVENT_ON_PENDING_OPERATION_RESET";}
	static get EVENT_ON_FATAL_PENDING_OPERATION_OCCURED()			{return "EVENT_ON_FATAL_PENDING_OPERATION_OCCURED";}
	static get EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED()	{return "EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED";}

	constructor(aOptInfo_poi)
	{
		super(aOptInfo_poi || new PendingOperationInfo);

		this._fRefreshPendingOperationStatusTimer_tmr = null;
	}

	init()
	{
		super.init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (this.info.isPendingOperationHandlingSupported)
		{
			let wsInteractionController = APP.webSocketInteractionController;
			wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
			wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
			wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onServerConnectionOpened, this);
			wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
		}
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.CRASH_GAME_INFO:
				if (data.pending === true)
				{
					this._startPendingOperation();
				}
				else if (this.info.isPendingOperationInProgress)
				{
					this._completePendingOperation();
				}
				break;
			case SERVER_MESSAGES.PENDING_OPERATION_STATUS:
				this._destroyPendingOperationStatusCheckTimer();
				
				if (this.info.isPendingOperationInProgress && data.pending !== undefined && !data.pending)
				{
					this._completePendingOperation();
				}
				else
				{
					this._startPendingOperationStatusCheckTimer();
				}
				break;
			case SERVER_MESSAGES.SIT_OUT_RESPONSE:
				if (this.info.isPendingOperationInProgress && data.pending === true)
				{
					this.info.isFatalPendingOperation = true;
					this.emit(PendingOperationController.EVENT_ON_FATAL_PENDING_OPERATION_OCCURED);
				}
				break;
		}
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		if (serverData.code === GameWebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION)
		{
			this._startPendingOperation();
		}
	}

	_onServerConnectionOpened(event)
	{
		this._resetPendingOperationIfPossible();
	}

	_onServerConnectionClosed(event)
	{
		this._resetPendingOperationIfPossible();
	}

	/**
	 * Start pending operation.
	 * @private
	 */
	_startPendingOperation()
	{
		if (this.info.isPendingOperationInProgress)
		{
			return;
		}

		this.info.isPendingOperationInProgress = true;

		this.emit(PendingOperationController.EVENT_ON_PENDING_OPERATION_STARTTED);

		this._startPendingOperationStatusCheckTimer();
	}

	/**
	 * Complete pending operation.
	 * @private
	 */
	_completePendingOperation()
	{
		if (!this.info.isPendingOperationInProgress)
		{
			return;
		}
		
		this.info.isPendingOperationInProgress = false;

		this._destroyPendingOperationStatusCheckTimer();

		this.emit(PendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED);
	}

	/**
	 * Start a timer upon completion of which a check of the status of the operation will be initiated
	 * @private
	 */
	_startPendingOperationStatusCheckTimer()
	{
		this._destroyPendingOperationStatusCheckTimer();
		
		let lFreq_num = PENDING_OPERATION_REFRESH_INTERVAL;
		this._fRefreshPendingOperationStatusTimer_tmr = new Timer(this._onRefreshPendingOperationStatusTimerCycleCompleted.bind(this), lFreq_num);
	}

	_onRefreshPendingOperationStatusTimerCycleCompleted()
	{
		this._destroyPendingOperationStatusCheckTimer();

		this._requestPendingOperationStatus();
	}

	_destroyPendingOperationStatusCheckTimer()
	{
		this._fRefreshPendingOperationStatusTimer_tmr && this._fRefreshPendingOperationStatusTimer_tmr.destructor();
		this._fRefreshPendingOperationStatusTimer_tmr = null;
	}

	/**
	 * Check pending operation status.
	 * @private
	 */
	_requestPendingOperationStatus()
	{
		this._destroyPendingOperationStatusCheckTimer();

		this.emit(PendingOperationController.EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED);
	}

	_resetPendingOperationIfPossible()
	{
		if (this.info.isPendingOperationInProgress && !this.info.isFatalPendingOperation)
		{
			this._resetPendingOperation();
		}
	}

	_resetPendingOperation()
	{
		this.info.isPendingOperationInProgress = false;

		this._destroyPendingOperationStatusCheckTimer();

		this.emit(PendingOperationController.EVENT_ON_PENDING_OPERATION_RESET);
	}
}

export default PendingOperationController;