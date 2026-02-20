import { APP } from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import SimpleUIController from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController";
import RoundController from "../RoundController";
import BetsController from "../bets/BetsController";
import RoomController from "../RoomController";
import CrashAPP from '../../../CrashAPP';

/**
 * Controls behavior of the top panel in battleground mode.
 * Top panel includes information about POT, seating fee, and amount of astronauts.
 * 
 * @class
 * @extends SimpleUIController
 * @inheritdoc
 */
class BattleGroundTopPanelController extends SimpleUIController
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

        let lBetsController_bsc = this._fBetsController_bsc = APP.gameController.gameplayController.gamePlayersController.betsController;
        lBetsController_bsc.on(BetsController.EVENT_ON_BETS_UPDATED, this._onBetsUpdated, this);
        lBetsController_bsc.on(BetsController.EVENT_ON_BETS_CLEARED, this._onBetsUpdated, this);
        lBetsController_bsc.on(BetsController.EVENT_ON_BET_CONFIRMED, this._onBetsUpdated, this);
        lBetsController_bsc.on(BetsController.EVENT_ON_BET_CANCELLED, this._onBetsUpdated, this);
        
        this._fRoundController_rc = APP.gameController.gameplayController.roundController;
        this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);

        this._fRoomController_rc = APP.gameController.gameplayController.roomController;
        this._fRoomController_rc.on(RoomController.EVENT_ON_RAKE_DEFINED, this._onRakeDefined, this);

        APP.once(CrashAPP.EVENT_ON_GAME_PRELOADER_REMOVED, this._onGamePreloaderRemoved, this);
    }

    __initViewLevel()
    {
        super.__initViewLevel();
    }
    //...INIT

    /**
     * Update indicators when bet of master player updates.
     * @param {*} event 
     * @private
     */
    _onBetsUpdated(event)
    {
        let lBetsListInfo_bif = APP.gameController.info.betsListInfo;
        let lTotalAstronautsValue_num = lBetsListInfo_bif.betsTotalSum;

        this.view.updateTotalAstronauts(lBetsListInfo_bif ? lBetsListInfo_bif.allBets.length : 0);
        this.view.updateTotalPot(lTotalAstronautsValue_num);
    }

    /**
     * Update indicators when rake updates.
     * @private
     */
    _onRakeDefined()
    {
        let lBetsListInfo_bif = APP.gameController.info.betsListInfo;
        let l_rci = APP.gameController.gameplayController.roomController.info;
        
        let lTotalAstronautsValue_num = lBetsListInfo_bif.betsTotalSum;
        let lRakePercent_num = l_rci.rakePercent;

        this.view.updateRake(lRakePercent_num);
        this.view.updateTotalPot(lTotalAstronautsValue_num);
    }

    /**
     * Validate panel view when round state changes.
     * The appearance of the panel and the set of indicators are different for different round states
     * @param {*} event 
     * @private
     */
    _onRoundStateChanged(event)
    {
        this.view.adjustLayoutSettings();
        this.view.validate();
    }

    /**
     * Set zero values to be displayed while corresponding parameters are not defined but game screen is already visible.
     * @param {*} event 
     * @private
     */
    _onGamePreloaderRemoved(event)
    {
        this.view.updateTotalPot(0);
        this.view.updateTotalAstronauts(0);
    }
}
export default BattleGroundTopPanelController;