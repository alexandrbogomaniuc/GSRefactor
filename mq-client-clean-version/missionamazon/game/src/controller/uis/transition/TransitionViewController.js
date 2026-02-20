import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import TransitionView from '../../../view/uis/transition/TransitionView';
import TransitionViewInfo from '../../../model/uis/transition/TransitionViewInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameStateController from '../../state/GameStateController';
import { ROUND_STATE } from '../../../model/state/GameStateInfo';
import GameField from '../../../main/GameField';
import GameScreen from '../../../main/GameScreen';
import RoundResultScreenController from '../roundresult/RoundResultScreenController';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import GameExternalCommunicator from '../../../external/GameExternalCommunicator';
import { LOBBY_MESSAGES } from '../../../external/GameExternalCommunicator';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import {ROUND_FINISH_SOON_DURATION} from '../../../config/Constants';
import BattlegroundCountDownDialogController from '../../../controller/uis/custom/gameplaydialogs/custom/BattlegroundCountDownDialogController';
import GameCafRoomManagerController from '../battleground/GameCafRoomManagerController';
import CafPrivateRoundCountDownDialogController from '../../../controller/uis/custom/gameplaydialogs/custom/CafPrivateRoundCountDownDialogController';

const INTRO_PRE_ROUND_END_INTERVAL = 1000;

class TransitionViewController extends SimpleUIController
{
	static get EVENT_ON_TRANSITION_INTRO_COMPLETED() { return "EVENT_ON_TRANSITION_INTRO_COMPLETED"; }

	constructor()
	{
		super (new TransitionViewInfo(), new TransitionView());

		this._isReady = false;
	}

	__init()
	{
		super.__init();
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		this._fTimer_t = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (!this.info.isFeatureActive)
		{
			return;
		}

		this._gameField = APP.currentWindow.gameField;
		this._gameField.on(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);
		this._gameField.on(GameField.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);

		this._gameStateController = APP.currentWindow.gameStateController;
		this._gameStateController.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
		this._gameStateController.on(GameStateController.EVENT_ON_SUBROUND_STATE_CHANGED, this._onSubroundStateChanged, this);

		this._gameScreen = APP.gameScreen;
		this._gameScreen.on(GameScreen.EVENT_ON_TICK_OCCURRED, this._onGSTickOccurred, this);
		this._gameScreen.on(GameScreen.EVENT_ON_BATTLEGROUND_ROUND_CANCELED, this._onCancellBattlegroundRound, this);
		this._gameScreen.on(GameScreen.EVENT_ON_NO_EMPTY_SEATS, this._onNoPlaceToSeatHandler, this);

		this._gameScreen.gameplayDialogController.gameBattlegroundCountDownDialogController.on(BattlegroundCountDownDialogController.EVENT_COUNT_DOWN_DIALOG_ACTIVATED, this._onCountDownDialogActivated, this);
		this._gameScreen.gameplayDialogController.gameBattlegroundCountDownDialogController.on(BattlegroundCountDownDialogController.EVENT_COUNT_DOWN_DIALOG_DEACTIVATED, this._onCountDownDialogDeactivated, this);
		this._gameScreen.gameplayDialogController.gameBattlegroundCountDownDialogController.on(BattlegroundCountDownDialogController.EVENT_COUNT_DOWN_DIALOG_ACTIVATED, this._onCountDownDialogActivated, this);
		this._gameScreen.gameplayDialogController.gameBattlegroundCountDownDialogController.on(BattlegroundCountDownDialogController.EVENT_COUNT_DOWN_DIALOG_DEACTIVATED, this._onCountDownDialogDeactivated, this);

		this._gameScreen.gameplayDialogController.gameCafPrivateRoundCountDownDialogController.on(CafPrivateRoundCountDownDialogController.EVENT_DIALOG_PRESENTED, this._onPrivateRoundCountDownDialogRequired, this);

		this._gameScreen.battlegroundGameController.cafRoomManagerController.on(GameCafRoomManagerController.EVENT_ROOM_MANAGER_ROUND_START_MODE_ACTIVATED, this._onRoomManagerRoundStartModeActivated, this);
		this._gameScreen.battlegroundGameController.cafRoomManagerController.on(GameCafRoomManagerController.EVENT_ROOM_MANAGER_ROUND_START_MODE_DEACTIVATED, this._onRoomManagerRoundStartModeDeactivated, this);
		
		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();
		let lView_tw = this.view;
		lView_tw.on(TransitionView.EVENT_ON_TRANSITION_INTRO_COMPLETED, this._onTransitionIntroCompleted, this);
	}

