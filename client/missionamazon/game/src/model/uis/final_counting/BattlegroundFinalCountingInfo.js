import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class BattlegroundFinalCountingInfo extends SimpleUIInfo
{
	static get NUMBER_COUNT()						{ return 4; }
	static get STEP_TIME_COUNTING()					{ return 925; }
	static get TIME_COUNTING()						{ return BattlegroundFinalCountingInfo.NUMBER_COUNT * BattlegroundFinalCountingInfo.STEP_TIME_COUNTING; }
	static get TIME_COUNTING_NEEDFULL()				{ return 4000; }
	static get TIME_COUNTING_DELAY()				{ return 200; }
	static get TIME_COUNTING_DIFFERENCE()			{ return BattlegroundFinalCountingInfo.TIME_COUNTING_NEEDFULL - BattlegroundFinalCountingInfo.TIME_COUNTING; }

	static get STATE_INVALID()						{ return -1; }
	static get STATE_WAITING()						{ return 0; }
	static get STATE_PLAYING()						{ return 1; }
	static get STATE_COMPLETING()					{ return 2; }
	static get STATE_COMPLETED()					{ return 3; }

	constructor()
	{
		super();

		this._fCurrentNumberCount_num = null;
		this._fStartTime_num = null;
		this._fIsFinalCountingStarted_bl = null;
		this._fIsStartFinalCountingTimeExpectedOnScoreBoard_bl = null;
		this._fIsStartFinalCountingAllowedOnThisState_bl = null;
		this._fIsFinalCountingFireDenied_bl = null;
	}

	get isFinalCountingFireDenied()
	{
		return this._fIsFinalCountingFireDenied_bl;
	}

	set isFinalCountingFireDenied(aVal_bl)
	{
		this._fIsFinalCountingFireDenied_bl = aVal_bl;
	}

	get isStartFinalCountingTimeExpectedOnScoreBoard()
	{
		return this._fIsStartFinalCountingTimeExpectedOnScoreBoard_bl;
	}

	set isStartFinalCountingTimeExpectedOnScoreBoard(aVal_bl)
	{
		this._fIsStartFinalCountingTimeExpectedOnScoreBoard_bl = aVal_bl;
	}

	get isStartFinalCountingAllowedOnThisState()
	{
		return this._fIsStartFinalCountingAllowedOnThisState_bl;
	}

	set isStartFinalCountingAllowedOnThisState(aVal_bl)
	{
		this._fIsStartFinalCountingAllowedOnThisState_bl = aVal_bl;
	}

	get currentNumberCount()
	{
		return this._fCurrentNumberCount_num;
	}

	set currentNumberCount(aVal_bl)
	{
		this._fCurrentNumberCount_num = aVal_bl;
	}

	get isFinalCountingStarted()
	{
		return this._fIsFinalCountingStarted_bl;
	}

	set isFinalCountingStarted(aVal_bl)
	{
		return this._fIsFinalCountingStarted_bl = aVal_bl;
	}

	get isFinalCountingPlaying()
	{
		return this.getTimeState() == BattlegroundFinalCountingInfo.STATE_PLAYING;
	}

	get isFinalCountingWaitPlaying()
	{
		return this.getTimeState() == BattlegroundFinalCountingInfo.STATE_WAITING;
	}

	get isFinalCountingInvalid()
	{
		return this.getTimeState() == BattlegroundFinalCountingInfo.STATE_INVALID;
	}

	get isFinalCountingCompleted()
	{
		return this.getTimeState() == BattlegroundFinalCountingInfo.STATE_COMPLETED;
	}

	get isFinalCountingCompleting()
	{
		return this.getTimeState() == BattlegroundFinalCountingInfo.STATE_COMPLETING;
	}

	nextNumberCount()
	{
		this._fCurrentNumberCount_num++;
	}

	rememberStartTime(aValue_num)
	{
		this._fStartTime_num = aValue_num + BattlegroundFinalCountingInfo.TIME_COUNTING_DELAY + BattlegroundFinalCountingInfo.TIME_COUNTING_DIFFERENCE;
	}

	get startTime()
	{
		return this._fStartTime_num;
	}

	resetStartTime()
	{
		this._fStartTime_num = null;
	}

	getActualNumerCountAndTimeToStart()
	{
		if (!this._fStartTime_num || !this.isFinalCountingPlaying)
		{
			return null;
		}

		let lTime_num = APP.gameScreen.accurateCurrentTime;
		let lTimeDif_num = BattlegroundFinalCountingInfo.TIME_COUNTING + this._fStartTime_num - lTime_num ;
		let lNumber_num = BattlegroundFinalCountingInfo.NUMBER_COUNT - Math.floor((lTimeDif_num) / BattlegroundFinalCountingInfo.STEP_TIME_COUNTING);

		if (lNumber_num <= BattlegroundFinalCountingInfo.NUMBER_COUNT
			&& lNumber_num >= 0)
		{
			return {
				number: lNumber_num,
				time: (lTimeDif_num - (BattlegroundFinalCountingInfo.NUMBER_COUNT - lNumber_num) * BattlegroundFinalCountingInfo.STEP_TIME_COUNTING)
			}
		}

		return null;
	}

	getTimeStateBeforeStartCounting()
	{
		if (!this._fStartTime_num)
		{
			return null;
		}

		let lTime_num = APP.gameScreen.accurateCurrentTime;
		let lTimeLeft_num = this._fStartTime_num - lTime_num;

		if (lTimeLeft_num > 0)
		{
			return (lTimeLeft_num);
		}

		return 0;
	}

	getTimeStateBeforeCompleted()
	{
		if (!this._fStartTime_num)
		{
			return null;
		}

		let lTime_num = APP.gameScreen.accurateCurrentTime;
		let lTimeLeft_num = this._fStartTime_num + BattlegroundFinalCountingInfo.TIME_COUNTING - lTime_num;

		if (lTimeLeft_num > 0)
		{
			return (lTimeLeft_num);
		}

		return 0;
	}

	getTimeState()
	{
		if (!this._fStartTime_num)
		{
			return BattlegroundFinalCountingInfo.STATE_INVALID;
		}

		let lTime_num = APP.gameScreen.accurateCurrentTime;
		let lTimeLeft_num = lTime_num - this._fStartTime_num;

		if (lTimeLeft_num < 0) 
		{
			return BattlegroundFinalCountingInfo.STATE_WAITING;
		}
		else if (lTimeLeft_num >= 0 
			&& lTimeLeft_num <= (BattlegroundFinalCountingInfo.TIME_COUNTING - BattlegroundFinalCountingInfo.STEP_TIME_COUNTING))
		{
			return BattlegroundFinalCountingInfo.STATE_PLAYING;
		}
		else if (lTimeLeft_num > (BattlegroundFinalCountingInfo.TIME_COUNTING - BattlegroundFinalCountingInfo.STEP_TIME_COUNTING)
				&& lTimeLeft_num <= BattlegroundFinalCountingInfo.TIME_COUNTING)
		{
			return BattlegroundFinalCountingInfo.STATE_COMPLETING;
		}
		else
		{
			return BattlegroundFinalCountingInfo.STATE_COMPLETED;
		}
	}

	destroy()
	{
		super.destroy();

		this._fCurrentNumberCount_num = null;
		this._fStartTime_num = null;
		this._fIsFinalCountingStarted_bl = null;
		this._fIsStartFinalCountingTimeExpectedOnScoreBoard_bl = null;
		this._fIsStartFinalCountingAllowedOnThisState_bl = null;
		this._fIsFinalCountingFireDenied_bl = null;
	}
}

export default BattlegroundFinalCountingInfo