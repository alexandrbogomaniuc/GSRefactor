import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

const BIG_WIN_TYPE = 0;
const HUGE_WIN_TYPE = 1;
const MEGA_WIN_TYPE = 2;

const BIG_WIN_TYPES = [BIG_WIN_TYPE, HUGE_WIN_TYPE, MEGA_WIN_TYPE];

export const BIG_WIN_PAYOUT_RATIOS = {
	BIG: 75,
	HUGE: 150,
	MEGA: 300
};

class BigWinInfo extends SimpleUIInfo {

	i_clear()
	{
		this._clear();
	}

	constructor(aTotalWin_num, aShotStake_num, aOptPowerUpMult_int=1)
	{
		super();
		this._fTotalWin_num = this._fNotLandedWin_num = this._fUncountedWin_num = aTotalWin_num;
		this._fShotStake_num = aShotStake_num;
		this._fPowerUpMult_int = aOptPowerUpMult_int;

		this._fRatio_num = null;
		this._fBigWinType_int = null;

		let lRatioBase = this.ratio * this._fPowerUpMult_int;

		if (lRatioBase >= BIG_WIN_PAYOUT_RATIOS.MEGA)
		{
			this.bigWinType = MEGA_WIN_TYPE;
		}
		else if (lRatioBase >= BIG_WIN_PAYOUT_RATIOS.HUGE)
		{
			this.bigWinType = HUGE_WIN_TYPE;
		}
		else if (lRatioBase >= BIG_WIN_PAYOUT_RATIOS.BIG)
		{
			this.bigWinType = BIG_WIN_TYPE;
		}
	}

	get totalWin()
	{
		return this._fTotalWin_num;
	}

	get shotStake()
	{
		return this._fShotStake_num;
	}

	get ratio()
	{
		if (this._fRatio_num == null)
		{
			this._fRatio_num = this._fTotalWin_num / this._fShotStake_num;

			if (this._fPowerUpMult_int)
			{
				this._fRatio_num /= this._fPowerUpMult_int;
			}
		}
		return this._fRatio_num;
	}

	get bigWinType()
	{
		return this._fBigWinType_int;
	}

	set bigWinType(aValue_int)
	{
		if (!~BIG_WIN_TYPES.indexOf(aValue_int))
		{
			throw new Error ("Wrong big win type " + aValue_int);
		}
		this._fBigWinType_int = aValue_int;
	}

	get isMegaWin()
	{
		return this.bigWinType === MEGA_WIN_TYPE;
	}

	get isHugeWin()
	{
		return this.bigWinType === HUGE_WIN_TYPE;
	}

	get isBigWin()
	{
		return this.bigWinType === BIG_WIN_TYPE;
	}

	get notLandedWin()
	{
		return this._fNotLandedWin_num;
	}

	set notLandedWin(aValue_num)
	{
		if (aValue_num > this.totalWin || aValue_num < 0 || isNaN(aValue_num))
		{
			throw new Error ("Trying to set wrong notLandedWin value " + aValue_num, "while totalWin value is " + this.totalWin);
		}
		this._fNotLandedWin_num = aValue_num;
	}

	get uncountedWin()
	{
		return this._fUncountedWin_num;
	}

	set uncountedWin(aValue_num)
	{
		if (aValue_num > this.totalWin || aValue_num < 0 || isNaN(aValue_num))
		{
			throw new Error ("Trying to set wrong uncountedWin value " + aValue_num, "while totalWin value is " + this.totalWin);
		}
		this._fUncountedWin_num = aValue_num;
	}

	_clear()
	{
		this._fTotalWin_num = this._fNotLandedWin_num = this._fUncountedWin_num =  null;
		this._fRatio_num = null;
		this._fBigWinType_int = null;
		this._fPowerUpMult_int = null;
	}

}

export default BigWinInfo;