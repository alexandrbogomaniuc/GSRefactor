import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUPseudoGameWebSocketInteractionController from '../../../../interaction/server/GUSPseudoGameWebSocketInteractionController';
import GUSLobbyTournamentModeController from '../../../../custom/tournament/GUSLobbyTournamentModeController';

class GUCriticalErrorDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeController_tmc = null;
		this._fTournamentModeInfo_tmi = null;

		this._initCriticalErrorDialogController();
	}

	_initCriticalErrorDialogController()
	{	
	}

	__init()
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

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		let pseudoGamewebSocketInteractionController = APP.pseudoGamewebSocketInteractionController;
		pseudoGamewebSocketInteractionController.on(GUPseudoGameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		this._fTournamentModeController_tmc = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = this._fTournamentModeController_tmc.info;

		this._fTournamentModeController_tmc.on(GUSLobbyTournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);
	}

	//VALIDATION...
	__validateModelLevel()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;

			let messageId = "TADialogMessageCriticalErrorInternal";
			switch (this.info.errorCode)
			{
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.INTERNAL_ERROR:
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.BAD_BUYIN:
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.BAD_STAKE:
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.AVATAR_PART_NOT_AVAILABLE:
					messageId = "TADialogMessageCriticalErrorInternal";
					break;
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
					messageId = "TADialogMessageCriticalErrorServerShutdown";
					break;
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.INVALID_SESSION:
					messageId = "TADialogMessageCriticalErrorInvalidSession";
					break;
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.DEPRECATED_REQUEST:
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.BAD_REQUEST:
					messageId = "TADialogMessageCriticalErrorDeprecatedRequest";
					break;
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION:
					messageId = "TADialogMessageCriticalErrorPendingTransaction";
					break;
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.CONFLICTING_LOBBY_SESSION:
					messageId = "TADialogMessageCriticalErrorConflictingLobbySession";
					break;
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.OPERATION_FAILED:
					messageId = "TADialogMessageCriticalErrorOperationFailed";
					break;
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.UNKNOWN_TRANSACTION_ID:
					messageId = "TADialogMessageCriticalErrorUnknownTransactionId";
					break;
				case GUSLobbyWebSocketInteractionController.ERROR_CODES.EXPIRED_WEBSITE_SESSION:
					messageId = "TADialogMessageCriticalErrorExpiredWebsiteSession";
					break;
				default:
					if (GUSLobbyWebSocketInteractionController.isUnknownWalletError(this.info.errorCode)) //Workaround: consider all unknown Wallet error codes as an error BAD_BUYIN: 1010. Until the task is completed https://jira.dgphoenix.com/browse/DI-94
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

	__activateDialog()
	{
		super.__activateDialog();
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;

		if (GUSLobbyWebSocketInteractionController.isFatalError(event.errorType))
		{
			this.info.errorCode = serverData.code;
			this.info.errorTime = serverData.date;
			this.info.rid = serverData.rid;

			this.__activateDialog();
		}
	}

	_onTournamentModeStateChanged()
	{
		if (!this._fTournamentModeInfo_tmi.isTournamentMode)
		{
			return;
		}

		if (this._fTournamentModeInfo_tmi.isTournamentReady)
		{
			this.info.errorTime = this._fTournamentModeInfo_tmi.lastStateUpdateTime;
			this.info.rid = this._fTournamentModeInfo_tmi.lastStateUpdateRID;

			this.__activateDialog();
		}
	}

	__onDialogCustomButtonClicked(event)
	{
		this.view.copyToClibboard();

		super.__onDialogCustomButtonClicked(event);
	}
}

export default GUCriticalErrorDialogController