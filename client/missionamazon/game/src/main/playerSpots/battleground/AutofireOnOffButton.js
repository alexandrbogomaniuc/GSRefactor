
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PointerSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/PointerSprite';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class AutofireOnOffButton extends PointerSprite
{
	static get EVENT_ON_ENABLE()			{ return 'onEnableAutoFire'; }
	static get EVENT_ON_DISABLE()			{ return 'onDisableAutoFire'; }

	i_setEnable(aValue_bl, aEnemy_e, aOptIsQuietMode_bl=false)
	{
		if (aValue_bl == this._fIsActive_bl)
		{
			return
		}

		if (aValue_bl)
		{
			this.setEnabled(aEnemy_e, aOptIsQuietMode_bl);
		}
		else
		{
			this.setDisabled(aOptIsQuietMode_bl);
		}

		this._checkAndValidateView();
	}

	get enabled()
	{
		return this._fIsActive_bl;
	}

	updateView()
	{
		this._checkAndValidateView();
	}

	constructor()
	{
		super();

		this._fIsActive_bl = APP.gameScreen.gameField._fAutofireButtonEnabled_bl;
		this._fBody_sprt = null;

		this._init();
	}

	_init()
	{
		this._fBody_sprt = this.addChild(new Sprite());

		/*let lButtonBack_spr = this._fBody_sprt.addChild(APP.library.getSprite("player_spot/battleground/autofire_button_back"));
		lButtonBack_spr.scale.set(1.05);
		lButtonBack_spr.position.set(-0.5, -2.5);*/

		this._fButtonContainer_spr = this._fBody_sprt.addChild(new Sprite());
		this._fButtonContainer_spr.position.set(0, -1);
		this._fButtonContainer_spr.scale.set(1.5);
		
		if(APP.isAutoFireMode)
		{
			this._fONButtonView_spr = this._fButtonContainer_spr.addChild(APP.library.getSpriteFromAtlas("player_spot/battleground/autofire_button_on"));
			this._fOFFButtonView_spr = this._fButtonContainer_spr.addChild(APP.library.getSpriteFromAtlas("player_spot/battleground/autofire_button_off"));
		}else
		{
			this._fONButtonView_spr = this._fButtonContainer_spr.addChild(APP.library.getSprite("player_spot/battleground/autofire_button_back"));
			this._fOFFButtonView_spr = this._fButtonContainer_spr.addChild(APP.library.getSprite("player_spot/battleground/autofire_button_back"));
		}  
		
		this._fText_ta = this._fButtonContainer_spr.addChild(I18.generateNewCTranslatableAsset("TABattlegroundPlayerSpotAutoFireTitle"));
		this._fText_ta.position.set(0, -1.5);
		this._fText_ta.visible = APP.isAutoFireMode;

		this._initButtonBehaviour();

		this._checkAndValidateView();
	}

	_initButtonBehaviour()
	{
		//set cursor to default over the sidebar icon
		var cursorController = APP.currentWindow.cursorController;
		this.mouseover = () => cursorController.setOverRestrictedZone(true);

		this.setHitArea(new PIXI.Circle(0, -1, 20));

		this.on("pointerdown", (e)=> {
			e.stopPropagation();
			this._onClick();
		}, this);
		
		this.on("pointerup", (e)=> {
			e.stopPropagation();
			this._changeState();
		}, this);
	}

	setEnabled(aEnemy_e, aOptIsQuietMode_bl=false)
	{
		this._fIsActive_bl = true;
		!aOptIsQuietMode_bl && this.emit(AutofireOnOffButton.EVENT_ON_ENABLE, {enemy: aEnemy_e});
	}

	setDisabled(aOptIsQuietMode_bl=false)
	{
		this._fIsActive_bl = false;
		!aOptIsQuietMode_bl && this.emit(AutofireOnOffButton.EVENT_ON_DISABLE);
	}

	_onClick()
	{
		if(APP.gameScreen.gameField.isFinalCountingFireDenied || !APP.isAutoFireMode) return;

		this._fButtonContainer_spr.scale.set(1.4);
	}

	_changeState()
	{
		if(APP.gameScreen.gameField.isFinalCountingFireDenied  || !APP.isAutoFireMode) return;

		this._fButtonContainer_spr.scale.set(1.5);

		if (this._fIsActive_bl)
		{
			this.setDisabled();
		}
		else
		{
			this.setEnabled();
		}

		this._checkAndValidateView();
	}

	_checkAndValidateView()
	{
		this._fOFFButtonView_spr.visible = !this._fIsActive_bl;
		this._fONButtonView_spr.visible = this._fIsActive_bl;
	}

	destroy()
	{
		this._fIsActive_bl = undefined;

		this._fButtonContainer_spr && this._fButtonContainer_spr.destroy();
		this._fButtonContainer_spr = null;

		this._fBody_sprt && this._fBody_sprt.destroy();
		this._fBody_sprt = null;

		super.destroy();
	}
}

export default AutofireOnOffButton;