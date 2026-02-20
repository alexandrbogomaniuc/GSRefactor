import SimpleController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import ArtilleryStrikesInfo from '../../../../model/uis/weapons/artillerystrike/ArtilleryStrikesInfo';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../../main/GameScreen';
import GameField from '../../../../main/GameField';
import { WEAPONS } from '../../../../../../shared/src/CommonConstants';
import ArtilleryStrikeController from './ArtilleryStrikeController';

class ArtilleryStrikesController extends SimpleController {

	static get EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED() 		{ return ArtilleryStrikeController.EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED; }
	static get EVENT_ON_STRIKE_MISSILE_HIT() 					{ return ArtilleryStrikeController.EVENT_ON_STRIKE_MISSILE_HIT; }
	static get EVENT_ON_ALL_ARTILLERY_STRIKES_COMPLETED() 		{ return 'EVENT_ON_ALL_ARTILLERY_STRIKES_COMPLETED'; }
	static get EVENT_ON_ALL_MAIN_MISSILES_HIT() 				{ return ArtilleryStrikeController.EVENT_ON_ALL_MAIN_MISSILES_HIT; }
	static get EVENT_ON_ARTILLERY_GRENADE_LANDED() 				{ return ArtilleryStrikeController.EVENT_ON_ARTILLERY_GRENADE_LANDED; }

	constructor()
	{
		super(new ArtilleryStrikesInfo());

		this._gameScreen = null;
		this._fAtrilleryStrikeControllers_asc_arr = [];
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;

		this._gameScreen.once(GameScreen.EVENT_ON_READY, this._onGameScreenReady, this);

		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);
	}

	_onGameScreenReady(aEvent_obj)
	{
		this._gameScreen.gameField.on(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);
		this._gameScreen.gameField.on(GameField.EVENT_SHOW_FIRE, this._onTimeToShowFire, this);
	}

	_onGameFieldScreenCreated(aEvent_obj)
	{

	}

	_onRoomPaused(aEvent_obj)
	{
		this._clearAll();
	}

	_onRoomUnpaused(aEvent_obj)
	{

	}

	_onRoomFieldCleared()
	{
		this._clearAll();
	}

	_onTimeToShowFire(aEvent_obj)
	{
		let data = aEvent_obj.data;
		let callback = aEvent_obj.callback;

		if (data.usedSpecialWeapon === WEAPONS.ARTILLERYSTRIKE || data.usedSpecialWeapon === WEAPONS.ARTILLERYSTRIKE_POWER_UP)
		{
			this._showFire(data, callback);
		}
	}

	_showFire(data, callback)
	{
		let lArtilleryStrikeController_asc = new ArtilleryStrikeController(data, callback);
		lArtilleryStrikeController_asc.once(ArtilleryStrikeController.EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED, this.emit, this);
		lArtilleryStrikeController_asc.once(ArtilleryStrikeController.EVENT_ON_ARTILLERY_GRENADE_LANDED, this.emit, this);
		lArtilleryStrikeController_asc.once(ArtilleryStrikeController.EVENT_ON_FULL_ANIMATION_COMPLETED, this._onArtilleryStrikeFullAnimationCompleted, this);
		lArtilleryStrikeController_asc.on(ArtilleryStrikeController.EVENT_ON_STRIKE_MISSILE_HIT, this.emit, this);
		lArtilleryStrikeController_asc.on(ArtilleryStrikeController.EVENT_ON_ALL_MAIN_MISSILES_HIT, this.emit, this);
		this._fAtrilleryStrikeControllers_asc_arr.push(lArtilleryStrikeController_asc);
		this.info.i_increaseActiveArtilleryStrikesCounter();
		lArtilleryStrikeController_asc.i_init();
	}

	_onArtilleryStrikeFullAnimationCompleted(aEvent_obj)
	{
		let lArtilleryStrikeController_asc = aEvent_obj.target;
		let lIndex_int = this._fAtrilleryStrikeControllers_asc_arr.indexOf(lArtilleryStrikeController_asc);
		if (~lIndex_int)
		{
			lArtilleryStrikeController_asc.destroy();
			this._fAtrilleryStrikeControllers_asc_arr.splice(lIndex_int, 1);
			this.info.i_decreaseActiveArtilleryStrikesCounter();
			this._onAllArtilleryStrikesCompletedSuspicion();
		}
		else
		{
			throw new Error('Can\'t find ArtilleryStrikeController in array');
		}
	}

	_onAllArtilleryStrikesCompletedSuspicion()
	{
		if (this.info.activeArtilleryStrikesCounter === 0)
		{
			this.emit(ArtilleryStrikesController.EVENT_ON_ALL_ARTILLERY_STRIKES_COMPLETED);
		}
	}

	_clearAll()
	{
		this.info.i_clearAll();

		while (this._fAtrilleryStrikeControllers_asc_arr.length > 0)
		{
			let lArtilleryStrikeController_asc = this._fAtrilleryStrikeControllers_asc_arr.pop();
			lArtilleryStrikeController_asc.destroy();
		}
		this._fAtrilleryStrikeControllers_asc_arr = [];
	}
}

export default ArtilleryStrikesController;