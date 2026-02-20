import BonusInfo from './../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/bonus/BonusInfo';

class GameFRBInfo extends BonusInfo
{
	constructor()
	{
		super();

		this._fIsFrbActive_bln = false;
		this._fFrbEnded_bln = false;
		this._fFrbEndReason_str = null;
		this._fNextModeFrb_bln = false;
		this._fIsRoomRestartRequired_bl = false;
		this._fBonusCompletionData_obj = null;
		this._fRealWinSum_num = null;
		this._fContinousNextModeFrb_bln = false;
		this._fIsFrbEndedAndConnectionClosed_bln = false;
		this._fIsWinLimitExceeded_bl = false;
	}

	i_clearAll()
	{
		super.i_clearAll();

		this._fIsFrbActive_bln = false;
		this._fFrbEnded_bln = false;
		this._fFrbEndReason_str = null;
		this._fNextModeFrb_bln = false;
		this._fIsRoomRestartRequired_bl = false;
		this._fBonusCompletionData_obj = null;
		this._fRealWinSum_num = null;
		this._fIsWinLimitExceeded_bl = false;
	}

	set realWinSum(aVal_num)
	{
		this._fRealWinSum_num = aVal_num;
	}

	get realWinSum()
	{
		return this._fRealWinSum_num;
	}

	get frbMode()
	{
		return this._fIsFrbActive_bln;
	}

	set frbMode(aVal_bln)
	{
		this._fIsFrbActive_bln = aVal_bln;
	}

	set frbEnded(aVal_bln)
	{
		this._fFrbEnded_bln = aVal_bln;
	}

	get frbEnded()
	{
		return this._fFrbEnded_bln;
	}

	set frbEndReason(aVal_str)
	{
		this._fFrbEndReason_str = aVal_str;
	}

	get frbEndReason()
	{
		return this._fFrbEndReason_str;
	}

	get isFrbCompleted()
	{
		return this.frbEndReason == BonusInfo.STATUS_RELEASED;
	}

	get isFrbCancelled()
	{
		return this.frbEndReason == BonusInfo.STATUS_CANCELLED;
	}

	get isFrbExpired()
	{
		return this.frbEndReason == BonusInfo.STATUS_EXPIRED;
	}

	get nextModeFrb()
	{
		return this._fNextModeFrb_bln;
	}

	set nextModeFrb(aVal_bln)
	{
		this._fNextModeFrb_bln = aVal_bln;
	}

	get continousNextModeFrb()
	{
		return this._fContinousNextModeFrb_bln;
	}

	set continousNextModeFrb(aVal_bln)
	{
		this._fContinousNextModeFrb_bln = aVal_bln;
	}

	get winSum()
	{
		return this._fWinSum_num;
	}

	set winSum(aVal_num)
	{
		this._fWinSum_num = aVal_num;
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

	set isFrbEndedAndConnectionLost(aVal_num)
	{
		this._fIsFrbEndedAndConnectionClosed_bln = aVal_num;
	}

	get isFrbEndedAndConnectionLost()
	{
		return this._fIsFrbEndedAndConnectionClosed_bln;
	}

	set isWinLimitExceeded(value)
	{
		this._fIsWinLimitExceeded_bl = value;
	}

	get isWinLimitExceeded()
	{
		return this._fIsWinLimitExceeded_bl;
	}
}

export default GameFRBInfo;