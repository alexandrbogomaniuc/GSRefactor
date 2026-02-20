import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import { MAX_COUNT_INACTIVE_ROUNDS } from '../../../model/gameplay/RoundInfo';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import BetsController from '../bets/BetsController';
import RoundController from '../RoundController';
import BottomPanelController from '../../uis/custom/bottom_panel/BottomPanelController';
import DialogController from '../../uis/custom/dialogs/DialogController';
import PendingOperationController from '../PendingOperationController';
import BattlegroundCafRoomManagerDialogController from '../../uis/custom/battleground/caf/BattlegroundCafRoomManagerDialogController';
import { CAF_SUMARRY_DURATION } from '../../../config/Constants';

/**
 * Controls players entering and exiting the room
 * @class
 * @extends SimpleController
 * @inheritdoc
 */
class GamePlayersController extends SimpleController
{
	static get EVENT_ON_ROOM_JOIN_INITIATED() 				{ return "EVENT_ON_ROOM_JOIN_INITIATED" };
	static get EVENT_ON_ROOM_LEAVE_INITIATED() 				{ return "EVENT_ON_ROOM_LEAVE_INITIATED" };
	static get EVENT_ON_MASTER_PLAYER_IN() 					{ return "EVENT_ON_MASTER_PLAYER_IN" };
	static get EVENT_ON_MASTER_PLAYER_OUT() 				{ return "EVENT_ON_MASTER_PLAYER_OUT" };
	static get EVENT_ON_INACTIVE_ROUNDS_LIMIT() 			{ return "EVENT_ON_INACTIVE_ROUNDS_LIMIT" };
	static get EVENT_ON_REJOIN() 							{ return "EVENT_ON_REJOIN" };
	static get EVENT_ACTIVATE_CAF_ROOM_MANAGER()			{return "EVENT_ACTIVATE_CAF_ROOM_MANAGER"};
	static get EVENT_ROUND_ALREADY_STARTED_DIALOG()	{return "EVENT_ACTIVE_ROUND_ALREADY_STARTED_DIALOG"};
	static get EVENT_SHOW_CAF_SUMMARY()	{return "EVENT_SHOW_CAF_SUMMARY"};

	constructor(aOptInfo_usi, aOptParentController_usc)
	{
		super(aOptInfo_usi, aOptParentController_usc);

		this._fBetsController_bsc = new BetsController(this.info.betsInfo, this);
		this._fCountOfInactiveRounds_num = undefined;
		this._isInactiveRoundsLimit_bln = false;
		this._firstLaunch = true;
	}

	/**
	 * @type {BetsController}
	 */
	get betsController()
	{
		return this._fBetsController_bsc;
	}

	/**
	 * Indicates whether leaving room by master player is initiated (due to max amount of inactive rounds reached) or not.
	 * Actual for battleground mode.
	 * @type {boolean}
	 */
	get isInactiveRoundsLimit()
	{
		return this._isInactiveRoundsLimit_bln;
	}

	init()
	{
		super.init();

		this.betsController.init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let wsInteractionController = this._wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);

