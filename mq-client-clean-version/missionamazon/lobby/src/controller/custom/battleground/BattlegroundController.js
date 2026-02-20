import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../interaction/server/LobbyWebSocketInteractionController';
import BattlegroundInfo from '../../../model/custom/battleground/BattlegroundInfo';
import LobbyScreen from '../../../main/LobbyScreen';
import LobbyExternalCommunicator from '../../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import BattlegroundBuyInConfirmationDialogController from '../../uis/custom/dialogs/custom/BattlegroundBuyInConfirmationDialogController';
import BattlegroundCafRoomManagerDialogController from '../../uis/custom/dialogs/custom/BattlegroundCafRoomManagerDialogController';
import LobbyAPP from '../../../LobbyAPP';
import DialogsController from '../../uis/custom/dialogs/DialogsController';
import { BATTLEGROUND_ROOM_STATE } from '../../../config/Constants';
import DialogController from '../../uis/custom/dialogs/DialogController';
import LobbyBattlegroundNotEnoughPlayersDialogController from '../../uis/custom/dialogs/custom/LobbyBattlegroundNotEnoughPlayersDialogController';
import PaytableScreenController from '../../uis/custom/secondary/paytable/PaytableScreenController';
import SettingsScreenController from '../../uis/custom/secondary/settings/SettingsScreenController';
import GameBattlegroundContinueReadingDialogController from '../../uis/custom/dialogs/custom/game/GameBattlegroundContinueReadingDialogController';
import BattlegroundCAFPlayerKickedDialogController from '../../uis/custom/dialogs/custom/BattlegroundCAFPlayerKickedDialogController';
import BattlegroundBuyInConfirmationDialogControllerCAF from '../../uis/custom/dialogs/custom/BattlegroundBuyInConfirmationDialogControllerCAF';

class BattlegroundController extends SimpleController
{
	static get EVENT_BATTLEGROUND_ROOM_ID_RECEIVED() 							{ return 'EVENT_BATTLEGROUND_ROOM_ID_RECEIVED'; }
	static get EVENT_BATTLEGROUND_BUY_IN_ALLREADY_DONE_IN_TOURNAMENT_LOBBY()	{ return 'EVENT_BATTLEGROUND_BUY_IN_ALLREADY_DONE_IN_TOURNAMENT_LOBBY'; }
	static get EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL()					{ return 'EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL'; }
	static get EVENT_BATTLEGROUND_NEED_UPDATE_MODE_ON_GAME_LEVEL()				{ return 'EVENT_BATTLEGROUND_NEED_UPDATE_MODE_ON_GAME_LEVEL'; }
	static get EVENT_BATTLEGROUND_TIME_TO_START_UPDATED()						{ return 'EVENT_BATTLEGROUND_TIME_TO_START_UPDATED'; }
	static get EVENT_BATTLEGROUND_COUNTDOWN_VISIBILITY_CHANGED()				{ return 'EVENT_BATTLEGROUND_COUNTDOWN_VISIBILITY_CHANGED'; }	
	static get EVENT_BATTLEGROUND_BUY_IN_REOPENED()								{ return 'EVENT_BATTLEGROUND_BUY_IN_REOPENED'; }
	static get EVENT_BATTLEGROUND_BUY_IN_CLOSED()								{ return 'EVENT_BATTLEGROUND_BUY_IN_CLOSED'; }	
	static get EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_REOPENED()					{ return 'EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_REOPENED'; }
	static get EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CLOSED()					{ return 'EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CLOSED'; }
	static get EVENT_ON_ROUND_PROGRESS_STATE_CHANGED()							{ return 'EVENT_ON_ROUND_PROGRESS_STATE_CHANGED'; }

	isRoundResultWasActivated()
	{
		return this.info.isRoundResultWasActivated;
	}

	isSecondaryScreenTimerRequired()
	{
		return APP.isBattlegroundGame 
			&& !APP.battlegroundController.info.isConfirmBuyinDialogRequired
			&& !APP.battlegroundController.info.isNotEnoughPlayersDialogRequired
			&& !APP.battlegroundController.isRoundResultWasActivated()
			&& APP.battlegroundController.info.getTimeToStartInMillis() > 0;
	}

	constructor()
	{
		super(new BattlegroundInfo());

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
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._onLobbyAppStarted, this);
		APP.dialogsController.on(DialogsController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING, this._onBattlegroundNotEnoughPlayersContinueWaiting, this)

		let l_lbnepdc = APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController;
		l_lbnepdc.on(LobbyBattlegroundNotEnoughPlayersDialogController.EVENT_DIALOG_ACTIVATED, this._onLobbyBattlegroundNotEnoughPlayersDialogActivated, this);

		let l_bbicdc = APP.dialogsController.battlegroundBuyInConfirmationDialogController;
		l_bbicdc.on(BattlegroundBuyInConfirmationDialogController.EVENT_DIALOG_ACTIVATED, this._onBattlegroundBuyInConfirmationDialogActivated, this);
		l_bbicdc.on(BattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS, this._onBattlegroundBuyInConfirmationDialogNotRequired, this);

