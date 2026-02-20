import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import LobbyWeaponsPanelInfo from '../../model/custom/LobbyWeaponsPanelInfo';
import LobbyExternalCommunicator, {GAME_MESSAGES, LOBBY_MESSAGES} from '../../external/LobbyExternalCommunicator';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class LobbyWeaponsPanelController extends SimpleController {

	static get EVENT_ON_INFO_UPDATED() 	{ return "EVENT_ON_INFO_UPDATED"; }

	onSwitchToLobby()
	{
		this._clear();
	}

	constructor()
	{
		super(new LobbyWeaponsPanelInfo());
	}

	_clear()
	{
		let lInfo_wqi = this.info;

		if (lInfo_wqi)
		{
			lInfo_wqi.allWeaponsAmmo = {};
			lInfo_wqi.selectedId = -1;
		}

		this.emit(LobbyWeaponsPanelController.EVENT_ON_INFO_UPDATED);
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameExternalMessageReceived, this);
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

			case GAME_MESSAGES.WEAPONS_INTERACTION_CHANGED:
				//this._fAllowanceFromGame_bl = aEvent_obj.data.allowed;
				//this._validateInteractivity();
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
		this.emit(LobbyWeaponsPanelController.EVENT_ON_INFO_UPDATED);
	}

	_updateSelectedInfo(aWeaponId_int)
	{
		this.info.selectedId = aWeaponId_int;
		this.emit(LobbyWeaponsPanelController.EVENT_ON_INFO_UPDATED);
	}
}

export default LobbyWeaponsPanelController;