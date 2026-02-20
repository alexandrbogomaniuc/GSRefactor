import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';
import BetInfo from './BetInfo';
import { SERVER_MESSAGES } from '../../interaction/server/GameWebSocketInteractionInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { DecimalPartValidator } from '../../../ui/BetInputField';

export const MAX_MASTER_BETS_AMOUNT = 3;
const MIN_AUTO_EJECT_MULT = 1.01;

class BetsInfo extends SimpleInfo
{
	constructor(aParentInfo_usi)
	{
		super(undefined, aParentInfo_usi);

		this._fBets_bi_arr = [];
		this._fQualifyBets_bi_arr = [];
		this._fLastRoundMasterBets_bi_arr = [];
		this._fCanceledBetAmount_num = 0;
		this._fMaxAutoEjectMultiplier_num = undefined;
		this._fIsExistentClientBetsClearedOnServer_bl = false;
		this._fRememberedAutoEjectCancelledBetIds_str_arr = [];
		this._fRoundInfo_ri = null;
		this._fIsNoMoreBetsPeriodMode_bl = undefined;

		this.updateLimits(1, 10);
	}

	get roundInfo()
	{
		return this._fRoundInfo_ri || (this._fRoundInfo_ri = this.gamePlayersInfo.roundInfo);
	}

	get isNoMoreBetsPeriodModeDefined()
	{
		return this._fIsNoMoreBetsPeriodMode_bl !== undefined;
	}

	get isNoMoreBetsPeriodMode()
	{
		return this._fIsNoMoreBetsPeriodMode_bl;
	}

	set isNoMoreBetsPeriodMode(value)
	{
		this._fIsNoMoreBetsPeriodMode_bl = value;
	}

	get isNoMoreBetsPeriod()
	{
		return this.isNoMoreBetsPeriodMode
				&& (
						this.roundInfo.isRoundBuyInState
						|| this.roundInfo.isRoundPauseState
					);
	}

	get gamePlayersInfo()
	{
		return this.i_getParentInfo();
	}

	get canceledBetAmount()
	{
		return this._fCanceledBetAmount_num;
	}

	set canceledBetAmount(value)
	{
		this._fCanceledBetAmount_num = value;
	}

	getLastConfirmedBet(aBetIndex_num)
	{
		return this._fLastRoundMasterBets_bi_arr[aBetIndex_num];
	}

	get minAutoEjectMultiplier()
	{
		return MIN_AUTO_EJECT_MULT;
	}

	get maxAutoEjectMultiplier()
	{
		return this._fMaxAutoEjectMultiplier_num;
	}

	set maxAutoEjectMultiplier(value)
	{
		this._fMaxAutoEjectMultiplier_num = value;
	}

	get isMaxAutoEjectMultiplierDefined()
	{
		return this._fMaxAutoEjectMultiplier_num !== undefined;
	}

	set isExistentClientBetsClearedOnServer(value)
	{
		this._fIsExistentClientBetsClearedOnServer_bl = value;
	}

	get isExistentClientBetsClearedOnServer()
	{
		return this._fIsExistentClientBetsClearedOnServer_bl;
	}

	setBets(aBetsServerData_obj_arr)
	{
		let lActualBets_bi_arr = [];
		for (let i=0; i<aBetsServerData_obj_arr.length; i++)
		{
			let lBetServerData_obj = aBetsServerData_obj_arr[i];
			let lCurBetInfo_bi;
			if (this.getBetInfo(lBetServerData_obj.betId))
			{
				lCurBetInfo_bi = this._updateBetInfo(lBetServerData_obj);
			}
			else
			{
				lCurBetInfo_bi = this._addBetInfo(lBetServerData_obj);
			}

			lActualBets_bi_arr.push(lCurBetInfo_bi);
		}

		for (let i=0; i<this._fBets_bi_arr.length; i++)
		{
			let lCurExistBetInfo_bi = this._fBets_bi_arr[i];
			let lIsBetActual_bl = false;
			if (lCurExistBetInfo_bi.isMasterBet && !lCurExistBetInfo_bi.isConfirmedMasterBet)
			{
				lIsBetActual_bl = true;
			}
			else
			{
				for (let j=0; j<lActualBets_bi_arr.length; j++)
				{
					let lCurActualBetInfo_bi = lActualBets_bi_arr[j];
					if (lCurActualBetInfo_bi.betId == lCurExistBetInfo_bi.betId)
					{
						lIsBetActual_bl = true;
						break;
					}
				}
			}

			if (!lIsBetActual_bl)
			{
				this._removeBetInfo(lCurExistBetInfo_bi.betId);
				i--;
			}
		}
	}

