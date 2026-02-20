import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CrashAPP from '../../../../CrashAPP';
import BetsListBaseController from './BetsListBaseController';
import BetsController from '../../../gameplay/bets/BetsController'
import GamePlayersController from '../../../gameplay/players/GamePlayersController';
import RoundController from '../../../gameplay/RoundController';

class BetsListController extends BetsListBaseController
{
	init()
	{
		super.init();
	}

	//INIT...
	constructor(...args)
	{
		super(...args);

		this._fBetsController_bsc = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();
        
		APP.on(CrashAPP.EVENT_ON_CURRENCY_INFO_UPDATED, this._onCurrencyInfoUpdated, this);

		let lGamaplayController_gpc = APP.gameController.gameplayController;
		let lGamePlayersController_gpsc = lGamaplayController_gpc.gamePlayersController;
		lGamePlayersController_gpsc.on(GamePlayersController.EVENT_ON_MASTER_PLAYER_IN, this._onMasterPlayerIn, this);
		lGamePlayersController_gpsc.on(GamePlayersController.EVENT_ON_MASTER_PLAYER_OUT, this._onMasterPlayerOut, this);

		let lBetsController_bsc = this._fBetsController_bsc = lGamePlayersController_gpsc.betsController;
		lBetsController_bsc.on(BetsController.EVENT_ON_BETS_UPDATED, this._onAllBetsUpdated, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BETS_CLEARED, this._onBetsCleared, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_CONFIRMED, this._onBetUpdated, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_CANCELLED, this._onBetUpdated, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_OUTDATED_BET_REMOVED, this._onBetRemoved, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_NOT_CONFIRMED, this._onAllBetsUpdated, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_ALL_PLAYER_BETS_REJECTED_BY_SERVER, this._onAllBetsUpdated, this);

		this._fRoundController_rc = lGamaplayController_gpc.roundController;
		this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);
	}

    __initViewLevel()
    {
        super.__initViewLevel();
    }
	//...INIT

	_onAllBetsUpdated(event)
	{
		this.info.setBetsData(this._fBetsController_bsc.info.allActiveBets);
		this.view && this.view.updateBets();

		this._updateTotalmasterWinIfRequired();
	}

	_onBetUpdated(event)
	{
		this.info.updateBetsData(event.betInfo);
		this.view && this.view.updateBets();

		this._updateTotalmasterWinIfRequired();
	}

	_onBetRemoved(event)
	{
		this.info.removeBetData(event.betInfo);
		this.view && this.view.updateBets();

		this._updateTotalmasterWinIfRequired();
	}

	_onBetsCleared(event)
	{
		this.info.clearBetsData();
		this.view && this.view.updateBets();

		this._updateTotalmasterWinIfRequired();
	}

	_onRoundStateChanged(event)
	{
		this._updateTotalmasterWinIfRequired();
	}

	_updateTotalmasterWinIfRequired()
	{
		let l_ri = this._fRoundController_rc.info;
		if (l_ri.isRoundPlayState || l_ri.isRoundWaitState)
		{
			this.info.updateTotalMasterWin();
		}

		this.view && this.view.updateTotalWinIndicator();
	}

	_onCurrencyInfoUpdated(event)
	{
		this.view && this.view.updateBets();

		this._updateTotalmasterWinIfRequired();
	}

	_onMasterPlayerIn(event)
	{
		this.info.setBetsData(this._fBetsController_bsc.info.allActiveBets);
		this.view && this.view.updateBets();

		this._updateTotalmasterWinIfRequired();
	}

	_onMasterPlayerOut(event)
	{
		let l_ri = this._fRoundController_rc.info;

		this.view && this.view.updateBets();

		if (!l_ri.isRoundPlayState)
		{
			this._updateTotalmasterWinIfRequired();
		}
	}
}

export default BetsListController