		let l_bbicdc_caf = APP.dialogsController.battlegroundBuyInConfirmationDialogControllerCAF;
		l_bbicdc_caf.on(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_DIALOG_ACTIVATED, this._onBattlegroundBuyInConfirmationDialogActivated, this);
		l_bbicdc_caf.on(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS, this._onBattlegroundBuyInConfirmationDialogNotRequired, this);
		
		let l_bcrmdc = APP.dialogsController.battlegroundCafRoomManagerDialogController;
		l_bcrmdc.on(BattlegroundCafRoomManagerDialogController.EVENT_DIALOG_ACTIVATED, this._onBattlegroundBuyInConfirmationDialogActivated, this);
		l_bcrmdc.on(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS, this._onBattlegroundBuyInConfirmationDialogNotRequired, this);

		let l_gbcrdc = APP.dialogsController.gameBattlegroundContinueReadingDialogController;
		l_gbcrdc.on(GameBattlegroundContinueReadingDialogController.EVENT_BATTLEGROUND_CONTINUE_READING_OK_CLICKED, this._onBattlegroundBuyInConfirmationDialogActivated, this);

		let l_bcafpkdc = APP.dialogsController.cafPlayerKickedDialogController;
		l_bcafpkdc.on(BattlegroundCAFPlayerKickedDialogController.EVENT_DIALOG_ACTIVATED, this._onBattlegroundCafPlayerKickedDialogActivated, this);

		let l_psc = APP.secondaryScreenController.paytableScreenController;
		l_psc.on(PaytableScreenController.EVENT_ON_SCREEN_SHOW, this._onCloseDialogsIfRequired, this);
		l_psc.on(PaytableScreenController.EVENT_ON_SCREEN_HIDE, this._onReopenDialogsIfRequired, this);

