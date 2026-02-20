import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import PlazmaGunsInfo from '../../../../model/uis/weapons/plazmagun/PlazmaGunsInfo';
import PlazmaGunsView from '../../../../view/uis/weapons/plazmagun/PlazmaGunsView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../../main/GameScreen';
import GameField from '../../../../main/GameField';
import { WEAPONS } from '../../../../../../shared/src/CommonConstants';
import ShotResultsUtil from '../../../../main/ShotResultsUtil';
import Weapon from '../../../../main/playerSpots/Weapon';

class PlazmaGunsController extends SimpleUIController {

	static get EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED() 	{ return 'EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED'; }
	static get EVENT_ON_BEAM_TARGET_PRE_ACHIEVED() 					{ return PlazmaGunsView.EVENT_ON_BEAM_TARGET_PRE_ACHIEVED; }
	
	constructor()
	{
		super(new PlazmaGunsInfo(), new PlazmaGunsView());

		this._gameScreen = null;
		this._fRelatedGun_w = null;
	}

	set relatedGun(gun)
	{
		this._fRelatedGun_w = gun;
	}

	get relatedGun()
	{
		return this._fRelatedGun_w;
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

	__initViewLevel()
	{
		super.__initViewLevel();
		this.view.on(PlazmaGunsView.EVENT_ON_BEAM_BASIC_ANIMATION_COMPLETED, this._onSomeBeamBasicAnimationCompleted, this);
		this.view.on(PlazmaGunsView.EVENT_ON_BEAM_TARGET_PRE_ACHIEVED, this.emit, this);
	}

	_onGameScreenReady(aEvent_obj)
	{
		this._gameScreen.gameField.on(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);
		this._gameScreen.gameField.on(GameField.EVENT_SHOW_FIRE, this._onTimeToShowFire, this);
	}

	_onGameFieldScreenCreated(aEvent_obj)
	{
		this.view.i_init();
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
		let targetEnemyId = aEvent_obj.targetEnemyId;

		if (data.usedSpecialWeapon === WEAPONS.INSTAKILL || data.usedSpecialWeapon === WEAPONS.INSTAKILL_POWER_UP)
		{
			this._showFire(data, targetEnemyId, callback);
		}
	}

	_showFire(data, targetEnemyId, callback)
	{
		let lStartPos_pt = this._gameScreen.gameField.getGunPosition(data.seatId);

		let lTargetEnemyId_int = targetEnemyId;
		let lEndPos_pt = this._gameScreen.gameField.getEnemyPosition(lTargetEnemyId_int);
		if (!lEndPos_pt)
		{
			callback(null, 0);
			return;
		}

		this.view.i_showFire(data, lTargetEnemyId_int, lStartPos_pt, lEndPos_pt, callback);
	}

	_onSomeBeamBasicAnimationCompleted(aEvent_obj)
	{
		this.relatedGun && this.relatedGun.shotCompleted();

		let lShotData_obj = aEvent_obj.shotData;
		if (ShotResultsUtil.isMasterShot(lShotData_obj))
		{
			this.emit(PlazmaGunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED);
		}
	}
	
	_clearAll()
	{
		this.info.i_clearAll();
		this.view.i_clearAll();	

		this._fRelatedGun_w = null;
	}
}

export default PlazmaGunsController;