import CalloutController from '../CalloutController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameField from '../../../../../main/GameField';
import { ENEMIES } from '../../../../../../../shared/src/CommonConstants';

class WeaponCarrierCalloutController extends CalloutController
{
	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
	}

	__init ()
	{
		super.__init();
		this._fCurrentTypeOrb_str = null;
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().weaponCarrierCalloutView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();
		APP.currentWindow.gameField.on(GameField.EVENT_ORB_CALLOUT_CREATED, this._onRareEnemyCreated, this);
	}

	 _onRareEnemyCreated (aEvent)
	 {
		this._fCurrentTypeOrb_str = aEvent.data;
		this.__activateCallout();
	 }

	 _startCallout()
	{
		let l_ltv = this.view;

		if (l_ltv)
		{
			l_ltv.startCallout();
		}
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		let info = this.info;
		if (info.isActive)
		{
			let view = this.__fView_uo;
			var lRareCaption_spr = APP.isMobile ? "TARareEnemyCaptionMobile" : "TARareEnemyCaption";
			var lWeaponCaption_spr = "TAWeaponCarrierCaption";

			view.setCaption(lRareCaption_spr, lWeaponCaption_spr);
		}

		super.__validateViewLevel();
	}
	//...VALIDATION
}

export default WeaponCarrierCalloutController