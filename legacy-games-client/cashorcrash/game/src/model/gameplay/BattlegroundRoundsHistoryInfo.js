import RoundsHistoryInfo from './RoundsHistoryInfo';

class BattlegroundRoundsHistoryInfo extends RoundsHistoryInfo
{
	constructor()
	{
		super();

		
	}

	getWinnersById(aId_int)
	{
		if (this._fMultHistory_obj_arr && this._fMultHistory_obj_arr[aId_int])
		{
			return this._fMultHistory_obj_arr[aId_int].winners;
		}
		return 0;
	}

	destroy()
	{
		super.destroy();
	}
}
export default BattlegroundRoundsHistoryInfo;