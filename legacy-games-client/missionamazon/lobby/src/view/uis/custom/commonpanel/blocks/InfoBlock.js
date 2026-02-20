import { Sprite } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWeaponsPanelController from '../../../../../controller/custom/LobbyWeaponsPanelController';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import InfoBlockFreeShotsLabel from './InfoBlockFreeShotsLabel';
import FRBController from './../../../../../controller/custom/frb/FRBController';

class InfoBlock extends Sprite {

	hideFreeLabel()
	{
		if (this._fInfoBlockFreeShotsLabel_ibfsl)
		{
			this._fInfoBlockFreeShotsLabel_ibfsl.hide();
		}
	}

	constructor()
	{
		super();

		this._fInfoBlockFreeShotsLabel_ibfsl = null;

		this._init();
	}

	_init()
	{
		let l_lwpc = APP.commonPanelController.lobbyWeaponsPanelController;
		l_lwpc.on(LobbyWeaponsPanelController.EVENT_ON_INFO_UPDATED, this._onInfoUpdated, this);

		let l_frbc = APP.FRBController;
		l_frbc.on(FRBController.EVENT_ON_FRB_STATE_CHANGED, this._onInfoUpdated, this);

		this._fInfoBlockFreeShotsLabel_ibfsl = this.addChild(new InfoBlockFreeShotsLabel());
		this._fInfoBlockFreeShotsLabel_ibfsl.position.set(-25, 4);
	}

	_onInfoUpdated(event)
	{
		let l_lwpi = APP.commonPanelController.lobbyWeaponsPanelController.info;

		this._fInfoBlockFreeShotsLabel_ibfsl.i_updateFreeShots(l_lwpi.totalFreeWeaponsShot);

		this._invalidateVisibility();
	}

	_invalidateVisibility()
	{
		if (APP.isBattlegroundGame)
		{
			this._fInfoBlockFreeShotsLabel_ibfsl.hide();
			return;
		}

		let l_lwpi = APP.commonPanelController.lobbyWeaponsPanelController.info;

		if (((l_lwpi.isCurrentWeaponSpecialAndFree || l_lwpi.isFreeWeaponsQueueActivated) && l_lwpi.isRoundStatePlay)
			|| (APP.FRBController.info.isActivated && APP.FRBController.info.playerSatIn)
			)
		{
			this._fInfoBlockFreeShotsLabel_ibfsl.show();
		}
		else
		{
			this._fInfoBlockFreeShotsLabel_ibfsl.hide();
		}
	}

	destroy()
	{
		super.destroy();
		this._fInfoBlockFreeShotsLabel_ibfsl = null;
	}
}

export default InfoBlock