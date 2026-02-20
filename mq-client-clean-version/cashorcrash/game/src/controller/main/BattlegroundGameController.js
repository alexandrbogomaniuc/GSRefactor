import GameBaseController from './GameBaseController';
import BattlegroundGameModel from '../../model/main/BattlegroundGameModel';
import BattlegroundGameView from '../../view/main/BattlegroundGameView';
import BalanceController from './BalanceController';
import RoundsHistoryController from '../gameplay/RoundsHistoryController';
import BottomPanelController from '../uis/custom/bottom_panel/BottomPanelController';
import GameplayController from '../gameplay/GameplayController';
import BattlegroundRoundDetailsController from '../uis/custom/battleground/BattlegroundRoundDetailsController';
import BattlegroundBetsListController from '../uis/custom/betslist/BattlegroundBetsListController';
import BattlegroundPlaceBetsController from '../uis/custom/betslist/BattlegroundPlaceBetsController';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { SERVER_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';
import BattleGroundTopPanelController from '../gameplay/battleground_top_panel/BattleGroundTopPanelController';
import RoundController from '../gameplay/RoundController';
import BetInfo from '../../model/gameplay/bets/BetInfo';
import BetsController from '../gameplay/bets/BetsController';
import CrashAPP from '../../CrashAPP';
import { ROUND_STATES } from '../../model/gameplay/RoundInfo';


class BattlegroundGameController extends GameBaseController
{
	static get EVENT_ON_NEED_NOT_ENOUGH_PLAYERS_DIALOG () { return "EVENT_ON_NEED_NOT_ENOUGH_PLAYERS_DIALOG"};
	static get EVENT_ON_BATTLEGROUND_BUY_IN_DEFINED () { return "EVENT_ON_BATTLEGROUND_BUY_IN_DEFINED"};

	get topPanelController()
	{
		return this._fTopPanelController_tpc;
	}

	constructor(aOptModel_rcm, aOptView_rcv)
	{
		super(aOptModel_rcm || new BattlegroundGameModel(), aOptView_rcv || new BattlegroundGameView());

		this._fTopPanelController_tpc = this._generateTopPanelController();

		this._fRoundInfo_ri = null;
		this._fBetsController_bsc = null;
	}

	init()
	{
		super.init();

		let wsInteractionController = this._wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);

		this.topPanelController.init();

		let lRoundController_rc = this.gameplayController.roundController;
		this._fRoundInfo_ri = lRoundController_rc.info;
		lRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);

		let lBetsController_bsc = this._fBetsController_bsc = this.gameplayController.gamePlayersController.betsController;	
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_CONFIRMED, this._onBetConfirmedByTheServer, this);
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.CRASH_GAME_INFO:
				// APP.appParamsInfo.battlegroundBuyIn should be used only in ENTER request, in other cases in the game we should use 
				// buyIn value from CrashGameInfo message (https://jira.dgphoenix.com/browse/CRG-523)
				this.info.battlegroundBetValue = +data.buyIn || +APP.appParamsInfo.battlegroundBuyIn;
				this.emit(BattlegroundGameController.EVENT_ON_BATTLEGROUND_BUY_IN_DEFINED)
				break;
			case SERVER_MESSAGES.CRASH_BET_RESPONSE:
				if (data.rid !== -1)
				{
					this.info.battlegroundBuyInConfirmed = true;
				}
				if(!this._fRoundInfo_ri.isRoundWaitState)
				{
					APP.forcedState = "WAIT";
				}
				break;
			case SERVER_MESSAGES.CRASH_CANCEL_BET_RESPONSE:
				if (data.rid !== -1)
				{
					this.info.battlegroundBuyInConfirmed = false;
				}
				break;
			case SERVER_MESSAGES.GAME_STATE_CHANGED:
				
				if (data.state == ROUND_STATES.WAIT && this.info.battlegroundBuyInConfirmed)
				{
					this.info.battlegroundBuyInConfirmed = false;

					if (this.info.previousGameState == ROUND_STATES.WAIT)
					{
						this.emit(BattlegroundGameController.EVENT_ON_NEED_NOT_ENOUGH_PLAYERS_DIALOG);
					}
				}
				break;
		}
	}

	_onRoundStateChanged()
	{
		if (APP.isBattlegroundGame)
		{
			if (this._fRoundInfo_ri.isRoundWaitState)
			{
				if (this.info.battlegroundBuyInConfirmed)
				{
					this.info.battlegroundBuyInConfirmed = false;
				}
			}
		}

		this.info.previousGameState = this._fRoundInfo_ri.roundState;
	}

	_onBetConfirmedByTheServer(e)
	{
		if (e.isMasterPlayerIn)
		{
			this.info.battlegroundBuyInConfirmed = true;
		}
	}

	_generateTopPanelController()
	{
		return new BattleGroundTopPanelController(undefined, this.view.topPanelView);
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
		return new BattlegroundBetsListController(this.info.betsListInfo, this.view.betsListView);
	}

	__generateRoundDetailsController()
	{
		return new BattlegroundRoundDetailsController(this.info.roundDetailsInfo, this.view.roundDetailsView);
	}

	__generatePlaceBetsController()
	{
		return new BattlegroundPlaceBetsController(this.info.placeBetsInfo, this.view.placeBetsView);
	}
}

export default BattlegroundGameController;