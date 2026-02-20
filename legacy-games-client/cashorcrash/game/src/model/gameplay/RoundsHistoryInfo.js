import SimpleInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class RoundsHistoryInfo extends SimpleInfo
{
	constructor()
	{
		super();

		this._fMultHistory_obj_arr = null;
		this._fSnapshotTime_num = null;
	}

	tryToRefreshHistory(aMultHistory_obj_arr, aDate_num)
	{
		if (!this._fSnapshotTime_num || this._fSnapshotTime_num < aDate_num)
		{
			this._fSnapshotTime_num = aDate_num;
			this._fMultHistory_obj_arr = [];
			for (let i = 0; i < aMultHistory_obj_arr.length; i++)
			{
				this._fMultHistory_obj_arr.unshift(aMultHistory_obj_arr[i]);
			}
			return true;
		}
		return false;
	}

	getIdByRoundId(aRoundId_num)
	{
		if (this._fMultHistory_obj_arr && aRoundId_num !== undefined)
		{
			for (let i = 0; i < this._fMultHistory_obj_arr.length; i++)
			{
				if (this._fMultHistory_obj_arr[i].roundId === aRoundId_num)
				{
					return i;
				}
			}
		}
	}

	getRoundIdById(aId_int)
	{
		if (this._fMultHistory_obj_arr && this._fMultHistory_obj_arr[aId_int])
		{
			return this._fMultHistory_obj_arr[aId_int].roundId;
		}
		return 0;
	}

	getMultiplierById(aId_int)
	{
		if (this._fMultHistory_obj_arr && this._fMultHistory_obj_arr[aId_int])
		{
			if (APP.isBattlegroundGame)
			{
				return 1 + (this._fMultHistory_obj_arr[aId_int].mult - 1) * this._fMultHistory_obj_arr[aId_int].kilometerMult;
			}
			return this._fMultHistory_obj_arr[aId_int].mult;
		}
		return 0;
	}

	getStartTimeById(aId_int)
	{
		if (this._fMultHistory_obj_arr && this._fMultHistory_obj_arr[aId_int])
		{
			return this._fMultHistory_obj_arr[aId_int].startTime;
		}
		return 0;
	}

	getBetsCountById(aId_int)
	{
		if (this._fMultHistory_obj_arr && this._fMultHistory_obj_arr[aId_int])
		{
			return this._fMultHistory_obj_arr[aId_int].bets;
		}
		return 0;
	}

	getUniqueTokenById(aId_int)
	{
		if (this._fMultHistory_obj_arr && this._fMultHistory_obj_arr[aId_int])
		{
			return this._fMultHistory_obj_arr[aId_int].token;
		}
		return 0;
	}

	get multHistorySize()
	{
		return this._fMultHistory_obj_arr ? this._fMultHistory_obj_arr.length : 0;
	}

	get isMultHistoryDefined()
	{
		return this._fMultHistory_obj_arr !== null;
	}

	destroy()
	{
		this._fMultHistory_obj_arr = null;
		this._fSnapshotTime_num = null;

		super.destroy();
	}
}
export default RoundsHistoryInfo;