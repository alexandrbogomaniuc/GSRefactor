import SimpleController from '../../../unified/controller/base/SimpleController';
import GUPendingOperationInfo from '../../model/gameplay/GUPendingOperationInfo';
import { APP } from '../../../unified/controller/main/globals';
import Timer from '../../../unified/controller/time/Timer';
import WebSocketInteractionController from '../../../unified/controller/interaction/server/WebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../unified/model/interaction/server/WebSocketInteractionInfo';

const PENDING_OPERATION_REFRESH_INTERVAL = 500;

class GUPendingOperationController extends SimpleController
{
	static get EVENT_ON_PENDING_OPERATION_STARTTED() 				{return "EVENT_ON_PENDING_OPERATION_STARTTED";}
	static get EVENT_ON_PENDING_OPERATION_COMPLETED() 				{return "EVENT_ON_PENDING_OPERATION_COMPLETED";}
	static get EVENT_ON_PENDING_OPERATION_RESET() 					{return "EVENT_ON_PENDING_OPERATION_RESET";}
	static get EVENT_ON_FATAL_PENDING_OPERATION_OCCURED()			{return "EVENT_ON_FATAL_PENDING_OPERATION_OCCURED";}
	static get EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED()	{return "EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED";}
	static get EVENT_ON_PENDING_OPERATION_STATUS_TRACKING_TURNED_ON()	{return "EVENT_ON_PENDING_OPERATION_STATUS_TRACKING_TURNED_ON";}
	static get EVENT_ON_PENDING_OPERATION_STATUS_TRACKING_TURNED_OFF()	{return "EVENT_ON_PENDING_OPERATION_STATUS_TRACKING_TURNED_OFF";}

	constructor(aOptInfo_poi)
	{
		super(aOptInfo_poi || new GUPendingOperationInfo);

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
			this.__startHandleServerMessages();
		}
	}

	__startHandleServerMessages()
	{
		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(WebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(WebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onServerConnectionOpened, this);
		wsInteractionController.on(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.PENDING_OPERATION_STATUS:
				this._destroyPendingOperationStatusCheckTimer();

				if (data.pending === true && !this.info.isPendingOperationInProgress)
				{
					this._startPendingOperation();

					this.__turnOperationStatusTrackingOn();
				}
				else if (
							(this.info.isPendingOperationInProgress || !this.info.isPendingOperationProgressStatusDefined)
							&& data.pending !== undefined
							&& !data.pending
						)
				{
					this._completePendingOperation();
				}
				else
				{
					if (this.info.isOperationStatusTrackingOn)
					{
						this._startPendingOperationStatusCheckTimer();
					}
				}
				break;
		}
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		if (serverData.code === WebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION)
		{
			this._startPendingOperation();
		}
	}

	_onServerConnectionOpened(event)
	{
	}

	_onServerConnectionClosed(event)
	{
	}

	_startPendingOperation()
	{
		if (this.info.isPendingOperationProgressStatusDefined && this.info.isPendingOperationInProgress)
		{
			return;
		}

		this.info.isPendingOperationInProgress = true;

		this.emit(GUPendingOperationController.EVENT_ON_PENDING_OPERATION_STARTTED);

		if (this.info.isOperationStatusTrackingOn)
		{
			this._startPendingOperationStatusCheckTimer();
		}
	}

	_completePendingOperation()
	{
		if (this.info.isPendingOperationProgressStatusDefined && !this.info.isPendingOperationInProgress)
		{
			return;
		}
		
		this.info.isPendingOperationInProgress = false;

		this._destroyPendingOperationStatusCheckTimer();

		this.emit(GUPendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED);
	}

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

	_requestPendingOperationStatus()
	{
		this._destroyPendingOperationStatusCheckTimer();

		this.emit(GUPendingOperationController.EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED);
	}

	__turnOperationStatusTrackingOn()
	{
		if (this.info.isOperationStatusTrackingOn)
		{
			return;
		}

		this.info.isOperationStatusTrackingOn = true;

		this._startPendingOperationStatusCheckTimer();

		this.emit(GUPendingOperationController.EVENT_ON_PENDING_OPERATION_STATUS_TRACKING_TURNED_ON);
	}

	__turnOperationStatusTrackingOff()
	{
		if (!this.info.isOperationStatusTrackingOn)
		{
			return;
		}

		this.info.isOperationStatusTrackingOn = false;

		this._destroyPendingOperationStatusCheckTimer();

		this.emit(GUPendingOperationController.EVENT_ON_PENDING_OPERATION_STATUS_TRACKING_TURNED_OFF);
	}

	__resetPendingOperationIfPossible()
	{
		if (this.info.isPendingOperationProgressStatusDefined && !this.info.isFatalPendingOperation)
		{
			this.__resetPendingOperation();
		}
	}

	__resetPendingOperation()
	{
		this.info.resetPendingOperationStatus();

		this._destroyPendingOperationStatusCheckTimer();

		this.emit(GUPendingOperationController.EVENT_ON_PENDING_OPERATION_RESET);
	}
}

export default GUPendingOperationController;