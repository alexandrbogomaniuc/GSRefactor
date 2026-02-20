import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import SettingsScreenView from './settings/SettingsScreenView';
import EditProfileScreenView from './edit_profile/EditProfileScreenView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class SecondaryScreenView extends SimpleUIView
{
	get settingsScreenView()
	{
		return this._settingsScreenView;
	}

	get editProfileScreenView()
	{
		return this._editProfileScreenView;
	}

	hideTransparentBack()
	{
		this._hideTransparentBack();
	}

	showTransparentBack()
	{
		this._showTransparentBack()
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
		this._fEditProfileScreenView_epsv = null;

		this._addTransparentBack();

		this.visible = false;

		let platformInf = window.getPlatformInfo ? window.getPlatformInfo() : {};
		this._isMobile = platformInf.mobile;

		if (this._isMobile)
		{
			this._mobileFormation();
		}
	}
	
	_addTransparentBack()
	{
		this._transparentBack = new PIXI.Graphics();
		this._transparentBack.beginFill(0x000000, 0.45);
		this._transparentBack.drawRect(-480, -270, 960, 540);
		this._transparentBack.endFill();

		this._fBgContainer_sprt.addChild(this._transparentBack);
		this._fBgContainer_sprt.interactive = true;
		this._fBgContainer_sprt.buttonMode = false;
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

	get _editProfileScreenView()
	{
		return this._fEditProfileScreenView_epsv || (this._fEditProfileScreenView_epsv = this._fScreenContainer_sprt.addChild(new EditProfileScreenView()));
	}
	//...VIEWS
}

export default SecondaryScreenView;