import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../../../model/interaction/server/GameWebSocketInteractionInfo';

class CriticalErrorDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initCriticalErrorDialogController();
	}

	_initCriticalErrorDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().criticalErrorDialogView;
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		if (APP.appParamsInfo.timerOffset)
		{
			this.info.timeOffset = APP.appParamsInfo.timerOffset;
		}
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	//DEBUG...
	get debugMessages()
	{
		return ["TADialogMessageCriticalErrorInternal", "TADialogMessageCriticalErrorServerShutdown", "TADialogMessageCriticalErrorInvalidSession",
				"TADialogMessageCriticalErrorDeprecatedRequest", "TADialogMessageCriticalErrorPendingTransaction", 
				"TADialogMessageCriticalErrorConflictingLobbySession", "TADialogMessageCriticalErrorOperationFailed",
				"TADialogMessageCriticalErrorUnknownTransactionId", "TADialogMessageCriticalErrorExpiredWebsiteSession"];
	}
	//...DEBUG

	__validateViewLevel ()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;

			let messageId = "TADialogMessageCriticalErrorInternal";
			switch (this.info.errorCode)
			{
				case GameWebSocketInteractionController.ERROR_CODES.INTERNAL_ERROR:
				case GameWebSocketInteractionController.ERROR_CODES.BAD_BUYIN:
				case GameWebSocketInteractionController.ERROR_CODES.BAD_STAKE:
				case GameWebSocketInteractionController.ERROR_CODES.AVATAR_PART_NOT_AVAILABLE:
				case GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE:
					messageId = "TADialogMessageCriticalErrorInternal";
					break;
				case GameWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
					messageId = "TADialogMessageCriticalErrorServerShutdown";
					break;
				case GameWebSocketInteractionController.ERROR_CODES.INVALID_SESSION:
					messageId = "TADialogMessageCriticalErrorInvalidSession";
					break;
				case GameWebSocketInteractionController.ERROR_CODES.DEPRECATED_REQUEST:
				case GameWebSocketInteractionController.ERROR_CODES.BAD_REQUEST:
					messageId = "TADialogMessageCriticalErrorDeprecatedRequest";
					break;
				case GameWebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION:
					messageId = "TADialogMessageCriticalErrorPendingTransaction";
					break;
				case GameWebSocketInteractionController.ERROR_CODES.CONFLICTING_LOBBY_SESSION:
					messageId = "TADialogMessageCriticalErrorConflictingLobbySession";
					break;
				case GameWebSocketInteractionController.ERROR_CODES.OPERATION_FAILED:
					messageId = "TADialogMessageCriticalErrorOperationFailed";
					break;
				case GameWebSocketInteractionController.ERROR_CODES.UNKNOWN_TRANSACTION_ID:
					messageId = "TADialogMessageCriticalErrorUnknownTransactionId";
					break;
				case GameWebSocketInteractionController.ERROR_CODES.EXPIRED_WEBSITE_SESSION:
					messageId = "TADialogMessageCriticalErrorExpiredWebsiteSession";
					break;
				default:
					if (GameWebSocketInteractionController.isUnknownWalletError(this.info.errorCode)) //Workaround: consider all unknown Wallet error codes as an error BAD_BUYIN: 1010. Until the task is completed https://jira.dgphoenix.com/browse/DI-94
					{
						messageId = "TADialogMessageCriticalErrorInternal";
					}
					break;
			}

			//DEBUG...
			if (this.curDebugMessage !== undefined)
			{
				messageId = this.curDebugMessage;
			}
			//...DEBUG

			let date = new Date();
			let timeZoneOffset = (info.timeOffset !== undefined ? info.timeOffset : -date.getTimezoneOffset()) * 60000;
			date.setTime(this.info.errorTime + timeZoneOffset);

			view.setMessage(messageId, APP.urlBasedParams.SID, date, info.errorCode, info.rid);
			view.setCustomMode();

			if (APP.appParamsInfo.closeErrorFuncNameDefined)
			{
				view.customButton.setOKCaption();
			}
			else
			{
				view.customButton.setCopyCaption();
			}
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__activateDialog ()
	{
		super.__activateDialog();
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;

		if (GameWebSocketInteractionController.isFatalError(event.errorType))
		{
			this.info.errorCode = serverData.code;
			this.info.errorTime = serverData.date;
			this.info.rid = serverData.rid;
			
			this.__activateDialog();
		}
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		let requestData = event.requestData;

		switch(data.class)
		{
			case SERVER_MESSAGES.ENTER_GAME_RESPONSE:
				if (data.minStake === undefined || data.maxStake === undefined)
				{
					this.info.errorTime = data.date;
					this.info.rid = data.rid;

					this.__activateDialog();
				}
				break;

			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE:
				if (
						APP.webSocketInteractionController.isFatalError(data.errorCode)
						|| data.errorCode === GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE
					)
				{
					this.info.errorCode = data.errorCode;
					this.info.errorTime = data.date;
					this.info.rid = data.rid;

					this.__activateDialog();
				}
				break;
		}
	}

	__onDialogCustomButtonClicked(event)
	{
		this.view.copyToClibboard();

		super.__onDialogCustomButtonClicked(event);
	}
}

export default CriticalErrorDialogController