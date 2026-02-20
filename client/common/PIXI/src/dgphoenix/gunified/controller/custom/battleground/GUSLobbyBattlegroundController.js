import SimpleController from '../../../../unified/controller/base/SimpleController';
import GUSLobbyBattlegroundInfo, { BATTLEGROUND_ROOM_STATE } from '../../../model/custom/battleground/GUSLobbyBattlegroundInfo';
import { APP } from '../../../../unified/controller/main/globals';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from '../../external/GUSLobbyExternalCommunicator';
import GUSLobbyApplication from '../../main/GUSLobbyApplication';
import GUSLobbyDialogsController from '../../uis/custom/dialogs/GUSLobbyDialogsController';
import GUSLobbyBattlegroundNotEnoughPlayersDialogController from '../../uis/custom/dialogs/custom/GUSLobbyBattlegroundNotEnoughPlayersDialogController';
import GUSBattlegroundBuyInConfirmationDialogController from '../../uis/custom/dialogs/custom/GUSBattlegroundBuyInConfirmationDialogController';
import GUSLobbyPaytableScreenController from '../../uis/custom/secondary/paytable/GUSLobbyPaytableScreenController';
import GUSettingsScreenController from '../../uis/custom/secondary/settings/GUSettingsScreenController';
import GUSLobbyScreen from '../../../view/main/GUSLobbyScreen';
import GUSLobbyWebSocketInteractionController from '../../interaction/server/GUSLobbyWebSocketInteractionController';
import GURoundAlreadyFinishedDialogController from '../../uis/custom/dialogs/custom/GURoundAlreadyFinishedDialogController';
import GUSGameBattlegroundContinueReadingDialogController from '../../uis/custom/dialogs/custom/game/GUSGameBattlegroundContinueReadingDialogController';
import GUSGameBattlegroundNoWeaponsFiredDialogController from '../../uis/custom/dialogs/custom/game/GUSGameBattlegroundNoWeaponsFiredDialogController';

class GUSLobbyBattlegroundController extends SimpleController
{
	static get EVENT_BATTLEGROUND_ROOM_ID_RECEIVED() 							{ return 'EVENT_BATTLEGROUND_ROOM_ID_RECEIVED'; }
	static get EVENT_BATTLEGROUND_WAITING_TIME_IS_OUT()	 						{ return 'EVENT_BATTLEGROUND_WAITING_TIME_IS_OUT'; }
	static get EVENT_BATTLEGROUND_BUY_IN_ALLREADY_DONE_IN_TOURNAMENT_LOBBY()	{ return 'EVENT_BATTLEGROUND_BUY_IN_ALLREADY_DONE_IN_TOURNAMENT_LOBBY'; }
	static get EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL()					{ return 'EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL'; }
	static get EVENT_BATTLEGROUND_NEED_UPDATE_MODE_ON_GAME_LEVEL()				{ return 'EVENT_BATTLEGROUND_NEED_UPDATE_MODE_ON_GAME_LEVEL'; }
	static get EVENT_BATTLEGROUND_TIME_TO_START_UPDATED()						{ return 'EVENT_BATTLEGROUND_TIME_TO_START_UPDATED'; }
	static get EVENT_BATTLEGROUND_COUNTDOWN_VISIBILITY_CHANGED()				{ return 'EVENT_BATTLEGROUND_COUNTDOWN_VISIBILITY_CHANGED'; }
	static get EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_REOPENED()					{ return 'EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_REOPENED'; }
	static get EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CLOSED()					{ return 'EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CLOSED'; }
	static get EVENT_BATTLEGROUND_BUY_IN_CLOSED()								{ return 'EVENT_BATTLEGROUND_BUY_IN_CLOSED'; }
	static get EVENT_BATTLEGROUND_BUY_IN_REOPENED()								{ return 'EVENT_BATTLEGROUND_BUY_IN_REOPENED'; }
	static get EVENT_BATTLEGROUND_NO_WEAPONS_FIRED_CLOSED()						{ return 'EVENT_BATTLEGROUND_NO_WEAPONS_FIRED_CLOSED'; }
	static get EVENT_BATTLEGROUND_NO_WEAPONS_FIRED_REOPENED()					{ return 'EVENT_BATTLEGROUND_NO_WEAPONS_FIRED_REOPENED'; }

	isRoundResultWasActivated()
	{
		return this.info.isRoundResultWasActivated;
	}

	isSecondaryScreenTimerRequired()
	{
		return APP.isBattlegroundGame
			&& !this.info.isConfirmBuyinDialogRequired
			&& !this.info.isNotEnoughPlayersDialogRequired
			&& !this.isRoundResultWasActivated()
			&& this.info.getTimeToStartInMillis() > 0;
	}

