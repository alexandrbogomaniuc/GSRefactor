import CalloutController from '../CalloutController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameField from '../../../../../main/GameField';
import GameScreen from '../../../../../main/GameScreen';

class TimesRunningOutController extends CalloutController
{
	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().timesRunningOutView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();
		APP.gameScreen.on(GameScreen.EVENT_ON_BOSS_DESTROYING, this._onBossDestroying, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_DRAGON_DISAPPEARED, this._onBossDisappeared, this);

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	//DEBUG...
	/*keyDownHandler(keyCode)
	{
		if (keyCode.keyCode == 85) //u
	 	{
	 		this.__activateCallout();
		}
	}*/
	// ...DEBUG

	_onBossDestroying()
	{
		if (!this.info.isPresented)
		{
			this.__deactivateCallout();
		}
	}

	_onBossDisappeared()
	{
		if (!this.info.isPresented)
		{
			this.__deactivateCallout();
		}
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
			view.setCaption("TATimesRunningOutCaption", "TAHurryCaption");
		}

		super.__validateViewLevel();
	}
	//...VALIDATION
}

export default TimesRunningOutController