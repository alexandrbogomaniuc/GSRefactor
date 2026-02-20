import GameBaseModel from './GameBaseModel';
import GameplayInfo from '../gameplay/GameplayInfo';
import BattlegroundBetsListInfo from '../uis/custom/betslist/BattlegroundBetsListInfo';
import BattlegroundRoundDetailsInfo from '../uis/custom/battleground/BattlegroundRoundDetailsInfo';
import BattlegroundPlaceBetsInfo from '../uis/custom/placebets/BattlegroundPlaceBetsInfo';

class BattlegroundGameModel extends GameBaseModel
{
	static get DEFAULT_BET_INDEX () { return 0};

	constructor()
	{
		super();

		this._fBattlegroundBetValue_num = null;
		this._fIsBattlegroundBuyInConfirmed_bl = null;
		this._fPreviousGameState_num = null;
	}

	get previousGameState()
	{
		return this._fPreviousGameState_num;
	}

	set previousGameState(aValue_num)
	{
		this._fPreviousGameState_num = aValue_num;
	}

	get battlegroundBetValue()
	{
		return this._fBattlegroundBetValue_num;
	}

	set battlegroundBetValue(aValue_num)
	{
		this._fBattlegroundBetValue_num = aValue_num;
	}

	get battlegroundBuyInConfirmed()
	{
		return this._fIsBattlegroundBuyInConfirmed_bl;
	}

	set battlegroundBuyInConfirmed(aValue_bl)
	{
		this._fIsBattlegroundBuyInConfirmed_bl = aValue_bl;
	}

	destroy()
	{
		super.destroy();

		this._fBattlegroundBetValue_num = null;
		this._fIsBattlegroundBuyInConfirmed_bl = null;
		this._fPreviousGameState_num = null;
	}

	__generateGameplayInfo()
	{
		let lGameplayInfo_gpi = new GameplayInfo();
		return lGameplayInfo_gpi;
	}

	__generateRoundDetailsInfo()
	{
		let lRoundDetailsInfo_rdsi = new BattlegroundRoundDetailsInfo();
		return lRoundDetailsInfo_rdsi;
	}

	__generateBetsListInfo()
	{
		let lBetsListInfo_bsli = new BattlegroundBetsListInfo();
		return lBetsListInfo_bsli;
	}

	__generatePlaceBetsInfo()
	{
		let lPlaceBetsInfo_pbsi = new BattlegroundPlaceBetsInfo();
		return lPlaceBetsInfo_pbsi;
	}
}
export default BattlegroundGameModel;