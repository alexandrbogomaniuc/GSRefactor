import SimpleUIInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import RoundInfo from './RoundInfo';
import RoomInfo from './RoomInfo';
import { PSEUDO_RANDOM_NUMBERS, PSEUDO_RANDOM_NUMBERS_BTG } from '../../config/Constants';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GamePlayersInfo from './players/GamePlayersInfo';
import MAnimation from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MAnimation';
import PendingOperationInfo from './PendingOperationInfo';

const MIN_MULTIPLIER_VALUE = 1;
const MAX_ROUND_DURATION = 30000;

export const BTG_PLAY_STATE_MULTIPLIER_CHANGE_DELAY = 5284;

class GameplayInfo extends SimpleUIInfo
{
	static get MULTIPLIER_MAX_LAG_DELAY()
	{
		return 500;
	}

	static get MAX_CONNECTION_INTERRUPTION_DELAY()
	{
		return 3500;
	}

	static formatMultiplier(aMult_num)
	{
		let lMultiplier_num = aMult_num;

		if(lMultiplier_num < 1)
		{
			lMultiplier_num = 1;
		}

		let lFormatted_str = (lMultiplier_num).toFixed(2) + "x";

		if (APP.isBattlegroundGame)
		{
			lFormatted_str = ((lMultiplier_num-MIN_MULTIPLIER_VALUE)*100).toFixed(0) + "km"
		}

		return lFormatted_str;
	}

	static formatTime(aTime_num)
	{
		aTime_num = +aTime_num;
		if (isNaN(aTime_num) || aTime_num < 0)
		{
			aTime_num = 0;
		}

		let lMillisecondsCount_int = Math.floor((aTime_num % 1000) / 100);
		let lSecondsCount_int = Math.floor(aTime_num / 1000);

		lMillisecondsCount_int = (lMillisecondsCount_int < 10) ? lMillisecondsCount_int + "0" : lMillisecondsCount_int;

		let lFormattedTime_str = `${lSecondsCount_int}.${lMillisecondsCount_int}`;
		return lFormattedTime_str;
	}

	constructor()
	{
		super();

		this._fServerMultiplierValue_num = this.minMultiplierValue;
		this._fServerMultiplierTime_num = undefined;
		this._fServerMultiplierRecieveTime_num = undefined;
		this._fPrevServerMultiplierValue_num = 0;
		this._fMultiplierValue_num = this.minMultiplierValue;
		this._fMultiplierTime_num = 0;
		this._fRoundInfo_rsi = null;
		this._fRoomInfo_ri = null;
		this._fGamePlayersInfo_gpsi = null;
		this._fCalcMultExpression_func = new Function('t', "return Math.exp((t * 0.06012) / 1000)");
		this._fCalcTimeExpression_func = new Function('t', "return Math.log(t) * 1000 / 0.06012");
		this._fGameplayTime_num = 0;
		this._fDistanceMultiplier_num = 1;
		this._fTimeMultiplier_num = 1;
		this._fAllEjectedTime = 0;
	}

	get isPreLaunchFlightRequired()
	{
		return this.preLaunchFlightDuration > 0;
	}

	get preLaunchFlightDuration()
	{
		return APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode ? 3000 : APP.isBattlegroundGame ? 2000 : 0;
	}

	get preLaunchFlightTurnDuration()
	{
		return APP.isBattlegroundGame ? 1200 : 1800;
	}

	get preLaunchZoomOutDuration()
	{
		return this.preLaunchFlightDuration-200;
	}

	get maxRoundDuration()
	{
		return MAX_ROUND_DURATION;
	}

	set distanceMultiplier(value)
	{
		this._fDistanceMultiplier_num = value;
	}

	get distanceMultiplier()
	{
		return this._fDistanceMultiplier_num;
	}

	set timeMultiplier(value)
	{
		this._fTimeMultiplier_num = value;
	}

	get timeMultiplier()
	{
		return this._fTimeMultiplier_num;
	}

	set allEjectedTime(value)
	{
		this._fAllEjectedTime = value;
	}

	get allEjectedTime()
	{
		return this._fAllEjectedTime;
	}

	get isPreLaunchTimePeriod()
	{
		if (!this.isPreLaunchFlightRequired || !this.roundInfo.isRoundStartTimeDefined)
		{
			return false;
		}

		if (APP.isBattlegroundGame && !this.roundInfo.isRoundPlayState)
		{
			return false;
		}

		let lRestTime_num = this.multiplierChangeFlightRestTime;

		return lRestTime_num > 0 && lRestTime_num <= this.preLaunchFlightDuration;
	}

	get roundStartRestTime()
	{
		if (!this.roundInfo.isRoundStartTimeDefined)
		{
			return undefined;
		}

		return this.roundInfo.roundStartTime - this.gameplayTime;
	}