	setInitialQualifiedBets(aBetsServerData_obj_arr)
	{
		if (!aBetsServerData_obj_arr || !aBetsServerData_obj_arr.length)
		{
			return;
		}

		for (let i=0; i<aBetsServerData_obj_arr.length; i++)
		{
			let lBetServerData_obj = aBetsServerData_obj_arr[i];
			let lCurBetInfo_bi = this._addBetInfo(lBetServerData_obj, true);

			this._fQualifyBets_bi_arr.push(lCurBetInfo_bi);
		}
	}

	resetBets(aKeepQualifyBets_bl=false)
	{
		let lBets_bi_arr = this._fBets_bi_arr;
		while (lBets_bi_arr && !!lBets_bi_arr.length)
		{
			let lCur_bi = lBets_bi_arr.pop();
			if (lCur_bi.isAutoEjectCancelledInRound)
			{
				this._fRememberedAutoEjectCancelledBetIds_str_arr.push(lCur_bi.betId);
			}
			lCur_bi.destroy();
		}

		if (aKeepQualifyBets_bl)
		{
			// keep bets placed in QUALIFY state to be able to add them when WAIT state ocurres
		}
		else
		{
			this._removeQualifiedBets();
		}
	}

	_removeQualifiedBets()
	{
		let lBets_bi_arr = this._fQualifyBets_bi_arr;
		while (lBets_bi_arr && !!lBets_bi_arr.length)
		{
			let lCur_bi = lBets_bi_arr.pop();
			lCur_bi.destroy();
		}
	}

	applyQualifyBets()
	{
		let lQualifyBets_bi_arr = this._fQualifyBets_bi_arr;
		while (lQualifyBets_bi_arr && !!lQualifyBets_bi_arr.length)
		{
			this._fBets_bi_arr.push(lQualifyBets_bi_arr.shift());
		}
	}

	get allBets()
	{
		return this._fBets_bi_arr;
	}

	get allActiveBets()
	{
		let lBets_bi_arr = this.allBets;
		if (!lBets_bi_arr || !lBets_bi_arr.length)
		{
			return null;
		}

		return lBets_bi_arr.filter(betInfo => !betInfo.isDeactivatedBet);
	}

	get allDeactiveBets()
	{
		let lBets_bi_arr = this.allBets;
		if (!lBets_bi_arr || !lBets_bi_arr.length)
		{
			return null;
		}

		return lBets_bi_arr.filter(betInfo => betInfo.isDeactivatedBet);
	}

	get allEjectedBets()
	{
		let lBets_bi_arr = this.allActiveBets;
		if (!lBets_bi_arr || !lBets_bi_arr.length)
		{
			return null;
		}

		return lBets_bi_arr.filter(betInfo => betInfo.isEjected);
	}

	addBetInfo(aBetServerData_obj)
	{
		return this._addBetInfo(aBetServerData_obj);
	}

	addQualifiedBetInfo(aBetServerData_obj)
	{
		return this._addBetInfo(aBetServerData_obj, true);
	}

	removeBetInfo(aBetId_num)
	{
		this._removeBetInfo(aBetId_num);
	}

	removeQualifiedBetInfo(aBetServerData_obj)
	{
		let lBetId_str = aBetServerData_obj.betId || aBetServerData_obj.crashBetId || aBetServerData_obj.betId || aBetServerData_obj.crashBetKey || undefined;

		this._removeBetInfo(lBetId_str, true)
	}

	updateBetInfo(aBetServerData_obj)
	{
		this._updateBetInfo(aBetServerData_obj);
	}

	getBetInfo(aBetId_num, aOptIsQualifiedBet_bl=false)
	{
		let lBets_bi_arr = !!aOptIsQualifiedBet_bl ? this._fQualifyBets_bi_arr : this._fBets_bi_arr;

		if (!lBets_bi_arr.length)
		{
			return null;
		}

		for (let i=0; i<lBets_bi_arr.length; i++)
		{
			let lCur_bi = lBets_bi_arr[i];
			if (lCur_bi.betId === aBetId_num)
			{
				return lCur_bi;
			}
		}

		return null;
	}

	getBetByIndex(aBetIndex_int, aSeatId_num)
	{
		let lBets_bi_arr = this._fBets_bi_arr;

		if (!lBets_bi_arr.length)
		{
			return null;
		}

		for (let i=0; i<lBets_bi_arr.length; i++)
		{
			let lCur_bi = lBets_bi_arr[i];
			if (lCur_bi.betIndex === aBetIndex_int && lCur_bi.seatId === aSeatId_num)
			{
				return lCur_bi;
			}
		}

		return null;
	}

