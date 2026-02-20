import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BattlegroundFinalCountingView from '../../../view/uis/final_counting/BattlegroundFinalCountingView';
import GameStateController from '../../state/GameStateController';
import { ROUND_STATE } from '../../../model/state/GameStateInfo';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import GameScreen from '../../../main/GameScreen';
import BattlegroundFinalCountingInfo from '../../../model/uis/final_counting/BattlegroundFinalCountingInfo';
import GameField from '../../../main/GameField';
import {GAME_MESSAGES} from '../../../external/GameExternalCommunicator';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';

class BattlegroundFinalCountingController extends SimpleUIController
{
	static get EVENT_ON_COUNT_FINISHED()			{return "EVENT_ON_COUNT_FINISHED";}
	static get EVENT_ON_BATTLEGROUND_FINAL_COUNTING_STARTED()			{return "EVENT_ON_BATTLEGROUND_FINAL_COUNTING_STARTED";}
	static get EVENT_ON_BATTLEGROUND_FINAL_COUNTING_NEXT_COUNT_STARTED()			{return BattlegroundFinalCountingView.EVENT_ON_BATTLEGROUND_FINAL_COUNTING_NEXT_COUNT_STARTED;}
	static get EVENT_ON_BATTLEGROUND_FINAL_COUNTING_COMPLETED()			{return "EVENT_ON_BATTLEGROUND_FINAL_COUNTING_COMPLETED";}

	constructor(aOptInfo_usuii, aOptView_uo)
	{
		super(aOptInfo_usuii, aOptView_uo);

		this._gameStateController = null;
		this._fCountingTimer_tmr = null;
		this._fStartTimer_tmr = null;
		this._fCompletedTimer_tmr = null;
	}

	i_checkCountingComplete()
	{
		this._checkCountingComplete();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._gameStateController = APP.currentWindow.gameStateController;
		this._gameStateController.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);

		this._gameField = APP.currentWindow.gameField;
		this._gameField.on(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);

		// window.addEventListener(
		//    "keydown", this.keyDownHandler.bind(this), false
		//  );