	_onTransitionIntroCompleted()
	{
		let lView_tw = this.view;
		if (APP.isBattlegroundGame)
		{
			lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN);
		}
		this.emit(TransitionViewController.EVENT_ON_TRANSITION_INTRO_COMPLETED);
	}

	_onCancellBattlegroundRound(event)
	{
		this.info.isBattlegroundNoWeaponsFiredDialogExpected = !event.isWaitState;

		let lView_tw = this.view;
		if (
				(
					(APP.isCAFMode && APP.playerController.info.isCAFRoomManager && !this.info.isCafRoomManagerRoundStartModeActivated)
					|| (APP.isBattlegroundGame && !this.info.isBattlegroundCountDownDialogActivated)
				)
				&& lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_IVALID
		)
		{
			lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
		}

		this._validateState();
	}

	_onGameFieldScreenCreated(event)
	{
		this.view.initOnScreen(APP.currentWindow.gameField.transitionViewContainerInfo);

		this._startHandle();

		let lGameStateInfo_gsi = this._gameStateController.info;
		if (lGameStateInfo_gsi.isPlayState && lGameStateInfo_gsi.roundEndTime > 0)
		{
			this._validatePreRoundEndIntro(lGameStateInfo_gsi.roundEndTime);
		}
	}

	_startHandle()
	{
		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		this._gameField.roundResultScreenController.on(RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this._onRoundResultActivated, this);
		this._gameField.roundResultScreenController.on(RoundResultScreenController.EVENT_ON_NEED_TRANSITION_BEFORE_ROUND_RESULT_SCREEN_ACTIVATED, this._onNeedTranstionBeforeRoundResultScreenActivated, this);
		this._isReady = true;
	}

	_onLobbyExternalMessageReceived(aEvent_obj)
	{
		switch (aEvent_obj.type)
		{
			case LOBBY_MESSAGES.BACK_TO_GAME:
				this._validateState();
				break;
		}
	}

	_onCountDownDialogActivated()
	{
		if (this.info.isBattlegroundNoWeaponsFiredDialogExpected)
		{
			this.info.isBattlegroundNoWeaponsFiredDialogExpected = false;
		}

		if (APP.isBattlegroundGame && !this.info.isBattlegroundCountDownDialogActivated)
		{
			let lView_tw = this.view;
			let roundState = this._gameStateController.info.gameState;

			this.info.isBattlegroundCountDownDialogActivated = true;

			if (lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_IVALID 
				&& lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO);
			}
			else
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_IVALID);
			}
		}
	}

	_onCountDownDialogDeactivated(event)
	{
		if (APP.isBattlegroundGame && this.info.isBattlegroundCountDownDialogActivated)
		{
			let lView_tw = this.view;

			let roundState = this._gameStateController.info.gameState;

			this.info.isBattlegroundCountDownDialogActivated = false;

			if (event.isReasonForDeactivateDialog)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
			}
		}
	}

	_onRoomManagerRoundStartModeActivated()
	{
		if (this.info.isBattlegroundNoWeaponsFiredDialogExpected)
		{
			this.info.isBattlegroundNoWeaponsFiredDialogExpected = false;
		}

		if (APP.isCAFMode && APP.playerController.info.isCAFRoomManager && !this.info.isCafRoomManagerRoundStartModeActivated)
		{
			let lView_tw = this.view;

			this.info.isCafRoomManagerRoundStartModeActivated = true;

			lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
		}
	}

	_onRoomManagerRoundStartModeDeactivated(event)
	{
		if (APP.isCAFMode && this.info.isCafRoomManagerRoundStartModeActivated)
		{
			let lView_tw = this.view;

			this.info.isCafRoomManagerRoundStartModeActivated = false;

			if (event.isReasonForDeactivateDialog)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
			}
		}
	}

	_onPrivateRoundCountDownDialogRequired()
	{
		if (APP.isBattlegroundGame && APP.isCAFMode)
		{
			this.view.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
		}
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
			case SERVER_MESSAGES.FULL_GAME_INFO:
				let roundState = data.state;
				let lView_tw = this.view;

				if (
						APP.isCAFMode
						&& (
								this.info.isCafRoomManagerRoundStartModeActivated
								|| APP.playerController.info.isKicked
							)
					)
				{
					lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
					break;
				}
				else if (APP.isBattlegroundGame && this.info.isBattlegroundCountDownDialogActivated)
				{
					if (lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO)
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_IVALID);
					}
					break;
				}

				if (roundState == ROUND_STATE.QUALIFY)
				{
					if (!APP.isBattlegroundGame && (lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_INTRO || !APP.currentWindow.gameStateController.info.isPlayerSitIn))
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
					}
					else if (
								APP.isBattlegroundGame
								&& (
										(APP.gameScreen.gameField.roundResultScreenController.info.isScreenActive && lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_INTRO)
										|| !APP.gameScreen.player.sitIn
									)
							)
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
					}
					else if (APP.isBattlegroundGame)
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
					}
				}
				else if (roundState == ROUND_STATE.WAIT)
				{
					if (lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_INTRO)
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
					}
				}
				else if (roundState == ROUND_STATE.PLAY)
				{
					if (lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_INTRO || lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN)
					{
						APP.logger.i_pushWarning(`Transition. Invalid smoke state for PLAY round state: ${lView_tw.stateId}.`);
					}
					
					if(APP.isCAFMode 
						&& !APP.gameScreen.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
						&& !APP.gameScreen.gameStateController.info.isPlayerSitIn
						)
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
					}
					else if (lView_tw.stateId !== TransitionView.TRANSITION_VIEW_STATE_ID_IVALID)
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO);
					}
					else
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_IVALID);
						lView_tw.drop();
					}

					this._validatePreRoundEndIntro(data.endTime);
				}

				break;

			case SERVER_MESSAGES.ROUND_FINISH_SOON:
				data.endTime = data.endTime || data.date || this._gameScreen.accurateCurrentTime;
				this._validatePreRoundEndIntro(data.endTime);
				break;
		}
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		let lView_tw = this.view;
		
		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.SIT_OUT_NOT_ALLOWED:
				if (APP.isBattlegroundGame)
				{
					if (lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO)
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_IVALID);
					}
				}
				break;
		}
	}

	_validatePreRoundEndIntro(aRoundFinishSoonTime_num)
	{
		if (this._gameScreen.isPaused || this._gameScreen.restoreAfterLagsInProgress || !this._gameStateController.info.isPlayState)
		{
			return;
		}

		let lGameStateInfo_gsi = this._gameStateController.info;
		if (!lGameStateInfo_gsi.isPlayState || lGameStateInfo_gsi.isBossSubround)
		{
			return;
		}

		if (aRoundFinishSoonTime_num > 0)
		{
			let lPreRoundInterval_num = APP.isBattlegroundGame ? 0: INTRO_PRE_ROUND_END_INTERVAL;
			let lIntroPreRoundTime_num = aRoundFinishSoonTime_num + ROUND_FINISH_SOON_DURATION - lPreRoundInterval_num;
			let lCurrentTime_num = this._gameScreen.accurateCurrentTime;

			this._destroyTimer();

			if (APP.isCAFMode && APP.playerController.info.isKicked)
			{
				return;
			}

			if (lIntroPreRoundTime_num - lCurrentTime_num <= 0)
			{
				this.view.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
			}
			else if (lIntroPreRoundTime_num - lCurrentTime_num <= ROUND_FINISH_SOON_DURATION)
			{
				let lIntroStateDelay_num = (lIntroPreRoundTime_num - lCurrentTime_num);
				this._fTimer_t = new Timer((e) => {this._onPreRoundEndIntroTimerCompleted();}, lIntroStateDelay_num);
			}
		}
	}

	_destroyTimer()
	{
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = null;
	}

	_onPreRoundEndIntroTimerCompleted()
	{
		this._destroyTimer();

		let lGameStateInfo_gsi = this._gameStateController.info;
		let lView_tw = this.view;
		if (lGameStateInfo_gsi.isPlayState && !lGameStateInfo_gsi.isBossSubround)
		{
			if (
					lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_IVALID
					|| lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO
				)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
			}
		}
	}

	_onGameStateChanged(event)
	{
		if (APP.currentWindow.isPaused)
		{
			return;
		}
		this._validateState();
	}

	_onSubroundStateChanged(event)
	{
		if (APP.isCAFMode && APP.playerController.info.isKicked)
		{
			// keep current LOOP state
			return;
		}

		let lGameStateInfo_gsi = this._gameStateController.info;
		if (lGameStateInfo_gsi.isBossSubround)
		{
			this._destroyTimer();

			let lView_tw = this.view;
			if (lView_tw.stateId !== TransitionView.TRANSITION_VIEW_STATE_ID_IVALID && lView_tw.stateId !== TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_IVALID);
			}
		}
	}

	_validateState()
	{
		let lView_tw = this.view;
		let roundState = this._gameStateController.info.gameState;
		
		if (
				APP.isCAFMode
				&& (
						this.info.isCafRoomManagerRoundStartModeActivated
						|| APP.playerController.info.isKicked
					)
			)
		{
			lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
			return;
		}
		else if (APP.isBattlegroundGame && this.info.isBattlegroundCountDownDialogActivated)
		{
			if (lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_IVALID);
			}
			return;
		}

		if (roundState == ROUND_STATE.QUALIFY)
		{
			this._destroyTimer();

			if (APP.isBattlegroundGame)
			{
				this.info.isBattlegroundRoundResultExpected = true;

				if(!APP.gameScreen.player.sitIn && APP.gameScreen.room && !APP.gameScreen.room.alreadySitInNumber >= 0)
				{
					this.info.isBattlegroundRoundResultExpected = false;

					if (
							lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_IVALID
							|| lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO
						)
					{
						lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
					}
				}

				return;
			}

			if (lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_IVALID)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
			}
			else if (lView_tw.stateId !== TransitionView.TRANSITION_VIEW_STATE_ID_INTRO)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
			}
		}

		if (roundState == ROUND_STATE.WAIT)
		{
			this._destroyTimer();

			if (lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_IVALID
				|| lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_LOOP
				|| lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO)
			{
				if (APP.isBattlegroundGame && this.info.isBattlegroundRoundResultExpected)
				{
					lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
				}
				else
				{
					lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
				}
			}
			else if (lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_INTRO)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN);
			}
		}

		if (roundState == ROUND_STATE.PLAY)
		{
			if(this.info.isBattlegroundNoWeaponsFiredDialogExpected)
			{
				return;
			}

			if (lView_tw.stateId !== TransitionView.TRANSITION_VIEW_STATE_ID_IVALID)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO);
			}
		}
	}

	_onNeedTranstionBeforeRoundResultScreenActivated()
	{
		if (APP.isBattlegroundGame)
		{
			let lView_tw = this.view;
			if (lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_IVALID || lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO)
			{
				let lView_tw = this.view;
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
			}
			else if (lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_LOOP || lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN)
			{
				this.emit(TransitionViewController.EVENT_ON_TRANSITION_INTRO_COMPLETED);
			}
		}
	}

	_onRoundResultActivated()
	{
		let lView_tw = this.view;

		let roundState = this._gameStateController.info.gameState;

		if (this.info.isBattlegroundRoundResultExpected)
		{
			this.info.isBattlegroundRoundResultExpected = false;
		}

		if (roundState == ROUND_STATE.QUALIFY && !APP.isBattlegroundGame)
		{
			if (lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_IVALID)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
			}
			else if (lView_tw.stateId != TransitionView.TRANSITION_VIEW_STATE_ID_LOOP)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN);
			}
		}

		if (roundState == ROUND_STATE.WAIT)
		{
			if (lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_IVALID)
			{
				lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
			}
		}
	}

	_onRoomFieldCleared(event)
	{
		if (!event.keepPlayersOnScreen)
		{
			this._destroyTimer();

			if (APP.currentWindow.isPaused)
			{
				this.view.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_IVALID);
			}
			else
			{
				let lView_tw = this.view;

				if (
						lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_INTRO
						|| lView_tw.stateId == TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO
					)
				{
					lView_tw.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN);
				}
			}
		}
	}

	_onNoPlaceToSeatHandler(event)
	{
		this.view.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN);
	}

	_onGSTickOccurred(event)
	{
		this.view.tick(event.realDelta);
	}

	destroy()
	{
		if (this._gameScreen)
		{
			this._gameScreen.off(GameScreen.EVENT_ON_TICK_OCCURRED, this._onGSTickOccurred, this);
			this._gameScreen.off(GameScreen.EVENT_ON_BATTLEGROUND_ROUND_CANCELED, this._onCancellBattlegroundRound, this);
			this._gameScreen = null;
		}

		if (this._gameField)
		{
			this._gameField.off(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);
			this._gameField.off(GameField.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);
			this._gameField = null;
		}

		if (this._gameStateController)
		{
			this._gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
			this._gameStateController.off(GameStateController.EVENT_ON_SUBROUND_STATE_CHANGED, this._onSubroundStateChanged, this);
			this._gameStateController = null;
		}

		if (APP.externalCommunicator)
		{
			APP.externalCommunicator.off(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
		}

		super.destroy();

		this._isReady = undefined;

		this._destroyTimer();
	}
}

export default TransitionViewController;