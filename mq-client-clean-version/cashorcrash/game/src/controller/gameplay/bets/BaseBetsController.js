import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import BetInfo from '../../../model/gameplay/bets/BetInfo';
import RoundController from '../RoundController';
import GameplayController from '../GameplayController';
import GamePlayersController from '../players/GamePlayersController';
import CrashAPP from '../../../CrashAPP';
import PlaceBetsController from '../../uis/custom/betslist/PlaceBetsController';

/**
 * Possible reasons to reject bet placing on a client.
 * @constant
 * @type {Object}
 */
export const BET_PLACE_REJECT_REASONS =
{
	NO_MASTER: "NO_MASTER",
	WRONG_AUTO_EJECT_VALUE: "WRONG_AUTO_EJECT_VALUE",
	WRONG_BET_AMOUNT: "WRONG_BET_AMOUNT",
	NOT_ENOUGH_MONEY: "NOT_ENOUGH_MONEY",
	BET_ALREADY_EXISTS: "BET_ALREADY_EXISTS",
	WRONG_ROUND_STATE: "WRONG_ROUND_STATE"
};

/**
 * Possible reasons to reject bet cancelling on a client.
 * @constant
 * @type {Object}
 */
export const BET_CANCEL_REJECT_REASONS =
{
	NOT_MASTER_BET: "NOT_MASTER_BET",
	NOT_CONFIRMED_BET: "NOT_CONFIRMED_BET",
	WRONG_ROUND_STATE: "WRONG_ROUND_STATE"
};

/**
 * Possible reasons to reject cancelling of auto eject on a client.
 * @constant
 * @type {Object}
 */
export const CANCEL_AUTO_EJECT_REJECT_REASONS =
{
	NOT_MASTER_BET: "NOT_MASTER_BET",
	NOT_CONFIRMED_BET: "NOT_CONFIRMED_BET",
	WRONG_ROUND_STATE: "WRONG_ROUND_STATE",
	NOT_AUTO_EJECT_BET: "NOT_AUTO_EJECT_BET",
	ALREADY_EJECTED_BET: "ALREADY_EJECTED_BET",
	AUTO_EJECT_CANCEL_IN_PROGRESS: "AUTO_EJECT_CANCEL_IN_PROGRESS"
};

/**
 * Possible reasons to reject editing of auto eject value on a client.
 * @constant
 * @type {Object}
 */
export const EDIT_AUTO_EJECT_REJECT_REASONS =
{
	NOT_MASTER_BET: "NOT_MASTER_BET",
	NOT_CONFIRMED_BET: "NOT_CONFIRMED_BET",
	WRONG_ROUND_STATE: "WRONG_ROUND_STATE",
	ALREADY_EJECTED_BET: "ALREADY_EJECTED_BET",
	WRONG_AUTO_EJECT_VALUE: "WRONG_AUTO_EJECT_VALUE",
	AUTO_EJECT_CANCEL_IN_PROGRESS: "AUTO_EJECT_CANCEL_IN_PROGRESS",
	AUTO_EJECT_CHANGE_IN_PROGRESS: "AUTO_EJECT_CHANGE_IN_PROGRESS"
};

/**
 * Base class to control bets.
 * @class
 * @extends SimpleController
 * @inheritdoc
 */
class BaseBetsController extends SimpleController
{
	static get EVENT_ON_BET_REJECTED() 							{ return "EVENT_ON_BET_REJECTED" };
	static get EVENT_ON_BETS_ACCEPTED() 						{ return "EVENT_ON_BETS_ACCEPTED" };
	static get EVENT_ON_BET_CONFIRMED() 						{ return "EVENT_ON_BET_CONFIRMED" };
	static get EVENT_ON_BET_NOT_CONFIRMED() 					{ return "EVENT_ON_BET_NOT_CONFIRMED" };
	static get EVENT_ON_ALL_PLAYER_BETS_REJECTED_BY_SERVER()	{ return "EVENT_ON_ALL_PLAYER_BETS_REJECTED_BY_SERVER" };
	static get EVENT_ON_ALL_PLAYER_BETS_CONFIRMED_BY_SERVER()	{ return "EVENT_ON_ALL_PLAYER_BETS_CONFIRMED_BY_SERVER" };
	static get EVENT_ON_BET_CANCEL_REJECTED() 					{ return "EVENT_ON_BET_CANCEL_REJECTED" };
	static get EVENT_ON_BET_CANCEL_INITIATED() 					{ return "EVENT_ON_BET_CANCEL_INITIATED" };
	static get EVENT_ON_CANCEL_ALL_BETS_INITIATED() 			{ return "EVENT_ON_CANCEL_ALL_BETS_INITIATED" };
	static get EVENT_ON_BET_CANCELLED() 						{ return "EVENT_ON_BET_CANCELLED" };
	static get EVENT_ON_OUTDATED_BET_REMOVED() 					{ return "EVENT_ON_OUTDATED_BET_REMOVED" };
	static get EVENT_ON_BETS_CLEARED() 							{ return "EVENT_ON_BETS_CLEARED" };
	static get EVENT_ON_BETS_UPDATED() 							{ return "EVENT_ON_BETS_UPDATED" };
	static get EVENT_ON_BET_LIMITS_UPDATED() 					{ return "EVENT_ON_BET_LIMITS_UPDATED" };
	
