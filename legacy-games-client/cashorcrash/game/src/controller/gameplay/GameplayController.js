import SimpleUIController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { SERVER_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';
import RoundController from './RoundController';
import RoomController from './RoomController';
import CrashAPP from '../../CrashAPP';
import GameplayInfo from '../../model/gameplay/GameplayInfo';
import GamePlayersController from './players/GamePlayersController';
import DOMLayout from '../../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';
import GameplayView from '../../view/gameplay/GameplayView';
import DialogsController from '../uis/custom/dialogs/DialogsController';
import DialogsInfo from '../../model/uis/custom/dialogs/DialogsInfo';
import PendingOperationController from './PendingOperationController';
import { ROUND_STATES } from '../../model/gameplay/RoundInfo';
import BalanceController from '../main/BalanceController';
import GameplayBackgroundView from '../../view/gameplay/background/GameplayBackgroundView';

/**
 * Controls gameplay.
 * @class
 * @extends SimpleUIController
 * @inheritdoc
 */
class GameplayController extends SimpleUIController
{
	static get EVENT_ON_EJECT_INITIATED()								{ return GameplayView.EVENT_ON_EJECT_INITIATED };
	static get EVENT_ON_EJECT_ALL_INITIATED()							{ return GameplayView.EVENT_ON_EJECT_ALL_INITIATED };
	static get EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED()			{ return GameplayView.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED };
	static get EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED ()				{ return GameplayView.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED };
	static get EVENT_ON_ASTRONAUT_EJECT_STARTED()						{ return GameplayView.EVENT_ON_ASTRONAUT_EJECT_STARTED };
	static get EVENT_ON_ASTRONAUT_LANDED ()								{ return GameplayView.EVENT_ON_ASTRONAUT_LANDED };
	static get EVENT_ON_TIMER_VALUE_UPDATED()							{ return GameplayView.EVENT_ON_TIMER_VALUE_UPDATED };
	static get EVENT_ON_ASTRONAUT_ENTERS_ROCKET()						{ return GameplayView.EVENT_ON_ASTRONAUT_ENTERS_ROCKET };
	static get EVENT_ON_ASTRONAUT_EXIT_ROCKET()							{ return GameplayView.EVENT_ON_ASTRONAUT_EXIT_ROCKET };
	static get EVENT_ON_ASTRONAUT_TRAMPOLINE()							{ return GameplayView.EVENT_ON_ASTRONAUT_TRAMPOLINE };
	static get EVENT_ON_YOU_WON_ANIMATION_STARTED()						{ return GameplayView.EVENT_ON_YOU_WON_ANIMATION_STARTED; }
	static get EVENT_ON_WIN_CROWN_ANIMATION_STARTED()					{ return GameplayView.EVENT_ON_WIN_CROWN_ANIMATION_STARTED; }
	static get EVENT_TMB_ROUND_ENDED()									{ return "EVENT_TMB_ROUND_ENDED";}
	static get REVALIDATE_CAF_ROOM_MANAGER()							{ return "REVALIDATE_CAF_ROOM_MANAGER";}
	static get EVENT_NEW_ASTEROID()										{ return "EVENT_NEW_ASTEROID";}
	
	static get EVENT_ON_GAMEPLAY_TIME_UPDATED()							{ return 'onGameplayTimeUpdated' }
	static get EVENT_ON_ALL_ASTRONAUTS_EJECTED_BTG()					{ return 'onAllAstronautsEjectedBTG' }
	static get LATENCY_REQUEST()										{ return 'LATENCY_REQUEST' }

	/**
	 * @type {RoundController}
	 */
	get roundController()
	{
		return this._fRoundController_rsc;
	}

	/**
	 * @type {RoomController}
	 */
	get roomController()
	{
		return this._fRoomController_rc;
	}

	/**
	 * @type {GamePlayersController}
	 */
	get gamePlayersController()
	{
		return this._fGamePlayersController_rsc;
	}

	/**
	 * @type {PendingOperationController}
	 */
	get pendingOperationController()
	{
		return this._fPendingOperationController_poc;
	}

	constructor(aModel_gpi, aView_gpv, aParentController_usc)
	{
		super(aModel_gpi, aView_gpv, aParentController_usc);

		this._fPendingOperationController_poc = new PendingOperationController(this.info.pendingOperationInfo, this)
		this._fRoomController_rc = new RoomController(this.info.roomInfo, this);
		this._fRoundController_rsc = new RoundController(this.info.roundInfo, this);
		this._fGamePlayersController_rsc = new GamePlayersController(this.info.gamePlayersInfo, this);
	}

	init()
	{
		super.init();

		this.pendingOperationController.init();
		this.roomController.init();
		this.roundController.init();
		this.gamePlayersController.init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);
		APP.layout.on(DOMLayout.EVENT_ON_ORIENTATION_CHANGED, this._onAppOrientationChange, this);

		APP.on(CrashAPP.EVENT_ON_APPLICATION_FATAL_ERROR, this._onApplicationFatalErrorOccured, this);
		APP.on(CrashAPP.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onServerConnectionOpened, this)

		this.roundController.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);
		this.roundController.on(RoundController.EVENT_ON_ROUND_ID_CHANGED, this._onRoundIdChanged, this);

		this.roomController.on(RoomController.EVENT_ON_ROOM_ID_CHANGED, this._onRoomIdChanged, this);

		APP.dialogsController.on(DialogsController.EVENT_DIALOG_ACTIVATED, this._onSomeDialogActivated, this);
		this.gamePlayersController.on(GamePlayersController.EVENT_ON_INACTIVE_ROUNDS_LIMIT, this._onInactiveRoundsLimit, this);

		APP.gameController.balanceController.on(BalanceController.BALANCE_REFRESH_COMPLETED, this._onBalanceRefreshCompleted, this);
	}


	_onBalanceRefreshCompleted(event)
	{
		this.view.lastBalanceUpdate(Date.now());
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		this.view.on(GameplayView.EVENT_ON_EJECT_INITIATED, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_EJECT_ALL_INITIATED, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_ASTRONAUT_EJECT_STARTED, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_ASTRONAUT_LANDED, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_TIMER_VALUE_UPDATED, (event)=>{
			if(event.restTime<3000){
				if(!this.roundController.info.lastSecondsStarted)
				{
					if(!APP.isBattlegroundGame){
						this.emit(GameplayController.EVENT_TMB_ROUND_ENDED);
					}
				}
				this.roundController.info.lastSecondsStarted = true;
			}else if (event.restTime>=3000)
			{
				this.roundController.info.lastSecondsStarted = false;
			}
			this.emit(GameplayController.EVENT_ON_TIMER_VALUE_UPDATED, event);
		}, this);
		this.view.on(GameplayView.EVENT_ON_ASTRONAUT_ENTERS_ROCKET, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_ASTRONAUT_EXIT_ROCKET, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_ASTRONAUT_TRAMPOLINE, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_YOU_WON_ANIMATION_STARTED, this.emit, this);
		this.view.on(GameplayView.EVENT_ON_WIN_CROWN_ANIMATION_STARTED, this.emit, this);
	}

	/**
	 * Handle server messages to validate gameplay data on client side. 
	 * @param {*} event 
	 * @private
	 */
	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.CRASH_GAME_INFO:
				if (data.function !== undefined)
				{
					this.info.updateMultiplierExpression(data.function);
				}

				if (data.kilometerMult !== undefined && APP.isBattlegroundGame)
				{
					this.info.distanceMultiplier = data.kilometerMult;
				}

				if (data.state === ROUND_STATES.PLAY)
				{
					if (data.date >= data.startTime)
					{
						this.info.serverMultiplierRecieveTime = data.date;
					}

					if (data.currentMult != undefined && data.currentMult >= this.info.minMultiplierValue)
					{
						this.info.serverMultiplierValue = 1 + (data.currentMult-1)*this.info.distanceMultiplier;
						this.info.serverMultiplierRecieveTime = data.date;
						this.info.lastServerMultiplierRecievedValue = +data.currentMult;
						this.info.serverMultiplierTime = this.info.multiplierChangeFlightStartTime + this.info.calculateTime(+data.currentMult);

						this.info.multiplierTime = data.date;
						this.info.multiplierValue = this.info.serverMultiplierValue;
						this.info.prevServerMultiplierValue = data.currentMult;
					}

					if (APP.isBattlegroundGame && data.timeSpeedMult > 1)
					{
						this.info.timeMultiplier = data.timeSpeedMult;
						if (!this.info.allEjectedTime && data.allEjectedTime !== undefined)
						{
							this.info.allEjectedTime = data.allEjectedTime;
							this.emit(GameplayController.EVENT_ON_ALL_ASTRONAUTS_EJECTED_BTG);
						}
					}
				}
				break;
			case SERVER_MESSAGES.CRASH_STATE_INFO:
				let l_ri = this.roundController.info;

				if(data.crash == true)
				{
					GameplayBackgroundView.TILESET_ASTEROIDS.crash();
					GameplayBackgroundView.TILESET_ASTEROIDS_BTG.crash();
				}

				if(data.asteroid && APP.isBattlegroundGame)
				{
					/**
					 * 
					 * {type: 11, speed: 0.70315, x: 93, y: 0, slow: 62}
					 */
					const dataDif  = (Date.now() - data.date);
					APP.gameController.gameplayController.info.newAsteroid = {
						type:data.asteroid.type, dateDif:dataDif, 
						totalTime: data.asteroid.speed * 900, 
						startXPercent:data.asteroid.x * 0.01, 
						startYPercent:data.asteroid.y * 0.01, 
						slowDownAt:data.asteroid.slow * 0.01, 
						roundId:APP.roundId,
						currentMult:data.currentMult
					};
					this.emit(GamePlayersController.EVENT_NEW_ASTEROID);
				}
				
				if(!l_ri.isRoundPlayActive && data.state == "PLAY")
				{
					
					APP.webSocketInteractionController.forcePlayState(l_ri.roundId);
					
				}

				if (data.currentMult <= this.info.prevServerMultiplierValue && (data.crash !== true || !l_ri.isRoundPlayActive)) break;

				this.info.serverMultiplierValue = 1 + (data.currentMult-1)*this.info.distanceMultiplier;
				this.info.serverMultiplierRecieveTime = data.date;
				this.info.lastServerMultiplierRecievedValue = +data.currentMult;
				if(data.date)
				{
					this.info.serverMultiplierTime = data.date;
				}else if(l_ri.isRoundPlayActive)
				{
					this.info.serverMultiplierTime = Date.now();
				}
				
				this.info.multiplierTime = Math.max(data.date, this.info.multiplierTime);
				this.info.multiplierValue = Math.max(this.info.serverMultiplierValue, this.info.multiplierValue);
				this.info.prevServerMultiplierValue = data.currentMult;

				if (APP.isBattlegroundGame && data.timeSpeedMult > 1)
				{
					this.info.timeMultiplier = data.timeSpeedMult;
					if (!this.info.allEjectedTime && data.allEjectedTime !== undefined)
					{
						this.info.allEjectedTime = data.allEjectedTime;
						this.emit(GameplayController.EVENT_ON_ALL_ASTRONAUTS_EJECTED_BTG);
					}
				}

				break;
			case SERVER_MESSAGES.ROUND_RESULT:
				
				if (data.crashMultiplier !== undefined)
				{
					this.info.serverMultiplierValue = 1 + (data.crashMultiplier-1)*this.info.distanceMultiplier;
					this.info.lastServerMultiplierRecievedValue = +data.crashMultiplier;
					this.info.multiplierValue = Math.max(this.info.serverMultiplierValue, this.info.multiplierValue);
				}

				if(APP.isBattlegroundGame)
				{
					let l_brasv = GameplayBackgroundView.TILESET_ASTEROIDS_BTG;
					l_brasv.endOfGame();
				}
				
				break;
			case SERVER_MESSAGES.OK:
				this.view && this.view.updateDebugIndicators(true);
				break;
			case  SERVER_MESSAGES.LATENCY:
				this._onServerLatencyRequest(data);
				break;
			case SERVER_MESSAGES.GAME_STATE_CHANGED:
				
				if(data.state == ROUND_STATES.PLAY && APP.isBattlegroundGame)
				{
					let l_brasv = GameplayBackgroundView.TILESET_ASTEROIDS_BTG;
					l_brasv.endOfGame();
				}
				break;
		}
	}

	_onServerLatencyRequest(aEvent_obj) {
			/*{
			"rid": 1,
			"step": 1, //it could be [1,2,3,4]
			"class": "Latency",
			"serverTs": "1709640957383, //timestamp
			"serverAckTs": 1709640957456,
			"clientTs": 1709640957420,
			"clientAckTs": 1709640957560
			"latencyValue":optional float
		}*/

		const step = aEvent_obj.step;
		const serverTs = aEvent_obj.serverTs;
		const serverAckTs = aEvent_obj.serverAckTs;
		const clientTs = aEvent_obj.clientTs;
		const clientAckTs = aEvent_obj.clientAckTs;
		const serverLatency = aEvent_obj.latencyValue;
		let serverMessage;
		switch (step) 
		{
			case 1:

				serverMessage = {
					serverTs: serverTs,
					serverAckTs: serverAckTs,
					clientTs: Date.now(),
					clientAckTs: clientAckTs,
					step: 2
				}

				break;
			case 3:
				serverMessage = {
					serverTs: serverTs,
					serverAckTs: serverAckTs,
					clientTs: clientTs,
					clientAckTs: Date.now(),
					step: 4
				}
				break;

		}

		if(serverMessage.step == 4){
			const finalLatency = serverLatency || ((serverMessage.serverAckTs - serverMessage.serverTs) + (serverMessage.clientAckTs - serverMessage.clientTs)) * 0.5;
			this.view.updateLatency(finalLatency);
		}
	
		this.emit(GameplayController.LATENCY_REQUEST, serverMessage);

	}

	_onRoomIdChanged(event)
	{
		this.view && this.view.updateDebugIndicators();
	}

	_onRoundStateChanged(event)
	{
		let l_ri = this.roundController.info;
		if (l_ri.isRoundWaitState)
		{
			this.info.resetMultiplier();
		}
	}

	_onRoundIdChanged(event)
	{
		if (this.view)
		{
			this.view.adjustRandomElements();
			this.view.updateDebugIndicators();
		}
	}

	_onSomeDialogActivated(event)
	{
		if (event.dialogId == DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS)
		{
			this.view.interruptAstronautAnimations();
		}
	}

	/**
	 * Update multiplier data on every tick.
	 * @param {*} event 
	 * @private
	 */
	_onTickTime(event)
	{
		let wsController = APP.webSocketInteractionController;

		let l_ri = this.roundController.info

		if (!wsController || !wsController.info.isLastServerMessageTimeDefined)
		{
			return;
		}

		if (this.info.serverMultiplierRecievedValueDefined)
		{
			this.info.serverMultiplierTime = this.info.multiplierChangeFlightStartTime + this.info.calculateTime(this.info.lastServerMultiplierRecievedValue);
		}

		if (l_ri.isRoundPlayActive)
		{
			let lCurCalculatedMultTime_num = Math.max(APP.appClientServerTime, this.info.multiplierTime);
			
			let lServerMultiplierUpdateLagDuration_num = 0;
			if (this.info.serverMultiplierTimeDefined)
			{
				lServerMultiplierUpdateLagDuration_num = (lCurCalculatedMultTime_num - this.info.serverMultiplierRecieveTime) - GameplayInfo.MULTIPLIER_MAX_LAG_DELAY;
			}
			else
			{
				lServerMultiplierUpdateLagDuration_num = (lCurCalculatedMultTime_num - this.info.multiplierChangeFlightStartTime) - GameplayInfo.MULTIPLIER_MAX_LAG_DELAY;
			}

			if (lServerMultiplierUpdateLagDuration_num > 0)
			{
				// should not update client-side multiplierTime/multiplierValue, wait for actual info from the server
			}
			else
			{
				this.info.multiplierTime = lCurCalculatedMultTime_num;
				this.info.multiplierValue = this.info.calculateMultiplier(this.info.multiplierRoundDuration);
			}
		}

		this.info.gameplayTime = Math.max(APP.appClientServerTime, this.info.gameplayTime);
		this.emit(GameplayController.EVENT_ON_GAMEPLAY_TIME_UPDATED);

		this.view && this.view.adjust();

		//for debug...
		// if (this.info.gamePlayersInfo.betsInfo.isNoMoreBetsPeriodMode && this.info.roundStartRestTime <= 5000 && this.info.roundInfo.isRoundWaitState)
		// {
		// 	APP.webSocketInteractionController._processServerMessage({"state":"BUY_IN", "roundId": this.info.roundInfo.roundId, "ttnx": this.info.roundStartRestTime, "date": APP.appClientServerTime,"rid":-1,"class":"GameStateChanged"});
		// }
		//...for debug
	}


	_onServerConnectionOpened(event){
		APP.isOnline = true;
		if(this._wasClosed && APP.isCAFMode){
			this.emit(GameplayController.REVALIDATE_CAF_ROOM_MANAGER);
			this._wasClosed = false;
		}
	}


	_onServerConnectionClosed(event)
	{
		APP.isOnline = false;
		this.info.resetMultiplier();
		this.view.interruptAstronautAnimations();
		if(APP.isCAFMode){

			this._wasClosed = true;
			
		}
	}

	_onApplicationFatalErrorOccured(event)
	{
		this.info.resetMultiplier();
		this.view.interruptAstronautAnimations();
	}

	_onInactiveRoundsLimit(event)
	{
		this.info.resetMultiplier();
	}

	_onRoomPaused(event)
	{
		this.view.onRoomPaused();
	}

	//ORIENTATION...
	_onAppOrientationChange(event)
	{
		this.view.updateArea();
	}
	//...ORIENTATION
}

export default GameplayController;