	getMasterBetInfoByIndex(aBetIndex_num, aSkipDeactivatedBets_bl=false)
	{
		let lBets_bi_arr = this._fBets_bi_arr;

		if (!lBets_bi_arr.length)
		{
			return null;
		}

		for (let i=0; i<lBets_bi_arr.length; i++)
		{
			let lCur_bi = lBets_bi_arr[i];
			if (lCur_bi.isMasterBet && lCur_bi.betIndex === aBetIndex_num)
			{
				if (aSkipDeactivatedBets_bl && lCur_bi.isDeactivatedBet)
				{
					continue;
				}

				return lCur_bi;
			}
		}

		return null;
	}

	getPlayerBets(aSeatId_num)
	{
		let lBets_bi_arr = this._fBets_bi_arr;

		if (!lBets_bi_arr.length)
		{
			return null;
		}

		let lPlayerBets_bi_arr = null;
		for (let i=0; i<lBets_bi_arr.length; i++)
		{
			let lCur_bi = lBets_bi_arr[i];
			if (lCur_bi.seatId === aSeatId_num)
			{
				lPlayerBets_bi_arr = lPlayerBets_bi_arr || [];
				lPlayerBets_bi_arr.push(lCur_bi);
			}
		}

		return lPlayerBets_bi_arr;
	}

	getPlayerActiveBets(aSeatId_num)
	{
		let lPlayerBets_bi_arr = this.getPlayerBets(aSeatId_num);
		
		if (!lPlayerBets_bi_arr || !lPlayerBets_bi_arr.length)
		{
			return null;
		}

		return lPlayerBets_bi_arr.filter(betInfo => !betInfo.isDeactivatedBet);
	}

	confirmMasterBets()
	{
		let lIsAnyBetConfirmed_bl = false;
		let lBets_bi_arr = this._fBets_bi_arr;

		for (let i=0; i<lBets_bi_arr.length; i++)
		{
			let lCur_bi = lBets_bi_arr[i];
			if (lCur_bi.isMasterBet)
			{
				lCur_bi.isConfirmedMasterBet = true;
				if (!this.isNoMoreBetsPeriodMode)
				{
					lCur_bi.isExternallyConfirmedMasterBet = true;
				}
				lIsAnyBetConfirmed_bl = true;
			}
		}

		return lIsAnyBetConfirmed_bl;
	}

	externallyConfirmMasterBets()
	{
		let lIsAnyBetConfirmed_bl = false;
		let lBets_bi_arr = this._fBets_bi_arr;

		for (let i=0; i<lBets_bi_arr.length; i++)
		{
			let lCur_bi = lBets_bi_arr[i];
			if (lCur_bi.isMasterBet)
			{
				if (!lCur_bi.isConfirmedMasterBet)
				{
					// Possible if we send bet request too close to transition to BUY_IN state.
					// In this case we get CrashAllBetsResponse, then GameStateChanged to BUY_IN, and then Error 1021 (BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE) for this bet
					continue;
				}

				lCur_bi.isExternallyConfirmedMasterBet = true;
				lIsAnyBetConfirmed_bl = true;
			}
		}

		return lIsAnyBetConfirmed_bl;
	}

	cancelAutoEject(aBetId_str)
	{
		let lBetInfo_bi = this.getBetInfo(aBetId_str);
		if (lBetInfo_bi)
		{
			lBetInfo_bi.isAutoEject = false;
			lBetInfo_bi.autoEjectMultiplier = undefined;
		}
	}

	_addBetInfo(aBetServerData_obj, aOptIsQualifiedBet_bl=false)
	{
		let lBetId_str = aBetServerData_obj.betId || aBetServerData_obj.crashBetId || aBetServerData_obj.crashBetKey;

		let lExBet_bi = this.getBetInfo(lBetId_str, aOptIsQualifiedBet_bl);
		if (!!lExBet_bi)
		{
			throw new Error(`Bet already exists: ${lBetId_str}.`);
			return null;
		}

		this._generateBetInfo(lBetId_str, aOptIsQualifiedBet_bl);

		return this._updateBetInfo(aBetServerData_obj, aOptIsQualifiedBet_bl);
	}

	_removeBetInfo(aBetId_num, aOptIsQualifiedBet_bl=false)
	{
		let lBets_bi_arr = !!aOptIsQualifiedBet_bl ? this._fQualifyBets_bi_arr : this._fBets_bi_arr;
		for (let i=0; i<lBets_bi_arr.length; i++)
		{
			let lCur_bi = lBets_bi_arr[i];
			if (lCur_bi.betId === aBetId_num)
			{
				lCur_bi.destroy();
				lBets_bi_arr.splice(i, 1);
				break;
			}
		}
	}

