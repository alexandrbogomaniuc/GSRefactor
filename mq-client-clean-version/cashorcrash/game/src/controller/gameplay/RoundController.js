import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { SERVER_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';
import CrashAPP from '../../CrashAPP';
import GamePlayersController from './players/GamePlayersController';

/**
 * Controls round states.
 * @class
 * @extends SimpleController
 */
class RoundController extends SimpleController
{
	static get EVENT_ON_ROUND_STATE_CHANGED() 					{return "EVENT_ON_ROUND_STATE_CHANGED";}
	static get EVENT_ON_ROUND_ID_CHANGED() 						{return "EVENT_ON_ROUND_ID_CHANGED";}
	

	constructor(aOptInfo_usi, aOptParentController_usc)
	{
		super(aOptInfo_usi, aOptParentController_usc);
	}

	init()
	{
		super.init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(CrashAPP.EVENT_ON_APPLICATION_FATAL_ERROR, this._onApplicationFatalErrorOccured, this);
		APP.gameController.gameplayController.gamePlayersController.on(GamePlayersController.EVENT_ON_INACTIVE_ROUNDS_LIMIT, this._onInactiveRoundsLimit, this)

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
	}

	/**
	 * Handle server messages to have actual round info on client side. 
	 * @param {*} event 
	 * @private
	 */
	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.CRASH_GAME_INFO:
				this._updateRoundIdIfRequired(data.roundId);
				this._updateRoundStateIfRequired(data.state);

				this.info.buyInStateStartTime = undefined;
				this.info.pauseStateStartTime = undefined;

				if (this.info.isRoundPlayState && !APP.isCAFMode)
				{
					this.info.roundStartTime = data.startTime;
					this.info.resetBattlegroundRoundResults();

					if (data.crash == true && !this.info.isRoundEndTimeDefined)
					{
						this.info.roundEndTime = data.date;
					}

				}

				if (this.info.isRoundWaitState )
				{
					this.info.roundStartTime = data.date + data.ttnx;
					this.info.resetRoundEndTime();
					this.info.resetRoundResultsRecieved();
				}

				if (this.info.isRoundBuyInState)
				{
					this.info.roundStartTime = data.date + data.ttnx;
				}

				if (this.info.isRoundPauseState)
				{
					this.info.roundStartTime = data.date + data.ttnx;
				}
				break;
			case SERVER_MESSAGES.GAME_STATE_CHANGED:
				this._updateRoundIdIfRequired(data.roundId);
				APP.roundId = data.roundId;
				this._updateRoundStateIfRequired(data.state);

				if (this.info.isRoundPlayState)
				{
					this.info.roundStartTime = data.roundStartTime || data.date;
					this.info.resetRoundEndTime();
					this.info.resetRoundResultsRecieved();
					this.info.resetBattlegroundRoundResults();
				}

				if (this.info.isRoundQualifyState && !this.info.isRoundEndTimeDefined)
				{
					this.info.roundEndTime = data.date;
				}

				if (this.info.isRoundWaitState)
				{
					this.info.roundStartTime = data.roundStartTime || data.date + data.ttnx;
					this.info.resetRoundEndTime();
					this.info.resetRoundResultsRecieved();
				}

				if (this.info.isRoundBuyInState)
				{
					this.info.buyInStateStartTime = data.date;
					this.info.roundStartTime = data.date + data.ttnx;
				}

				if (this.info.isRoundPauseState)
				{
					this.info.pauseStateStartTime = data.date;
					this.info.roundStartTime = data.date + data.ttnx;
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
					this.info.reset();
				}
				break;
			
			case SERVER_MESSAGES.ROUND_RESULT:
				if (this.info.isRoundPlayState)
				{
					this.info.isRoundResultRecieved = true;

					if (!this.info.isRoundEndTimeDefined)
					{
						this.info.roundEndTime = data.date;
					}

					if (APP.isBattlegroundGame)
					{
						this._updateBattlegroundRoundResults(data);
					}
				}
			break;

			case SERVER_MESSAGES.CRASH_STATE_INFO:
				if (data.crash == true && this.info.isRoundPlayActive)
				{
					this.info.roundEndTime = data.date;
				}
			break;

			case SERVER_MESSAGES.CANCEL_BATTLEGROUND_ROUND:
				if (this.info.isRoundQualifyState || this.info.isRoundPlayState)
				{
					this.info.refundValue = data.refundedAmount;
				}
				break;

			case SERVER_MESSAGES.CRASH_BET_RESPONSE:
				if (data.rid !== -1)
				{
					this.info.resetBattlegroundRoundResults();
				}
				break;
		}
	}

	//ROUND STATE...
	/**
	 * Update round state if changed.
	 * @param {string} aRecievedRoundState_num - New round state. Possible round states: RoundInfo#ROUND_STATES
	 * @private
	 */
	_updateRoundStateIfRequired(aRecievedRoundState_num)
	{
		if (!this.info.isRoundStateDefined)
		{
			this._updateRoundState(aRecievedRoundState_num)
			return;
		}

		let lNewRoundState_num = aRecievedRoundState_num;
		let lCurRoundState_num = this.info.roundState;

		if (lNewRoundState_num !== lCurRoundState_num)
		{
			this._updateRoundState(lNewRoundState_num);
		}
	}

	/**
	 * Set round state and fire event.
	 * @param {string} aNewRoundState_num - New round state. Possible round states: RoundInfo#ROUND_STATES
	 * @private
	 */
	_updateRoundState(aNewRoundState_num)
	{
		this.info.roundState = aNewRoundState_num;

		this.emit(RoundController.EVENT_ON_ROUND_STATE_CHANGED);
	}
	//...ROUND STATE

	//ROUND ID...
	/**
	 * Update round id if changed.
	 * @param {number} aRecievedRoundId_num - Expected round id.
	 * @private
	 */
	_updateRoundIdIfRequired(aRecievedRoundId_num)
	{
		if (!this.info.isRoundIdDefined)
		{
			this._updateRoundId(aRecievedRoundId_num)
			return;
		}

		let lNewRoundId_num = aRecievedRoundId_num;
		let lCurRoundId_num = this.info.roundId;

		if (lNewRoundId_num !== lCurRoundId_num)
		{
			this._updateRoundId(lNewRoundId_num);
		}
	}

	/**
	 * Set round id and fire event.
	 * @param {number} aNewRoundId_num - New round id.
	 */
	_updateRoundId(aNewRoundId_num)
	{
		this.info.roundId = aNewRoundId_num;

		this.emit(RoundController.EVENT_ON_ROUND_ID_CHANGED);
	}
	//...ROUND ID

	_onServerConnectionClosed(event)
	{
		this.info.reset();
	}

	_onApplicationFatalErrorOccured(event)
	{
		this.info.reset();
	}

	_onInactiveRoundsLimit(event)
	{
		this.info.reset();
	}

	//BUTTLEGROUND...
	/**
	 * Update round result information.
	 * Actual for battleground mode only.
	 * @param {Object} aRoundResultMessageData_obj - Server message data.
	 * @private
	 */
	_updateBattlegroundRoundResults(aRoundResultMessageData_obj)
	{
		let lBattlegroundRoundResult_obj_arr = aRoundResultMessageData_obj.battlegroundRoundResult;

		let lNoOneEject = true;
		if (lBattlegroundRoundResult_obj_arr.length > 0)
		{
			for (let i = 0; i < lBattlegroundRoundResult_obj_arr.length; i++)
			{
				let lCurResult_obj = lBattlegroundRoundResult_obj_arr[i];
				if (lCurResult_obj.pot != 0 || lCurResult_obj.score != lCurResult_obj.rank)
				{
					lNoOneEject = false;
					break;
				}
			}
		}

		if (lNoOneEject)
		{
			let lRefundValue_num = 0;
			if (lBattlegroundRoundResult_obj_arr.length > 0)
			{
				lRefundValue_num = lBattlegroundRoundResult_obj_arr[0].rank;
			}

			let lIsMasterPlayed_bl = false;
			let lMasterNickName_str = this.info.i_getParentInfo().gamePlayersInfo.observerId;
			for (let i = 0; i < lBattlegroundRoundResult_obj_arr.length; i++)
			{
				let lCurResult_obj = lBattlegroundRoundResult_obj_arr[i];
				if (lCurResult_obj.nickName === lMasterNickName_str)
				{
					lIsMasterPlayed_bl = true;
					break;
				}
			}

			if (!lIsMasterPlayed_bl)
			{
				lRefundValue_num = 0;
			}

			this.info.refundValue = lRefundValue_num;
		}
		else
		{
			let lMaxWinValue_num = 0;
			if (lBattlegroundRoundResult_obj_arr.length > 0)
			{
				for (let i = 0; i < lBattlegroundRoundResult_obj_arr.length; i++)
				{
					let lCurResult_obj = lBattlegroundRoundResult_obj_arr[i];
					if (lCurResult_obj.score > lMaxWinValue_num)
					{
						lMaxWinValue_num = lCurResult_obj.score;
					}
				}
			}
			this.info.battlegroundRoundWinValue = lMaxWinValue_num;
		
			let lWinnersNicks_str_arr = [];
			for (let i = 0; i < lBattlegroundRoundResult_obj_arr.length; i++)
			{
				let lCurResult_obj = lBattlegroundRoundResult_obj_arr[i];
				if (lCurResult_obj.score == lMaxWinValue_num)
				{
					lWinnersNicks_str_arr.push(lCurResult_obj.nickName);
				} 
			}
			this.info.battlegroundRoundWinners = lWinnersNicks_str_arr;
			this.info.battlegroundRoundEndTime = this.info.roundEndTime;
		}
	}
	//...BUTTLEGROUND
}

export default RoundController;