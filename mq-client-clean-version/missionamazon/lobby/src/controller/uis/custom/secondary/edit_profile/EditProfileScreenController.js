import SimpleUIController from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyAPP from '../../../../../LobbyAPP';

import EditProfileScreenView from '../../../../../view/uis/custom/secondary/edit_profile/EditProfileScreenView';
import LobbyPlayerController from '../../../../custom/LobbyPlayerController';
import PlayerInfo from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import SubloadingController from '../../../../subloading/SubloadingController';
import { SUBLOADING_ASSETS_TYPES } from '../../../../../config/Constants';

class EditProfileScreenController extends SimpleUIController
{
	static get EVENT_ON_CANCEL_BUTTON_CLICKED()		{return EditProfileScreenView.EVENT_ON_CANCEL_BUTTON_CLICKED;}
	static get EVENT_ON_CLOSE_SCREEN()				{return EditProfileScreenView.EVENT_ON_CLOSE_SCREEN;}

	static get EVENT_ON_AVATAR_CHANGE_REQUIRED()	{return EditProfileScreenView.EVENT_ON_AVATAR_CHANGE_REQUIRED;}
	static get EVENT_ON_AVATAR_CHANGED()			{return EditProfileScreenView.EVENT_ON_AVATAR_CHANGED;}
	static get EVENT_ON_AVATAR_UPDATED()			{return EditProfileScreenView.EVENT_ON_AVATAR_UPDATED;}
	
	static get EVENT_ON_NICKNAME_CHECK_REQUIRED()	{return EditProfileScreenView.EVENT_ON_NICKNAME_CHECK_REQUIRED;}
	static get EVENT_ON_NICKNAME_CHANGE_REQUIRED()	{return EditProfileScreenView.EVENT_ON_NICKNAME_CHANGE_REQUIRED;}
	static get EVENT_ON_NICKNAME_CHANGED()			{return EditProfileScreenView.EVENT_ON_NICKNAME_CHANGED;}
	static get EVENT_ON_RENDER_TEXTURE_INVALIDATED(){return EditProfileScreenView.EVENT_ON_RENDER_TEXTURE_INVALIDATED;}

	static get EVENT_ON_SCREEN_SHOW()				{return "onScreenShow";}
	static get EVENT_ON_SCREEN_HIDE()				{return "onScreenHide";}

	constructor()
	{
		super();

		this._fIsWaitingToShowScreen = false;

		this._fSubloadingController_sc = APP.subloadingController;
		this._fIsBGLoading_bl = APP.appParamsInfo.backgroundLoadingAllowed;
	}

	showScreen()
	{
		this._showScreen();
	}

	hideScreen()
	{
		this._hideSceeen();
	}

	onProfileInitiation(data)
	{
		this.view.onProfileInitiation(data.availableStyles, data.userStyles);
	}

	getAvatarRenderTexture()
	{
		return this.view.avatarRenderTexture;
	}

	randomizeAvatar()
	{
		this.view.randomizeAvatar();
	}

	get avatarStyle()
	{
		return this.view.avatarStyle;
	}

