import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GamePlayerController from '../../custom/GamePlayerController';
import PlayerSpot from '../../../main/playerSpots/PlayerSpot';
import MainPlayerSpot from '../../../main/playerSpots/MainPlayerSpot';
import { WEAPONS } from '../../../../../shared/src/CommonConstants';
import PlayerInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import GameScreen from '../../../main/GameScreen';
import GameField from '../../../main/GameField';
import GameStateController from '../../state/GameStateController';
import {SUBROUND_STATE, ROUND_STATE} from '../../../model/state/GameStateInfo';
import {LOBBY_MESSAGES, GAME_MESSAGES} from '../../../external/GameExternalCommunicator';
import GameExternalCommunicator from '../../../external/GameExternalCommunicator';
import WeaponsSidebarController from './sidebar/WeaponsSidebarController';
import WeaponsController from './WeaponsController';

class WeaponsQueueController extends SimpleController
{
	static get EVENT_ON_WEAPON_SELECTED()	{ return "onWeaponSelected"; }

	constructor(aPlayerSpot_ps)
	{
		super ();

		this._fPlayerSpot_ps = aPlayerSpot_ps;

		this._fLastLobbySelectedWeapon_obj = null;

		this._fWeaponsController_wsc = APP.currentWindow.weaponsController;
		this._fWeaponsInfo_wsi = this._fWeaponsController_wsc.i_getInfo();
	}