	static get EVENT_ON_CANCEL_AUTOEJECT_INITIATED()			{ return "EVENT_ON_CANCEL_AUTOEJECT_INITIATED" };
	static get EVENT_ON_CRASH_CANCEL_AUTOEJECT_REJECTED() 		{ return "EVENT_ON_CRASH_CANCEL_AUTOEJECT_REJECTED" };
	static get EVENT_ON_CRASH_CANCEL_AUTOEJECT_CONFIRMED() 		{ return "EVENT_ON_CRASH_CANCEL_AUTOEJECT_CONFIRMED" };

	static get EVENT_ON_EDIT_AUTO_EJECT_INITIATED() 			{ return "EVENT_ON_EDIT_AUTO_EJECT_INITIATED" };
	static get EVENT_ON_EDIT_AUTOEJECT_REJECTED() 				{ return "EVENT_ON_EDIT_AUTOEJECT_REJECTED" };
	static get EVENT_ON_EDIT_AUTOEJECT_CONFIRMED() 				{ return "EVENT_ON_EDIT_AUTOEJECT_CONFIRMED" };

	/**
	 * @type {GamePlayersController}
	 */
	get gamePlayersController()
	{
		return this.parentController;
	}

	constructor(aOptInfo_usi, aOptParentController_usc)
	{
		super(aOptInfo_usi, aOptParentController_usc);
	}

	init()
	{
		super.init();
	}

	/**
	 * Checks if the bet is currently cancelling.
	 * @param {string} aBetId_num - Target bet id.
	 * @returns {boolean}
	 */
	isBetCancelInProgress(aBetId_num)
	{
		let wsInteractionController = APP.webSocketInteractionController;
		let lBetInfo_bi = this.info.getBetInfo(aBetId_num);

		let lIsBetCancelInProgress_bl = lBetInfo_bi
										&& (
												wsInteractionController.hasUnRespondedRequest(CLIENT_MESSAGES.CRASH_CANCEL_BET, {crashBetId: lBetInfo_bi.betId})
												|| wsInteractionController.hasDelayedRequests(CLIENT_MESSAGES.CRASH_CANCEL_BET, {crashBetId: lBetInfo_bi.betId})
											);

		return lIsBetCancelInProgress_bl;
	}

	/**
	 * Checks if the bet's auto-eject is currently cancelling.
	 * @param {string} aBetId_num - Target bet id.
	 * @returns {boolean}
	 */
	isBetCancelAutoEjectInProgress(aBetId_num)
	{
		let wsInteractionController = APP.webSocketInteractionController;
		let lBetInfo_bi = this.info.getBetInfo(aBetId_num);

		let lIsBetCancelInProgress_bl = lBetInfo_bi
										&& (
												wsInteractionController.hasUnRespondedRequest(CLIENT_MESSAGES.CRASH_CANCEL_AUTOEJECT, {crashBetId: lBetInfo_bi.betId})
												|| wsInteractionController.hasDelayedRequests(CLIENT_MESSAGES.CRASH_CANCEL_AUTOEJECT, {crashBetId: lBetInfo_bi.betId})
											);

		return lIsBetCancelInProgress_bl;
	}

	/**
	 * Checks if placing bet is in progress (waiting for server confirmation).
	 * @returns {boolean}
	 */
	isPlaceBetInProgress()
	{
		let wsInteractionController = APP.webSocketInteractionController;

		let lIsPlaceBetInProgress_bl = (
											wsInteractionController.hasUnRespondedRequest(CLIENT_MESSAGES.CRASH_BET)
											|| wsInteractionController.hasDelayedRequests(CLIENT_MESSAGES.CRASH_BET)
										);

		return lIsPlaceBetInProgress_bl;
	}

	/**
	 * Checks if changing of auto-eject value for a bet is in progress (waiting for server confirmation).
	 * @type {string} aBetId_num - Target bet id.
	 * @returns {boolean}
	 */
	isBetChangeAutoEjectInProgress(aBetId_num)
	{
		let wsInteractionController = APP.webSocketInteractionController;
		let lBetInfo_bi = this.info.getBetInfo(aBetId_num);

		let lIsBetChangeAutoEjectInProgress_bl = lBetInfo_bi
										&& (
												wsInteractionController.hasUnRespondedRequest(CLIENT_MESSAGES.CRASH_CHANGE_AUTOEJECT, {crashBetId: lBetInfo_bi.betId})
												|| wsInteractionController.hasDelayedRequests(CLIENT_MESSAGES.CRASH_CHANGE_AUTOEJECT, {crashBetId: lBetInfo_bi.betId})
											);

		return lIsBetChangeAutoEjectInProgress_bl;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(CrashAPP.EVENT_ON_APPLICATION_FATAL_ERROR, this._onApplicationFatalErrorOccured, this);

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);

		this._fGameplayController_gpc = APP.gameController.gameplayController;
		this._fGameplayController_gpc.on(GameplayController.EVENT_ON_EJECT_INITIATED, this._onEjectInitiated, this);
		this._fGameplayController_gpc.on(GameplayController.EVENT_ON_EJECT_ALL_INITIATED, this._onEjectAllInitiated, this);

		this._fBalanceController_bc = APP.gameController.balanceController;