	//INIT...
	__initControlLevel()
	{
		super.__initControlLevel();

		APP.playerController.on(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		this._fIsBGLoading_bl && this._fSubloadingController_sc.on(SubloadingController.EVENT_ON_LOADING_COMPLETED, this._onLoaded, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_ssv = this.view;
		!this._fIsBGLoading_bl && lView_ssv.initScreen();

		lView_ssv.on(EditProfileScreenView.EVENT_ON_CANCEL_BUTTON_CLICKED, this.emit, this);
		lView_ssv.on(EditProfileScreenView.EVENT_ON_CLOSE_SCREEN, this.emit, this);

		lView_ssv.on(EditProfileScreenView.EVENT_ON_AVATAR_CHANGE_REQUIRED, this.emit, this);
		lView_ssv.on(EditProfileScreenView.EVENT_ON_AVATAR_CHANGED, this.emit, this);
		lView_ssv.on(EditProfileScreenView.EVENT_ON_AVATAR_UPDATED, this.emit, this);

		lView_ssv.on(EditProfileScreenView.EVENT_ON_NICKNAME_CHECK_REQUIRED, this.emit, this);
		lView_ssv.on(EditProfileScreenView.EVENT_ON_NICKNAME_CHANGE_REQUIRED, this.emit, this);
		lView_ssv.on(EditProfileScreenView.EVENT_ON_NICKNAME_CHANGED, this.emit, this);

		lView_ssv.on(EditProfileScreenView.EVENT_ON_RENDER_TEXTURE_INVALIDATED, this.emit, this);
	}
	//...INIT

	_onLoaded(aEvent_e)
	{
		if (aEvent_e.assetsType === SUBLOADING_ASSETS_TYPES.profile)
		{
			this.view.initScreen();
			this._fIsWaitingToShowScreen && this._showScreen();
		}
	}

	_onPlayerInfoUpdated(event)
	{
		let lView_ssv = this.view;

		if(!lView_ssv)
		{
			return;
		}

		if (event.data[PlayerInfo.KEY_NICKNAME_GLYPHS])
		{
			lView_ssv.updateRestrictedNicknameGlyphs();
		}
	}

	_showScreen()
	{
		if (this._fIsBGLoading_bl && !this._fSubloadingController_sc.i_isLoaded(SUBLOADING_ASSETS_TYPES.profile)) 
		{
			this._fSubloadingController_sc.i_showLoadingScreen();
			this._fIsWaitingToShowScreen = true;
			return;
		}

		this._fSubloadingController_sc && this._fSubloadingController_sc.i_hideLoadingScreen();

		let lView_ssv = this.view;

		let lPlayerInfo_pi = APP.playerController.info;
		lView_ssv.onShow();
		lView_ssv.visible = true;

		this.emit(EditProfileScreenController.EVENT_ON_SCREEN_SHOW);

		//BLOCKING NICKNAME EDIT IF REQUIRED...
		let lIsNicknameEditable_bl = lPlayerInfo_pi.playerInfo.nicknameEditEnabled.value;
		if(lIsNicknameEditable_bl === undefined)
		{
			lIsNicknameEditable_bl = true;
		}

		lView_ssv.setNicknameEditEnabled(lIsNicknameEditable_bl);
		//...BLOCKING NICKNAME EDIT IF REQUIRED
	}

	_hideSceeen()
	{
		if (this._fIsBGLoading_bl)
		{
			this._fSubloadingController_sc.i_hideLoadingScreen();
			this._fIsWaitingToShowScreen = false;
		}
		
		let lView_ssv = this.view;

		if (lView_ssv)
		{
			lView_ssv.onHide();
			lView_ssv.visible = false;

			this.emit(EditProfileScreenController.EVENT_ON_SCREEN_HIDE);
		}
	}

	destroy()
	{
		let lView_ssv = this.view;

		if (lView_ssv)
		{
			lView_ssv.off(EditProfileScreenView.EVENT_ON_CANCEL_BUTTON_CLICKED, this.emit, this);
			lView_ssv.off(EditProfileScreenView.EVENT_ON_CLOSE_SCREEN, this.emit, this);

			lView_ssv.off(EditProfileScreenView.EVENT_ON_AVATAR_CHANGE_REQUIRED, this.emit, this);
			lView_ssv.off(EditProfileScreenView.EVENT_ON_AVATAR_CHANGED, this.emit, this);

			lView_ssv.off(EditProfileScreenView.EVENT_ON_NICKNAME_CHECK_REQUIRED, this.emit, this);
			lView_ssv.off(EditProfileScreenView.EVENT_ON_NICKNAME_CHANGE_REQUIRED, this.emit, this);
			lView_ssv.off(EditProfileScreenView.EVENT_ON_NICKNAME_CHANGED, this.emit, this);
		}

		super.destroy();
	}
}

export default EditProfileScreenController