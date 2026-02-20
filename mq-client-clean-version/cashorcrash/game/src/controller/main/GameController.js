import GameBaseController from './GameBaseController';
import GameModel from '../../model/main/GameModel';
import GameView from '../../view/main/GameView';
import BalanceController from './BalanceController';
import RoundsHistoryController from '../gameplay/RoundsHistoryController';
import BottomPanelController from '../uis/custom/bottom_panel/BottomPanelController';
import GameplayController from '../gameplay/GameplayController';
import RoundDetailsController from '../uis/custom/RoundDetailsController';
import BetsListController from '../uis/custom/betslist/BetsListController';
import PlaceBetsController from '../uis/custom/betslist/PlaceBetsController';


class GameController extends GameBaseController
{
	constructor(aOptModel_rcm, aOptView_rcv)
	{
		super(aOptModel_rcm || new GameModel(), aOptView_rcv || new GameView());
	}

	init()
	{
		super.init();
	}

	__generateBalanceController()
	{
		return new BalanceController();
	}

	__generateRoundsHistoryController()
	{
		return new RoundsHistoryController();
	}

	__generateBottomPanelController()
	{
		return new BottomPanelController();
	}

	__generateGameplayController()
	{
		return new GameplayController(this.info.gameplayInfo, this.view.gameplayView, this);
	}

	__generateBetsListController()
	{
		return new BetsListController(this.info.betsListInfo, this.view.betsListView);
	}

	__generateRoundDetailsController()
	{
		return new RoundDetailsController(this.info.roundDetailsInfo, this.view.roundDetailsView);
	}

	__generatePlaceBetsController()
	{
		return new PlaceBetsController(this.info.placeBetsInfo, this.view.placeBetsView);
	}
}

export default GameController;