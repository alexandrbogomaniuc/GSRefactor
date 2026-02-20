import GameplayDialogController from '../GameplayDialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import GameExternalCommunicator from '../../../../../external/GameExternalCommunicator';
import BattlegroundGameController from '../../../battleground/BattlegroundGameController';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSGameExternalCommunicator';
import GameScreen from '../../../../../main/GameScreen';
import { ROUND_STATE } from '../../../../../model/state/GameStateInfo';
import { CLIENT_MESSAGES } from '../../../../../model/interaction/server/GameWebSocketInteractionInfo';
import GameStateController from '../../../../state/GameStateController';
import GamePlayerController from '../../../../custom/GamePlayerController';
import BattlegroundTutorialController from '../../tutorial/BattlegroundTutorialController';

const REQUEST_TIMEOUT = 730;

class BattlegroundCountDownDialogController extends GameplayDialogController
{
	static get EVENT_DIALOG_PRESENTED () { return GameplayDialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_COUNT_DOWN_DIALOG_ACTIVATED () { return "EVENT_COUNT_DOWN_DIALOG_ACTIVATED" };
	static get EVENT_COUNT_DOWN_DIALOG_DEACTIVATED () { return "EVENT_COUNT_DOWN_DIALOG_DEACTIVATED" };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return GameplayDialogController.EVENT_PRESENTED_DIALOG_UPDATED };
	static get EVENT_BATTLEGROUND_COUNTDOWN_CANCEL_CLICKED() { return "EVENT_BATTLEGROUND_COUNTDOWN_CANCEL_CLICKED" };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeInfo_tni = null;
		this._fInterval = null;

		this._fCancelTime = null;
		this._fIsUpdateCancelTimeAwaiting_bl = null;

		this._fIsSecondaryScreenActivate = null;

