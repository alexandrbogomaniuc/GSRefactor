import GameBaseModel from './GameBaseModel';
import GameplayInfo from '../gameplay/GameplayInfo';
import BetsListInfo from '../uis/custom/betslist/BetsListInfo';
import RoundDetailsInfo from '../uis/custom/RoundDetailsInfo';
import PlaceBetsInfo from '../uis/custom/placebets/PlaceBetsInfo';

class GameModel extends GameBaseModel
{
	constructor()
	{
		super();
	}

	destroy()
	{
		super.destroy();
	}

	__generateGameplayInfo()
	{
		let lGameplayInfo_gpi = new GameplayInfo();
		return lGameplayInfo_gpi;
	}

	__generateRoundDetailsInfo()
	{
		let lRoundDetailsInfo_rdsi = new RoundDetailsInfo();
		return lRoundDetailsInfo_rdsi;
	}

	__generateBetsListInfo()
	{
		let lBetsListInfo_bsli = new BetsListInfo();
		return lBetsListInfo_bsli;
	}

	__generatePlaceBetsInfo()
	{
		let lPlaceBetsInfo_pbsi = new PlaceBetsInfo();
		return lPlaceBetsInfo_pbsi;
	}
}
export default GameModel;