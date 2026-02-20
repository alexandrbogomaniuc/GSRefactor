import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CrashAPP from '../../../../CrashAPP';
import BetsListBaseController from './BetsListBaseController';
import BetsController from '../../../gameplay/bets/BetsController';
import GamePlayersController from '../../../gameplay/players/GamePlayersController';
import RoundController from '../../../gameplay/RoundController';
import GameplayController from '../../../gameplay/GameplayController';
import GameplayInfo from '../../../../model/gameplay/GameplayInfo';

class BattlegroundBetsListController extends BetsListBaseController
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

		let lGamePlayController_gpc = APP.gameController.gameplayController;
		
		let lGamePlayersController_gpsc = lGamePlayController_gpc.gamePlayersController;
		let lBetsController_bsc = this._fBetsController_bsc = lGamePlayersController_gpsc.betsController;
		lBetsController_bsc.on(BetsController.EVENT_ON_BETS_UPDATED, this._onAllBetsUpdated, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BETS_CLEARED, this._onBetsCleared, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_CONFIRMED, this._onBetUpdated, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_CANCELLED, this._onBetUpdated, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_OUTDATED_BET_REMOVED, this._onBetRemoved, this);
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_NOT_CONFIRMED, this._onAllBetsUpdated, this);

		this._fRoundController_rc = lGamePlayController_gpc.roundController;
		this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);

		APP.on(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);
	}

    __initViewLevel()
    {
        super.__initViewLevel();
    }
	//...INIT

	_onTickTime(event)
	{
		if (this.view)
		{
			if (this.info.isBetsListModified)
			{
				this._updateBetsListModifiedState(false);
				this.view.updateBets();
			}

			if (this.info.isBetsListAdjustLayoutRequired)
			{
				this._updateBetsListPendingAdjustLayoutState(false);
				this.view.adjustLayoutSettings();
			}

			this.view.validateVisibleArea();
		}
	}

	_onAllBetsUpdated(event)
	{
		this.info.setBetsData(this._fBetsController_bsc.info.allActiveBets);

		this._updateBetsListModifiedState(true);
	}

	_onBetUpdated(event)
	{
		this.info.updateBetsData(event.betInfo);

		this._updateBetsListModifiedState(true);
	}

	_onBetRemoved(event)
	{
		this.info.removeBetData(event.betInfo);

		this._updateBetsListModifiedState(true);
	}

	_onBetsCleared(event)
	{
		this.info.clearBetsData();

		this._updateBetsListModifiedState(true);
	}

	_onRoundStateChanged(event)
	{
		this._updateBetsListPendingAdjustLayoutState(true);
	}

	_onCurrencyInfoUpdated(event)
	{
		this._updateBetsListModifiedState(true);
	}

	_updateBetsListModifiedState(aBetsListModified_bl)
	{
		if (this.info.isBetsListModified === aBetsListModified_bl)
		{
			return;
		}

		this.info.isBetsListModified = aBetsListModified_bl;

		this._validateViewLockState();
	}

	_updateBetsListPendingAdjustLayoutState(aIsBetsListAdjustLayoutRequired_bl)
	{
		if (this.info.isBetsListAdjustLayoutRequired === aIsBetsListAdjustLayoutRequired_bl)
		{
			return;
		}

		this.info.isBetsListAdjustLayoutRequired = aIsBetsListAdjustLayoutRequired_bl;

		this._validateViewLockState();
	}

	_validateViewLockState()
	{
		if (!this.view)
		{
			return;
		}
		
		if (this.info.isBetsListAdjustLayoutRequired || this.info.isBetsListModified)
		{
			this.view.lockList();
		}
		else
		{
			this.view.unlockList();
		}
	}
}

export default BattlegroundBetsListController