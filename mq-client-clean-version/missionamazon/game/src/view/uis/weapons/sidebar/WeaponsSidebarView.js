import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { WEAPONS } from '../../../../../../shared/src/CommonConstants';
import WeaponSidebarIcon from './WeaponSidebarIcon';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const SPECIAL_WEAPONS = [
						WEAPONS.ARTILLERYSTRIKE,
						WEAPONS.CRYOGUN,
						WEAPONS.FLAMETHROWER,
						WEAPONS.RICOCHET,
						WEAPONS.INSTAKILL,
						];

const VERTICAL_DELTA = 65;

class WeaponsSidebarView extends SimpleUIView {

	static get EVENT_WEAPON_SIDEBAR_ICON_CLICKED() { return WeaponSidebarIcon.EVENT_WEAPON_SIDEBAR_ICON_CLICKED; }
	static get EVENT_WEAPON_SIDEBAR_ICON_DEFAULT_DISAPPEARED () { return WeaponSidebarIcon.EVENT_WEAPON_SIDEBAR_ICON_DEFAULT_DISAPPEARED; }

	set enabled(aValue_bl)
	{
		if (this._fIsEnabled_bl !== aValue_bl)
		{
			this._fIsEnabled_bl = aValue_bl
			this._invalidateAll();
		}
	}

	get enabled()
	{
		return this._fIsEnabled_bl;
	}

	set isAnimating(aValue_bl)
	{
		this._fIsAnimating_bl = aValue_bl;
		this._updateIsAnimatingParam();
	}

	get isAnimating()
	{
		return this._fIsAnimating_bl;
	}

	set delaySWSwitchAnimation(aValue_bl)
	{
		this._fDelaySWSwitchAnimation_bl = aValue_bl
	}

	get delaySWSwitchAnimation()
	{
		return this._fDelaySWSwitchAnimation_bl;
	}

	i_invalidatePrices()
	{
		this._invalidatePrices()
	}

	i_invalidateShots()
	{
		this._invalidateShots();
	}

	revertToDefault()
	{
		for (let lWeaponSidebarIcon_wsi of this._fWeaponSidebarIcons_wsi_arr)
		{
			if(lWeaponSidebarIcon_wsi.isSelected)
			{
				if(!lWeaponSidebarIcon_wsi.isClicked)
				{
					lWeaponSidebarIcon_wsi._onClicked({stopPropagation:()=>{}});
					lWeaponSidebarIcon_wsi.isClicked = false;
				}
				
			}else{
				lWeaponSidebarIcon_wsi.isClicked = false;
			}

			
		}
	}

	i_getWeaponLandingPosition(aWeaponId_int)
	{
		return this._getWeaponLandingPosition(aWeaponId_int);
	}

	interruptTransitions()
	{
		for (let lWeaponSidebarIcon_wsi of this._fWeaponSidebarIcons_wsi_arr)
		{
			lWeaponSidebarIcon_wsi.interruptTransitions();
		}
	}

	constructor()
	{
		super();

		this._fIsEnabled_bl = false;
		this._fIsAnimating_bl = false;
		this._fDelaySWSwitchAnimation_bl = false;

		this._fWeaponsController_wsc = APP.currentWindow.weaponsController;
		this._fWeaponsInfo_wsi = this._fWeaponsController_wsc.info;

		this._fWeaponSidebarIcons_wsi_arr = [];

		this._init();
	}

	_init()
	{
		let lWeaponsInfo_wsi = this._fWeaponsInfo_wsi;
		for (let lWeaponId_int of SPECIAL_WEAPONS)
		{
			let index = this._fWeaponSidebarIcons_wsi_arr.length;

			let lWeaponSidebarIcon_wsi = new WeaponSidebarIcon(lWeaponId_int, lWeaponsInfo_wsi.i_getWeaponShotPrice(lWeaponId_int));
			lWeaponSidebarIcon_wsi.isAnimating = this.isAnimating;
			lWeaponSidebarIcon_wsi.on(WeaponSidebarIcon.EVENT_WEAPON_SIDEBAR_ICON_CLICKED, this.emit, this);
			lWeaponSidebarIcon_wsi.on(WeaponSidebarIcon.EVENT_WEAPON_SIDEBAR_ICON_DEFAULT_DISAPPEARED, this.emit, this);
			
			this._fWeaponSidebarIcons_wsi_arr.push(lWeaponSidebarIcon_wsi);
		}

		this._fWeaponSidebarIcons_wsi_arr.sort( (a, b) => { return (b.weaponPrice - a.weaponPrice) } );

		for (let i=0; i<this._fWeaponSidebarIcons_wsi_arr.length; i++)
		{
			let lWeaponSidebarIcon_wsi = this._fWeaponSidebarIcons_wsi_arr[i];
			lWeaponSidebarIcon_wsi.position.set(0, i * VERTICAL_DELTA);
			this.addChild(lWeaponSidebarIcon_wsi);
		}

		const lTotalHeight_num = SPECIAL_WEAPONS.length * VERTICAL_DELTA;
		const lVerticalOffset_num = VERTICAL_DELTA/2;
		const lHorizontalOffset_num = 40;
		this.position.set(lHorizontalOffset_num, (540 - lTotalHeight_num)/2 + lVerticalOffset_num);

		this._invalidatePrices();
		this._invalidateShots();
	}

