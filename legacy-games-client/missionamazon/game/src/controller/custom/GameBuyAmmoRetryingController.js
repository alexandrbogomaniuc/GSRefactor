import {DIALOG_ID_GAME_BUY_AMMO_FAILED, DIALOG_ID_PENDING_OPERATION_FAILED} from '../../../../shared/src/CommonConstants';
import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../main/GameScreen';
import GameBuyAmmoRetryingInfo from '../../model/custom/GameBuyAmmoRetryingInfo';
import { ROUND_STATE } from '../../model/state/GameStateInfo';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import Game from '../../Game';
import GamePlayerController from './GamePlayerController';
import GameField from '../../main/GameField';
import WeaponsController from '../uis/weapons/WeaponsController';
import GameStateController from '../state/GameStateController';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSGameExternalCommunicator';
import GameExternalCommunicator from '../../external/GameExternalCommunicator';
import { CLIENT_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';

const DEFAULT_RETRY_TIMEOUT_IN_MS = 50;
const PENDING_OPERATION_RETRY_TIMEOUT_IN_MS = 500;

class GameBuyAmmoRetryingController extends SimpleController
{
	static get EVENT_ON_RETRY_BUY_IN_REQUIRED() 		{return "onRetryBuyInRequired";}
	static get EVENT_ON_RETRY_RE_BUY_REQUIRED() 		{return "onRetryReBuyRequired";}

	constructor()
	{
		super(new GameBuyAmmoRetryingInfo());

		this._tournamentModeInfo = null;
	}

	__init()
	{
		super.__init();
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		this._tournamentModeInfo = APP.tournamentModeController.info;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._retryTimer = null;

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_BUY_IN_RESPONSE_MESSAGE, this._onServerBuyInResponseMessage, this);

		let gs = this._gameScreen = APP.currentWindow;
		gs.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
		gs.on(GameScreen.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);

		APP.on(Game.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.playerController.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		gs.gameField.on(GameField.EVENT_ON_BUY_AMMO_REQUIRED, this._onBuyAmmoRequired, this);
		gs.gameField.on(GameField.EVENT_ON_FIRE_CANCELLED_WITH_NOT_ENOUGH_AMMO, this._onFireCancelledWithNotEnoughAmmo, this);

		gs.weaponsController.on(WeaponsController.EVENT_ON_WEAPONS_UPDATED, this._onWeaponsUpdated, this);
		gs.on(GameScreen.EVENT_ON_REVERT_AMMO_BACK, this._onRevertAmmoBack, this);
		gs.on(GameScreen.EVENT_DECREASE_AMMO, this._onDecreaseAmmo, this);

		gs.gameStateController.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);

		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
	}

	_onLobbyExternalMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case LOBBY_MESSAGES.DIALOG_ACTIVATED:
				if (event.data.dialogId === DIALOG_ID_GAME_BUY_AMMO_FAILED
					|| event.data.dialogId === DIALOG_ID_PENDING_OPERATION_FAILED)
				{
					this.info.isRetryDialogActive = true;
				}
				break;
			case LOBBY_MESSAGES.DIALOG_DEACTIVATED:
				if (event.data.dialogId === DIALOG_ID_GAME_BUY_AMMO_FAILED
					|| event.data.dialogId === DIALOG_ID_PENDING_OPERATION_FAILED)
				{
					this.info.isRetryDialogActive = false;
				}
				break;
			case LOBBY_MESSAGES.BUY_AMMO_FAILED_RETRY_CONFIRMED:
			case LOBBY_MESSAGES.PENDING_OPERATION_FAILED_RETRY_CONFIRMED:
				this.info.isRetryDialogActive = false;
				let retryType = event.data.type;
				if (retryType == CLIENT_MESSAGES.RE_BUY && APP.currentWindow.player.sitIn)
				{
					this._retryReBuy();
				}
				else
				{
					if(!APP.currentWindow.isBattlegroundMode)
					{
						this._retryBuyIn();
					}
					else
					{
						APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_BUY_IN_REOPENING_REQUIRED);
					}
				}
				break;
		}
	}

	_onServerBuyInResponseMessage(event)
	{
		this._hideRetryDialog();
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;
		let lRetryTimeout_int = undefined;

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE:
				lRetryTimeout_int = PENDING_OPERATION_RETRY_TIMEOUT_IN_MS;
			case GameWebSocketInteractionController.ERROR_CODES.NOT_FATAL_BAD_BUYIN:
				if (requestData && requestData.rid >= 0)
				{
					if (requestData.class === CLIENT_MESSAGES.BUY_IN)
					{
						let weaponsInfo = APP.currentWindow.weaponsController.info;
						if (
								weaponsInfo.ammo > 0
								&& weaponsInfo.ammo >= weaponsInfo.i_getCurrentWeaponShotPriceConvertedIntoDefaultAmmo() // enough ammo for at least 1 more shot
								&& !this._tournamentModeInfo.isTournamentMode
							)
						{
							this._startRetryTimer(lRetryTimeout_int);
						}
						else
						{
							this._showRetryDialog(CLIENT_MESSAGES.BUY_IN);
						}
					}
					else if (requestData.class === CLIENT_MESSAGES.RE_BUY && !APP.isBattlegroundMode)
					{
						this._showRetryDialog(CLIENT_MESSAGES.RE_BUY);
					}
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.TEMPORARY_PENDING_OPERATION:
				if (requestData && requestData.rid >= 0)
				{
					if (requestData.class === CLIENT_MESSAGES.BUY_IN)
					{
						this._showRetryDialogOnPendingOperation(CLIENT_MESSAGES.BUY_IN);
					}
					else if (requestData.class === CLIENT_MESSAGES.RE_BUY)
					{
						this._showRetryDialogOnPendingOperation(CLIENT_MESSAGES.RE_BUY);
					}
				}
				break;
		}
	}

	_onSitOutResponse(event)
	{
		let data = event.messageData;
		if (data.rid != -1 || APP.playerController.info.seatId == data.id)
		{
			this._resetRetryBuyInTimer();
			this._hideRetryDialog();
		}
	}

	_onPlayerInfoUpdated(event)
	{
		if (event.data.balance !== undefined)
		{
			let isBuyInAvailable = this._isBuyInAvailable;
			if (!this._isBuyInAvailable)
			{
				this._resetRetryBuyInTimer();
			}
		}
	}

	_onFireCancelledWithNotEnoughAmmo(event)
	{
		this._resetRetryBuyInTimer();
	}

	_onBuyAmmoRequired(event)
	{
		this._resetRetryBuyInTimer();
	}

	_onWeaponsUpdated()
	{
		if (!this._isBuyInAvailable)
		{
			this._resetRetryBuyInTimer();
		}
	}

	_onRevertAmmoBack(event)
	{
		if (!this._isBuyInAvailable)
		{
			this._resetRetryBuyInTimer();
		}
	}

	_onDecreaseAmmo(event)
	{
		if (!this._isBuyInAvailable)
		{
			this._resetRetryBuyInTimer();
		}
	}

	_onGameStateChanged(event)
	{
		if (!this._isBuyInAvailable)
		{
			this._resetRetryBuyInTimer();
		}
	}

	_onRoomPaused(aEvent_obj)
	{
		this._resetRetryBuyInTimer();
		this._hideRetryDialog();
	}

	_onRoomFieldCleared(aEvent_obj)
	{
		this._resetRetryBuyInTimer();
		this._hideRetryDialog();
	}

	_startRetryTimer(aCustomTimeoutInMs_int=undefined)
	{
		if (this._retryTimer)
		{
			this._resetRetryBuyInTimer();
		}

		let lRetryTimeout_int = aCustomTimeoutInMs_int || DEFAULT_RETRY_TIMEOUT_IN_MS;
		this._retryTimer = new Timer(this._onRetryTimeoutCompleted.bind(this), lRetryTimeout_int);
		this._retryTimer.start();
	}

	get _retryTimerInProgress()
	{
		return this._retryTimer && this._retryTimer.isInProgress();
	}

	_onRetryTimeoutCompleted()
	{
		this._resetRetryBuyInTimer();

		let isBuyInAvailable = this._isBuyInAvailable;
		let weaponsInfo = APP.currentWindow.weaponsController.info;

		if (isBuyInAvailable || this._tournamentModeInfo.isTournamentMode)
		{
			if (
					weaponsInfo.ammo > 0
					&& weaponsInfo.ammo >= weaponsInfo.i_getCurrentWeaponShotPriceConvertedIntoDefaultAmmo() // enough ammo for at least 1 more shot
					&& !this._tournamentModeInfo.isTournamentMode
				)
			{
				this._retryBuyIn();
			}
			else
			{
				this._showRetryDialog(CLIENT_MESSAGES.BUY_IN);
			}
		}
	}

	_resetRetryBuyInTimer()
	{
		if (this._retryTimer)
		{
			this._retryTimer.destructor();
			this._retryTimer = null;
		}
	}

	get _isBuyInAvailable()
	{
		let gs = APP.currentWindow;
		let gameStateInfo = gs.gameStateController.info;
		let weaponsInfo = gs.weaponsController.info;
		let playerInfo = APP.playerController.info;
		let lIsFrb_bln = APP.currentWindow.gameFrbController.info.frbMode;
		let lIsBonusMode_bl = APP.currentWindow.gameBonusController.info.isActivated;

		let lAmmoLimit_num = playerInfo.balance < playerInfo.currentStake || gameStateInfo.extraBuyInAvailable ? 0 : playerInfo.stakesLimit * playerInfo.betLevel;
		let lAmmoCost_num = lIsFrb_bln ? 0 : (weaponsInfo.ammo * playerInfo.currentStake);

		return (
					!lIsFrb_bln
					&& !lIsBonusMode_bl
					&& gameStateInfo.gameState == ROUND_STATE.PLAY
					&& gs.player.sitIn
					&& !gs.gameField.roundResultActive
					&& !gs.gameField.roundResultActivationInProgress
					&& weaponsInfo.ammo <= lAmmoLimit_num
					&& !gs.gameField.isAmmoBuyingInProgress
					&& (playerInfo.balance + lAmmoCost_num) > 0
					&& playerInfo.balance >= playerInfo.currentStake
					&& !APP.pendingOperationController.info.isPendingOperationStatusCheckInProgress
				)
	}

	_retryBuyIn()
	{
		this.emit(GameBuyAmmoRetryingController.EVENT_ON_RETRY_BUY_IN_REQUIRED);
	}

	_retryReBuy()
	{
		this.emit(GameBuyAmmoRetryingController.EVENT_ON_RETRY_RE_BUY_REQUIRED);
	}

	_showRetryDialog(retryType)
	{
		this._resetRetryBuyInTimer();

		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_BUY_AMMO_RETRY_DIALOG_APPEAR_REQUIRED, {type: retryType});
	}

	_showRetryDialogOnPendingOperation(retryType)
	{
		this._resetRetryBuyInTimer();

		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_PENDING_OPERATION_FAILED_DIALOG_REQUIRED, {type: retryType});
	}

	_hideRetryDialog()
	{
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_BUY_AMMO_RETRY_DIALOG_DISAPPEAR_REQUIRED);
	}
}

export default GameBuyAmmoRetryingController;