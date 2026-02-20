import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class BattlegroundPlaceBetsInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._fBetIndex_num = null;
		this._fBetValue_num = null;
		this._fBetAutoEjectMult_num = null;
	}

	get betIndex()
	{
		return this._fBetIndex_num;
	}

	set betIndex(aValue_num)
	{
		this._fBetIndex_num = aValue_num;
	}

	get betValue()
	{
		return this._fBetValue_num;
	}

	set betValue(aValue_num)
	{
		this._fBetValue_num = aValue_num;
	}

	get betAutoEjectMult()
	{
		return this._fBetAutoEjectMult_num;
	}

	set betAutoEjectMult(aValue_num)
	{
		this._fBetAutoEjectMult_num = aValue_num;
	}


	destroy()
	{
		super.destroy();
	}
}

export default BattlegroundPlaceBetsInfo