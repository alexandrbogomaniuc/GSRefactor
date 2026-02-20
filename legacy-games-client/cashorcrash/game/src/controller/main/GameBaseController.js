import SimpleUIController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import CrashAPP from '../../CrashAPP';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DOMLayout from '../../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';

class GameBaseController extends SimpleUIController
{
	get balanceController()
	{
		return this._fBalanceController_bc;
	}

	get roundsHistoryController()
	{
		return this._fRoundsHistoryController_rshc;
	}

	get bottomPanelController()
	{
		return this._fBottomPanelController_bpc;
	}

	get gameplayController()
	{
		return this._fGameplayController_gpc;
	}

	get roundDetailsController()
	{
		return this._fRoundDetailsController_rdsc;
	}
	
	get betsListController()
	{
		return this._fBetsListController_bslc;
	}

	get placeBetsController()
	{
		return this._fPlaceBetsController_pbsc;
	}

	constructor(aOptModel_rcm, aOptView_rcv)
	{
		super(aOptModel_rcm, aOptView_rcv);	

		this._fBalanceController_bc = this.__generateBalanceController();
		this._fRoundsHistoryController_rshc = this.__generateRoundsHistoryController();
		this._fBottomPanelController_bpc = this.__generateBottomPanelController();
		this._fGameplayController_gpc = this.__generateGameplayController();
		this._fBetsListController_bslc = this.__generateBetsListController();
		this._fRoundDetailsController_rdsc = this.__generateRoundDetailsController();
		this._fPlaceBetsController_pbsc = this.__generatePlaceBetsController();
	}

	init()
	{
		super.init();

		this.view.hide();

		APP.layout.on(DOMLayout.EVENT_ON_ORIENTATION_CHANGED, this._onAppOrientationChange, this);
		APP.once(CrashAPP.EVENT_ON_GAME_PRELOADER_REMOVED, this._onGamePreloaderRemoved, this);

		this.balanceController.init();
		this.roundsHistoryController.init();
		this.bottomPanelController.init();
		this.gameplayController.init();
		this.roundDetailsController.init();
		this.betsListController.init();
		this.placeBetsController.init();
	}

	__generateBalanceController()
	{
		// should be overridden
		return null;
	}

	__generateRoundsHistoryController()
	{
		// should be overridden
		return null;
	}

	__generateBottomPanelController()
	{
		// should be overridden
		return null;
	}

	__generateGameplayController()
	{
		// should be overridden
		return null;
	}

	__generateBetsListController()
	{
		// should be overridden
		return null;
	}

	__generateRoundDetailsController()
	{
		// should be overridden
		return null;
	}

	__generatePlaceBetsController()
	{
		// should be overridden
		return null;
	}

	_onGamePreloaderRemoved(aEvent_evt)
	{
		this.view.show();
	}

	//ORIENTATION...
	_onAppOrientationChange(event)
	{
		this.view.updateArea();
	}
	//...ORIENTATION
}

export default GameBaseController;