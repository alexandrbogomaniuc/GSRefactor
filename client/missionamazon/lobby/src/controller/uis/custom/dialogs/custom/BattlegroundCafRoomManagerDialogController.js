import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import BattlegroundController from '../../../../custom/battleground/BattlegroundController';
import { BACK_TYPE } from '../../../../../view/uis/custom/dialogs/custom/BattlegroundCafRoomManagerDialogView';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';
import BattlegroundBuyInConfirmationDialogInfo from '../../../../../model/uis/custom/dialogs/custom/BattlegroundBuyInConfirmationDialogInfo';
import GameBattlegroundNoWeaponsFiredDialogController from './game/GameBattlegroundNoWeaponsFiredDialogController';
import { BATTLEGROUND_ROOM_STATE } from '../../../../../config/Constants';
import { GAME_CLIENT_MESSAGES } from '../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyApp from '../../../../../LobbyAPP';
import BattlegroundCafRoomManagerDialogView from '../../../../../view/uis/custom/dialogs/custom/BattlegroundCafRoomManagerDialogView';
import { GAME_ROUND_STATE } from '../../../../../../../shared/src/CommonConstants';
import { MODE_TYPE } from '../../../../../view/uis/custom/dialogs/custom/BattlegroundCafRoomManagerDialogView';

class BattlegroundCafRoomManagerDialogController extends DialogController
{
	static get EVENT_BATTLEGROUND_RE_BUY_CONFIRMED () { return "EVENT_BATTLEGROUND_RE_BUY_CONFIRMED" };
	static get EVENT_BATTLEGROUND_MANAGER_START_ROUND_CLICKED () { return "EVENT_BATTLEGROUND_MANAGER_START_ROUND_CLICKED" };
	static get EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED () { return "EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED" };
	static get EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS() { return "EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS" };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fBattlegroundController_bc = null;

		this._fCurrentBalance = null;