	__init()
	{
		super.__init();

		this._fWeaponsController_wsc.on(WeaponsController.EVENT_ON_FRB_AMMO_UPDATED, this._onFrbAmmoUpdated, this);

		this._fPlayerSpot_ps.on(MainPlayerSpot.EVENT_AMMO_UPDATED, this._onSpotAmmoUpdated, this);
		this._fPlayerSpot_ps.on(MainPlayerSpot.EVENT_CHANGE_WEAPON, this._onWeaponSelected, this);

		this._playerController.on(GamePlayerController.EVENT_ON_PLAYER_WEAPON_UPDATED, this._onPlayerWeaponUpdated, this);
		this._playerController.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_RESTORING_ON_LAGS_STARTED, this._onRoomRestoringOnLagsStarted, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
		this._gameScreen.on(GameScreen.EVENT_NEW_WEAPON_CONTENT_LANDED_WITHOUT_CHANGING, this._onNewWeaponContentLandedWithoutChanging, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);

		this._gameField.on("fire", this._onFire, this);
		this._gameField.on(GameField.EVENT_DECREASE_AMMO, this._onAmmoUpdated, this);
		this._gameField.on(GameField.EVENT_ON_CHANGE_WEAPON_FAILED, this._onChangeWeaponFailed, this);
		this._gameField.on(GameField.EVENT_ON_CLEAR_WEAPONS_REQUIRED, this._onClearWeaponRequired, this);

		this._gameStateController.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);

		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);

		this._gameField.weaponsSidebarController.on(WeaponsSidebarController.EVENT_ON_TRY_TO_SELECT_WEAPON_FROM_SIDEBAR, this._onTryToSelectWeaponFromSidebar, this);

		this._updateView();
	}

	get _gameScreen()
	{
		return APP.currentWindow;
	}

	get _gameField()
	{
		return APP.currentWindow.gameField;
	}

	get _playerController()
	{
		return APP.playerController;
	}

	get _weaponsInfo()
	{
		return this._gameScreen.weaponsController.info;
	}

	get _gameStateController()
	{
		return this._gameScreen.gameStateController;
	}

	_onClearWeaponRequired()
	{
		this._clearWeapons();
	}

	_onRoomUnpaused()
	{
		if (this._gameField.roundResultActive || this._gameStateController.info.gameState == ROUND_STATE.QUALIFY)
		{
			if (APP.currentWindow.gameFrbController.info.frbMode) return;
			this._clearWeapons();
		}
		else
		{
			this._updateView();
		}
	}

	_onChangeWeaponFailed()
	{
		// initiated from Weapons Panel changing failed, we need to return selected weapon back
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.WEAPON_SELECTED, {weaponId:  this._fWeaponsInfo_wsi.currentWeaponId});
	}

	_onLobbyExternalMessageReceived(aEvent_obj)
	{
		switch (aEvent_obj.type)
		{
			case LOBBY_MESSAGES.WEAPON_SELECTED:
				let lData_obj = aEvent_obj.data;

				if (!this._fLastLobbySelectedWeapon_obj || (lData_obj.date > this._fLastLobbySelectedWeapon_obj.date))
				{
					this._fLastLobbySelectedWeapon_obj = {weaponId: lData_obj.weaponId, date: lData_obj.date};
				}

				this.emit(WeaponsQueueController.EVENT_ON_WEAPON_SELECTED, {weaponId: this._fLastLobbySelectedWeapon_obj.weaponId});
			break;
		}
	}

	_onTryToSelectWeaponFromSidebar(aEvent_obj)
	{
		const lWeaponId_int = aEvent_obj.weaponId;
		this.emit(WeaponsQueueController.EVENT_ON_WEAPON_SELECTED, {weaponId: lWeaponId_int});
	}


	_onPlayerWeaponUpdated()
	{
		this._updateView();
	}

	_onPlayerInfoUpdated(aEvent_obj)
	{
		let lUpdatedWeapons_obj = aEvent_obj.data[PlayerInfo.KEY_WEAPONS];
		if (lUpdatedWeapons_obj && lUpdatedWeapons_obj.value)
		{
			this._updateView();
		}
	}

	_onRoomPaused()
	{
		this._updateView();
	}

	_onRoomRestoringOnLagsStarted()
	{
		this._updateView();
	}

	_onNewWeaponContentLandedWithoutChanging()
	{
		this._updateView();
	}

	_onFire()
	{
		this._updateView();
	}

	_onFrbAmmoUpdated()
	{
		this._onAmmoUpdated();
	}

	_onAmmoUpdated()
	{
		this._updateView();
	}

	_onGameStateChanged()
	{
		this._updateView();
	}

	_onSpotAmmoUpdated()
	{
		this._updateView();
	}

	_updateView()
	{
		let lWeapons_obj_arr = this._weaponsInfo.weapons;
		let lWeaponsQueue_obj = {};

		for (let i = 0; i < lWeapons_obj_arr.length; ++i)
		{
			if (lWeapons_obj_arr[i] != null && lWeapons_obj_arr[i].shots > 0)
			{
				lWeaponsQueue_obj[lWeapons_obj_arr[i].id] = lWeapons_obj_arr[i].shots;
			}
		}
		lWeaponsQueue_obj[WEAPONS.DEFAULT] = this._weaponsInfo.ammo;
		this._sendWeaponsUpdatedIfRequired({weapons: lWeaponsQueue_obj,
											isFreeWeaponsQueueActivated: this._weaponsInfo.isFreeWeaponsQueueActivated, 
											isRoundStatePlay: APP.gameScreen.gameField.isRoundStatePlay
										});
	}

	_clearWeapons()
	{
		this.emit(WeaponsQueueController.EVENT_ON_WEAPON_SELECTED, {weaponId: -1});
		this._sendWeaponsUpdatedIfRequired({weapons: [],
											isFreeWeaponsQueueActivated: this._weaponsInfo.isFreeWeaponsQueueActivated
										});
	}

	_sendWeaponsUpdatedIfRequired(aWeaponsUpdatedInfo_obj)
	{
		if (this._isWeaponsUpdatedInfoChanged(aWeaponsUpdatedInfo_obj))
		{
			this._fLastWeaponsUpdatedInfo_obj = Object.assign({}, aWeaponsUpdatedInfo_obj);

			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.WEAPONS_UPDATED, aWeaponsUpdatedInfo_obj);
		}
	}

	_isWeaponsUpdatedInfoChanged(aWeaponsUpdatedInfo_obj)
	{
		let lLastWeaponsUpdatedInfo_obj = Object.assign({}, this._fLastWeaponsUpdatedInfo_obj);
		let lCurWeaponsUpdatedInfo_obj = Object.assign({}, aWeaponsUpdatedInfo_obj);

		return JSON.stringify(lLastWeaponsUpdatedInfo_obj) !== JSON.stringify(lCurWeaponsUpdatedInfo_obj);
	}

	_onWeaponSelected(aEvent_obj)
	{
		let lWeaponId_num = aEvent_obj.weaponId === undefined ? -1 : aEvent_obj.weaponId;
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.WEAPON_SELECTED, {weaponId: lWeaponId_num});
	}

	destroy()
	{
		if (this._fPlayerSpot_ps)
		{
			this._fPlayerSpot_ps.off(MainPlayerSpot.EVENT_AMMO_UPDATED, this._onSpotAmmoUpdated, this);
			this._fPlayerSpot_ps.off(MainPlayerSpot.EVENT_CHANGE_WEAPON, this._onWeaponSelected, this);
		}
		this._fWeaponsController_wsc.off(WeaponsController.EVENT_ON_FRB_AMMO_UPDATED, this._onFrbAmmoUpdated, this);
		this._playerController.off(GamePlayerController.EVENT_ON_PLAYER_WEAPON_UPDATED, this._onPlayerWeaponUpdated, this);
		this._playerController.off(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		this._gameScreen.off(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
		this._gameScreen.off(GameScreen.EVENT_ON_ROOM_RESTORING_ON_LAGS_STARTED, this._onRoomRestoringOnLagsStarted, this);
		this._gameScreen.off(GameScreen.EVENT_NEW_WEAPON_CONTENT_LANDED_WITHOUT_CHANGING, this._onNewWeaponContentLandedWithoutChanging, this);
		this._gameScreen.off(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
		this._gameField.off("fire", this._onFire, this);
		this._gameField.off(GameField.EVENT_DECREASE_AMMO, this._onAmmoUpdated, this);
		this._gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
		this._gameField.off(GameField.EVENT_ON_CHANGE_WEAPON_FAILED, this._onChangeWeaponFailed, this);
		this._gameField.off(GameField.EVENT_ON_CLEAR_WEAPONS_REQUIRED, this._onClearWeaponRequired, this);

		APP.externalCommunicator.off(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);

		super.destroy();

		this._fPlayerSpot_ps = null;
		this._fLastLobbySelectedWeapon_obj = null;
	}
}

export default WeaponsQueueController;