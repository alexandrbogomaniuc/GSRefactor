import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { GameStateInfo, SUBROUND_STATE, ROUND_STATE } from '../../model/state/GameStateInfo';
import GameScreen from '../../main/GameScreen';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';
import BossModeController from '../uis/custom/bossmode/BossModeController';
import GamePlayerController from '../custom/GamePlayerController';
import { GAME_MESSAGES } from '../../external/GameExternalCommunicator';

class GameStateController extends SimpleController
{
	//CL INTERFACE...
	static get EVENT_ON_GAME_ROUND_STATE_CHANGED() 		{return "onGameRoundChanged";}
	static get EVENT_ON_GAME_STATE_CHANGED() 			{return "onGameStateChanged";}
	static get EVENT_ON_SUBROUND_STATE_CHANGED() 		{return "onSubroundStateChanged";}
	static get EVENT_ON_PLAYER_SEAT_STATE_CHANGED() 	{return "onPlayerSeatStateChanged";}
	//...CL INTERFACE

	//IL CONSTRUCTION...
	constructor(aOptInfo_ussi)
	{
		super(aOptInfo_ussi ? aOptInfo_ussi : new GameStateInfo());
	}
	//...IL CONSTRUCTION

	//ILI INIT...
	__initControlLevel()
	{
		super.__initControlLevel();

		APP.currentWindow.on(GameScreen.EVENT_ON_NEW_ROUND_STATE, this._updateRoundProgress, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_NO_EMPTY_SEATS, this._onNoPlaceToSeatHandler, this);

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
		APP.currentWindow.bossModeController.on(BossModeController.EVENT_DISAPPEARING_PRESENTATION_COMPLETED, this._onBossModeCompleted, this);

		APP.playerController.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
	}
	//...ILI INIT

	//IL IMPLEMENTATION...
	_onPlayerInfoUpdated(event)
	{
		let l_pi = APP.playerController.info;

		let lPrevIsMasterServerSeatIdDefined_bl = l_pi.isMasterServerSeatIdDefined;
		this._updatePlayerState(l_pi.isMasterServerSeatIdDefined);

		if (lPrevIsMasterServerSeatIdDefined_bl && !l_pi.isMasterServerSeatIdDefined)
		{
			this._updateSubroundState(undefined);
		}
	}

	_onNoPlaceToSeatHandler(event)
	{
		this._updateGameState(undefined);
	}
	
	updateRoundProgress()
	{
		this.info.isGameInProgress = GameStateInfo.gameState;
		this.emit(GameStateController.EVENT_ON_GAME_ROUND_STATE_CHANGED, {value: GameStateInfo.gameState});
	}

	_updateGameState(aState_str, aOptMapId_num)
	{
		if (!aState_str || aState_str === this.info.gameState)
		{
			return;
		}

		this.info.gameState = aState_str;
		this.emit(GameStateController.EVENT_ON_GAME_STATE_CHANGED, {value: aState_str, mapId: aOptMapId_num});
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_GAME_STATE_CHANGED, {roundState: this.info.gameState});
	}

	_updatePlayerState(aState_bl)
	{
		if (aState_bl === undefined || aState_bl === this.info.isPlayerSitIn)
		{
			return;
		}

		this.info.isPlayerSitIn = aState_bl;
		this.emit(GameStateController.EVENT_ON_PLAYER_SEAT_STATE_CHANGED, {value: aState_bl});
	}

	_updateRoundProgress(e)
	{
		if (e.state === undefined || e.state === this.info.isGameInProgress)
		{
			return;
		}

		if((!e.state) && APP.currentWindow.gameField && APP.currentWindow.gameField.isWaitingAnimationsEnd)
		{
			return;
		}

		this.info.isGameInProgress = e.state;
		this.emit(GameStateController.EVENT_ON_GAME_ROUND_STATE_CHANGED, {value: e.state});
	}

	_updateSubroundState(aState_str, aIsLasthand_bl = false, aOptMapId_num)
	{
		this.info.subroundLasthand = aIsLasthand_bl;

		if (aState_str !== this.info.subroundState)
		{
			this.info.subroundState = aState_str;
			this.emit(GameStateController.EVENT_ON_SUBROUND_STATE_CHANGED, {value: aState_str, mapId: aOptMapId_num});
		}
	}

	_onBossModeCompleted()
	{
		if(APP.gameScreen.isPaused)
		{
			return;
		}
		this._updateSubroundState(SUBROUND_STATE.BASE, false);
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		let l_pi = APP.playerController.info;
		switch(data.class)
		{
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
				this.info.roundEndTime = data.endTime;
				this._updateGameState(data.state, data.mapId);
				data.subround && this._updateSubroundState(data.subround, (data.state === ROUND_STATE.PLAY), data.mapId);
				break;

			case SERVER_MESSAGES.GAME_STATE_CHANGED:
				this._updateGameState(data.state);
				this._updateRoundProgress({state: data.state == "PLAY"});
				if (data.state !== "PLAY")
				{
					this.info.resetRoundEndTime();
					this._updateSubroundState(undefined);
				}
				else
				{
					this.info.extraBuyInAvailable = true;
				}
				break;

			case SERVER_MESSAGES.BUY_IN_RESPONSE:
			case SERVER_MESSAGES.RE_BUY_RESPONSE:
				this.info.extraBuyInAvailable = false;
				break;

			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				if (data.rid !== -1)
				{
					if (data.ammoAmount > 0)
					{
						this.info.extraBuyInAvailable = false;
					}
				}
				break;

			case SERVER_MESSAGES.FULL_GAME_INFO:
				this.info.roundEndTime = data.endTime;
				this._updateGameState(data.state, data.mapId);
				this._updateSubroundState(data.subround, true);
				break;

			case SERVER_MESSAGES.CHANGE_MAP:
				this._updateSubroundState(data.subround);
				break;

			case SERVER_MESSAGES.WEAPONS:
				if (data.ammoAmount > 0)
				{
					this.info.extraBuyInAvailable = false;
				}
				break;
		}
	}

	_onServerConnectionClosed()
	{
		this._updatePlayerState(false);
	}
	//...IL IMPLEMENTATION

	destroy()
	{
		APP.currentWindow && APP.currentWindow.off(GameScreen.EVENT_ON_NEW_ROUND_STATE, this._updateRoundProgress, this);
		if (APP.webSocketInteractionController)
		{
			APP.webSocketInteractionController.off(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
			APP.webSocketInteractionController.off(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
		}

		if (APP.currentWindow && APP.currentWindow.bossModeController)
		{
			APP.currentWindow.bossModeController.off(BossModeController.EVENT_DISAPPEARING_PRESENTATION_COMPLETED, this._onBossModeCompleted, this);
		}

		super.destroy();
	}
}

export default GameStateController;