		this._initBattlegroundCountDownDialogController();
	}

	_initBattlegroundCountDownDialogController()
	{
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundCountDownDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();

		this._fTournamentModeInfo_tni = APP.tournamentModeController.info;
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		if (!APP.gameScreen.isBattlegroundMode)
		{
			return;
		}

		if (APP.isCAFMode)
		{
			APP.playerController.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		}

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND, this._onCancelBattlegroundRound, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyMessageReceived, this);

		if (!APP.isCAFMode)
		{
			APP.gameScreen.battlegroundGameController.on(BattlegroundGameController.EVENT_BATTLEGROUND_WAITING_TIME_IS_OUT, this._onTimeIsOut, this);
		}

		APP.gameScreen.on(GameScreen.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED, this._onTimeToStartUpdated, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_SIT_IN_COUNT_DOWN_REQUIRED, this._onTimeToStartUpdated, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_BATTLEGROUND_ROUND_CANCELED, this._onCancelBattlegroundRound, this);
		APP.gameScreen.on(GameScreen.EVENT_BATTLEGROUND_REQUEST_TIME_TO_START, this._onTimeToStartUpdated, this);

		APP.gameScreen.gameField.battlegroundFinalCountingController.on(GameScreen.EVENT_ON_BATTLEGROUND_FINAL_COUNTING_STARTED, this._onFinalCounting, this);

		let lGameStateController_gsc = this._fGameStateController_gsc = APP.gameScreen.gameStateController;
		lGameStateController_gsc.on(GameStateController.EVENT_ON_PLAYER_SEAT_STATE_CHANGED, this._onGamePlayerStateChanged, this);

		APP.gameScreen.gameField.battlegroundTutorialController.on(BattlegroundTutorialController.VIEW_APPEARING, this._onBattlegroundTutorialAppearing, this);
		APP.gameScreen.gameField.battlegroundTutorialController.on(BattlegroundTutorialController.VIEW_HIDDEN, this._onBattlegroundTutorialHidden, this);

		if (APP.isCAFMode)
		{
			lGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
			this._validateRoundPlayState();
		}

		//GAME_MESSAGES.GAME_STARTED
		// DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	//DEBUG...
	/*keyDownHandler(keyCode)
	{
		if (keyCode.keyCode == 69) //e
		{
			this.__activateDialog();
		}
	}*/
	//...DEBUG

	_onPlayerInfoUpdated(event)
	{
		let l_pi = APP.playerController.info;
		if (l_pi.isCAFRoomManagerDefined)
		{
			APP.playerController.off(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

			if (APP.isCAFMode && l_pi.isCAFRoomManager)
			{
				this._stopHandleEnvironmentMessages();
			}
		}
	}

	_stopHandleEnvironmentMessages()
	{
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.off(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.off(GameWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND, this._onCancelBattlegroundRound, this);
		webSocketInteractionController.off(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		webSocketInteractionController.off(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
		
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.off(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyMessageReceived, this);

		APP.gameScreen.battlegroundGameController.off(BattlegroundGameController.EVENT_BATTLEGROUND_WAITING_TIME_IS_OUT, this._onTimeIsOut, this);

		APP.gameScreen.off(GameScreen.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED, this._onTimeToStartUpdated, this);
		APP.gameScreen.off(GameScreen.EVENT_ON_SIT_IN_COUNT_DOWN_REQUIRED, this._onTimeToStartUpdated, this);
		APP.gameScreen.off(GameScreen.EVENT_ON_BATTLEGROUND_ROUND_CANCELED, this._onCancelBattlegroundRound, this);
		APP.gameScreen.off(GameScreen.EVENT_BATTLEGROUND_REQUEST_TIME_TO_START, this._onTimeToStartUpdated, this);

		APP.gameScreen.gameField.battlegroundFinalCountingController.off(GameScreen.EVENT_ON_BATTLEGROUND_FINAL_COUNTING_STARTED, this._onFinalCounting, this);

		this._fGameStateController_gsc.off(GameStateController.EVENT_ON_PLAYER_SEAT_STATE_CHANGED, this._onGamePlayerStateChanged, this);
		this._fGameStateController_gsc.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
	}

	_onGameStateChanged(event)
	{
		this._validateRoundPlayState();
	}

	_validateRoundPlayState()
	{
		let l_pi = APP.playerController.info;

		if (
				APP.isCAFMode
				&& this._fGameStateController_gsc.info.isPlayState
				&& l_pi.isMasterServerSeatIdDefined
				&& l_pi.masterServerSeatId >= 0
				&& this.info.isActive
			)
		{
			this.__deactivateDialog();
		}
	}

	_onLobbyMessageReceived(event)
	{
		switch (event.type)
		{
			case LOBBY_MESSAGES.BATTLEGROUND_RE_BUY_CONFIRMED:
				this.__deactivateDialog();
				if(APP.playerController.info.isCAFRoomManager) return;
				this._fIsUpdateCancelTimeAwaiting_bl = true;
				this._fIsSecondaryScreenActivate = false;
				this.__activateDialog();
			break;
			case LOBBY_MESSAGES.BACK_TO_LOBBY:
				this.__deactivateDialog();
			break;
			case LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_HIDE:
				this._fIsSecondaryScreenActivate = true;
				this.__deactivateDialog(true);
			break;
			case LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_SHOW:
				this.__deactivateDialog();
				if(APP.playerController.info.isCAFRoomManager) return;
				this._fIsSecondaryScreenActivate = false;
				this.__activateDialog();
			break;
		}
	}

	__onDialogCancelButtonClicked(event)
	{
		if(!this._fIsUpdateCancelTimeAwaiting_bl)
		{
			this.__deactivateDialog(true);
			
			super.__onDialogCancelButtonClicked(event);

			this.emit(BattlegroundCountDownDialogController.EVENT_BATTLEGROUND_COUNTDOWN_CANCEL_CLICKED);
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_BUY_IN_REOPENING_REQUIRED);
		}
	}

	_onTimeToStartUpdated(event)
	{
		let l_pi = APP.playerController.info;

		if(
			!event.isPlayerClickedConfirmPlayForNextBattlegroundRound
			|| event.isRoundResultDisplayInProgress
			|| (!APP.isCAFMode && !event.timeToStart) //Zero, null or undefined
			|| (!APP.isCAFMode && event.timeToStart === 9223372036854775807) //unset long java type, taken as undefined
			|| APP.playerController.info.isCAFRoomManager
			|| (APP.isCAFMode && this._fGameStateController_gsc.info.isPlayState && l_pi.isMasterServerSeatIdDefined && l_pi.masterServerSeatId >= 0)
		)
		{
			this.__deactivateDialog();
			return;
		}
		this.__activateDialog();
	}

	_onCancelBattlegroundRound()
	{
		this._fIsSecondaryScreenActivate = false;
		this.__deactivateDialog(true);
	}

	_onGamePlayerStateChanged(event)
	{
		let lIsPlayerSitIn_bln = event.value;

		if (!lIsPlayerSitIn_bln && this.info.isActive)
		{
			this._fIsSecondaryScreenActivate = false;

			this.__deactivateDialog(true);
		}
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}
	__validateViewLevel ()
	{
		let info = this.info;
		let view = this.__fView_uo;

		//buttons configuration...
		view.setCancelMode();
		//...buttons configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog(aIsReasonForDeactivateDialog)
	{
		if (!this.info.isActive)
		{
			return;
		}
		
		super.__deactivateDialog();

		clearInterval(this._fInterval);
		this._fCancelTime = null;

		this.view.scale.set(1);

		this.emit(BattlegroundCountDownDialogController.EVENT_COUNT_DOWN_DIALOG_DEACTIVATED, {isReasonForDeactivateDialog: aIsReasonForDeactivateDialog});
	}

	__activateDialog()
	{
		if (APP.playerController.info.isCAFRoomManager) return;

		super.__activateDialog();

		let lBattleGroundGameInfo_bgi = APP.gameScreen.battlegroundGameController.info;

		clearInterval(this._fInterval);

		if (
			!APP.isCAFMode &&
			(!this._fCancelTime
			|| (lBattleGroundGameInfo_bgi.getTimeToStartInMillis() > this._fCancelTime)			|| this._fIsUpdateCancelTimeAwaiting_bl)
			)
		{
			this._fCancelTime = lBattleGroundGameInfo_bgi.getTimeToStartInMillis();
			this._fIsUpdateCancelTimeAwaiting_bl = false;
			this.view._deactivateCancelButton();
		}

		this.view.updateIfTutorial();
		if(!APP.isCAFMode) 
		{
			this.view.updateTimeIndicator(lBattleGroundGameInfo_bgi.getFormattedTimeToStart(false));
			this._fInterval = setInterval(this._tick.bind(this), 100);
		}
		else
		{
			this._fIsUpdateCancelTimeAwaiting_bl = false;
		}

		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_COUNTDOWN_ACTIVATED);
		this.emit(BattlegroundCountDownDialogController.EVENT_COUNT_DOWN_DIALOG_ACTIVATED);

		if(this._fIsSecondaryScreenActivate)
		{
			this.__deactivateDialog();
		}
	}

	_tick()
	{
		let lBattleGroundGameInfo_bgi = APP.gameScreen.battlegroundGameController.info;
		let delta = this._fCancelTime - lBattleGroundGameInfo_bgi.getTimeToStartInMillis();

		if (!APP.isCAFMode &&
			(lBattleGroundGameInfo_bgi.getTimeToStartInMillis() <= 1500
			|| !APP.gameScreen.player.sitIn
			|| !lBattleGroundGameInfo_bgi.getTimeToStartInMillis()
			|| this._fIsUpdateCancelTimeAwaiting_bl
			|| APP.gameScreen.gameField.players.length === 6
			|| (
					APP.gameScreen.gameField.players.length === 5 
					&& !APP.gameScreen.player.battlegroundBuyInConfirmIsApproved
				)
			)
			|| !lBattleGroundGameInfo_bgi.getTimeToStartInMillis())
		{
			this.view._deactivateCancelButton();
		}
		else
		{
			this.view._activateCancelButton();
			this._fIsUpdateCancelTimeAwaiting_bl = false;
		}

		if(!APP.isCAFMode) this.view.updateTimeIndicator(APP.gameScreen.battlegroundGameController.getFormattedTimeToStart(false));
		
		//CLOSE IF NOT REQUIRED...
		if(!APP.gameScreen.isBattlegroundMode)
		{
			this.__deactivateDialog();
		}
		//...CLOSE IF NOT REQUIRED
	}

	_onRebuyDialogExitButtonClicked()
	{
		this.__deactivateDialog();
	}

	_onTimeIsOut()
	{
		let roundState = APP.currentWindow.gameStateController.info.gameState;
		if (roundState != ROUND_STATE.QUALIFY)
		{
			this.__deactivateDialog();
		}
	}

	_onFinalCounting()
	{
		this.__deactivateDialog();
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		
		switch (serverData.code) 
		{
			case GameWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS:
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
				this.__deactivateDialog();
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_BUY_IN_REOPENING_REQUIRED);
				break;
			case GameWebSocketInteractionController.ERROR_CODES.SW_PURCHASE_LIMIT_EXCEEDED:
				this.__deactivateDialog();
				break;
			case GameWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE:
			case GameWebSocketInteractionController.ERROR_CODES.NOT_FATAL_BAD_BUYIN:
			case GameWebSocketInteractionController.ERROR_CODES.TEMPORARY_PENDING_OPERATION:
			case GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE:
				if (
						requestData
						&& (requestData.class === CLIENT_MESSAGES.RE_BUY || requestData.class === CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN)
					)
				{
					this._fIsSecondaryScreenActivate = false;
					this.__deactivateDialog(true);

					if (serverData.code === GameWebSocketInteractionController.ERROR_CODES.TEMPORARY_PENDING_OPERATION)
					{
						// BATTLEGROUND_BUY_IN_REOPENING_REQUIRED will be emitted after confirmation LOBBY_MESSAGES.PENDING_OPERATION_FAILED_RETRY_CONFIRMED
					}
					else
					{
						APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_BUY_IN_REOPENING_REQUIRED);
					}
				}
				break;
		}
	}

	_onGameServerConnectionOpened(event)
	{
		this.__deactivateDialog();
	}

	_onGameServerConnectionClosed(event)
	{
		this.__deactivateDialog();
	}

	_onBattlegroundTutorialAppearing(event)
	{
		if (this.info.isActive)
		{
			this.view.updateIfTutorial();
		}
	}

	_onBattlegroundTutorialHidden(event)
	{
		if (this.info.isActive)
		{
			this.view.updateIfTutorial();
		}
	}
}

export default BattlegroundCountDownDialogController