	constructor(aOptInfo_si)
	{
		super(aOptInfo_si || new GUSLobbyBattlegroundInfo());

		if (APP.isBattlegroundGame)
		{
			this._startServerMessagesListening();
		}
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (APP.isBattlegroundGame)
		{
			this._startMessagesListening();
		}
	}

	_startMessagesListening()
	{
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyAppStarted, this);
		APP.dialogsController.on(GUSLobbyDialogsController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING, this._onBattlegroundNotEnoughPlayersContinueWaiting, this)

		let l_dsc = APP.dialogsController;
		l_dsc.gameGameBattlegroundNoWeaponsFiredDialogController.on(GUSGameBattlegroundNoWeaponsFiredDialogController.EVENT_DIALOG_ACTIVATED, this._onNoWeaponsFiredDialogActivated, this);
		l_dsc.gameGameBattlegroundNoWeaponsFiredDialogController.on(GUSGameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED, this._onNoWeaponsFiredDialogOkClicked, this);

		let l_lbnepdc = l_dsc.lobbyBattlegroundNotEnoughPlayersDialogController;
		l_lbnepdc.on(GUSLobbyBattlegroundNotEnoughPlayersDialogController.EVENT_DIALOG_ACTIVATED, this._onLobbyBattlegroundNotEnoughPlayersDialogActivated, this);
		l_lbnepdc.on(GUSLobbyBattlegroundNotEnoughPlayersDialogController.EVENT_DIALOG_DEACTIVATED, this._onLobbyBattlegroundNotEnoughPlayersDialogDeactivated, this);

		let l_bbicdc = l_dsc.battlegroundBuyInConfirmationDialogController;
		l_bbicdc.on(GUSBattlegroundBuyInConfirmationDialogController.EVENT_DIALOG_ACTIVATED, this._onBattlegroundBuyInConfirmationDialogActivated, this);
		l_bbicdc.on(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS, this._onBattlegroundBuyInConfirmationDialogNotRequired, this);

		let l_gbcrdc = APP.dialogsController.gameBattlegroundContinueReadingDialogController;
		l_gbcrdc.on(GUSGameBattlegroundContinueReadingDialogController.EVENT_BATTLEGROUND_CONTINUE_READING_OK_CLICKED, this._onBattlegroundBuyInConfirmationDialogActivated, this);

		let l_rafdc = l_dsc.roundAlreadyFinishedDialogController;
		l_rafdc.on(GURoundAlreadyFinishedDialogController.EVENT_DIALOG_ACTIVATED, this._onBattlegroundRoundAlreadyFinishedDialogActivated, this);
		
		let l_psc = APP.secondaryScreenController.paytableScreenController;
		l_psc.on(GUSLobbyPaytableScreenController.EVENT_ON_SCREEN_SHOW, this._onCloseDialogsIfRequired, this);
		l_psc.on(GUSLobbyPaytableScreenController.EVENT_ON_SCREEN_HIDE, this._onReopenDialogsIfRequired, this);

