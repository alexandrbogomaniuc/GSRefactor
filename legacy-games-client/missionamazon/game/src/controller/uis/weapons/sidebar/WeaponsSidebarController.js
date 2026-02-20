import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import WeaponsSidebarInfo from '../../../../model/uis/weapons/sidebar/WeaponsSidebarInfo';
import WeaponsSidebarView from '../../../../view/uis/weapons/sidebar/WeaponsSidebarView';
import PlayerController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/custom/PlayerController';
import PlayerInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import WeaponsController from '../WeaponsController';
import GameStateController from '../../../state/GameStateController';
import { ROUND_STATE } from '../../../../model/state/GameStateInfo';
import GamePlayerController from '../../../custom/GamePlayerController';
import { FRAME_RATE, WEAPONS } from '../../../../../../shared/src/CommonConstants';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import GameScreen from '../../../../main/GameScreen';

class WeaponsSidebarController extends SimpleUIController {

	static get EVENT_ON_TRY_TO_SELECT_WEAPON_FROM_SIDEBAR() { return "EVENT_ON_TRY_TO_SELECT_WEAPON_FROM_SIDEBAR"; }

	i_getWeaponLandingPosition(aWeaponId_int)
	{
		if (this.view)
		{
			return this.view.i_getWeaponLandingPosition(aWeaponId_int);
		}
		throw new Error ("Didn't find the landing position for weaponId = " + aWeaponId_int);
	}

	i_hideView()
	{
		this.view.visible = false;
	}

	i_showView()
	{
		this.view.visible = true;
	}

	constructor(aWeaponsSidebarView_wssv)
	{
		super(new WeaponsSidebarInfo(), aWeaponsSidebarView_wssv);

		this._fSWSwitchInProgress_bl = false;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.playerController.on(PlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.playerController.on(GamePlayerController.EVENT_ON_PLAYER_WEAPON_UPDATED, this._onSelectedWeaponUpdated, this);
		APP.currentWindow.weaponsController.on(WeaponsController.EVENT_ON_WEAPONS_UPDATED, this._onWeaponsUpdated, this);
		APP.currentWindow.weaponsController.on(WeaponsController.EVENT_ON_AMMO_UPDATED, this._onWeaponsUpdated, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);

		this._fGameStateController_gsc = APP.currentWindow.gameStateController;
		this._fGameStateInfo_gsi = this._fGameStateController_gsc.info;
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameRoundStateChanged, this);
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_PLAYER_SEAT_STATE_CHANGED, this._onPlayerSeatStateChanged, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		this.view.on(WeaponsSidebarView.EVENT_WEAPON_SIDEBAR_ICON_CLICKED, this._onWeaponSidebarIconClicked, this);

		this._invalidateView();
	}

	_onWeaponSidebarIconClicked(aEvent_obj)
	{
		let lSelectedWeaponId_int = aEvent_obj.weaponId;

		let lWeaponsInfo_wsi = APP.currentWindow.weaponsController.info;
		if (lWeaponsInfo_wsi.currentWeaponId === lSelectedWeaponId_int && (lWeaponsInfo_wsi.remainingSWShots <= 0 || this._isFRBMode))
		{
			lSelectedWeaponId_int = WEAPONS.DEFAULT;
		}
		if (lSelectedWeaponId_int != WEAPONS.DEFAULT)
		{
			if (this._fSWSwitchInProgress_bl)
			{
				this.view.interruptTransitions();
			}
			this._fSWSwitchInProgress_bl = true;
			if(lWeaponsInfo_wsi.currentWeaponId != WEAPONS.DEFAULT)
				this.view.on(WeaponsSidebarView.EVENT_WEAPON_SIDEBAR_ICON_DEFAULT_DISAPPEARED, this._onSelectedWeaponUpdatedFinal, this);
			else
				this._fchangeIconViewTimer = new Timer(() => {
					this._onSelectedWeaponUpdatedFinal();
				}, 13 * FRAME_RATE);
		}

		this.emit(WeaponsSidebarController.EVENT_ON_TRY_TO_SELECT_WEAPON_FROM_SIDEBAR, {weaponId: lSelectedWeaponId_int});
	}

	_onPlayerInfoUpdated(aEvent_obj)
	{
		if (aEvent_obj.data[PlayerInfo.KEY_STAKE]
			|| aEvent_obj.data[PlayerInfo.KEY_BET_LEVEL]
			|| aEvent_obj.data[PlayerInfo.KEY_WEAPON_COST_MULTIPLIERS])
		{
			let lView_wssv = this.view;
			if (lView_wssv)
			{
				lView_wssv.i_invalidatePrices();
			}
		}
		if (aEvent_obj.data[PlayerInfo.KEY_WEAPON_ID])
		{
			this._onWeaponsUpdated();
		}
	}

	_onSelectedWeaponUpdated()
	{
		this._onWeaponsUpdated();
	}

	_onSelectedWeaponUpdatedFinal()
	{
		if (!this._fSWSwitchInProgress_bl)
		{
			return;
		}
		this._fSWSwitchInProgress_bl = false;
		this.view.off(WeaponsSidebarView.EVENT_WEAPON_SIDEBAR_ICON_DEFAULT_DISAPPEARED, this._onSelectedWeaponUpdatedFinal, this);
		this._onWeaponsUpdated();
	}

	_onWeaponsUpdated()
	{
		let lView_wssv = this.view;
		if (lView_wssv)
		{
			lView_wssv.isAnimating = this._getIsAnimatingNeeded();
			lView_wssv.delaySWSwitchAnimation = this._fSWSwitchInProgress_bl;
			lView_wssv.i_invalidateShots();
		}
	}

	_onGameRoundStateChanged(aEvent_obj)
	{
		this._invalidateView();
	}

	_onPlayerSeatStateChanged(aEvent_obj)
	{
		this._invalidateView();
	}

	_onRoomPaused(aEvent_obj)
	{
		this.view.interruptTransitions();
	}

	_invalidateView()
	{
		let lView_wssv = this.view;
		const lIsPlayerSitIn_bl = this._fGameStateInfo_gsi.isPlayerSitIn;

		switch (this._fGameStateInfo_gsi.gameState)
		{
			case ROUND_STATE.PLAY:
				lView_wssv.enabled = lIsPlayerSitIn_bl;

				let lWeaponsInfo_wsi = APP.currentWindow.weaponsController.info;
				if(lWeaponsInfo_wsi.currentWeaponId != WEAPONS.DEFAULT && lIsPlayerSitIn_bl)
				{
					lView_wssv.revertToDefault();
				}

				break;
			default:
				lView_wssv.enabled = false;
				break;
		}

		lView_wssv.isAnimating = this._getIsAnimatingNeeded();
		lView_wssv.i_invalidateShots();
	}

	get _isFRBMode()
	{
		return APP.currentWindow.gameFrbController.info.frbMode;
	}

	_getIsAnimatingNeeded()
	{
		return this._fGameStateInfo_gsi.isPlayState || this._fGameStateInfo_gsi.isPlayerSitIn;
	}
}

export default WeaponsSidebarController;