	get multiplierChangeFlightRestTime()
	{
		if (this.multiplierChangeFlightStartTime === undefined)
		{
			return undefined;
		}

		return this.multiplierChangeFlightStartTime - this.gameplayTime;
	}

	get multiplierChangeFlightStartTime() // flight point when multiplier starts growing and rocket's explosion is possible
	{
		if (!this.roundInfo.isRoundStartTimeDefined)
		{
			return undefined;
		}

		let l_num = this.roundInfo.roundStartTime;

		if (APP.isBattlegroundGame)
		{
			if (this.roundInfo.isRoundPlayState || this.roundInfo.isRoundEndTimeDefined)
			{
				l_num = this.roundInfo.roundStartTime;
			}
			else
			{
				if (this.gameplayTime > this.roundInfo.roundStartTime+100) // timer finished but GameStateChanged (with PLAY or WAIT again) is not still recieved
				{
					l_num = this.gameplayTime + BTG_PLAY_STATE_MULTIPLIER_CHANGE_DELAY;
				}
				else
				{
					l_num = this.roundInfo.roundStartTime + BTG_PLAY_STATE_MULTIPLIER_CHANGE_DELAY;
				}
			}
		}

		return l_num;
	}

	calculateMultiplier(aRoundDuration_num)
	{
		let lRoundDuration_num = aRoundDuration_num;

		if (lRoundDuration_num < 0)
		{
			if (this.isPreLaunchFlightRequired)
			{
				let lPassedPreLaunchDuration_num = this.preLaunchFlightDuration - Math.abs(lRoundDuration_num);
				let lPreLaunchDurationProgress_num = (lPassedPreLaunchDuration_num / this.preLaunchFlightDuration);

				let lFullTurnMultDelta_num = Math.abs(this.minMultiplierValue);
				let lMult_num = lFullTurnMultDelta_num*MAnimation.getEasingMultiplier(MAnimation.EASE_OUT, lPreLaunchDurationProgress_num);

				return lMult_num;
			}
		}

		let lMult_num = 1 + (this._fCalcMultExpression_func(aRoundDuration_num) - 1)*this._fDistanceMultiplier_num;

		return lMult_num;
	}

	/**
	 * calculates duration in milliseconds since round start time till aCurrentMultiplier_num
	 */
	calculateTime(aCurrentMultiplier_num)
	{
		let lCurrentMultiplier_num = aCurrentMultiplier_num;

		return +this._fCalcTimeExpression_func(lCurrentMultiplier_num).toFixed(0);
	}

	updateMultiplierExpression(aExpression_str)
	{
		this._fCalcMultExpression_func = new Function('t', ("return " + aExpression_str));
	}

	set multiplierValue(value)
	{
		this._fMultiplierValue_num = value;
	}

	get multiplierValue()
	{
		return this._fMultiplierValue_num;
	}

	set serverMultiplierValue(value)
	{
		this._fServerMultiplierValue_num = value;
	}

	get serverMultiplierValue()
	{
		return Math.round(this._fServerMultiplierValue_num * 100) / 100;
	}

	set prevServerMultiplierValue(value)
	{
		this._fPrevServerMultiplierValue_num = value;
	}

	get prevServerMultiplierValue()
	{
		return this._fPrevServerMultiplierValue_num;
	}

	get serverMultiplierTimeDefined()
	{
		return this._fServerMultiplierTime_num !== undefined;
	}

	/**
	 * timestamp of last server multiplier value (calculated by calculateTime function)
	 */
	get serverMultiplierTime()
	{
		return this._fServerMultiplierTime_num;
	}

	set serverMultiplierTime(value)
	{
		this._fServerMultiplierTime_num = value;
	}

	/**
	 * timestamp of last server multiplier value (date in server message CrachStateInfo)
	 */
	get serverMultiplierRecieveTime()
	{
		return this._fServerMultiplierRecieveTime_num;
	}

	set serverMultiplierRecieveTime(value)
	{
		this._fServerMultiplierRecieveTime_num = value;
	}

	get serverMultiplierRecieveTimeDefined()
	{
		return this._fServerMultiplierRecieveTime_num !== undefined;
	}

	get lastServerMultiplierRecievedValue()
	{
		return this._fServerMultiplierRecievedValue_num;
	}

	set lastServerMultiplierRecievedValue(value)
	{
		this._fServerMultiplierRecievedValue_num = value;
	}

	get serverMultiplierRecievedValueDefined()
	{
		return this._fServerMultiplierRecievedValue_num !== undefined;
	}

	get minMultiplierValue()
	{
		return MIN_MULTIPLIER_VALUE;
	}

	set multiplierTime(value)
	{
		this._fMultiplierTime_num = value;
	}

	/**
	 * timestamp of last multiplier value calculated on client side (calculates every tick for smooth flight)
	 */
	get multiplierTime()
	{
		return this._fMultiplierTime_num;
	}

