import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import SettingsScreenView from './settings/SettingsScreenView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { GAME_VIEW_SETTINGS } from '../../../main/GameBaseView';

class SecondaryScreenView extends SimpleUIView
{
	get settingsScreenView()
	{
		return this._settingsScreenView;
	}

	hideTransparentBack()
	{
		this._hideTransparentBack();
	}

	showTransparentBack()
	{
		this._showTransparentBack()
	}

	updateArea()
	{
		this._updateArea();
	}

	//INIT...
	constructor(aParentContainer_sprt)
	{
		super();

		if (aParentContainer_sprt)
		{
			aParentContainer_sprt.addChild(this);
			this._fParentContainer_sprt = aParentContainer_sprt;
		}

		this.addChild(this._fBgContainer_sprt = new Sprite());
		this.addChild(this._fScreenContainer_sprt = new Sprite());
		this.addChild(this._fMenuContainer_sprt = new Sprite());

		this._transparentBack = null;
		this._fSettingsScreenView_ssv = null;

		this._addTransparentBack();

		this.visible = false;

		this._isMobile = APP.isMobile;

		if (this._isMobile)
		{
			this._mobileFormation();
		}

		this._updateArea();
	}
	
	_addTransparentBack()
	{
		this._transparentBack = new PIXI.Graphics();
		
		this._fBgContainer_sprt.addChild(this._transparentBack);
		this._fBgContainer_sprt.interactive = true;
		this._fBgContainer_sprt.buttonMode = false;
	}

	_updateArea()
	{
		let lScreenWidth_num = APP.screenWidth;
		let lScreenHeight_num = APP.screenHeight;

		let gr = this._transparentBack;
		gr.clear();
		gr.beginFill(0x000000, 0.45);
		gr.drawRect(-lScreenWidth_num/2, -lScreenHeight_num/2, lScreenWidth_num, lScreenHeight_num-GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT);
		gr.endFill();
	}

	_hideTransparentBack()
	{
		this._transparentBack.visible = false;
	}

	_showTransparentBack()
	{
		this._transparentBack.visible = true;
	}
	//...INIT

	_mobileFormation()
	{
		this._fMenuContainer_sprt.scale.set(1.8);
	}

	//VIEWS...

	get _settingsScreenView()
	{
		return this._fSettingsScreenView_ssv || (this._fSettingsScreenView_ssv = this._fScreenContainer_sprt.addChild(new SettingsScreenView()));
	}
	//...VIEWS
}

export default SecondaryScreenView;