import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import BalanceInfo from '../../model/main/BalanceInfo';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import CrashAPP from '../../CrashAPP';
import BetsController from '../gameplay/bets/BetsController';
import RoundController from '../gameplay/RoundController';
import GamePlayersController from '../gameplay/players/GamePlayersController';
import PendingOperationController from '../gameplay/PendingOperationController';

const DEFAULT_REFRESH_INTERVAL = 8000;

class BalanceController extends SimpleController
{
	static get EVENT_ON_BALANCE_UPDATED() 					{return "EVENT_ON_BALANCE_UPDATED";}
	static get BALANCE_REFRESH_COMPLETED()					{return "BALANCE_REFRESH_COMPLETED";}
	static get EVENT_SERVER_BALANCE_REFRESH_REQUIRED() 		{return "EVENT_SERVER_BALANCE_REFRESH_REQUIRED";}

	constructor(aOptInfo_si)
	{
		super(aOptInfo_si || new BalanceInfo());

		this._fRefreshTimer_tmr = null;
		this._fLastRefreshTimerCycleCompleteTime_int = undefined;
		this._fRefundAmount_num = null;
	}

	__initModelLevel()
	{
		super.__initModelLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
		
		APP.on(CrashAPP.EVENT_ON_APPLICATION_FATAL_ERROR, this._onApplicationFatalErrorOccured, this);

		let lPlayersController_gpsc = this._fPlayersController_gpsc = APP.gameController.gameplayController.gamePlayersController;
		let lBetsController_bsc = this._fBetsController_bsc = lPlayersController_gpsc.betsController;
		lBetsController_bsc.on(BetsController.EVENT_ON_BETS_ACCEPTED, this._onNewBetAccepted, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_CONFIRMED, this._onBetConfirmedByTheServer, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_NOT_CONFIRMED, this._onBetNotConfirmedByTheServer, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_CANCELLED, this._onBetCancelled, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BETS_UPDATED, this._onBetsUpdated, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_ALL_PLAYER_BETS_CONFIRMED_BY_SERVER, this._onAllPlayerBetsConfirmedByServer, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_ALL_PLAYER_BETS_REJECTED_BY_SERVER, this._onAllPlayerBetsRejectedByServer, this);

		this._fRoundController_rc = APP.gameController.gameplayController.roundController;
		this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);