		let l_ssc = APP.secondaryScreenController.settingsScreenController;
		l_ssc.on(SettingsScreenController.EVENT_SCREEN_ACTIVATED, this._onCloseDialogsIfRequired, this);
		l_ssc.on(SettingsScreenController.EVENT_SCREEN_DEACTIVATED, this._onReopenDialogsIfRequired, this);
	}

	_onLobbyAppStarted(event)
	{
		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_BATTLEGROUND_START_URL_UPDATED, this._onBattlegroundStartUrlUpdated, this);
		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_LAUNCH_GAME_CLICKED, this._onGameLaunchClicked, this);
	}

	_startServerMessagesListening()
	{
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlBattlegroundResponseMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_GAME_ROOM_INFO_RESPONSE, this._onRoomInfoReceived, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlBattlegroundResponseMessage, this);
	}

	destroy()
	{
		APP.webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		APP.webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE, this._onServerBonusStatusChangedMessage, this);
		APP.webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_GAME_ROOM_INFO_RESPONSE, this._onRoomInfoReceived, this);
		APP.webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlBattlegroundResponseMessage, this);
		super.destroy();
	}

	_onLobbyBattlegroundNotEnoughPlayersDialogActivated()
	{
		this.info.isNotEnoughPlayersDialogRequired = true;
		this.info.isConfirmBuyinDialogRequired = false;
	}

	_onBattlegroundBuyInConfirmationDialogActivated()
	{
		this.info.isConfirmBuyinDialogRequired = true;
		this.info.isNotEnoughPlayersDialogRequired = false;
	}

	_onBattlegroundBuyInConfirmationDialogNotRequired()
	{
		this.info.isConfirmBuyinDialogRequired = false;
	}

	_onBattlegroundCafPlayerKickedDialogActivated()
	{
		this.info.isConfirmBuyinDialogRequired = false;
		this.info.isNotEnoughPlayersDialogRequired = false;
	}

	_onReopenDialogsIfRequired()
	{
		if(this.info.isNotEnoughPlayersDialogRequired)
		{
			this.emit(BattlegroundController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_REOPENED);
		}
		else if(this.info.isConfirmBuyinDialogRequired)
		{
			this.emit(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_REOPENED);
		}
	}

	_onCloseDialogsIfRequired()
	{
		if(this.info.isNotEnoughPlayersDialogRequired)
		{
			this.emit(BattlegroundController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CLOSED);
		}
		else if(this.info.isConfirmBuyinDialogRequired)
		{
			this.emit(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_CLOSED);
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
		this.emit(BattlegroundController.EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL, { buyIn: l_bi.getConfirmedBuyInCost() || l_bi.getSelectedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn});
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
				this.emit(BattlegroundController.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED);
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
				l_bi.setReconnectionToPlayingRound(false);
				this.info.isStartGameLevelWasInitiated = false;
				break;
			case GAME_MESSAGES.BATTLEGROUND_GAME_URL_NEED_UPDATED_ON_PLAY_AGAIN:
				this.info.isGameStartUrlOnPlayAgainUpdateExpected = true;
				this.emit(BattlegroundController.EVENT_BATTLEGROUND_NEED_UPDATE_START_GAME_URL, { buyIn: l_bi.getConfirmedBuyInCost()});
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
			case GAME_MESSAGES.BATTLEGROUND_TIME_SYNC:
				this.info.syncTime(event.data.serverTime);
				break;
			case GAME_MESSAGES.GAME_ROUND_STATE_CHANGED:
				this._updateRoundProgressState(event.data.state);
				break;
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (event.data.errorCode === LobbyWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE)
				{
					if(APP.isBattlegroundGame && APP.isNewMatchMakingSupported)
					{
						this.info.isGameStartUrlOnPlayAgainUpdateExpected = false;
						APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.UPDATE_BATTLEGROUND_END_TIME, {endTime:Date.now() + APP.btgRoundDuration})
					}else{
						this.info.isGameStartUrlOnPlayAgainUpdateExpected = true;
					}				}
				break;
		}
	}

	_onServerEnterLobbyMessage(event)
	{
		let l_bi = this.info;

		if(event.messageData.battleground)
		{
			let lBattlegroundMessageData_obj = event.messageData.battleground;
			l_bi.setBattlegroundMode(true);

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
				l_bi.setReconnectionToPlayingRound(true);
				l_bi.setStartOnEntnerLobbyInProgress(true);
			}
			//...BATTLEGROUND LASTHAND CASE
			else if(l_bi.isStartOnEnterLobbyInProgress())
			//BATTLEGROUND BUY IN ALLREADY DONE IN TOURNAMENT LOBBY CASE...
			{
				this.info.isBattlegroundGameStarted = true;
				this.emit(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_ALLREADY_DONE_IN_TOURNAMENT_LOBBY);
			}
			//...BATTLEGROUND BUY IN ALLREADY DONE IN TOURNAMENT LOBBY CASE
		}

		let l_bbicdc = APP.dialogsController.battlegroundBuyInConfirmationDialogController;
		let l_bbicdc_caf = APP.dialogsController.battlegroundBuyInConfirmationDialogControllerCAF;
		l_bbicdc.on(BattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_RE_BUY_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);
		l_bbicdc.on(BattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);
		l_bbicdc_caf.on(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_BATTLEGROUND_RE_BUY_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);
		l_bbicdc_caf.on(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_BATTLEGROUND_BUY_IN_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);
		
		let l_bcrmdc = APP.dialogsController.battlegroundCafRoomManagerDialogController;
		l_bcrmdc.on(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_RE_BUY_CONFIRMED, this._onBattlegroundBuyInConfirmed, this);
		l_bcrmdc.on(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_MANAGER_START_ROUND_CLICKED, this._onBattlegroundManagerStartRoundClicked, this);
	}

	_onBattlegroundBuyInConfirmed(event)
	{
		if (APP.isCAFMode && APP.playerController.info.isCAFRoomManager)
		{
			// dialog is still active
		}
		else
		{
			this.info.isConfirmBuyinDialogRequired = false;
		}

		this.info.isConfirmBuyinDialogExpectedOnLastHand = false;
		this.info.isBattlegroundGameStarted = true;
		this.info.setConfirmedBuyInCost(event.buyIn);
		this._needUpdateBattlegroundModeOnGameLevel();
	}

	_onBattlegroundManagerStartRoundClicked(event)
	{
		this.info.isConfirmBuyinDialogRequired = false;
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
			return aOptIsHHRequired_bl ? "--:--:--" : "--:--"
		}

		return this.info.getFormattedTimeToStart(aOptIsHHRequired_bl);
	}

	_needUpdateBattlegroundModeOnGameLevel()
	{
		let l_bi = this.info;
		this.emit(BattlegroundController.EVENT_BATTLEGROUND_NEED_UPDATE_MODE_ON_GAME_LEVEL, {
			isBattlegroundMode:  l_bi.isBattlegrGroundGame(), 
			isConfirmBuyinDialogExpectedOnLastHand: l_bi.isConfirmBuyinDialogExpectedOnLastHand
		});
	}

	_onRoomInfoReceived(event)
	{

		//if(event.messageData.battlegroundInfo)
		//{
		//	let lParams_obj = event.messageData.battlegroundInfo;

			//this.info.setTimeToStart(lParams_obj.timeToStart);

			/*
				"battlegroundInfo" {

				"buyIn" : 1000,
				"buyInConfirmed": true,
				"timeToStart" : 30,
				"kingOfHill": 1,
				"score": 4620,
				"rank" : 3,
				"pot": 3960
			  },
			*/
		//}
	}

	_updateRoundProgressState(aNewState_bl)
	{
		if (aNewState_bl === undefined || aNewState_bl === this.info.isRoundInProgress)
		{
			return;
		}

		this.info.isRoundInProgress = aNewState_bl;
		this.emit(BattlegroundController.EVENT_ON_ROUND_PROGRESS_STATE_CHANGED, {value: this.info.isRoundInProgress});
	}
}

export default BattlegroundController