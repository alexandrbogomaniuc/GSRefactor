import GameBaseDialogController from './GameBaseDialogController';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyExternalCommunicator from '../../../../../../external/LobbyExternalCommunicator';
import {GAME_MESSAGES} from '../../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import GameLauncher from '../../../../../../main/GameLauncher';
import LobbyAPP from '../../../../../../LobbyAPP';

class GameCriticalErrorDialogController extends GameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED () { return GameBaseDialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return GameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGameCriticalErrorDialogController();
	}

	_initGameCriticalErrorDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameCriticalErrorDialogView;
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		if (APP.appParamsInfo.timerOffset)
		{
			this.info.timeOffset = APP.appParamsInfo.timerOffset;
		}
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
			APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}
		
	}

	_onLobbyStarted(event)
	{
		this._startHandleGameErrors();
	}

	_startHandleGameErrors()
	{
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
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

			let messageId = "TADialogMessageCriticalErrorInternal";
			switch (this.info.errorCode)
			{
				case LobbyWebSocketInteractionController.ERROR_CODES.INTERNAL_ERROR:
				case LobbyWebSocketInteractionController.ERROR_CODES.BAD_BUYIN:
				case LobbyWebSocketInteractionController.ERROR_CODES.BAD_STAKE:
				case LobbyWebSocketInteractionController.ERROR_CODES.AVATAR_PART_NOT_AVAILABLE:
					messageId = "TADialogMessageCriticalErrorInternal";
					break;
				case LobbyWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
					messageId = "TADialogMessageCriticalErrorServerShutdown";
					break;
				case LobbyWebSocketInteractionController.ERROR_CODES.INVALID_SESSION:
					if(APP.tickerResumedDif > 300000)
					{
						messageId = "TADialogMessageCriticalErrorIdleSession";
					}else{
						messageId = "TADialogMessageCriticalErrorInvalidSession";
					}
					break;
				case LobbyWebSocketInteractionController.ERROR_CODES.DEPRECATED_REQUEST:
				case LobbyWebSocketInteractionController.ERROR_CODES.BAD_REQUEST:
					messageId = "TADialogMessageCriticalErrorDeprecatedRequest";
					break;
				case LobbyWebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION:
					messageId = "TADialogMessageCriticalErrorPendingTransaction";
					break;
				case LobbyWebSocketInteractionController.ERROR_CODES.CONFLICTING_LOBBY_SESSION:
					messageId = "TADialogMessageCriticalErrorConflictingLobbySession";
					break;
				case LobbyWebSocketInteractionController.ERROR_CODES.OPERATION_FAILED:
					messageId = "TADialogMessageCriticalErrorOperationFailed";
					break;
				case LobbyWebSocketInteractionController.ERROR_CODES.UNKNOWN_TRANSACTION_ID:
					messageId = "TADialogMessageCriticalErrorUnknownTransactionId";
					break;
				case LobbyWebSocketInteractionController.ERROR_CODES.EXPIRED_WEBSITE_SESSION:
					messageId = "TADialogMessageCriticalErrorExpiredWebsiteSession";
					break;
				default:
					if (LobbyWebSocketInteractionController.isUnknownWalletError(this.info.errorCode)) //Workaround: consider all unknown Wallet error codes as an error BAD_BUYIN: 1010. Until the task is completed https://jira.dgphoenix.com/browse/DI-94
					{
						messageId = "TADialogMessageCriticalErrorInternal";
					}
					break;
			}

			let date = new Date();
			let timeZoneOffset = (info.timeOffset !== undefined ? info.timeOffset : -date.getTimezoneOffset()) * 60000;
			date.setTime(this.info.errorTime + timeZoneOffset);

			view.setMessage(messageId, APP.urlBasedParams.SID, date, info.errorTime, info.errorCode, info.rid);
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

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (
						LobbyWebSocketInteractionController.isFatalError(event.data.errorType)
						&& event.data.errorCode != LobbyWebSocketInteractionController.ERROR_CODES.ROOM_WAS_DEACTIVATED //ROOM_WAS_DEACTIVATED error code has its own dlg
					)
				{
					this.info.errorCode = event.data.errorCode;
					this.info.errorTime = event.data.errorTime;
					this.info.rid = event.data.rid;

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

export default GameCriticalErrorDialogController