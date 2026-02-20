import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import PrizesController from './PrizesController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../main/GameScreen';
import SpecialWeaponPrizeController from './SpecialWeaponPrizeController';

class SpecialWeaponPrizesController extends SimpleController {

	static get EVENT_ON_SPECIAL_WEAPON_PRIZE_LANDED() { return 'EVENT_ON_SPECIAL_WEAPON_PRIZE_LANDED'; }
	
	constructor()
	{
		super();

		this._fUniqueCounter_int = 0;
		this._fSpecialWeaponPrizeControllers_swpc_arr = [];
		this._fPrizesController_psc = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._gameScreen = APP.gameScreen;
		this._gameScreen.on(GameScreen.EVENT_ON_CLOSE_ROOM, this._onCloseRoom, this);
		this._gameScreen.on(GameScreen.EVENT_ON_GAME_FIELD_CLEARED, this._onGameFieldCleared, this);

		this._fPrizesController_psc = this._gameScreen.prizesController;
		this._fPrizesController_psc.on(PrizesController.i_EVENT_ON_TIME_TO_SHOW_SPECIAL_WEAPON_PRIZE, this._onTimeToShowSpecialWeaponPrize, this);
	}

	_onCloseRoom()
	{
		this._clearAllPrizes();
	}

	_onGameFieldCleared()
	{
		this._clearAllPrizes();
	}

	_clearAllPrizes()
	{
		for (let lSpecialWeaponPrizeController_swpc of this._fSpecialWeaponPrizeControllers_swpc_arr)
		{
			lSpecialWeaponPrizeController_swpc.destroy();
		}

		this._fSpecialWeaponPrizeControllers_swpc_arr = [];
	}

	_onTimeToShowSpecialWeaponPrize(event)
	{
		let lSpecialWeaponId_int = event.specialWeaponId;
		let lSeatId_int = event.seatId;
		let lStartPosition_pt = event.startPosition;

		this._handleSpecialWeaponPrize(lSpecialWeaponId_int, lSeatId_int, lStartPosition_pt);
	}

	_handleSpecialWeaponPrize(aSpecialWeaponId_int, aSeatId_int, aStartPosition_pt)
	{
		let lSpecialWeaponPrizeController_swpc = new SpecialWeaponPrizeController(this._fUniqueCounter_int++, aSpecialWeaponId_int, aSeatId_int, aStartPosition_pt);
		lSpecialWeaponPrizeController_swpc.i_init();
		lSpecialWeaponPrizeController_swpc.on(SpecialWeaponPrizeController.i_EVENT_ON_ANIMATION_COMPLETED, this._onSpecialWeaponPrizeHandled, this);
		lSpecialWeaponPrizeController_swpc.on(SpecialWeaponPrizeController.i_EVENT_ON_ANIMATION_INTERRUPTED, this._onSpecialWeaponPrizeHandled, this);
		this._fSpecialWeaponPrizeControllers_swpc_arr.push(lSpecialWeaponPrizeController_swpc);
		lSpecialWeaponPrizeController_swpc.i_startAnimation();
	}

	_onSpecialWeaponPrizeHandled(event)
	{
		let lUniqueId_int = event.uniqueId;
		for (let i=0; i<this._fSpecialWeaponPrizeControllers_swpc_arr.length; i++)
		{
			let lSpecialWeaponPrizeController_swpc = this._fSpecialWeaponPrizeControllers_swpc_arr[i];
			if (lSpecialWeaponPrizeController_swpc.info.uniqueId === lUniqueId_int)
			{
				let lSeatId_int = lSpecialWeaponPrizeController_swpc.info.seatId;
				this.emit(SpecialWeaponPrizesController.EVENT_ON_SPECIAL_WEAPON_PRIZE_LANDED, {seatId: lSeatId_int});
				this._fSpecialWeaponPrizeControllers_swpc_arr.splice(i, 1);
				lSpecialWeaponPrizeController_swpc.destroy();
				break;
			}
		}
	}


}

export default SpecialWeaponPrizesController;