		this._fRoundController_rc = APP.gameController.gameplayController.roundController;
		this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);

		this.gamePlayersController.on(GamePlayersController.EVENT_ON_MASTER_PLAYER_IN, this._onMasterPlayerIn, this);
		this.gamePlayersController.on(GamePlayersController.EVENT_ON_INACTIVE_ROUNDS_LIMIT, this._onInactiveRoundsLimit, this);

		let lPlaceBetsController_pbsc = this._fPlaceBetsController_pbsc = APP.gameController.placeBetsController;
		lPlaceBetsController_pbsc.on(PlaceBetsController.EVENT_ON_PLACE_BETS, this._onPlaceBetsInitiated, this);
		lPlaceBetsController_pbsc.on(PlaceBetsController.EVENT_ON_CANCEL_BET, this._onCancelBetInitiated, this);

		lPlaceBetsController_pbsc.on(PlaceBetsController.EVENT_ON_EJECT_INITIATED, this._onEjectInitiated, this);
		lPlaceBetsController_pbsc.on(PlaceBetsController.EVENT_ON_CANCEL_AUTO_EJECT_INITIATED, this._onCancelAutoEjectInitiated, this);
		lPlaceBetsController_pbsc.on(PlaceBetsController.EVENT_ON_EDIT_AUTO_EJECT_INITIATED, this._onEditAutoEjectInitiated, this);

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		this.info.isNoMoreBetsPeriodMode = !APP.isBattlegroundGame && !APP.appParamsInfo.isCWSendRealBetWinMode;
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 97) // 1
	// 	{
	// 		this._acceptBet(0, 1, undefined)
	// 	}
	// 	else if (keyCode.keyCode == 98) // 2
	// 	{
	// 		this._acceptBet(1, 2, 1+3*Math.random())
	// 	}
	// 	else if (keyCode.keyCode == 99) // 3
	// 	{
	// 		this._acceptBet(2, 1, undefined)
	// 	}

	// 	if (keyCode.keyCode == 100 || keyCode.keyCode == 101 || keyCode.keyCode == 102) // 4
	// 	{
	// 		this._cancelBet(keyCode.keyCode-100);
	// 	}
	// }
	//...DEBUG

	/**
	 * Handle server messages to have actual state of bets on client side. 
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
				this._updateBetLimits(data.minStake, data.maxStake);
				break;
			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				if (data.maxMultiplier !== undefined)
				{
					this.info.maxAutoEjectMultiplier = +data.maxMultiplier;
				}
				break;
			case SERVER_MESSAGES.CRASH_GAME_INFO:
				if (requestData && requestData.class === CLIENT_MESSAGES.GET_FULL_GAME_INFO)
				{
					// no need to update bets due to client sends FullGameInfo to get actual history (not to update game info)
					break;
				}

				if (this._fRoundController_rc.info.isRoundQualifyState)
				{
					this.info.setInitialQualifiedBets(data);
					break;
				}

				if (requestData && requestData.class === CLIENT_MESSAGES.OPEN_ROOM && data.crash == true && this.info.roundInfo.isRoundPlayState)
				{
					// reset bets, because there could be no actual list of bets from the server: https://jira.dgphoenix.com/browse/CRG-633
					data.bets = [];
				}

				this.info.setBets(data.bets);

				if (data.canceledBetAmount !== undefined && !this.info.isNoMoreBetsPeriodMode)
				{
					this.info.canceledBetAmount = +data.canceledBetAmount;
				}

				this.emit(BaseBetsController.EVENT_ON_BETS_UPDATED);
				break;
			case SERVER_MESSAGES.CRASH_BET_RESPONSE:
				if (this._fRoundController_rc.info.isRoundQualifyState)
				{
					this.info.addQualifiedBetInfo(data);
					break;
				}

				if (data.rid == -1) // co-player's bet
				{
					// remove old outdated bets of co-player... (https://jira.dgphoenix.com/browse/CRG-167)
					let lCoPlayerBetId_str = data.betId || data.crashBetId || data.crashBetKey;
					let lCoPlayerBetIndex_num = BetInfo.extractBetIndex(lCoPlayerBetId_str);
					let lCoPlayerSeatId_str = BetInfo.extractSeatId(lCoPlayerBetId_str);
					let lExistantCoPlayerBets_bi_arr = this.info.getPlayerActiveBets(lCoPlayerSeatId_str);
					if (lExistantCoPlayerBets_bi_arr && lExistantCoPlayerBets_bi_arr.length)
					{
						for (let i=0; i<lExistantCoPlayerBets_bi_arr.length; i++)
						{
							let lExistantCoPlayerBet_bi = lExistantCoPlayerBets_bi_arr[i];
							if (lExistantCoPlayerBet_bi.betIndex === lCoPlayerBetIndex_num)
							{
								this.emit(BaseBetsController.EVENT_ON_OUTDATED_BET_REMOVED, {betInfo: lExistantCoPlayerBet_bi});
								this.info.removeBetInfo(lExistantCoPlayerBet_bi.betId);
								break;
							}
						}
					}
					// ...remove old outdated bets of co-player

					this.info.addBetInfo(data);
				}
				else
				{
					this.info.updateBetInfo(data);

					if (this.info.isNoMoreBetsPeriodMode)
					{
						// don't use canceledBetAmount in No More Bets Mode, because bets will be deducted from the server balance only in BUY_IN period
					}
					else
					{
						let lCanceledBetAmount_num = this.info.canceledBetAmount;
						lCanceledBetAmount_num -= data.crashBetAmount;
						if (lCanceledBetAmount_num < 0)
						{
							lCanceledBetAmount_num = 0;
						}
						this.info.canceledBetAmount = lCanceledBetAmount_num;
					}
				}

				let lCrashBetId_str = data.crashBetKey;
				let lConfirmedBetInfo_bi = this.info.getBetInfo(lCrashBetId_str);

				this.emit(BaseBetsController.EVENT_ON_BET_CONFIRMED, {betInfo: lConfirmedBetInfo_bi});				
				break;
			case SERVER_MESSAGES.CRASH_CANCEL_BET_RESPONSE:
				if (this._fRoundController_rc.info.isRoundQualifyState)
				{
					this.info.removeQualifiedBetInfo(data);
					break;
				}

				this.info.updateBetInfo(data);

				let lBetId_str = data.crashBetId;
				let lBetInfo_bi = this.info.getBetInfo(lBetId_str);

				if (
						(lBetInfo_bi.isConfirmedMasterBet || lBetInfo_bi.isPossiblyDeactivatedByServerMasterBet)
						&& lBetInfo_bi.isDeactivatedBet
						&& !this.info.isNoMoreBetsPeriodMode
					)
				{
					lBetInfo_bi.isPossiblyDeactivatedByServerMasterBet = false;
					this.info.canceledBetAmount += lBetInfo_bi.betWin;
				}

				this.emit(BaseBetsController.EVENT_ON_BET_CANCELLED, {betInfo: lBetInfo_bi, astronautIndex: lBetInfo_bi.betIndex});
				break;
			case SERVER_MESSAGES.CRASH_CANCEL_AUTOEJECT_RESPONSE:
				if (this._fRoundController_rc.info.isRoundQualifyState)
				{
					break;
				}

				this.info.cancelAutoEject(data.betId);
				let lThisBetInfo_bi = this.info.getBetInfo(data.betId);
				if (lThisBetInfo_bi.isConfirmedMasterBet) 
				{
					let lRepeatBetInfo_rbi = this.info.getLastConfirmedBet(lThisBetInfo_bi.betIndex);
					if (lRepeatBetInfo_rbi && !this._fRoundController_rc.info.isRoundPlayState)
					{
						lRepeatBetInfo_rbi.isAutoEject = false;
						lRepeatBetInfo_rbi.autoEjectMultiplier = undefined;
					}

					if (this._fRoundController_rc.info.isRoundPlayState)
					{
						lThisBetInfo_bi.isAutoEjectCancelledInRound = true;
					}

					let lCancelType_str = !!requestData ? requestData.excludeParams.cancelType : undefined;
					this.emit(BaseBetsController.EVENT_ON_CRASH_CANCEL_AUTOEJECT_CONFIRMED, {betInfo: lThisBetInfo_bi, astronautIndex: lThisBetInfo_bi.betIndex, cancelType: lCancelType_str});
				}
				break;
			case SERVER_MESSAGES.CRASH_CHANGE_AUTOEJECT_RESPONSE:
				if (this._fRoundController_rc.info.isRoundQualifyState)
				{
					break;
				}

				let lEditedBetInfo_bi = this.info.getBetInfo(data.betId);
				if (lEditedBetInfo_bi.isConfirmedMasterBet)
				{
					lEditedBetInfo_bi.isAutoEject = true;
					lEditedBetInfo_bi.autoEjectMultiplier = requestData.multiplier;

					let lRepeatBetInfo_rbi = this.info.getLastConfirmedBet(lEditedBetInfo_bi.betIndex);
					if (lRepeatBetInfo_rbi)
					{
						lRepeatBetInfo_rbi.isAutoEject = lEditedBetInfo_bi.isAutoEject;
						lRepeatBetInfo_rbi.autoEjectMultiplier = lEditedBetInfo_bi.autoEjectMultiplier;
					}
					this.emit(BaseBetsController.EVENT_ON_EDIT_AUTOEJECT_CONFIRMED, {betInfo: lEditedBetInfo_bi, astronautIndex: lEditedBetInfo_bi.betIndex});
				}
				break;
			case SERVER_MESSAGES.ROUND_RESULT:
				this.info.canceledBetAmount = 0;
				this.info.isExistentClientBetsClearedOnServer = true;
				break;

			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_CONFIRMED_RESPONSE:
				let lConfirmed_bl = this.info.externallyConfirmMasterBets();
				if (lConfirmed_bl)
				{
					this.emit(BaseBetsController.EVENT_ON_ALL_PLAYER_BETS_CONFIRMED_BY_SERVER);
				}
				break;
			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE:
			case SERVER_MESSAGES.CRASH_ALL_COPLAYERS_BETS_REJECTED_RESPONSE:
				if (
					data.errorCode !== undefined 
					&& (
							APP.webSocketInteractionController.isFatalError(data.errorCode)
							|| data.errorCode === GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE
						)
					)
				{
					this._clearBets();
					break;
				}

				let lSeatId_str = data.name;
				let lPlayerBets_bi_arr = this.info.getPlayerBets(lSeatId_str);
				if (!!lPlayerBets_bi_arr && !!lPlayerBets_bi_arr.length)
				{
					for (let i=0; i<lPlayerBets_bi_arr.length; i++)
					{
						let lPlayerBetInfo_bi = lPlayerBets_bi_arr[i];
						let lPlayerBetId_str = lPlayerBetInfo_bi.betId;
						let lPlayerBetIndex_num = lPlayerBetInfo_bi.betIndex;
						let lPlayerBetAmount_num = lPlayerBetInfo_bi.betAmount;

						this.info.removeBetInfo(lPlayerBetId_str);
					}

					this.emit(BaseBetsController.EVENT_ON_ALL_PLAYER_BETS_REJECTED_BY_SERVER, {seatId: lSeatId_str});
				}
				break;
		}
	}

	/**
	 * Handle server error messages (error codes) to have actual state of bets on client side. 
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
			this._clearBets();
			return;
		}

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.NOT_SEATER:
			case GameWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE:
			case GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE:
			case GameWebSocketInteractionController.ERROR_CODES.NOT_FATAL_BAD_BUYIN:
			case GameWebSocketInteractionController.ERROR_CODES.BAD_MULTIPLIER:
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
			case GameWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS:
			case GameWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_ALREADY:
				let lBetIds_str_arr = [];
				if (!!requestData.bets) //in request CLIENT_MESSAGES.CRASH_BETS
				{
					for (let i=0; i<requestData.bets.length; i++)
					{
						lBetIds_str_arr.push(requestData.bets[i].betId);
					}
				}
				else
				{
					lBetIds_str_arr.push(requestData.betId);
				}

				for (let i=0; i<lBetIds_str_arr.length; i++)
				{
					let lBetId_str = lBetIds_str_arr[i];
					let lBetInfo_bi = this.info.getBetInfo(lBetId_str);

					if (lBetInfo_bi && !lBetInfo_bi.isConfirmedMasterBet) // possible when we get error code after switching to WAIT state (== after bets clear)
					{
						if (serverData.code === GameWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_ALREADY)
						{
							lBetInfo_bi.isPossiblyDeactivatedByServerMasterBet = true;
						}
						else
						{
							let lBetIndex_num = lBetInfo_bi.betIndex;
							let lBetAmount_num = lBetInfo_bi.betAmount;

							this.info.removeBetInfo(lBetId_str);

							this.emit(BaseBetsController.EVENT_ON_BET_NOT_CONFIRMED, {betIndex: lBetIndex_num, betAmount: lBetAmount_num});
						}
					}
				}
				break;
		}
	}

	/**
	 * Validate bets when round state changes.
	 * @param {*} event 
	 * @private
	 */
	_onRoundStateChanged(event)
	{
		let l_ri = this._fRoundController_rc.info;
		if (l_ri.isRoundWaitState)
		{
			this._clearBets(true);
			
			this.info.applyQualifyBets();
			let lBets_bi_arr = this.info.allBets;

			if (lBets_bi_arr && lBets_bi_arr.length)
			{
				this.emit(BaseBetsController.EVENT_ON_BETS_UPDATED);
			}
		}
		else if (l_ri.isRoundPlayState)
		{
			if (this.gamePlayersController.info.isMasterSeatDefined)
			{
				let lActiveMasterBets_arr_bi = this.info.getPlayerActiveBets(this.gamePlayersController.info.masterSeatId);
				if (!!lActiveMasterBets_arr_bi && !!lActiveMasterBets_arr_bi.length)
				{
					this.info.rememberLastRoundMasterBets();
				}
			}
		}
	}

	_onCancelAutoEjectInitiated(event)
	{
		let lTargetBetInfo_bi = event.betInfo;
		let lCancelType_str = event.cancelType;

		this._cancelAutoEject(lTargetBetInfo_bi, lCancelType_str);
	}

	_onEditAutoEjectInitiated(event)
	{
		let lTargetBetInfo_bi = event.betInfo;
		let lTargetAutoEjectMultiplier_num = event.autoEjectMultiplier;

		this._editAutoEject(lTargetBetInfo_bi, lTargetAutoEjectMultiplier_num);
	}

	_onEjectInitiated(event)
	{
		let lTargetBetInfo_bi = event.betInfo;

		this._cancelBet(lTargetBetInfo_bi.betIndex, lTargetBetInfo_bi.betId, true);
	}

	_onEjectAllInitiated(event)
	{
		this._cancelAllBets();
	}

	/**
	 * Remove bets on client side.
	 * @param {boolean} [aKeepQualifyBets_bl=false] - If TRUE - 
	 */
	_clearBets(aKeepQualifyBets_bl=false)
	{
		this.info.resetBets(aKeepQualifyBets_bl);
		this.info.isExistentClientBetsClearedOnServer = false;

		this.emit(BaseBetsController.EVENT_ON_BETS_CLEARED);
	}

	_onMasterPlayerIn(event)
	{
		let lConfirmed_bl = this.info.confirmMasterBets();

		if (lConfirmed_bl)
		{
			this.emit(BaseBetsController.EVENT_ON_BET_CONFIRMED, {isMasterPlayerIn: true});
		}

		if (this._fRoundController_rc.info.isRoundPlayState)
		{
			let lActiveMasterBets_arr_bi = this.info.getPlayerActiveBets(this.gamePlayersController.info.masterSeatId);
			if (!!lActiveMasterBets_arr_bi && !!lActiveMasterBets_arr_bi.length)
			{
				this.info.rememberLastRoundMasterBets();
			}
		}
	}

	/**
	 * Accept bet placing on client side or reject it.
	 * @param {number} aBetIndex_int - Bet index.
	 * @param {number} aBetAmount_num - Bet amount in cents.
	 * @param {number} [aAutoEjectMultiplier_num=undefined] - Auto-eject multiplier of bet.
	 * @param {number} [aAdditionalBetAmountSum_num=0] - Already accepted bets amount (in cents) in case if several bets are placed by the same action.
	 * For example, when several bets are trying to be placed by "Repeat all bets" action.
	 * @returns {BetInfo} - Accepted bet or null.
	 * @private
	 */
	_acceptBet(aBetIndex_int, aBetAmount_num, aAutoEjectMultiplier_num=undefined, aAdditionalBetAmountSum_num=0)
	{
		if (!this.gamePlayersController.info.isMasterSeatDefined)
		{
			this._rejectBet(BET_PLACE_REJECT_REASONS.NO_MASTER, aBetIndex_int, aBetAmount_num, aAutoEjectMultiplier_num);
			return null;
		}

		if (!this._fRoundController_rc.info.isRoundWaitState)
		{
			this._rejectBet(BET_PLACE_REJECT_REASONS.WRONG_ROUND_STATE, aBetIndex_int, aBetAmount_num, aAutoEjectMultiplier_num);
			return null;
		}

		let lMasterSeatId_num = this.info.gamePlayersInfo.masterSeatId;
		let lBetData_obj = {};
		lBetData_obj.betId = BetInfo.generateBetId(aBetIndex_int, lMasterSeatId_num);

		if (this.info.isValidAutoEjectMultiplier(aAutoEjectMultiplier_num))
		{
			lBetData_obj.auto = aAutoEjectMultiplier_num !== undefined;
			lBetData_obj.autoEjectMult = aAutoEjectMultiplier_num || undefined;
		}
		else
		{
			this._rejectBet(BET_PLACE_REJECT_REASONS.WRONG_AUTO_EJECT_VALUE, aBetIndex_int, aBetAmount_num, aAutoEjectMultiplier_num);
			return null;
		}

		let lExistBetInfo_bi = this.info.getBetByIndex(aBetIndex_int, lMasterSeatId_num);
		if (!!lExistBetInfo_bi && !lExistBetInfo_bi.isDeactivatedBet)
		{
			this._rejectBet(BET_PLACE_REJECT_REASONS.BET_ALREADY_EXISTS, aBetIndex_int, aBetAmount_num, aAutoEjectMultiplier_num);
			return null;
		}

		let lBalanceInfo_bi = this._fBalanceController_bc.info;
		if (lBalanceInfo_bi.isBalanceEnoughForBet(aBetAmount_num+aAdditionalBetAmountSum_num))
		{
			lBetData_obj.crashBetAmount = aBetAmount_num;
		}
		else
		{
			this._rejectBet(BET_PLACE_REJECT_REASONS.NOT_ENOUGH_MONEY, aBetIndex_int, aBetAmount_num, aAutoEjectMultiplier_num);
			return null;
		}

		if (!this.info.isValidBetValue(aBetAmount_num))
		{
			this._rejectBet(BET_PLACE_REJECT_REASONS.WRONG_BET_AMOUNT, aBetIndex_int, aBetAmount_num, aAutoEjectMultiplier_num);
			return null;
		}

		let lAcceptedBetInfo_bi = this.info.addBetInfo(lBetData_obj);
		return lAcceptedBetInfo_bi;
	}

	/**
	 * Reject bet placing on client side. Rejected bets won't be sent to server.
	 * @param {number} aReason_str - Reject reason.
	 * @param {number} aBetIndex_int - Bet Index.
	 * @param {number} aBetAmount_num - Bet amount in cents.
	 * @param {number} [aAutoEjectMultiplier_num=undefined] - Auto-eject multiplier of bet.
	 * @private
	 */
	_rejectBet(aReason_str, aBetIndex_int, aBetAmount_num, aAutoEjectMultiplier_num=undefined)
	{
		console.log("** _rejectBet, reason:", aReason_str, "; aBetIndex_int:", aBetIndex_int, "; aBetAmount_num:", aBetAmount_num, "; aAutoEjectMultiplier_num:", aAutoEjectMultiplier_num);
		this.emit(BaseBetsController.EVENT_ON_BET_REJECTED, { 	rejectReason: aReason_str, 
															betIndex: aBetIndex_int,
															betAmount: aBetAmount_num, 
															autoEjectMultiplier: aAutoEjectMultiplier_num
														});
	}

	/**
	 * Cancel/Eject already placed bet or reject cancelling.
	 * @param {number} aBetIndex_int - Bet index.
	 * @param {string} aOptBetId_str - Bet id.
	 * @param {boolean} aIsEjectType_bl - Indicates type of cancelling: Eject or Remove bet. TRUE is for Eject type.
	 * @private
	 */
	_cancelBet(aBetIndex_int, aOptBetId_str=undefined, aIsEjectType_bl=false)
	{
		if (!(this._fRoundController_rc.info.isRoundWaitState || this._fRoundController_rc.info.isRoundPlayState))
		{
			this._rejectBetCancel(BET_CANCEL_REJECT_REASONS.WRONG_ROUND_STATE, aBetIndex_int, aOptBetId_str);
			return;
		}

		let lBetInfo_bi = null;
		if (aOptBetId_str !== undefined)
		{
			lBetInfo_bi = this.info.getBetInfo(aOptBetId_str);

			if (!lBetInfo_bi)
			{
				throw new Error(`Cannot cancel bet, bet not found by bet id '${aOptBetId_str}'.`);
				return;
			}

			if (lBetInfo_bi.betIndex !== aBetIndex_int)
			{
				throw new Error(`Cannot cancel bet, index '${aBetIndex_int}' does not match to bet id '${lBetInfo_bi.betId}'.`);
				return;
			}
		}
		else
		{
			lBetInfo_bi = this.info.getMasterBetInfoByIndex(aBetIndex_int, true);

			if (!lBetInfo_bi)
			{
				throw new Error(`Cannot cancel bet, bet not found by index '${aBetIndex_int}'.`);
				return;
			}
		}

		if (!lBetInfo_bi.isMasterBet)
		{
			this._rejectBetCancel(BET_CANCEL_REJECT_REASONS.NOT_MASTER_BET, lBetInfo_bi.betIndex, lBetInfo_bi.betId);
			return;
		}

		if (!lBetInfo_bi.isConfirmedMasterBet)
		{
			this._rejectBetCancel(BET_CANCEL_REJECT_REASONS.NOT_CONFIRMED_BET, lBetInfo_bi.betIndex, lBetInfo_bi.betId);
			return;
		}

		this.emit(BaseBetsController.EVENT_ON_BET_CANCEL_INITIATED, {betInfo: lBetInfo_bi, isEject: aIsEjectType_bl});
	}

	/**
	 * Attempt to eject all rest bets.
	 * @private
	 */
	_cancelAllBets()
	{
		this.emit(BaseBetsController.EVENT_ON_CANCEL_ALL_BETS_INITIATED, {});
	}

	/**
	 * Reject bet cancelling on client side. Rejected actions won't be sent to server.
	 * @param {string} aReason_str - Reject reason.
	 * @param {number} aBetIndex_int - Bet index.
	 * @param {number} aBetId_num - Bet id.
	 * @private
	 */
	_rejectBetCancel(aReason_str, aBetIndex_int, aBetId_num)
	{
		console.log("** _rejectBetCancel, reason:", aReason_str, "; aBetIndex_int:", aBetIndex_int, "; aBetId_num:", aBetId_num);

		this.emit(BaseBetsController.EVENT_ON_BET_CANCEL_REJECTED, { 	rejectReason: aReason_str, 
																	betIndex: aBetIndex_int,
																	betId: aBetId_num
																});
	}

	/**
	 * Cancel bet's auto-eject or reject cancelling.
	 * @param {BetInfo} aBetInfo_bi - Target bet info.
	 * @param {string} aCancelType_str - Type of auto-eject cancelling: reset during editing phase or final reset during round.
	 * @private 
	 */
	_cancelAutoEject(aBetInfo_bi, aCancelType_str)
	{
		let lBetInfo_bi = aBetInfo_bi;

		if (!(this._fRoundController_rc.info.isRoundWaitState || this._fRoundController_rc.info.isRoundPlayState || this._fRoundController_rc.info.isRoundBuyInState || this._fRoundController_rc.info.isRoundPauseState))
		{
			this._rejectCancelAutoEject(CANCEL_AUTO_EJECT_REJECT_REASONS.WRONG_ROUND_STATE, lBetInfo_bi, aCancelType_str);
			return;
		}

		if (this.isBetCancelAutoEjectInProgress(lBetInfo_bi.betId))
		{
			this._rejectCancelAutoEject(CANCEL_AUTO_EJECT_REJECT_REASONS.AUTO_EJECT_CANCEL_IN_PROGRESS, lBetInfo_bi, aCancelType_str);
			return;
		}

		if (!lBetInfo_bi.isMasterBet)
		{
			this._rejectCancelAutoEject(CANCEL_AUTO_EJECT_REJECT_REASONS.NOT_MASTER_BET, lBetInfo_bi, aCancelType_str);
			return;
		}

		if (!lBetInfo_bi.isConfirmedMasterBet)
		{
			this._rejectCancelAutoEject(CANCEL_AUTO_EJECT_REJECT_REASONS.NOT_CONFIRMED_BET, lBetInfo_bi, aCancelType_str);
			return;
		}

		if (!lBetInfo_bi.isAutoEject)
		{
			this._rejectCancelAutoEject(CANCEL_AUTO_EJECT_REJECT_REASONS.NOT_AUTO_EJECT_BET, lBetInfo_bi, aCancelType_str);
			return;
		}

		if (lBetInfo_bi.isEjected)
		{
			this._rejectCancelAutoEject(CANCEL_AUTO_EJECT_REJECT_REASONS.ALREADY_EJECTED_BET, lBetInfo_bi, aCancelType_str);
			return;
		}

		this.emit(BaseBetsController.EVENT_ON_CANCEL_AUTOEJECT_INITIATED, {betInfo: lBetInfo_bi, cancelType: aCancelType_str});
	}

	/**
	 * Reject cancelling of auto-eject on client side. Rejected actions won't be sent to server.
	 * @param {string} aReason_str - Reject reason.
	 * @param {BetInfo} aBetInfo_bi - Target bet info. 
	 * @param {string} aCancelType_str - Type of auto-eject cancelling: reset during editing phase or final reset during round.
	 * @private
	 */
	_rejectCancelAutoEject(aReason_str, aBetInfo_bi, aCancelType_str)
	{
		console.log("** _rejectCancelAutoEject, reason:", aReason_str, "; aBetIndex_int:", aBetInfo_bi.betIndex, "; aBetId_num:", aBetInfo_bi.betId, "; cancelType:", aCancelType_str);

		this.emit(BaseBetsController.EVENT_ON_CRASH_CANCEL_AUTOEJECT_REJECTED, { 	rejectReason: aReason_str, 
																				betIndex: aBetInfo_bi.betIndex,
																				betId: aBetInfo_bi.betId,
																				cancelType: aCancelType_str
																			});
	}

	/**
	 * Edit bet's auto-eject multiplier or reject editing.
	 * @param {BetInfo} aBetInfo_bi - Target bet.
	 * @param {number} aAutoEjectMultiplier_num - Expected auto-eject multiplier value.
	 * @private
	 */
	_editAutoEject(aBetInfo_bi, aAutoEjectMultiplier_num)
	{
		let lBetInfo_bi = aBetInfo_bi;
		let lAutoEjectMultiplier_num = aAutoEjectMultiplier_num;

		if (!this._fRoundController_rc.info.isRoundWaitState)
		{
			this._rejectEditAutoEject(EDIT_AUTO_EJECT_REJECT_REASONS.WRONG_ROUND_STATE, lBetInfo_bi, aAutoEjectMultiplier_num);
			return;
		}

		if (this.isBetCancelAutoEjectInProgress(lBetInfo_bi.betId))
		{
			this._rejectEditAutoEject(EDIT_AUTO_EJECT_REJECT_REASONS.AUTO_EJECT_CANCEL_IN_PROGRESS, lBetInfo_bi, aAutoEjectMultiplier_num);
			return;
		}

		if (this.isBetChangeAutoEjectInProgress(lBetInfo_bi.betId))
		{
			this._rejectEditAutoEject(EDIT_AUTO_EJECT_REJECT_REASONS.AUTO_EJECT_CHANGE_IN_PROGRESS, lBetInfo_bi, aAutoEjectMultiplier_num);
			return;
		}

		if (!lBetInfo_bi.isMasterBet)
		{
			this._rejectEditAutoEject(EDIT_AUTO_EJECT_REJECT_REASONS.NOT_MASTER_BET, lBetInfo_bi, aAutoEjectMultiplier_num);
			return;
		}

		if (!lBetInfo_bi.isConfirmedMasterBet)
		{
			this._rejectEditAutoEject(EDIT_AUTO_EJECT_REJECT_REASONS.NOT_CONFIRMED_BET, lBetInfo_bi, aAutoEjectMultiplier_num);
			return;
		}

		if (lBetInfo_bi.isEjected)
		{
			this._rejectEditAutoEject(EDIT_AUTO_EJECT_REJECT_REASONS.ALREADY_EJECTED_BET, lBetInfo_bi, aAutoEjectMultiplier_num);
			return;
		}

		if (!this.info.isValidAutoEjectMultiplier(aAutoEjectMultiplier_num))
		{
			this._rejectEditAutoEject(EDIT_AUTO_EJECT_REJECT_REASONS.WRONG_AUTO_EJECT_VALUE, lBetInfo_bi, aAutoEjectMultiplier_num);
			return;
		}	
		 
		this.emit(BaseBetsController.EVENT_ON_EDIT_AUTO_EJECT_INITIATED, {betInfo: lBetInfo_bi, autoEjectMultiplier: lAutoEjectMultiplier_num});
	}

	/**
	 * Reject editing of auto-eject on client side. Rejected actions won't be sent to server.
	 * @param {string} aReason_str - Reject reason.
	 * @param {BetInfo} aBetInfo_bi - Target bet.
	 * @param {number} aAutoEjectMultiplier_num - Auto-eject multiplier value.
	 */
	_rejectEditAutoEject(aReason_str, aBetInfo_bi, aAutoEjectMultiplier_num)
	{
		console.log("** _rejectEditAutoEject, reason:", aReason_str, "; aBetIndex_int:", aBetInfo_bi.betIndex, "; aBetId_num:", aBetInfo_bi.betId, "; aAutoEjectMultiplier_num:", aAutoEjectMultiplier_num);

		this.emit(BaseBetsController.EVENT_ON_EDIT_AUTOEJECT_REJECTED, { rejectReason: aReason_str, 
																			betIndex: aBetInfo_bi.betIndex,
																			betId: aBetInfo_bi.betId,
																			autoEjectMultiplier: aAutoEjectMultiplier_num
																	});
	}

	_onServerConnectionClosed(event)
	{
		this._clearBets();
	}

	_onApplicationFatalErrorOccured(event)
	{
		this._clearBets();
	}

	_onInactiveRoundsLimit(event)
	{
		this._clearBets();
	}

	_onPlaceBetsInitiated(event)
	{
		let lBetsData_arr = event.bets;

		let lAcceptedBetsInfo_bi_arr = [];
		let lAcceptedBetsAmount_num = 0;
		for (let i=0; i<lBetsData_arr.length; i++)
		{
			let lBetData_obj = lBetsData_arr[i];
			let lAcceptedBetInfo_bi = this._acceptBet(lBetData_obj.betIndex, lBetData_obj.betValue, lBetData_obj.autoEjectMultipleier, lAcceptedBetsAmount_num);

			if (!!lAcceptedBetInfo_bi)
			{
				lAcceptedBetsAmount_num += lAcceptedBetInfo_bi.betAmount;
				lAcceptedBetsInfo_bi_arr.push(lAcceptedBetInfo_bi);
			}
		}

		if (!!lAcceptedBetsInfo_bi_arr.length)
		{
			this.emit(BaseBetsController.EVENT_ON_BETS_ACCEPTED, {bets: lAcceptedBetsInfo_bi_arr});
		}
	}

	_onCancelBetInitiated(event)
	{
		this._cancelBet(event.betIndex);
	}

	/**
	 * Update bet limits.
	 * @param {number} aMinBet_num - Minimum possible bet value.
	 * @param {number} aMaxBet_num - Maximum possible bet value.
	 * @private
	 */
	_updateBetLimits(aMinBet_num, aMaxBet_num)
	{
		this.info.updateLimits(aMinBet_num, aMaxBet_num);

		this.emit(BaseBetsController.EVENT_ON_BET_LIMITS_UPDATED);
	}
}

export default BaseBetsController;