		this._fRoundController_rc = APP.gameController.gameplayController.roundController;
		this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);

		APP.gameController.bottomPanelController.on(BottomPanelController.EVENT_HOME_BUTTON_CLICKED, this._onHomeBtnClicked, this);

		APP.dialogsController.leaveRoomDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onLeaveRoomDialogConfirmed, this);
		APP.dialogsController.battlegroundRejoinDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onRejoinDialogConfirmed, this);

		let lPendingOperationController_poc = this._fPendingOperationController_poc = APP.gameController.gameplayController.pendingOperationController;
		lPendingOperationController_poc.on(PendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);
	}
	
	/**
	 * Handle home button click. 
	 * If master player has active bets, then leaving the room should be triggered only when player confirms leaving in the corresponding popup.
	 * @private
	 */
	_onHomeBtnClicked()
	{
		let lMasterActiveBets_bi_arr = this.info.isMasterSeatDefined ? this.info.masterPlayerInfo.activeBets : null;
		if (lMasterActiveBets_bi_arr && !!lMasterActiveBets_bi_arr.length)
		{
			// wait for confirmation from LeaveRoomDialogController
			return;
		}

		this._tryLeaveRoom();
	}

	/**
	 * Leave the room by player's confirmation.
	 * @private
	 */
	_onLeaveRoomDialogConfirmed(event)
	{
		this._tryLeaveRoom();
	}
	
	/**
	 * @ignore
	 * @deprecated
	 */
	_calculateTotalBetCurrentRoundWin() // make sense to calculate in the same cycle
	{
		let lTotalBets_num = 0;
		let lCurrentRoundWin_num = 0;
		let lMasterPlayerInfo_pi = this.info.masterPlayerInfo;
		if (!!lMasterPlayerInfo_pi)
		{
			let l_bi_arr = lMasterPlayerInfo_pi.activeBets;

			if (l_bi_arr && l_bi_arr.length)
			{
				for (let i=0; i<l_bi_arr.length; i++)
				{
					let l_bi = l_bi_arr[i];
					if (l_bi.isConfirmedMasterBet)
					{
						lTotalBets_num += l_bi.betAmount;
						if (l_bi.isEjected)
						{
							lCurrentRoundWin_num += l_bi.betWin;
						}
					}
				}
			}
		}
		return {
			totalBets: lTotalBets_num,
			currentRoundWin: lCurrentRoundWin_num
		};
	}

	/**
	 * @ignore
	 * @deprecated
	 */
	_calculateCurrentRoundWin() 
	{
		let lCurrentRoundWin_num = 0;
		let lMasterPlayerInfo_pi = this.info.masterPlayerInfo;
		if (!!lMasterPlayerInfo_pi)
		{
			let l_bi_arr = lMasterPlayerInfo_pi.activeBets;

			if (l_bi_arr && l_bi_arr.length)
			{
				for (let i=0; i<l_bi_arr.length; i++)
				{
					let l_bi = l_bi_arr[i];
					if (l_bi.isConfirmedMasterBet && l_bi.isEjected)
					{
						lCurrentRoundWin_num += l_bi.betWin;
					}
				}
			}
		}
		return lCurrentRoundWin_num;
	}

	/**
	 * Calculates total placed bets amount of master player.
	 * Only confirmed by server bets are counted.
	 * @returns {number}
	 * @private
	 */
	_calculateTotalBet() 
	{
		let lTotalBets_num = 0;
		let lMasterPlayerInfo_pi = this.info.masterPlayerInfo;
		if (!!lMasterPlayerInfo_pi)
		{
			let l_bi_arr = lMasterPlayerInfo_pi.activeBets;
			if (l_bi_arr && l_bi_arr.length)
			{
				for (let i=0; i<l_bi_arr.length; i++)
				{
					let l_bi = l_bi_arr[i];
					if (l_bi.isConfirmedMasterBet)
					{
						lTotalBets_num += l_bi.betAmount;
					}
				}
			}
		}
		return lTotalBets_num;
	}

	/**
	 * Handle server messages to have actual players info on client side. 
	 * @param {*} event 
	 * @private
	 */
	_onServerMessage(event)
	{
		let data = event.messageData;
		let requestData = event.requestData;
		switch(data.class)
		{
			case SERVER_MESSAGES.ENTER_GAME_RESPONSE:
				this.info.observerId = data.nickname;
				break;
			case SERVER_MESSAGES.CRASH_GAME_INFO:
				this.info.setPlayers(data.seats, data.isOwner);


				if(APP.isCAFMode && this._firstLaunch ){
					this._firstLaunch = false;
					this.emit(GamePlayersController.EVENT_ACTIVATE_CAF_ROOM_MANAGER, {open:true});
				}

				if (!this.info.isMasterSeatDefined)
				{
					let lRoundInfo_ri = this._fRoundController_rc.info;

					if (requestData && requestData.class === CLIENT_MESSAGES.OPEN_ROOM
							&& this.info.getPlayerInfo(this.info.observerId)
						)
					{
						this.info.isReSitInRequired = true;
					}
					
					this._tryJoinRoom();
				}

				
				

				
				break;
			case SERVER_MESSAGES.GAME_STATE_CHANGED:

				break;

			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				if (data.rid !== -1) // master seat
				{
					this.info.isReSitInRequired = false;
					this.info.addMasterPlayerInfo(data);
					this.emit(GamePlayersController.EVENT_ON_MASTER_PLAYER_IN, data);
				}
				else
				{
					if (this.info.getPlayerInfo(data.nickname))
					{
						this.info.updatePlayerInfo(data);
					}
					else
					{
						this.info.addPlayerInfo(data);
					}
				}
				this._afterInactiveRoundsLimit();
				break;
			case SERVER_MESSAGES.SIT_OUT_RESPONSE:
				let lPrevIsMasterPlayerDefined_bl = this.info.isMasterSeatDefined;
				this.info.removePlayerInfo(data.nickname);

				if (data.nickname === this.info.observerId)
				{
					this.info.isReSitInRequired = false;
				}

				if (lPrevIsMasterPlayerDefined_bl && !this.info.isMasterSeatDefined)
				{
					this.emit(GamePlayersController.EVENT_ON_MASTER_PLAYER_OUT);
				}
				break;
			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE:
				if (
					data.errorCode !== undefined 
					&& (
							APP.webSocketInteractionController.isFatalError(data.errorCode)
							|| data.errorCode === GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE
						)
					)
				{
					this.info.resetMasterSeat();
				}
				break;
		}
	}

	/**
	 * Handle server error messages (error codes) to have actual players info on client side. 
	 * @param {*} event 
	 * @private
	 */
	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		if (GameWebSocketInteractionController.isFatalError(errorType))
		{
			this.info.resetMasterSeat();
			return;
		}

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.NOT_SEATER:
				this.info.isReSitInRequired = false;

				if (this.info.isMasterSeatDefined)
				{
					this.info.removePlayerInfo(this.info.masterSeatId);
				}

				this._tryJoinRoom();
				break;
			case GameWebSocketInteractionController.ERROR_CODES.NEED_SITOUT:
				if (requestData)
				{
					if (requestData.class == CLIENT_MESSAGES.SIT_IN)
					{
						this._tryJoinRoom();
					}
					else if (requestData.class == CLIENT_MESSAGES.CLOSE_ROOM)
					{
						this._tryLeaveRoom();
					}
				}
				break;
		}
	}

	/**
	 * Reset leaving room due to max amount of inactive rounds reached state.
	 * @private
	 */
	_afterInactiveRoundsLimit()
	{
		if (this._isInactiveRoundsLimit_bln === true)
		{
			this._isInactiveRoundsLimit_bln === false;

			if (this.info.isMasterPlayerLeaveRoomTriggered)
			{
				this.info.isMasterPlayerLeaveRoomTriggered = false;
			}
		}
	}

	/**
	 * Handle closing of socket connection.
	 * @param {*} event 
	 * @private
	 */
	_onServerConnectionClosed(event)
	{
		this.info.isReSitInRequired = false;
		this.info.resetMasterSeat();
	}

	/**
	 * Checks if it's possible to join room (as a seater) at the moment.
	 * @private
	 */
	get _isRoomJoinAllowed()
	{
		let lRoundInfo_ri = this.parentController.roundController.info;
		let wsInteractionController = this._wsInteractionController;

		return !this.info.isMasterSeatDefined
				&& (!this.info.isMasterPlayerLeaveRoomTriggered || this._isInactiveRoundsLimit_bln)
				&& !this._fPendingOperationController_poc.info.isPendingOperationInProgress
				&& (
						lRoundInfo_ri.isRoundWaitState 
						|| ((lRoundInfo_ri.isRoundPlayState || lRoundInfo_ri.isRoundPauseState || lRoundInfo_ri.isRoundBuyInState) && this.info.isReSitInRequired)
					)
				&& !wsInteractionController.hasUnRespondedRequest(CLIENT_MESSAGES.SIT_IN)
				&& !wsInteractionController.hasDelayedRequests(CLIENT_MESSAGES.SIT_IN);
	}

	/**
	 * Attempt to join room (as a seater).
	 * @private
	 */
	_tryJoinRoom()
	{
		if (this._isRoomJoinAllowed)
		{
			this.emit(GamePlayersController.EVENT_ON_ROOM_JOIN_INITIATED);
		}
	}

	/**
	 * Checks if it's possible to leave room (as a seater) at the moment.
	 * @private
	 */
	get _isRoomLeaveAllowed()
	{
		let lRoundInfo_ri = this.parentController.roundController.info;
		let wsInteractionController = this._wsInteractionController;
		
		return 	this.info.isMasterSeatDefined
				&& !this.info.isMasterPlayerLeaveRoomTriggered
				&& !lRoundInfo_ri.isRoundBuyInState
				&& !lRoundInfo_ri.isRoundPauseState
				&& !this._fPendingOperationController_poc.info.isPendingOperationInProgress;
	}

	/**
	 * Attempt to leave room (as a seater).
	 * @private
	 */
	_tryLeaveRoom()
	{
		if (this._isRoomLeaveAllowed)
		{
			this.info.isMasterPlayerLeaveRoomTriggered = true;
			this.emit(GamePlayersController.EVENT_ON_ROOM_LEAVE_INITIATED);
		}
	}

	reJoinGame()
	{
		this._tryJoinRoom();
	}

	_onRoundStateChanged(event)
	{
		
		let lRoundInfo_ri = this._fRoundController_rc.info;
		console.log("event round state " + lRoundInfo_ri.roundState);
		APP.forcedState = null;
		if (lRoundInfo_ri.isRoundWaitState || lRoundInfo_ri.isRoundPlayState)
		{
			if (!this.info.isMasterSeatDefined)
			{
				this._tryJoinRoom();
			}
		}

		if (APP.isBattlegroundGame)
		{
			if (lRoundInfo_ri.isRoundWaitState)
			{
				if(this._fCountOfInactiveRounds_num === undefined)
				{
					this._fCountOfInactiveRounds_num = 0;
				}
				if(APP.isCAFMode && !this._firstLaunch && !APP.isKicked)
				{
					setTimeout(()=>{
						
						this.emit(GamePlayersController.EVENT_ACTIVATE_CAF_ROOM_MANAGER, {open:true});
					},CAF_SUMARRY_DURATION);
					
				}
			}
	
			if (lRoundInfo_ri.isRoundPlayState)
			{
				if(APP.isCAFMode){
					
					this.emit(GamePlayersController.EVENT_ACTIVATE_CAF_ROOM_MANAGER, {open:false});
					if(!APP.isCAFRoomManager){
						let asignerController = APP.dialogsController._cafGuestController;
						if(!asignerController.info.isReadyConfirmed && !APP.isKicked) 
						{
							this.emit(GamePlayersController.EVENT_ROUND_ALREADY_STARTED_DIALOG, {open:true});
						}
					}
					
				}
				
				let lCurrentBet_num = this._calculateTotalBet();
				if (!lCurrentBet_num)
				{
					if (this._fCountOfInactiveRounds_num !== undefined)
					{
						if (this._fCountOfInactiveRounds_num < MAX_COUNT_INACTIVE_ROUNDS)
						{
							this._fCountOfInactiveRounds_num++;
						} 
						else 
						{
							this._isInactiveRoundsLimit_bln = true;
							this._fCountOfInactiveRounds_num = undefined;
	
							this._tryLeaveRoom()
							this.emit(GamePlayersController.EVENT_ON_INACTIVE_ROUNDS_LIMIT);
						}
					} 
				}
				else
				{
					this._fCountOfInactiveRounds_num = undefined;
				}
			}
		}
	}

	_onRejoinDialogConfirmed(event)
	{
		this.emit(GamePlayersController.EVENT_ON_REJOIN);
	}

	_onPendingOperationCompleted(aEvent_e)
	{
		let lRoundInfo_ri = this._fRoundController_rc.info;
		if (lRoundInfo_ri.isRoundWaitState || lRoundInfo_ri.isRoundPlayState)
		{
			if (!this.info.isMasterSeatDefined)
			{
				this._tryJoinRoom();
			}
		}
	}
}

export default GamePlayersController;