		let lPendingOperationController_poc = this._fPendingOperationController_poc = APP.gameController.gameplayController.pendingOperationController;
		lPendingOperationController_poc.on(PendingOperationController.EVENT_ON_PENDING_OPERATION_STARTTED, this._onPendingOperationStarted, this);
		lPendingOperationController_poc.on(PendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.ENTER_GAME_RESPONSE:
			case SERVER_MESSAGES.BALANCE_UPDATED:
				this.info.serverBalance = data.balance;

				if (data.rid == -1)
				{
					let lIsServerBalanceIncludesBetWins_bl = this._fBetsController_bsc.info.isExistentClientBetsClearedOnServer;
					this.info.isAddBetsAllowed = !lIsServerBalanceIncludesBetWins_bl;
				}

				this._fRefundAmount_num = 0;
				this._validateBalanceValue();

				this._destroyBalanceRefreshTimer();
				this._initBalanceRefreshTimer();

				break;
			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_CONFIRMED_RESPONSE:
				this.info.serverBalance = data.balance;
				break;
			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				if (data.rid !== -1) // master seat
				{
					this.info.serverBalance = data.balance;
					this._validateBalanceValue();
				}
				break;
			case SERVER_MESSAGES.CRASH_BET_RESPONSE:
				if (data.rid !== -1) // master seat
				{
					this.info.serverBalance = data.balance;
					let lBetId_str = data.crashBetKey;
					let lBetInfo_bi = this._fBetsController_bsc.info.getBetInfo(lBetId_str);

					if (lBetInfo_bi && lBetInfo_bi.isConfirmedMasterBet)
					{
						this._validateBalanceValue();
					}
				}
				break;
			case SERVER_MESSAGES.CRASH_CANCEL_BET_RESPONSE:
				if (data.balance !== undefined)
				{
					this.info.serverBalance = data.balance;
					// _validateBalanceValue will be called from _onBetCancelled handler
				}
				break;
			case SERVER_MESSAGES.CRASH_GAME_INFO:
				this.info.isAddBetsAllowed = true;
				break;

			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE:
				if (data.balance !== undefined)
				{
					this.info.serverBalance = data.balance;
				}
				
				if (data.errorCode !== undefined)
				{
					if (
							APP.webSocketInteractionController.isFatalError(data.errorCode) 
							|| data.errorCode === GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE
						)
					{
						this._destroyBalanceRefreshTimer();
						this.info.isAddBetsAllowed = true;
					}
					else
					{
						if (
								data.errorCode === GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY
								|| data.errorCode === GameWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS
							)
						{
							this._requestRefreshBalance();
						}
					}
				}
				break;
			case SERVER_MESSAGES.CANCEL_BATTLEGROUND_ROUND:
				if (data.refundedAmount)
				{
					this._fRefundAmount_num = data.refundedAmount;
					this._validateBalanceValue();
				}
				break;
		}
	}

	/*  
	DEBUG...
	refundAmount(aValue)
	{
		this._fRefundAmount_num = aValue;
		this._validateBalanceValue();
	}
	...DEBUG
	*/

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		if (GameWebSocketInteractionController.isFatalError(errorType))
		{
			this._destroyBalanceRefreshTimer();
			this.info.isAddBetsAllowed = true;
			return;
		}

		let requestClass = undefined;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}

		switch (serverData.code) 
		{
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
			case GameWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS:
				this._requestRefreshBalance();
				break;
		}
	}

	_onRoundStateChanged(event)
	{
		let l_ri = this._fRoundController_rc.info;
		if (l_ri.isRoundWaitState)
		{
			this.info.isAddBetsAllowed = true;
		}

		let wsInteractionController = APP.webSocketInteractionController;
		if (!l_ri.isRoundPlayState)
		{
			if (
					!this._fRefreshTimer_tmr
					&& this._fLastRefreshTimerCycleCompleteTime_int !== undefined
					&& !wsInteractionController.hasUnRespondedRequest(CLIENT_MESSAGES.REFRESH_BALANCE)
    				&& !wsInteractionController.hasDelayedRequests(CLIENT_MESSAGES.REFRESH_BALANCE)
    				&& !this._fPendingOperationController_poc.info.isPendingOperationInProgress
				)
			{
				let lRefreshRequestDelayDuration_int = Date.now() - this._fLastRefreshTimerCycleCompleteTime_int;			
				if (lRefreshRequestDelayDuration_int < this.__refreshTimerDuration)
				{
					let lRestTimerDuration_int = this.__refreshTimerDuration - lRefreshRequestDelayDuration_int;
					this._initBalanceRefreshTimer(lRestTimerDuration_int);
	}
				else
				{
					this._requestRefreshBalance();
				}
			}
		}
	}

	_onNewBetAccepted(event)
	{
		this._validateBalanceValue();
	}

	_onBetConfirmedByTheServer(event)
	{
		this._validateBalanceValue();
	}

	_onBetNotConfirmedByTheServer(event)
	{
		this._validateBalanceValue();
	}

	_onBetCancelled(event)
	{
		this._validateBalanceValue();
	}

	_onBetsUpdated(event)
	{
		this._validateBalanceValue();
	}

	_onAllPlayerBetsConfirmedByServer(event)
	{
		this._validateBalanceValue();
	}

	_onAllPlayerBetsRejectedByServer(event)
	{
		if (event.seatId === this._fPlayersController_gpsc.info.masterSeatId)
		{
			this._validateBalanceValue();
		}
	}

	_onPendingOperationStarted(event)
	{
		this._validateBalanceValue();
	}

	_onPendingOperationCompleted(event)
	{
		this._validateBalanceValue();
	}

	_validateBalanceValue()
	{
		let lServerBalance_num = this.info.serverBalance;
		let lClientBalance_num = lServerBalance_num;
		let lPlayersInfo_gpsi = this._fPlayersController_gpsc.info;
		let lBetsInfo_bsi = lPlayersInfo_gpsi.betsInfo;
		let lRoundInfo_ri = lPlayersInfo_gpsi.roundInfo;
		let lMasterPlayerInfo_gpi = lPlayersInfo_gpsi.masterPlayerInfo;
		let lObserverId_str = lPlayersInfo_gpsi.observerId;
		let lMasterBets_bi_arr = !!lMasterPlayerInfo_gpi ? lMasterPlayerInfo_gpi.bets
														: !!lObserverId_str ? this._fBetsController_bsc.info.getPlayerBets(lObserverId_str) : null;
		
		let lNotConfirmedBetsAmount_num = 0;
		let lEjectedBetsAmount_num = 0;
		if (!!lMasterBets_bi_arr && this.info.isAddBetsAllowed)
		{
			for (let i=0; i<lMasterBets_bi_arr.length; i++)
			{
				let lCurMasterBetInfo_bi = lMasterBets_bi_arr[i];
				
				if (lCurMasterBetInfo_bi.isBetWinDefined && !lCurMasterBetInfo_bi.isDeactivatedBet)
				{
					lEjectedBetsAmount_num += this._fPendingOperationController_poc.info.isPendingOperationInProgress ? 0 : lCurMasterBetInfo_bi.betWin;
				}
				else if (
							!!lMasterPlayerInfo_gpi 
							&& (
									(!lCurMasterBetInfo_bi.isConfirmedMasterBet && !lCurMasterBetInfo_bi.isDeactivatedBet)
									|| (
											lBetsInfo_bsi.isNoMoreBetsPeriodMode 
											&& !lRoundInfo_ri.isRoundPlayState 
											&& !lCurMasterBetInfo_bi.isExternallyConfirmedMasterBet 
											&& !lCurMasterBetInfo_bi.isDeactivatedBet
										)
								)
						)
				{
					lNotConfirmedBetsAmount_num += lCurMasterBetInfo_bi.betAmount;
				}
			}
		}
		
		
		if(APP.isBattlegroundGame)
		{
			lClientBalance_num = lServerBalance_num - lNotConfirmedBetsAmount_num + lEjectedBetsAmount_num + this._fBetsController_bsc.info.canceledBetAmount;
			lClientBalance_num += this._fRefundAmount_num; 
		}else{
			lClientBalance_num = lServerBalance_num + this._fBetsController_bsc.info.canceledBetAmount;
		}
		
		this._updateBalanceIfRequired(lClientBalance_num);
	}

	_updateBalanceIfRequired(aRecievedBalance_num)
	{
		if (!this.info.isBalanceValueDefined)
		{
			this._updateBalance(aRecievedBalance_num)
			return;
		}

		let lNewBalance_num = aRecievedBalance_num;
		let lCurBalance_num = this.info.balance;

		if (lNewBalance_num !== lCurBalance_num)
		{
			this._updateBalance(lNewBalance_num);
		}


	}

	_updateBalance(aNewBalance_num)
	{
		this.info.balance = aNewBalance_num;

		this.emit(BalanceController.EVENT_ON_BALANCE_UPDATED);
	}

	_initBalanceRefreshTimer(aOptTimerDuration_int)
	{
		this.emit(BalanceController.BALANCE_REFRESH_COMPLETED);
		if (!this._fRefreshTimer_tmr)
		{
			this._fLastRefreshTimerCycleCompleteTime_int = undefined;
			
			let lFreq_num = aOptTimerDuration_int > 0 ? aOptTimerDuration_int : this.__refreshTimerDuration;
			this._fRefreshTimer_tmr = new Timer(this._onBalanceRefreshTimerCycleCompleted.bind(this), lFreq_num);
		}
	}

	get __refreshTimerDuration()
	{
		let lFreq_num = APP.appParamsInfo.updateBalanceFrequency || DEFAULT_REFRESH_INTERVAL;

		return lFreq_num;
	}

	_onBalanceRefreshTimerCycleCompleted()
	{
		this._destroyBalanceRefreshTimer();

		let l_ri = this._fRoundController_rc.info;
		if (l_ri.isRoundPlayState)
		{
			// should not send RefreshBalance due to https://jira.dgphoenix.com/browse/CRG-429
			this._fLastRefreshTimerCycleCompleteTime_int = Date.now();
		}
		else
		{
			this._requestRefreshBalance();
		}
	}

	_requestRefreshBalance()
	{
		this._destroyBalanceRefreshTimer();

		this.emit(BalanceController.EVENT_SERVER_BALANCE_REFRESH_REQUIRED);
	}

	_destroyBalanceRefreshTimer()
	{
		this._fRefreshTimer_tmr && this._fRefreshTimer_tmr.destructor();
		this._fRefreshTimer_tmr = null;
	}

	_onApplicationFatalErrorOccured(event)
	{
		this._destroyBalanceRefreshTimer();
	}

	_onServerConnectionClosed(event)
	{
		this._destroyBalanceRefreshTimer();
		this.info.isAddBetsAllowed = true;
	}
}

export default BalanceController;