	_invalidatePrices()
	{
		for (let lWeaponSidebarIcon_wsi of this._fWeaponSidebarIcons_wsi_arr)
		{
			let lWeaponsInfo_wsi = this._fWeaponsInfo_wsi;
			let lPrice_num = lWeaponsInfo_wsi.i_getWeaponShotPrice(lWeaponSidebarIcon_wsi.weaponId);
			let lIsTurretIcon_bl = this._isTurretWeaponIcon(lWeaponSidebarIcon_wsi);

			lWeaponSidebarIcon_wsi.i_updatePrice(lIsTurretIcon_bl ? lWeaponsInfo_wsi.i_getWeaponShotPrice(WEAPONS.DEFAULT) : lPrice_num);
		}
	}

	_isTurretWeaponIcon(aWeaponSidebarIcon_wsi)
	{
		let lWeaponsInfo_wsi = this._fWeaponsInfo_wsi;

		return (
			lWeaponsInfo_wsi.currentWeaponId === aWeaponSidebarIcon_wsi.weaponId
			&& lWeaponsInfo_wsi.i_getWeaponFreeShots(aWeaponSidebarIcon_wsi.weaponId) <= 0
			&& !lWeaponsInfo_wsi.isFreeWeaponsQueueActivated
		)
	}

	_invalidateShots()
	{
		for (let lWeaponSidebarIcon_wsi of this._fWeaponSidebarIcons_wsi_arr)
		{
			let lFreeShots_int = this._fWeaponsInfo_wsi.i_getWeaponFreeShots(lWeaponSidebarIcon_wsi.weaponId);
			lWeaponSidebarIcon_wsi.delayDefaultIcon = this._delayDefaultIconNeededForWeapon(lWeaponSidebarIcon_wsi.weaponId);
			lWeaponSidebarIcon_wsi.i_updateFreeShots(lFreeShots_int);
		}

		this._invalidateIconsStates();
		this._invalidatePrices();

		for (let lWeaponSidebarIcon_wsi of this._fWeaponSidebarIcons_wsi_arr)
		{
			lWeaponSidebarIcon_wsi.delayDefaultIcon = false;
		}
	}

	_invalidateIconsStates()
	{
		const lIsAnyFreeSpecialWeaponExist_bl = this._fWeaponsInfo_wsi.isAnyFreeSpecialWeaponExist;
		const lIsFreeWeaponsQueueActivated_bl = this._fWeaponsInfo_wsi.isFreeWeaponsQueueActivated;

		for (let lWeaponSidebarIcon_wsi of this._fWeaponSidebarIcons_wsi_arr)
		{
			if(lWeaponSidebarIcon_wsi.currentState == WeaponSidebarIcon.i_STATE_FREE_SW
				&& !lIsFreeWeaponsQueueActivated_bl)
				{
					lWeaponSidebarIcon_wsi.isSelected = this._fWeaponsInfo_wsi.currentWeaponId === lWeaponSidebarIcon_wsi.weaponId;
				}
			let lFreeShots_int = lWeaponSidebarIcon_wsi.freeShots;
			if (
					lFreeShots_int > 0
					||
					(
						this._fWeaponsInfo_wsi.currentWeaponId === lWeaponSidebarIcon_wsi.weaponId
						&& lFreeShots_int == 0
						&& lIsFreeWeaponsQueueActivated_bl
					)
				)
			{
				lWeaponSidebarIcon_wsi.i_updateState(WeaponSidebarIcon.i_STATE_FREE_SW);
				lWeaponSidebarIcon_wsi.active = this.enabled && true;
			}
			else
			{
				lWeaponSidebarIcon_wsi.i_updateState(WeaponSidebarIcon.i_STATE_PAID_SW);
				lWeaponSidebarIcon_wsi.active = this.enabled && !lIsAnyFreeSpecialWeaponExist_bl && !lIsFreeWeaponsQueueActivated_bl;
			}

			if (this._isFRBMode)
			{
				lWeaponSidebarIcon_wsi.i_updateState(WeaponSidebarIcon.i_STATE_BONUS);
				lWeaponSidebarIcon_wsi.active = this.enabled && lFreeShots_int > 0;
				lWeaponSidebarIcon_wsi.freeShots = lFreeShots_int;
			}
			lWeaponSidebarIcon_wsi.isSelected = this._fWeaponsInfo_wsi.currentWeaponId === lWeaponSidebarIcon_wsi.weaponId;
		}
	}

	get _isFRBMode()
	{
		return APP.currentWindow.gameFrbController.info.frbMode;
	}

	_invalidateAll()
	{
		this._invalidateIconsStates();
	}

	_getWeaponLandingPosition(aWeaponId_int)
	{
		for (let lWeaponSidebarIcon_wsi of this._fWeaponSidebarIcons_wsi_arr)
		{
			if (lWeaponSidebarIcon_wsi.parent && lWeaponSidebarIcon_wsi.weaponId == aWeaponId_int)
			{
				return lWeaponSidebarIcon_wsi.parent.localToGlobal(lWeaponSidebarIcon_wsi.x, lWeaponSidebarIcon_wsi.y);
			}

		}
		throw new Error ("Didn't find the landing position for weaponId = " + aWeaponId_int);
		return null;
	}

	_updateIsAnimatingParam()
	{
		for (let lWeaponSidebarIcon_wsi of this._fWeaponSidebarIcons_wsi_arr)
		{
			lWeaponSidebarIcon_wsi.isAnimating = this.isAnimating;
		}
	}

	_delayDefaultIconNeededForWeapon(aWeaponId_int)
	{
		return this.delaySWSwitchAnimation && aWeaponId_int === this._fWeaponsInfo_wsi.currentWeaponId;
	}
}

export default WeaponsSidebarView;