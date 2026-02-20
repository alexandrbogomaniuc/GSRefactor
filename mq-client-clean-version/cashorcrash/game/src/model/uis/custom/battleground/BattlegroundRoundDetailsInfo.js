import RoundDetailsInfo from '../RoundDetailsInfo';

class BattlegroundRoundDetailsInfo extends RoundDetailsInfo
{
	constructor()
	{
		super();

		this._init();
	}

	set totalPot(aValue_str)
	{
		this._fTotalPot_str = aValue_str;
	}

	get totalPot()
	{
		return this._fTotalPot_str;
	}

	set winnerPot(aValue_num)
	{
		this._fWinnerPot_str = aValue_num;
	}

	get winnerPot()
	{
		return this._fWinnerPot_str;
	}

	set currentPlayerPlace(aValue_num)
	{
		this._fCurrentPlayerPlace_num = aValue_num;
	}

	get currentPlayerPlace()
	{
		return this._fCurrentPlayerPlace_num;
	}

	set rakePercent(aValue_num)
	{
		this._fRakePercent_num = aValue_num;
	}

	get rakePercent()
	{
		return this._fRakePercent_num;
	}

	_init ()
	{
		this._fTotalPot_str = null;
		this._fWinnerPot_str = null;
		this._fCurrentPlayerPlace_num = null;
		this._fMultiplier_num = null;
		this._fUniqueToken_str = null;
		this._fCurrentRoundId_num = null;
		this._fRakePercent_num = null;
	}

	destroy()
	{
		super.destroy();
		
		this._fTotalPot_str = null;
		this._fWinnerPot_str = null;
		this._fCurrentPlayerPlace_num = null;
		this._fMultiplier_num = null;
		this._fUniqueToken_str = null;
		this._fCurrentRoundId_num = null;
		this._fRakePercent_num = null;
	}
}

export default BattlegroundRoundDetailsInfo