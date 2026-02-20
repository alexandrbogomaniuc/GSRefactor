import SimpleUIInfo from "../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo";
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class ScoreboardInfo extends SimpleUIInfo
{
	constructor(aOptId_obj, aOptParentInfo_usi)
	{
		super(aOptId_obj, aOptParentInfo_usi);

		this._fScoresData_obj = null;
		this._fBossRoundScoresData_obj = null;
		this._fRoundTimeIsOver_bl = null;
		this._fRoundEndTime_num = undefined;
		this._fRoundStartTime_num = undefined;
		this._fIsUpdateLockedOnBattlegroundRoundResultExpected_bl = null;
		this._fNeededToUpdateForNewBossRound_bl = false;
	}

	get isNeededToUpdateForNewBoss()
	{
		return this._fNeededToUpdateForNewBossRound_bl;
	}

	set isNeededToUpdateForNewBoss(aValue_bl)
	{
		this._fNeededToUpdateForNewBossRound_bl = Boolean(aValue_bl);
	}

	get isUpdateLockedOnBattlegroundRoundResultExpected()
	{
		return this._fIsUpdateLockedOnBattlegroundRoundResultExpected_bl;
	}

	set isUpdateLockedOnBattlegroundRoundResultExpected(aValue_bl)
	{
		this._fIsUpdateLockedOnBattlegroundRoundResultExpected_bl = aValue_bl;
	}

	get roundEndTime()
	{
		return this._fRoundEndTime_num;
	}

	set roundEndTime(aValue_bl)
	{
		this._fRoundEndTime_num = aValue_bl;
	}

	get isRoundEndTimeDefined()
	{
		return this.roundEndTime !== undefined;
	}

	get roundStartTime()
	{
		return this._fRoundStartTime_num;
	}

	set roundStartTime(aValue_bl)
	{
		this._fRoundStartTime_num = aValue_bl;
	}

	get isRoundStartTimeDefined()
	{
		return this.roundStartTime !== undefined;
	}

	get isRoundTimeIsOver()
	{
		return this.restRoundDuration <= 0;
	}

	get restRoundDuration()
	{
		if (!this.isRoundEndTimeDefined)
		{
			return undefined;
		}

		return (this.roundEndTime - APP.gameScreen.accurateCurrentTime);
	}

	get roundPlayableDuration()
	{
		if (!this.isRoundStartTimeDefined || !this.isRoundEndTimeDefined)
		{
			return undefined;
		}

		let l_int = (this.roundEndTime - this.roundStartTime - 4000); // 4000 is for 3-2-1-GO

		if (l_int < 0)
		{
			l_int = 0;
		}

		return l_int;
	}

	resetRoundTime()
	{
		this.roundStartTime = undefined;
		this.roundEndTime = undefined;
	}

	get currentScores()
	{
		return this._fScoresData_obj;
	}

	get currentBossRoundScores()
	{
		return this._fBossRoundScoresData_obj;
	}

	get seatsData()
	{
		return this._fSeats_obj_arr;
	}

	i_updateSeatsData(aData_obj_arr)
	{
		let lBuffer_arr = [];

		for (let i = 0; i < aData_obj_arr.length; i++)
		{
			lBuffer_arr[i] = Object.assign({}, aData_obj_arr[i]);
		}

		this._fSeats_obj_arr = lBuffer_arr;
	}

	i_updateScoresData(aScores_obj, aBossScores_obj)
	{
		this._fScoresData_obj = aScores_obj;
		this._fBossRoundScoresData_obj = aBossScores_obj;
	}

	destroy()
	{
		this._fScoresData_obj = null;
		this._fBossRoundScoresData_obj = null;
		this._fSeats_obj_arr = null;

		this._fRoundTimeIsOver_bl = null;
		this._fRoundEndTime_num = undefined;
		this._fRoundStartTime_num = undefined;
		this._fIsUpdateLockedOnBattlegroundRoundResultExpected_bl = null;
		this._fNeededToUpdateForNewBossRound_bl = null;

		super.destroy();
	}
}

export default ScoreboardInfo;