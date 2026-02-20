import SimpleUIController from '../../../../../../unified/controller/uis/base/SimpleUIController';
import GUSLobbyEditProfileScreenView from '../../../../../view/uis/secondary/edit_profile/GUSLobbyEditProfileScreenView';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyPlayerController from '../../../../custom/GUSLobbyPlayerController';
import GUSLobbySubloadingController from '../../../../subloading/GUSLobbySubloadingController';
import PlayerInfo from '../../../../../../unified/model/custom/PlayerInfo';
import { SUBLOADING_ASSETS_TYPES } from '../../../../../model/subloading/GUSubloadingInfo';

class GUSLobbyEditProfileScreenController extends SimpleUIController
{
	static get EVENT_ON_CANCEL_BUTTON_CLICKED()			{ return GUSLobbyEditProfileScreenView.EVENT_ON_CANCEL_BUTTON_CLICKED; }
	static get EVENT_ON_CLOSE_SCREEN()					{ return GUSLobbyEditProfileScreenView.EVENT_ON_CLOSE_SCREEN; }

	static get EVENT_ON_NICKNAME_CHECK_REQUIRED()		{ return GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHECK_REQUIRED; }
	static get EVENT_ON_NICKNAME_CHANGE_REQUIRED()		{ return GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHANGE_REQUIRED; }
	static get EVENT_ON_NICKNAME_CHANGED()				{ return GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHANGED; }

	static get EVENT_ON_SCREEN_SHOW()					{ return "onScreenShow"; }
	static get EVENT_ON_SCREEN_HIDE()					{ return "onScreenHide"; }

	constructor(aOptInfo)
	{
		super();

		this._fIsWaitingToShowScreen = false;

		this._fSubloadingController_sc = APP.subloadingController;
		this._fIsBGLoading_bl = APP.appParamsInfo.backgroundLoadingAllowed && !!SUBLOADING_ASSETS_TYPES.profile;
	}

	showScreen()
	{
		this._showScreen();
	}

	hideScreen()
	{
		this._hideSceeen();
	}

	//INIT...
	__initControlLevel()
	{
		super.__initControlLevel();

		APP.playerController.on(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		this._fIsBGLoading_bl && this._fSubloadingController_sc.on(GUSLobbySubloadingController.EVENT_ON_LOADING_COMPLETED, this._onLoaded, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_ssv = this.view;
		!this._fIsBGLoading_bl && lView_ssv.initScreen();

		lView_ssv.on(GUSLobbyEditProfileScreenView.EVENT_ON_CANCEL_BUTTON_CLICKED, this.emit, this);
		lView_ssv.on(GUSLobbyEditProfileScreenView.EVENT_ON_CLOSE_SCREEN, this.emit, this);

		lView_ssv.on(GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHECK_REQUIRED, this.emit, this);
		lView_ssv.on(GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHANGE_REQUIRED, this.emit, this);
		lView_ssv.on(GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHANGED, this.emit, this);
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
		lView_ssv.onShow();
		lView_ssv.visible = true;

		this.emit(GUSLobbyEditProfileScreenController.EVENT_ON_SCREEN_SHOW);

		//BLOCKING NICKNAME EDIT IF REQUIRED...
		let lIsNicknameEditable_bl = APP.playerController.info.playerInfo.nicknameEditEnabled.value;
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
			lView_ssv.visible = false;

			this.emit(GUSLobbyEditProfileScreenController.EVENT_ON_SCREEN_HIDE);
		}
	}

	destroy()
	{
		let lView_ssv = this.view;

		if (lView_ssv)
		{
			lView_ssv.off(GUSLobbyEditProfileScreenView.EVENT_ON_CANCEL_BUTTON_CLICKED, this.emit, this);
			lView_ssv.off(GUSLobbyEditProfileScreenView.EVENT_ON_CLOSE_SCREEN, this.emit, this);

			lView_ssv.off(GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHECK_REQUIRED, this.emit, this);
			lView_ssv.off(GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHANGE_REQUIRED, this.emit, this);
			lView_ssv.off(GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHANGED, this.emit, this);
		}

		super.destroy();
	}
}

export default GUSLobbyEditProfileScreenController