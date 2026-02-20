import SimpleUIView from '../../../../unified/view/base/SimpleUIView';
import { APP } from '../../../../unified/controller/main/globals';
import Sprite from '../../../../unified/view/base/display/Sprite';
import GUSLobbySettingsScreenView from './settings/GUSLobbySettingsScreenView';
import GUSLobbyEditProfileScreenView from './edit_profile/GUSLobbyEditProfileScreenView';

class GUSLobbySecondaryScreenView extends SimpleUIView
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

		this._transparentBack = null;
		this._fSettingsScreenView_ssv = null;
		this._fEditProfileScreenView_epsv = null;

		this._addTransparentBack();

		this.visible = false;
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

	get _settingsScreenView()
	{
		return this._fSettingsScreenView_ssv || (this._fSettingsScreenView_ssv = this._fScreenContainer_sprt.addChild(this.__provideSettingsScreenViewInstance()));
	}

	__provideSettingsScreenViewInstance()
	{
		return new GUSLobbySettingsScreenView();
	}

	get _editProfileScreenView()
	{
		return this._fEditProfileScreenView_epsv || (this._fEditProfileScreenView_epsv = this._fScreenContainer_sprt.addChild(this.__provideEditProfileScreenViewInstance()));
	}

	__provideEditProfileScreenViewInstance()
	{
		return new GUSLobbyEditProfileScreenView();
	}

	destroy()
	{
		super.destroy();
	}
}

export default GUSLobbySecondaryScreenView;