	resetMultiplier()
	{
		this._fServerMultiplierValue_num = this.minMultiplierValue;
		this._fServerMultiplierTime_num = undefined;
		this._fServerMultiplierRecieveTime_num = undefined;
		this._fServerMultiplierRecievedValue_num = undefined;
		this._fPrevServerMultiplierValue_num = 0;
		this._fMultiplierValue_num = this.minMultiplierValue;
		this._fMultiplierTime_num = 0;
		this._fDistanceMultiplier_num = 1;
		this._fTimeMultiplier_num = 1;
		this._fAllEjectedTime = 0;
	}

	set gameplayTime(value)
	{
		this._fGameplayTime_num = value;
	}

	get gameplayTime()
	{
		return this._fGameplayTime_num;
	}

	get multiplierRoundDuration()
	{
		// in milliseconds
		let lDurationInMilliseconds_num = this._calculateDurationByMultiplierTime(this.multiplierTime);
		
		if (isNaN(lDurationInMilliseconds_num) || lDurationInMilliseconds_num < 0)
		{
			lDurationInMilliseconds_num = 0;
		}

		return lDurationInMilliseconds_num;
	}

	get serverMultiplierRoundDuration()
	{
		if (!this.serverMultiplierTimeDefined)
		{
			return 0;
		}

		// in milliseconds
		let lDurationInMilliseconds_num = this._calculateDurationByMultiplierTime(this.serverMultiplierTime);
		
		if (isNaN(lDurationInMilliseconds_num) || lDurationInMilliseconds_num < 0)
		{
			lDurationInMilliseconds_num = 0;
		}

		return lDurationInMilliseconds_num;
	}

	_calculateDurationByMultiplierTime(aMultiplierTime_num)
	{
		let lMultiplierChangeStartTime_num = this.multiplierChangeFlightStartTime;
		return (aMultiplierTime_num - this.allEjectedTime)*this.timeMultiplier + this.allEjectedTime - lMultiplierChangeStartTime_num;
	}

	get outOfRoundDuration()
	{
		// in milliseconds
		if (!this.roundInfo.isRoundEndTimeDefined)
		{
			return 0;
		}

		let lDurationInMilliseconds_num = this.gameplayTime - this.roundInfo.roundEndTime;
		if (isNaN(lDurationInMilliseconds_num) || lDurationInMilliseconds_num < 0)
		{
			lDurationInMilliseconds_num = 0;
		}

		return +lDurationInMilliseconds_num.toFixed(0);
	}

	get roundInfo()
	{
		return this._fRoundInfo_rsi || (this._fRoundInfo_rsi = this._generateRoundInfo())
	}

	_generateRoundInfo()
	{
		let lRoundInfo_rsi = new RoundInfo(this);
		return lRoundInfo_rsi;
	}

	get pendingOperationInfo()
	{
		return this._fPendingOperationInfo_poi || (this._fPendingOperationInfo_poi = this._generatePendingOperationInfo());
	}

	_generatePendingOperationInfo()
	{
		let lPendingOperationInfo_poi = new PendingOperationInfo(this);
		return lPendingOperationInfo_poi;
	}

	get roomInfo()
	{
		return this._fRoomInfo_ri || (this._fRoomInfo_ri = this._generateRoomInfo())
	}

	_generateRoomInfo()
	{
		let lRoomInfo_ri = new RoomInfo(this);
		return lRoomInfo_ri;
	}

	get newAsteroid()
	{
		return this._new_asterioid_bul;
	}

	set newAsteroid(value)
	{
		this._new_asterioid_bul = value;
	}
	get gamePlayersInfo()
	{
		return this._fGamePlayersInfo_gpsi || (this._fGamePlayersInfo_gpsi = this._generateGamePlayersInfo());
	}

	_generateGamePlayersInfo()
	{
		let lGamePlayersInfo_gpsi = new GamePlayersInfo(this);
		return lGamePlayersInfo_gpsi;
	}

	getPseudoRandomValue(aIndex_int, forceOriginal)
	{
	
		if(APP.isBattlegroundGame && !forceOriginal)
		{
			return PSEUDO_RANDOM_NUMBERS_BTG[0];
		}else{
			let lFinalPseudoRandomIndex_int = PSEUDO_RANDOM_NUMBERS.length - 1;
			let lRoundId_int = this.roundInfo.roundId || 0;
			let lOffsetIndex_int = lRoundId_int % PSEUDO_RANDOM_NUMBERS.length;
			let lIndex_int = aIndex_int + lOffsetIndex_int;
	
			if(lIndex_int > lFinalPseudoRandomIndex_int)
			{
				lIndex_int = lIndex_int % PSEUDO_RANDOM_NUMBERS.length;
			}
	
			return PSEUDO_RANDOM_NUMBERS[lIndex_int];
		}
		
	}

	destroy()
	{
		super.destroy();
	}
}
export default GameplayInfo;