		let l_ssc = APP.secondaryScreenController.settingsScreenController;
		l_ssc.on(GUSettingsScreenController.EVENT_SCREEN_ACTIVATED, this._onCloseDialogsIfRequired, this);
		l_ssc.on(GUSettingsScreenController.EVENT_SCREEN_DEACTIVATED, this._onReopenDialogsIfRequired, this);
	}

	_onLobbyAppStarted(event)
	{
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_BATTLEGROUND_START_URL_UPDATED, this._onBattlegroundStartUrlUpdated, this);
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_LAUNCH_GAME_CLICKED, this._onGameLaunchClicked, this);
	}

	_startServerMessagesListening()
	{
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlBattlegroundResponseMessage, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlBattlegroundResponseMessage, this);
	}

	destroy()
	{
		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE, this._onServerBonusStatusChangedMessage, this);
		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlBattlegroundResponseMessage, this);
		super.destroy();
	}

	_onNoWeaponsFiredDialogActivated()
	{
		this.info.isNoWeaponsFiredDialogRequired = true;
	}

	_onNoWeaponsFiredDialogOkClicked()
	{
		this.info.isNoWeaponsFiredDialogRequired = false;
	}

	_onLobbyBattlegroundNotEnoughPlayersDialogActivated()
	{
		if (this.info.isConfirmBuyinDialogRequired)
		{
			this.info.isConfirmBuyinDialogRequired = false;
		}

		this.info.isLobbyBattlegroundNotEnoughPlayersDialogActivated = true;
		this.info.isNotEnoughPlayersDialogRequired = true;
	}

	_onLobbyBattlegroundNotEnoughPlayersDialogDeactivated()
	{
		this.info.isLobbyBattlegroundNotEnoughPlayersDialogActivated = false;
	}

	_onBattlegroundBuyInConfirmationDialogActivated()
	{
		if (this.info.isNotEnoughPlayersDialogRequired)
		{
			this.info.isNotEnoughPlayersDialogRequired = false;
		}

		this.info.isConfirmBuyinDialogRequired = true;
	}

	_onBattlegroundBuyInConfirmationDialogNotRequired()
	{
		this.info.isConfirmBuyinDialogRequired = false;
	}

	_onBattlegroundRoundAlreadyFinishedDialogActivated()
	{
		this.info.battlegroundRoundAlreadyFinishedDialogActivated = true;
	}

	_onReopenDialogsIfRequired()
	{
		if(this.info.isNotEnoughPlayersDialogRequired)
		{
			this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_REOPENED);
		}
		else if(this.info.isConfirmBuyinDialogRequired)
		{
			this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_BUY_IN_REOPENED);
		}
		else if(this.info.isNoWeaponsFiredDialogRequired)
		{
			this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NO_WEAPONS_FIRED_REOPENED);
		}
	}

	_onCloseDialogsIfRequired()
	{
		if(this.info.isNotEnoughPlayersDialogRequired)
		{
			this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CLOSED);
		}
		else if(this.info.isConfirmBuyinDialogRequired)
		{
			this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_BUY_IN_CLOSED);
		}
		else if(this.info.isNoWeaponsFiredDialogRequired)
		{
			this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NO_WEAPONS_FIRED_CLOSED);
		}
	}

	_onGameLaunchClicked()
	{
		this.info.isBattlegroundGameStarted = false;
		this._needUpdateBattlegroundModeOnGameLevel();
	}

	_onBattlegroundStartUrlUpdated()
	{
		this.info.isGameStartUrlOnContinueWaitingUpdateExpected = false;
		this.info.isGameStartUrlOnPlayAgainUpdateExpected = false;
	}

	_onBattlegroundNotEnoughPlayersContinueWaiting()
	{
		let l_bi = this.info;
		l_bi.isNotEnoughPlayersDialogRequired = false;
		l_bi.isGameStartUrlOnContinueWaitingUpdateExpected = true;
		l_bi.setTimeToStart(undefined);
		this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL, { buyIn: l_bi.getConfirmedBuyInCost() || l_bi.getSelectedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn});
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_CONTINUE_WAITING);
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		let l_bi = this.info;

		switch (msgType)
		{
			case GAME_MESSAGES.PRELOADER_READY:
				this._needUpdateBattlegroundModeOnGameLevel();
				break;
			case GAME_MESSAGES.GAME_START_CLICKED:
				this.info.isStartGameLevelWasInitiated = true;
				break;
			case GAME_MESSAGES.BATTLEGROUND_TIME_TO_START_UPDATED:
				l_bi.setTimeToStart(event.data.timeToStart);
				this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED);
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
				l_bi.setTimeToStart(undefined);
				break;
			case GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST:
				let lTime_num = l_bi.getTimeToStartInMillis();
				l_bi.setTimeToStart(undefined);
				break;
			case GAME_MESSAGES.BACK_TO_LOBBY:
				l_bi.isBattlegroundGameStarted = false;
				this.info.isStartGameLevelWasInitiated = false;
				break;
			case GAME_MESSAGES.BATTLEGROUND_GAME_URL_NEED_UPDATED_ON_PLAY_AGAIN:
				this.info.isGameStartUrlOnPlayAgainUpdateExpected = true;
				this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL, { buyIn: l_bi.getConfirmedBuyInCost()});
				break;
			case GAME_MESSAGES.ROUND_RESULT_ACTIVATED:
				this.info.isRoundResultWasActivated = true;
				break;
			case GAME_MESSAGES.ROUND_RESULT_SCREEN_DEACTIVATED:
				this.info.isRoundResultWasActivated = false;
				break;
			case GAME_MESSAGES.NEED_UPDATE_BATTLEGROUND_MODE_INFO_ON_GAME_LEVEL:
				this._needUpdateBattlegroundModeOnGameLevel();
				break;
			case GAME_MESSAGES.BATTLEGROUND_BACK_TO_MQB_LOBBY:
				APP.goToHome();
				break;
			case GAME_MESSAGES.BATTLEGROUND_COUNTDOWN_ACTIVATED:
				this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_COUNTDOWN_VISIBILITY_CHANGED);
				break;
			case GAME_MESSAGES.BATTLEGROUND_TIME_SYNC:
				this.info.syncTime(event.data.serverTime);
				break;
			case GAME_MESSAGES.GAME_ROUND_STATE_CHANGED:
				this.info.isRoundInProgress = event.data.state;
				break;
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (event.data.errorCode === GUSLobbyWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE)
				{
					this.info.isGameStartUrlOnPlayAgainUpdateExpected = true;
					// no need to emit EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL because url is requested from GUSLobbyScreen for this error code
				}
				break;
		}
	}

	_onServerEnterLobbyMessage(event)
	{
		let l_bi = this.info;

		if(event.messageData.battleground)
		{
			let lBattlegroundMessageData_obj = event.messageData.battleground;

			//NON BATTLEGROUND GAME LASTHAND CASE...
			if(event.messageData.alreadySitInStake !== 0)
			{
				l_bi.setStartOnEntnerLobbyInProgress(false)
			}
			//...NON BATTLEGROUND GAME LASTHAND CASE

			if (
				lBattlegroundMessageData_obj.roomState
				&&
				(
					lBattlegroundMessageData_obj.roomState == BATTLEGROUND_ROOM_STATE.PLAY
					|| lBattlegroundMessageData_obj.roomState == BATTLEGROUND_ROOM_STATE.QUALIFY
					|| lBattlegroundMessageData_obj.roomState == BATTLEGROUND_ROOM_STATE.WAIT && lBattlegroundMessageData_obj.buyInConfirmed
				)
			)
			{
				l_bi.isConfirmBuyinDialogExpectedOnLastHand = false;
				l_bi.isNoWeaponsFiredDialogRequired = false;
			}
			else
			{
				l_bi.isConfirmBuyinDialogExpectedOnLastHand = true;
			}

			//BATTLEGROUND LASTHAND CASE...
			if(
				lBattlegroundMessageData_obj.alreadySeatRoomId &&
				lBattlegroundMessageData_obj.startGameUrl
			)
			{
				this._onStartGameURLReceived(lBattlegroundMessageData_obj.startGameUrl);
				l_bi.isBattlegroundGameStarted = true;
				l_bi.setStartOnEntnerLobbyInProgress(true);
			}
			//...BATTLEGROUND LASTHAND CASE
			else if(l_bi.isStartOnEnterLobbyInProgress())
			//BATTLEGROUND BUY IN ALLREADY DONE IN TOURNAMENT LOBBY CASE...
			{
				this.info.isBattlegroundGameStarted = true;
				this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_BUY_IN_ALLREADY_DONE_IN_TOURNAMENT_LOBBY);
			}
			//...BATTLEGROUND BUY IN ALLREADY DONE IN TOURNAMENT LOBBY CASE
		}

		let l_bbicdc = APP.dialogsController.battlegroundBuyInConfirmationDialogController;
		l_bbicdc.on(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_RE_BUY_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);
		l_bbicdc.on(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);

		l_bbicdc.on(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_CANCELED, this._onBattlegroundBuyInCanceled, this);
	}

	_onBattlegroundBuyInConfirmed(event)
	{
		this.info.isConfirmBuyinDialogRequired = false;
		this.info.isConfirmBuyinDialogExpectedOnLastHand = false;
		this.info.isNoWeaponsFiredDialogRequired = false;
		this.info.isBattlegroundGameStarted = true;
		this.info.setConfirmedBuyInCost(event.buyIn);
		this._needUpdateBattlegroundModeOnGameLevel();
	}

	_onBattlegroundBuyInCanceled(event)
	{
		this.info.isConfirmBuyinDialogExpectedOnLastHand = true;
		this.info.isNoWeaponsFiredDialogRequired = false;
		this.info.setConfirmedBuyInCost(null);
		this._needUpdateBattlegroundModeOnGameLevel();
	}

	_onServerStartGameUrlBattlegroundResponseMessage(event)
	{
		this._onStartGameURLReceived(event.messageData.startGameUrl);
	}

	_onStartGameURLReceived(aURL_str)
	{
		this.info.setStartGameURL(aURL_str);
	}

	getFormattedTimeToStart(aOptIsHHRequired_bl)
	{
		let lMillisCount_int = this.info.getTimeToStartInMillis();

		if(lMillisCount_int <= 0)
		{
			this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_COUNTDOWN_VISIBILITY_CHANGED);
			this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_WAITING_TIME_IS_OUT, {dialogIdForPresentation: APP.dialogsController.info.dialogIdForPresentation});
			return aOptIsHHRequired_bl ? "--:--:--" : "--:--"
		}

		return this.info.getFormattedTimeToStart(aOptIsHHRequired_bl);
	}

	_needUpdateBattlegroundModeOnGameLevel()
	{
		let l_bi = this.info;
		this.emit(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NEED_UPDATE_MODE_ON_GAME_LEVEL, {
			isConfirmBuyinDialogExpectedOnLastHand: l_bi.isConfirmBuyinDialogExpectedOnLastHand,
			isLobbyBattlegroundNotEnoughPlayersDialogActivated: l_bi.isLobbyBattlegroundNotEnoughPlayersDialogActivated
		});
	}
}

export default GUSLobbyBattlegroundController