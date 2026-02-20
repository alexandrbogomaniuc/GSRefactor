import BonusInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/bonus/BonusInfo';


class GameBonusInfo extends BonusInfo {

	constructor()
	{
		super();

		this._fIsRoomRestartRequired_bl = false;
		this._fBonusCompletionData_obj = null;
		this._fRealWinSum_num = null;
	}

	i_clearAll()
	{
		super.i_clearAll();

		this._fIsRoomRestartRequired_bl = false;
		this._fBonusCompletionData_obj = null;
		this._fRealWinSum_num = null;
	}

	set realWinSum(aVal_num)
	{
		this._fRealWinSum_num = aVal_num;
	}

	get realWinSum()
	{
		return this._fRealWinSum_num;
	}

	get isWinLimitExceeded()
	{
		return this.winSum > this.realWinSum;
	}

	set isRoomRestartRequired(aValue_bl)
	{
		this._fIsRoomRestartRequired_bl = aValue_bl;
	}

	get isRoomRestartRequired()
	{
		return this._fIsRoomRestartRequired_bl;
	}

	set bonusCompletionData(aValue_obj)
	{
		this._fBonusCompletionData_obj = aValue_obj;
	}

	get bonusCompletionData()
	{
		return this._fBonusCompletionData_obj;
	}

	get keepBonusSW()
	{
		return false;
	}
}

export default GameBonusInfo;