		this._initBattlegroundCafRoomManagerDialogController();
	}

	_initBattlegroundCafRoomManagerDialogController()
	{

	}

	__init ()
	{
		super.__init()

	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundCafRoomManagerDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();

		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		if (!APP.isCAFMode)
		{
			return;
		}

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.once(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		this._fBattlegroundController_bc = APP.battlegroundController;
		this._fBattlegroundController_bc.on(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_REOPENED, this._reopenBuyInDialog, this);
		this._fBattlegroundController_bc.on(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_CLOSED, this._closeBuyInDialog, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		let roomNotFoundDialogController = APP.dialogsController.roomNotFoundDialogController;
		roomNotFoundDialogController.on(DialogController.EVENT_DIALOG_ACTIVATED, this._onRoomNotFoundDialogActivated, this);

		APP.dialogsController.gameGameBattlegroundNoWeaponsFiredDialogController.on(
			GameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED, this._noWeaponsFiredPlayAgainClicked, this);	

		APP.on(LobbyApp.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		// DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	_stopHandleEnvironmentMessages()
	{
		APP.off(LobbyApp.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this, true);
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);

		this._fBattlegroundController_bc.off(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_REOPENED, this._reopenBuyInDialog, this);
		this._fBattlegroundController_bc.off(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_CLOSED, this._closeBuyInDialog, this);

		APP.externalCommunicator.off(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		let roomNotFoundDialogController = APP.dialogsController.roomNotFoundDialogController;
		roomNotFoundDialogController.off(DialogController.EVENT_DIALOG_ACTIVATED, this._onRoomNotFoundDialogActivated, this);

		APP.dialogsController.gameGameBattlegroundNoWeaponsFiredDialogController.off(
			GameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED, this._noWeaponsFiredPlayAgainClicked, this);
	}

	__initViewLevel ()
	{
		super.__initViewLevel();

		let view = this.__fView_uo;
		
		view.on(BattlegroundCafRoomManagerDialogView.EVENT_ON_CANCEL_READY_BTN_CLICKED, this.__onDialogCancelReadyButtonClicked, this);
		view.on(BattlegroundCafRoomManagerDialogView.EVENT_ON_ROUND_START_BTN_CLICKED, this.__onDialogRoundStartButtonClicked, this);
		view.on(BattlegroundCafRoomManagerDialogView.EVENT_ON_ITEM_KICK_CLICKED, this.__onDialogItemKickButtonClicked, this);
		view.on(BattlegroundCafRoomManagerDialogView.EVENT_ON_CANCEL_KICK_CLICKED, this.__onDialogCancelKickButtonClicked, this);
		view.on(BattlegroundCafRoomManagerDialogView.EVENT_ON_INVITE_FRIEND_CLICKED, this.__onDialogInviteButtonClicked, this);
	}

		//DEBUG...
	/*keyDownHandler(keyCode)
	{
		if (keyCode.keyCode == 68) //d
		{
			this.__activateDialog();
		}
	}*/
	//...DEBUG

	_reopenBuyInDialog()
	{
		let l_bi = APP.battlegroundController.info;
		this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);

		this.__activateDialog();
	}

	_closeBuyInDialog()
	{
		// when open paytable or settings
		this.__deactivateDialog();
	}

	_noWeaponsFiredPlayAgainClicked()
	{
		let l_bi = APP.battlegroundController.info;
		this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);

		this.__activateDialog();
	}

	_onServerEnterLobbyMessage(event)
	{
		this._fCurrentBalance = event.messageData.balance;
		
		this._startHandleEnvironmentMessages();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		let msgData = event.data;
		let info = this.info;

		switch (msgType)
		{
			case GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST:
				let l_bi = APP.battlegroundController.info;
				
				if (APP.isCAFMode && l_bi.isRoundInProgress)
				{
					break;
				}

				this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);
				
				if (event.data && event.data.isRRActive)
				{
					this.view.updateBackType(BACK_TYPE.RETURN_TO_SCOREBOARD);
				}
				else
				{
					this.view.updateBackType(BACK_TYPE.RETURN_TO_LOBBY);
				}
				
				this.__activateDialog();

				if (this.info.isPlayerSitOutState && msgData.alreadySitInNumber !== undefined)
				{
					this.info.isPlayerSitOutState = msgData.alreadySitInNumber !== -1;
				}
				break;

			case GAME_MESSAGES.BATTLEGROUND_BUY_IN_REOPENING_REQUIRED:
				this.view.updateBackType(BACK_TYPE.RETURN_TO_LOBBY);

				this._reopenBuyInDialog();
				break;

			case GAME_MESSAGES.ON_GAME_STATE_CHANGED:
				if(!APP.isCAFMode) break;

				this.info.isGameStateQualify = event.data.roundState == GAME_ROUND_STATE.QUALIFY;
				this.info.updateKickAllowedState();
				break;

			case GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED:
				this.view.updateBackType(BACK_TYPE.RETURN_TO_LOBBY);

				if (event.data.isWaitState)
				{
					this.__activateDialog();
				}
				break;

			case GAME_MESSAGES.ROUND_RESULT_SCREEN_DEACTIVATED:
				this.view.updateBackType(BACK_TYPE.RETURN_TO_LOBBY);
				break;

			case GAME_MESSAGES.ON_SIT_OUT_REQUIRED:
				this.info.isReadyConfirmationTriggered = false;
				this.info.isCancelReadyTriggered = false;
				this.info.isPlayerSitOutState = true;
				break;

			case GAME_MESSAGES.ON_SIT_OUT_RESPONSE:
				if(event.data.rid != -1)
				{
					this.info.isReadyConfirmationTriggered = false;
					this.info.isCancelReadyTriggered = false;
					this.info.isPlayerSitOutState = false;
				}
				break;

			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (msgData.requestClass === GAME_CLIENT_MESSAGES.SIT_OUT)
				{
					this.info.isPlayerSitOutState = false;
				}
				else if (msgData.requestClass === GAME_CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN)
				{
					this.info.isReadyConfirmationTriggered = false;
				}

				if (
						msgData.requestClass === GAME_CLIENT_MESSAGES.BATTLEGROUND_START_PRIVATE_ROOM
						&& (
								msgData.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.NOT_ALLOWED_START_ROUND
								|| msgData.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE
							)
					)
				{
					this.view.updateBackType(BACK_TYPE.RETURN_TO_LOBBY);
					this.__activateDialog();
				}

				if (msgData.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.NOT_ALLOWED_KICK)
				{
					if (msgData.requestData)
					{
						this.info.updatePlayerKickProgressState(msgData.requestData.nickname, false);
					}
					else
					{
						APP.logger.i_pushError(`Cannot define requestData for error code NOT_ALLOWED_KICK (${msgData.errorCode}), rid: ${msgData.rid}, date: ${msgData.errorTime}`);

					}
				}
				break;

			case GAME_MESSAGES.BATTLEGROUND_BUY_IN_STATE_CHANGED:
				if (msgData.buyInConfirmed)
				{
					this.info.isReadyConfirmationTriggered = true;
					this.info.isCancelReadyTriggered = false;
				}
				break;

			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
				this.info.isReadyConfirmationTriggered = false;
				this.info.isCancelReadyTriggered = false;
				this.info.isPlayerSitOutState = false;
				this.info.isGameStateQualify = false;

				this.info.resetPlayersKickProgressState();
				break;
		}
	}

	_onPlayerInfoUpdated(event)
	{
		if (this._fPlayerInfo_pi.isCAFRoomManagerDefined)
		{
			if (!this._fPlayerInfo_pi.isCAFRoomManager)
			{
				APP.off(LobbyApp.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
				this._stopHandleEnvironmentMessages();
				return;
			}
		}

		if (this.info.isReadyConfirmationTriggered && !this._fPlayerInfo_pi.isObserver)
		{
			this.info.isReadyConfirmationTriggered = false;
		}

		if (
				event.data.roomObservers !== undefined
				|| event.data.roomServerSeaters !== undefined
				|| event.data.roomConfirmedBuyInBattlgroundSeats !== undefined
			)
		{
			this.__validateModelLevel();
		}
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();


		this.info.updatePlayersListData(this._fPlayerInfo_pi.roomObservers);
		this.info.updateFriendsListData(this._fPlayerInfo_pi.friends);
	}

	__validateViewLevel ()
	{
		var info = this.info;
		var view = this.__fView_uo;
		var messageAssetId;

		view.setOkCancelCustomMode();
		view.validatePlayersListView();
		view.validateFriendsListView();

		view.validateManagerStateButtons();

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		APP.off(LobbyApp.EVENT_ON_TICK_TIME, this._onTickTime, this);

		super.__deactivateDialog();
	}

	__onDialogOkButtonClicked(event) // ready button click
	{
		if (this.info.isPlayerSitOutState || this.info.isGameStateQualify)
		{
			this.__validateViewLevel();
			return;
		}
		
		let l_bi = APP.battlegroundController.info;
		if (this._fCurrentBalance >= l_bi.getConfirmedBuyInCost())
		{
			this.info.isReadyConfirmationTriggered = true;

			super.__onDialogOkButtonClicked(event);
			
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_CONFIRMED);
			this.emit(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_RE_BUY_CONFIRMED, { buyIn: this.info.getBuyInCost()} );

			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_RE_BUY_CONFIRMED);
			
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_BUTTON_CLICKED);
		}
		else
		{
			this.emit(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED);
		}
	}

	__onDialogCancelButtonClicked(event) // back to results/lobby button click
	{
		if (this._fBattlegroundController_bc.isRoundResultWasActivated())
		{
			this.__deactivateDialog();
			this.emit(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS);
		}
		else
		{
			this.__onGameDialogChangeWorldBuyInButtonClicked(event);
		}
	}

	__onDialogCancelReadyButtonClicked(event)
	{
		this.info.isCancelReadyTriggered = true;
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_CANCEL_READY_CLICKED);
	}

	__onDialogRoundStartButtonClicked(event)
	{
		this.__deactivateDialog();
		this.emit(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_MANAGER_START_ROUND_CLICKED);
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_MANAGER_START_ROUND_CLICKED);
	}

	__onDialogItemKickButtonClicked(event)
	{
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_CAF_PLAYER_KICK_TRIGGERED, {nickname: event.nickname});

		this.info.updatePlayerKickProgressState(event.nickname, true);
	}


	__onDialogCancelKickButtonClicked(event)
	{
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_CAF_PLAYER_REINVITE_TRIGGERED, {nickname: event.nickname});
	}


	__onDialogInviteButtonClicked(event)
	{
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_CAF_PLAYER_INVITE_TRIGGERED, {nicknames: [event.nickname]});
	}

	_startHandleEnvironmentMessages()
	{
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);
	}

	_onLobbyServerBalanceUpdated(event)
	{
		this._fCurrentBalance = event.messageData.balance;
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let requestClass = undefined;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}

		if (LobbyWebSocketInteractionController.isFatalError(event.errorType))
		{
			this.info.errorCode = serverData.code;
			this.info.errorTime = serverData.date;
			this.info.rid = serverData.rid;

			this.__deactivateDialog();
		}
	}

	_onRoomNotFoundDialogActivated(event)
	{
		if (APP.isCAFMode)
		{
			this.__deactivateDialog();
		}
	}

	__activateDialog()
	{
		if (
				!APP.battlegroundController.info.isBattlegroundMode()
				|| APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController.info.isActive
				|| (APP.isCAFMode && !this._fPlayerInfo_pi.isCAFRoomManager)
			)
		{
			return;
		}

		super.__activateDialog();


		APP.off(LobbyApp.EVENT_ON_TICK_TIME, this._onTickTime, this); // to prevent multiple listeners
		APP.on(LobbyApp.EVENT_ON_TICK_TIME, this._onTickTime, this);
	}

	_onTickTime(aEvent_e)
	{
		if (!this.info.isPresented)
		{
			return;
		}

		if (this.view)
		{
			this.__validateViewLevel();
		}
	}
}

export default BattlegroundCafRoomManagerDialogController