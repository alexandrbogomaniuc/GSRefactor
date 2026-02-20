import SimpleController from '../../../unified/controller/base/SimpleController';
import { APP } from '../../../unified/controller/main/globals';
import GUSLobbyWeaponsPanelInfo from '../../model/custom/GUSLobbyWeaponsPanelInfo';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES } from '../external/GUSLobbyExternalCommunicator';

class GUSLobbyWeaponsPanelController extends SimpleController
{
	static get EVENT_ON_INFO_UPDATED() { return "EVENT_ON_INFO_UPDATED"; }

	onSwitchToLobby()
	{
		this._clear();
	}

	constructor(aOptInfo)
	{
		super(aOptInfo || new GUSLobbyWeaponsPanelInfo());
	}

	_clear()
	{
		let lInfo_wqi = this.info;

		if (lInfo_wqi)
		{
			lInfo_wqi.allWeaponsAmmo = {};
			lInfo_wqi.selectedId = -1;
		}

		this.emit(GUSLobbyWeaponsPanelController.EVENT_ON_INFO_UPDATED);
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameExternalMessageReceived, this);
	}

	_onGameExternalMessageReceived(aEvent_obj)
	{
		switch (aEvent_obj.type)
		{
			case GAME_MESSAGES.WEAPONS_UPDATED:
				let lWeapons_obj = aEvent_obj.data.weapons;
				let lIsFreeWeaponsQueueActivated_bl = aEvent_obj.data.isFreeWeaponsQueueActivated;
				let lIsRoundStatePlay_bln = aEvent_obj.data.isRoundStatePlay;
				this._updateWeaponsInfo(lWeapons_obj, lIsFreeWeaponsQueueActivated_bl, lIsRoundStatePlay_bln);
				break;

			case GAME_MESSAGES.WEAPON_SELECTED:
				this._updateSelectedInfo(aEvent_obj.data.weaponId);
				break;
		}
	}

	_updateWeaponsInfo(aWeapons_obj, aIsFreeWeaponsQueueActivated_bl, lIsRoundStatePlay_bln = false)
	{
		this.info.allWeaponsAmmo = aWeapons_obj;
		this.info.isFreeWeaponsQueueActivated = aIsFreeWeaponsQueueActivated_bl;
		this.info.isRoundStatePlay = lIsRoundStatePlay_bln;
		this.emit(GUSLobbyWeaponsPanelController.EVENT_ON_INFO_UPDATED);
	}

	_updateSelectedInfo(aWeaponId_int)
	{
		this.info.selectedId = aWeaponId_int;
		this.emit(GUSLobbyWeaponsPanelController.EVENT_ON_INFO_UPDATED);
	}
}

export default GUSLobbyWeaponsPanelController;