	_updateBetInfo(aBetServerData_obj, aOptIsQualifiedBet_bl=false)
	{
		let lBetId_str = aBetServerData_obj.betId || aBetServerData_obj.crashBetId || aBetServerData_obj.betId || aBetServerData_obj.crashBetKey || undefined;
		let l_gpi = APP.gameController.gameplayController.info;

		let l_bi = this.getBetInfo(lBetId_str, aOptIsQualifiedBet_bl);
		if (!l_bi)
		{
			throw new Error(`Bet does not exist: ${lBetId_str}.`);
			return null;
		}

		let lServerBetAmountValue_num = +aBetServerData_obj.amount || +aBetServerData_obj.crashBetAmount;
		if (!isNaN(lServerBetAmountValue_num))
		{
			l_bi.betAmount = lServerBetAmountValue_num;
		}

		let lServerIsAutoEjectedValue_bl = aBetServerData_obj.auto;
		if (lServerIsAutoEjectedValue_bl !== undefined)
		{
			l_bi.isAutoEject = !!lServerIsAutoEjectedValue_bl;
		}

		let lServerEjectTime_num = +aBetServerData_obj.ejectTime;
		if (!isNaN(lServerEjectTime_num) && !isNaN(+aBetServerData_obj.mult) && +aBetServerData_obj.mult > 0)
		{
			if (lServerEjectTime_num > 0)
			{
				l_bi.ejectTime = l_gpi.multiplierChangeFlightStartTime + l_gpi.calculateTime(+aBetServerData_obj.mult);
			}
			else
			{
				l_bi.ejectTime = 0;
			}
		}

		let lIsReservedBet_bl = aBetServerData_obj.isReserved;
		if (lIsReservedBet_bl !== undefined)
		{
			l_bi.isExternallyConfirmedMasterBet = !lIsReservedBet_bl;
		}

		if (aBetServerData_obj.class !== undefined && aBetServerData_obj.class == SERVER_MESSAGES.CRASH_CANCEL_BET_RESPONSE)
		{
			if (aBetServerData_obj.currentMult > 0) // bet cancel - eject action
			{
				l_bi.multiplier = 1 + (aBetServerData_obj.currentMult - 1) * l_gpi.distanceMultiplier;

				l_bi.ejectTime = l_gpi.multiplierChangeFlightStartTime + l_gpi.calculateTime(+aBetServerData_obj.currentMult);

				l_bi.betWin = aBetServerData_obj.seatWin;
			}
			else // bet cancel out of round
			{
				l_bi.betWin = aBetServerData_obj.seatWin;
			}

			if (l_bi.isDeactivatedBet)
			{
				l_bi.deactivateBetTime = aBetServerData_obj.date;
			}
		}
		else
		{
			if (!lServerEjectTime_num) // still in ship (not ejected) or placebet state
			{
				l_bi.multiplier = 0;

				if (l_bi.isAutoEject)
				{
					let lAutoEjectMult_num = (+aBetServerData_obj.mult || +aBetServerData_obj.autoEjectMult);
					if (!isNaN(lAutoEjectMult_num))
					{
						l_bi.autoEjectMultiplier = lAutoEjectMult_num;
					}
				}
			}
			else // already ejected
			{
				if (aBetServerData_obj.autoPlayMultiplier !== undefined)
				{
					l_bi.isAutoEject = true;
					l_bi.autoEjectMultiplier = +aBetServerData_obj.autoPlayMultiplier;
				}
				l_bi.multiplier = (1 + (+aBetServerData_obj.mult - 1) * l_gpi.distanceMultiplier) || 0;
				l_bi.betWin = APP.isBattlegroundGame ? 0 : l_bi.multiplier * l_bi.betAmount;
			}
		}

		if (aBetServerData_obj.class !== undefined && aBetServerData_obj.class == SERVER_MESSAGES.CRASH_BET_RESPONSE)
		{
			l_bi.placeBetTime = aBetServerData_obj.date;

			if (aBetServerData_obj.rid !== -1)
			{
				l_bi.isConfirmedMasterBet = true;
				if (!this.isNoMoreBetsPeriodMode)
				{
					l_bi.isExternallyConfirmedMasterBet = true;
				}
			}
		}
		return l_bi;
	}

