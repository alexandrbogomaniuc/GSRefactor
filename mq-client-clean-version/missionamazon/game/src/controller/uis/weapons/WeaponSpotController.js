import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import PlayerSpot from '../../../main/playerSpots/PlayerSpot';
import WeaponSpotView from '../../../view/uis/weapons/WeaponSpotView';
import WeaponSpotInfo from '../../../model/uis/weapons/WeaponSpotInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

/*This controller is responsible for all animations of Weapon Spot*/
class WeaponSpotController extends SimpleUIController {
	constructor(aPlayerSpot_ps, aWeaponSpotContainer_sprt, aPlayer_obj)
	{
		super(new WeaponSpotInfo(), new WeaponSpotView(aWeaponSpotContainer_sprt, aPlayerSpot_ps.isBottom, aPlayer_obj.seatId));
		this._fPlayerSpot_ps = aPlayerSpot_ps;
	}

	get _isBattlegroundMode()
	{
		return APP.isBattlegroundGamePlayMode
	}

	__init()
	{
		super.__init();

		let lInfo_wsi = this.i_getInfo();
		lInfo_wsi.isMaster = this._fPlayerSpot_ps.isMaster;
		lInfo_wsi.seatId = this._fPlayerSpot_ps.isMaster;
		this._fPlayerSpot_ps.on(PlayerSpot.EVENT_CHANGE_WEAPON, this._onChangeWeapon, this);
	}

	_onChangeWeapon(aEvent_obj)
	{
		let lInfo_wsi = this.i_getInfo();

		let lPreviousWeaponId_int = lInfo_wsi.currentWeaponId;
		let lWeaponId_int = aEvent_obj.weaponId;
		let lDefaultWeaponId_int = aEvent_obj.defaultWeaponId;
		let lIsSkipAnimation_bl = aEvent_obj.isSkipAnimation_bl;

		if (lInfo_wsi.currentWeaponId === lWeaponId_int && lInfo_wsi.currentDefaultWeaponId === lDefaultWeaponId_int)
		{
			return;
		}

		lInfo_wsi.currentWeaponId = lWeaponId_int;
		lInfo_wsi.currentDefaultWeaponId = lDefaultWeaponId_int;

		this.view.i_updateWeapon(lIsSkipAnimation_bl);
	}

	destroy()
	{
		if (this._fPlayerSpot_ps)
		{
			this._fPlayerSpot_ps.off(PlayerSpot.EVENT_CHANGE_WEAPON, this._onChangeWeapon, this);
			this._fPlayerSpot_ps = null;
		}

		super.destroy();
	}
}

export default WeaponSpotController;