		let gs = this._gameScreen = APP.currentWindow;
		gs.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
		gs.on(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnPaused, this);

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_BATTLEGROUND_SCORE_BOARD, this._onBattlegroundScoreBoardResponse, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
	}

	// keyDownHandler()
	// {
	// 	this._checkStartCountingAnimation();
	// }

	_onBattlegroundScoreBoardResponse(e)
	{
		let lStartTime_num = e.messageData.startTime;
		
		if (lStartTime_num && lStartTime_num > 0 && this.info.isStartFinalCountingTimeExpectedOnScoreBoard)
		{
			this.info.rememberStartTime(lStartTime_num);
			this.info.isFinalCountingStarted = true;
			this._checkToStartCounting();
			this.emit(BattlegroundFinalCountingController.EVENT_ON_BATTLEGROUND_FINAL_COUNTING_STARTED);
		}
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		
		switch(data.class)
		{
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
				if (data.state === ROUND_STATE.PLAY && !this.info.isStartFinalCountingTimeExpectedOnScoreBoard)
				{
					this.checkExpectedFinalCounting();
				}
				break;
		}
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.SIT_OUT_NOT_ALLOWED:
				if (APP.currentWindow.gameStateController.info.isPlayState && !this.info.isStartFinalCountingTimeExpectedOnScoreBoard && !this._fStartTimer_tmr)
				{
					this.checkExpectedFinalCounting();
				}
				break;
		}
	}

	_onGameServerConnectionClosed(event)
	{
		this.info.resetStartTime();
		this._reset();
	}


	_onGameFieldScreenCreated(event)
	{
		this.view.initOnScreen(APP.currentWindow.gameField.btgFinalCountingViewContainerInfo);
		let roundState = APP.currentWindow.gameStateController.info.gameState;
		if (roundState == ROUND_STATE.WAIT)
		{
			this.info.isFinalCountingFireDenied = true;
		}
		else if (roundState == ROUND_STATE.PLAY)
		{
			this.checkExpectedFinalCounting();
		}
	}

	_onRoomPaused()
	{
		if (APP.isBattlegroundGame)
		{
			this._reset();
		}
	}

	_onRoomUnPaused()
	{	
		if (APP.isBattlegroundGame)
		{
			this._checkToStartCounting(true);
		}
	}

	checkExpectedFinalCounting()
	{
		if (APP.isBattlegroundGame && APP.gameScreen.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound)
		{
			this._reset();
			this.info.isStartFinalCountingTimeExpectedOnScoreBoard = true;
			this.info.isStartFinalCountingAllowedOnThisState = true;
			this.info.isFinalCountingFireDenied = true;

			if (!APP.gameScreen.isPaused)
			{
				this._checkToStartCounting();
			}
		}
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		this.view.on(BattlegroundFinalCountingView.EVENT_ON_COUNT_FINISHED, this._onNumberAnimationCompleted, this);
		this.view.on(BattlegroundFinalCountingView.EVENT_ON_BATTLEGROUND_FINAL_COUNTING_NEXT_COUNT_STARTED, this.emit, this);
	}

	_onGameStateChanged(event)
	{
		switch(event.value)
		{			
			case ROUND_STATE.PLAY:
				this.checkExpectedFinalCounting();
				break;
			case ROUND_STATE.WAIT:
				this.info.isStartFinalCountingAllowedOnThisState = false;
				this.info.isStartFinalCountingTimeExpectedOnScoreBoard = false;
				this.info.isFinalCountingStarted = false;
				this.info.isFinalCountingFireDenied = true;
				this.info.resetStartTime();
				break;
			case ROUND_STATE.QUALIFY:
				this.info.resetStartTime();
				this.info.isStartFinalCountingAllowedOnThisState = false;
				break;
		}
	}

	_checkToStartCounting(aAfterGamePause_bl = false)
	{
		if (!APP.isBattlegroundGame 
			|| !APP.gameScreen.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
			|| !APP.currentWindow.player.sitIn
			||  APP.gameScreen.isPaused && !aAfterGamePause_bl)
		{
			return;
		}

		if (this.info.isFinalCountingCompleted)
		{
			this._checkCountingComplete();
		} 
		else if (this.info.isFinalCountingWaitPlaying)
		{
			let lTimeLeft_num = this.info.getTimeStateBeforeStartCounting();
			if (lTimeLeft_num === null)
			{
				APP.logger.i_pushWarning("BTG Final Count. Time to start count is not defined");
				console.error("Time to start count Undefined");
			}
			else if (lTimeLeft_num > 0)
			{
				this.info.isStartFinalCountingTimeExpectedOnScoreBoard = false;
				this._fStartTimer_tmr = new Timer(this._tryStartCountingAnimation.bind(this), lTimeLeft_num);	
			}
			else
			{
				this._restoreCountingAnimation(aAfterGamePause_bl);
			}
		}
		else if (this.info.isFinalCountingPlaying)
		{
			this._restoreCountingAnimation(aAfterGamePause_bl);
		}
		else if (this.info.isFinalCountingCompleting)
		{
			let lEndTime_num = this.info.getTimeStateBeforeCompleted();

			if (lEndTime_num)
			{
				this._fCompletedTimer_tmr = new Timer(this._checkCountingComplete.bind(this), lEndTime_num);
			}
			else
			{
				this._checkCountingComplete();
			}
		}
		else
		{
			this._checkCountingComplete();
		}
	}

	_restoreCountingAnimation(aAfterGamePause_bl = false)
	{
		if (!this.info.isStartFinalCountingAllowedOnThisState || APP.gameScreen.isPaused && !aAfterGamePause_bl)
		{
			return;
		}
		
		if (this.info.isFinalCountingPlaying)
		{
			this.info.isStartFinalCountingTimeExpectedOnScoreBoard = false;
			let lActualNumberAndTime_obj = this.info.getActualNumerCountAndTimeToStart();

			if (lActualNumberAndTime_obj)
			{
				this.info.currentNumberCount = lActualNumberAndTime_obj.number;				
				this._fStartTimer_tmr = new Timer(this._tryStartCountingAnimation.bind(this), lActualNumberAndTime_obj.time);	
			}
			
		}
		else if (this.info.isFinalCountingCompleted)
		{
			this._checkCountingComplete();
		}
	}

	_tryStartCountingAnimation()
	{
		if (this.info.isStartFinalCountingTimeExpectedOnScoreBoard)
		{
			this.emit(BattlegroundFinalCountingController.EVENT_ON_BATTLEGROUND_FINAL_COUNTING_STARTED);
		}

		this._fStartTimer_tmr = null;

		if (!APP.gameScreen.isPaused)
		{
			this._startNextCountingAnimation();	
		}
	}

	_startNextCountingAnimation()
	{
		if (!APP.currentWindow.gameStateController.info.isPlayerSitIn 
			|| !this.info.isStartFinalCountingAllowedOnThisState
			|| APP.gameScreen.isPaused)
		{
			return;
		}

		let lCurrentNumberCount_int = this.info.currentNumberCount;
		if (lCurrentNumberCount_int >= 0)
		{
			this.view.startCountingAnimation(lCurrentNumberCount_int);
			this.info.nextNumberCount();

			if (this.info.currentNumberCount < BattlegroundFinalCountingInfo.NUMBER_COUNT)
			{
				this._fCountingTimer_tmr = new Timer(this._startNextCountingAnimation.bind(this), BattlegroundFinalCountingInfo.STEP_TIME_COUNTING);
			}
		}
	}

	_checkCountingComplete()
	{
		if (this.info.isFinalCountingStarted)
		{
			this.info.isFinalCountingStarted = false;
			this.info.isFinalCountingFireDenied = false;
			this.emit(BattlegroundFinalCountingController.EVENT_ON_BATTLEGROUND_FINAL_COUNTING_COMPLETED);
		} 
		else if (this.info.isFinalCountingCompleted)
		{
			this.info.isFinalCountingStarted = false;
			if (this.info.startTime != 0)
			{
				this.info.isFinalCountingFireDenied = false;
			}
		}
	}

	_onNumberAnimationCompleted(e)
	{
		if ((e.countingNumber + 1) >= BattlegroundFinalCountingInfo.NUMBER_COUNT)
		{
			this._checkCountingComplete();
		}
	}

	_reset()
	{
		this._fCountingTimer_tmr && this._fCountingTimer_tmr.destructor();
		this._fCountingTimer_tmr = null;

		this._fStartTimer_tmr && this._fStartTimer_tmr.destructor();
		this._fStartTimer_tmr = null;

		this._fCompletedTimer_tmr && this._fCompletedTimer_tmr.destructor();
		this._fCompletedTimer_tmr = null;		
		
		this.info.currentNumberCount = 0;
		this.view.reset();
	}

	destroy()
	{
		this._reset();
		this._gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
		this._gameStateController = null;

		super.destroy();
	}
}

export default BattlegroundFinalCountingController;