	_generateBetInfo(aBetId_num, aOptIsQualifiedBet_bl=false)
	{
		let l_bi = new BetInfo(this);
		l_bi.betId = aBetId_num;

		let lIndex_int = this._fRememberedAutoEjectCancelledBetIds_str_arr.indexOf(l_bi.betId);
		if (lIndex_int >= 0)
		{
			l_bi.isAutoEjectCancelledInRound = true;
			this._fRememberedAutoEjectCancelledBetIds_str_arr.splice(lIndex_int, 1);
		}

		if (!!aOptIsQualifiedBet_bl)
		{
			this._fQualifyBets_bi_arr.push(l_bi);
		}
		else
		{
			this._fBets_bi_arr.push(l_bi);
		}

		return l_bi;
	}

	rememberLastRoundMasterBets()
	{
		for (let i=0; i<MAX_MASTER_BETS_AMOUNT; i++)
		{
			let lCurRememberedBet_bi = this._fLastRoundMasterBets_bi_arr[i];
			lCurRememberedBet_bi && lCurRememberedBet_bi.destroy();

			let lBetInfo_bi = this.getMasterBetInfoByIndex(i, true);
			this._fLastRoundMasterBets_bi_arr[i] = !!lBetInfo_bi ? lBetInfo_bi.clone() : null;
		}
	}

	updateLimits(aMinBet_num, aMaxBet_num)
	{
		let lBetStep_int;
		// [OWL] TODO: apply changes for alll systems without any conditions
		if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
		{
			let lPower_num = DecimalPartValidator.getAmountOfDecimalsToTrunc();
			let lMinimalAllowedInCents_num = Math.pow(10, lPower_num);

			// normalize by minimal allowed price in cents
			aMinBet_num = Math.max(aMinBet_num, lMinimalAllowedInCents_num);
			aMinBet_num = Math.ceil(aMinBet_num / lMinimalAllowedInCents_num) * lMinimalAllowedInCents_num;
			aMaxBet_num = Math.floor(aMaxBet_num / lMinimalAllowedInCents_num) * lMinimalAllowedInCents_num;

			lBetStep_int = Math.floor(aMinBet_num / 2 / lMinimalAllowedInCents_num) * lMinimalAllowedInCents_num;
			this._fBetStep_int = lBetStep_int > 1 ? lBetStep_int : lMinimalAllowedInCents_num;
		}
		else
		{
			lBetStep_int = Math.floor(aMinBet_num / 2);
			this._fBetStep_int = lBetStep_int > 1 ? lBetStep_int : 1;
		}

		this._fBetMinimalCentsCount_int = aMinBet_num;
		this._fBetMaximalCentsCount_int = aMaxBet_num;
	}

	get betMinimalCentsCount()
	{
		return this._fBetMinimalCentsCount_int;
	}

	get betMaximalCentsCount()
	{
		return this._fBetMaximalCentsCount_int;
	}

	get centsCountPerBetStep()
	{
		return this._fBetStep_int;	
	}

	isValidBetValue(aBetCents_num)
	{
		let lBetValue_num = +aBetCents_num;

		if (APP.isBattlegroundGame)
		{
			if (isNaN(lBetValue_num))
			{
				return false;
			}
		}
		else
		{
			if (
					isNaN(lBetValue_num)
					|| lBetValue_num > this.betMaximalCentsCount
					|| lBetValue_num < this.betMinimalCentsCount
				)
			{
				return false;
			}
		}

		return true;
	}

	isValidAutoEjectMultiplier(aMult_num)
	{
		if (aMult_num === undefined)
		{
			return true;
		}

		let lMultValue_num = +aMult_num;

		if (
				isNaN(lMultValue_num)
				|| lMultValue_num < this.minAutoEjectMultiplier
				|| (this.isMaxAutoEjectMultiplierDefined && lMultValue_num > this.maxAutoEjectMultiplier)
			)
		{
			return false;
		}

		return true;
	}


	destroy()
	{
		while (this._fBets_bi_arr && this._fBets_bi_arr.length)
		{
			this._fBets_bi_arr.pop().destroy();
		}
		this._fBets_bi_arr = null;

		while (this._fQualifyBets_bi_arr && this._fQualifyBets_bi_arr.length)
		{
			this._fQualifyBets_bi_arr.pop().destroy();
		}
		this._fQualifyBets_bi_arr = null;
		
		this._fLastRoundMasterBets_bi_arr = null;

		this._fBetMinimalCentsCount_int = undefined;
		this._fBetMaximalCentsCount_int = undefined;
		this._fBetStep_int = undefined;

		super.destroy();
	}
}

export default BetsInfo