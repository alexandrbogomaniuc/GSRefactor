import SimpleUIView from '../../../../../unified/view/base/SimpleUIView';
import { APP } from '../../../../../unified/controller/main/globals';
import GUSLobbyScreen from '../../../main/GUSLobbyScreen';
import PlayerInfo from '../../../../../unified/model/custom/PlayerInfo';
import TextField from '../../../../../unified/view/base/display/TextField';
import InputText from '../../../../../unified/view/ui/InputText';
import Button from '../../GUSLobbyButton';

class GUSLobbyEditProfileScreenView extends SimpleUIView
{
	static get EVENT_ON_CANCEL_BUTTON_CLICKED()		{return "onCancelButtonClicked";}
	static get EVENT_ON_CLOSE_SCREEN()				{return "onCloseScreen";}

	static get EVENT_ON_NICKNAME_CHECK_REQUIRED()	{return "onNicknameCheckRequired";}
	static get EVENT_ON_NICKNAME_CHANGE_REQUIRED()	{return "onNicknameChangeRequired";}
	static get EVENT_ON_NICKNAME_CHANGED()			{return "onNicknameChanged";}

	onShow()
	{
		this._onShow();
	}

	updateRestrictedNicknameGlyphs()
	{
		if (this._fNickname_tf)
		{
			let restrictedGlyphs = this._getRestrictedNicknameGlyphs();

			if (restrictedGlyphs !== this._fNickname_tf.acceptableChars)
			{
				let lInputParams_obj = this._fNickname_tf.getParams();
				lInputParams_obj.acceptableChars = restrictedGlyphs;
				lInputParams_obj.fontFamily = this._generateProperUsernameFontName();
				this._fNickname_tf.setParams(lInputParams_obj);

				this._fNickname_tf.setValue(APP.lobbyScreen.playerInfo.nickname);
			}
			
		}
	}

	setNicknameEditEnabled(aIsEnabled_bl)
	{
		let lNickname_tf = this._fNickname_tf;

		if (lNickname_tf)
		{
			lNickname_tf.enabled = aIsEnabled_bl;
		}
	}

	constructor()
	{
		super();

		this._fAccept_btn = null;
		this._fCancel_btn = null;
		this._fExitButton_btn = null;

		this._fNickname_tf = null;
		this._fTip_ta = null;
		this._fNotAvailableTip_ta = null;

		this._fAvailableStyles_obj = null;
		this._fUserStyles_obj = null;

		this._fScreenInitiated_bln = false;
		this._fAcceptInitiated_bln = false;
		this._fNicknameValid_bln = true;

		this._wasFocusedBefore_bl = false;

		this._fIsBGLoading_bl = APP.appParamsInfo.backgroundLoadingAllowed;

		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_PROFILE_STYLES_INIT, this._onProfileInitiation, this);
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_NICKNAME_ACCEPTED, this._onNicknameAccepted, this);
	}

	//INIT...
	initScreen()
	{
		this._fScreenInitiated_bln = true;

		this._addBack();
		this._addButtons();
		this._addCaptions();
		this._addUsernameInput();

		this._fIsBGLoading_bl && this._initProfile();

		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_NICKNAME_AVAILABILITY_CAHNGED, this._onNicknameAvailabilityChanged, this);
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_NICKNAME_DENIED, this._onNicknameDenied, this);
	}

	_initProfile()
	{
		if (!this._fAvailableStyles_obj) return;

		this._fNickname_tf.setValue(APP.lobbyScreen.playerInfo.nickname);
	}

	_addBack()
	{
	}

	_addButtons()
	{
		this._fAccept_btn = this.addChild(this.__initAcceptButtonInstance());
		this._fAccept_btn.on("pointerclick", this._onAcceptBtnClicked, this);

		this._fCancel_btn = this.addChild(this.__initCancelButtonInstance());
		this._fCancel_btn.on("pointerclick", this._onCancelBtnClicked, this);

		this._fExitButton_btn = this.addChild(this.__initExitButtonInstance());
		this._fExitButton_btn.on("pointerclick", this._onCancelBtnClicked, this);
		APP.isMobile && (this._fExitButton_btn.hitArea = new PIXI.Rectangle(this._fExitButton_btn.hitArea.x * 2, this._fExitButton_btn.hitArea.y * 2, this._fExitButton_btn.hitArea.width * 2, this._fExitButton_btn.hitArea.height * 2));
	}

	__initAcceptButtonInstance()
	{
		// must be overridden
		return new Button(undefined);
	}

	__initCancelButtonInstance()
	{
		// must be overridden
		return new Button(undefined);
	}

	__initExitButtonInstance()
	{
		// must be overridden
		return new Button(undefined);
	}

	_addCaptions()
	{
	}

	_addUsernameInput()
	{
		let lInputParams_obj = this.__nicknameInputParams;
		let lInputConfig_obj = lInputParams_obj.config;

		this._fNickname_tf = this.addChild(new InputText(lInputConfig_obj));
		this._fNickname_tf.position.set(lInputParams_obj.position.x, lInputParams_obj.position.y);

		this._fNickname_tf.on(InputText.EVENT_ON_BLUR, this._onBlur, this);
		this._fNickname_tf.on(InputText.EVENT_ON_FOCUS, this._onFocus, this);
	}

	get __nicknameInputParams()
	{
		return { config: {}, position: {x: 0, y: -50} };
	}

	_getRestrictedNicknameGlyphs()
	{
		let restrictedGlyphs = APP.playerController.info.nicknameGlyphs;
		if (!restrictedGlyphs)
		{
			restrictedGlyphs = PlayerInfo.DEFAULT_NICK_GLYPHS;
		}

		return restrictedGlyphs;
	}

	_generateProperUsernameFontName()
	{
		let restrictedGlyphs = this._getRestrictedNicknameGlyphs();

		let fontName = "sans-serif";
		if (APP.fonts.isGlyphsSupported(this.__userNameFontName, restrictedGlyphs))
		{
			fontName = this.__userNameFontName;
		}

		return fontName;
	}

	get __userNameFontName()
	{
		return "sans-serif";
	}

	get _enterNicknameTip()
	{
		if (!this._fTip_ta)
		{
			this._fTip_ta = this.addChild(this.__initEnterNicknameTipInstance());
		}

		return this._fTip_ta;
	}

	__initEnterNicknameTipInstance()
	{
		return new Sprite;
	}

	get _notAvailableTip()
	{
		if (!this._fNotAvailableTip_ta)
		{
			this._fNotAvailableTip_ta = this.addChild(this.__initNameNotAvailableTipInstance());
			this._fNotAvailableTip_ta.visible = false;
		}

		return this._fNotAvailableTip_ta;
	}

	__initNameNotAvailableTipInstance()
	{
		return new Sprite;
	}

	_addUsername()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_cmn_calibri",
			fontSize: 19,
			fontWeight: "bold",
			fill: 0xffffff,
			stroke: 0x291909,
			strokeThickness: 1,
		};

		this._fNickname_tf = this.addChild(new TextField(lStyle_obj));
		this._fNickname_tf.maxWidth = 370;
		this._fNickname_tf.anchor.set(0.5, 0.5);
	}
	//...INIT

	//HANDLERS...
	_onBlur()
	{
		let lValue_str = this._fNickname_tf.getValue();

		if (lValue_str == "")
		{
			this._enterNicknameTip.visible = true;
			this._fAccept_btn.enabled = false;
		}
	}

	_onFocus()
	{
		if (!this._wasFocusedBefore_bl)
		{
			this._fNickname_tf.on(InputText.EVENT_ON_VALUE_CHANGED, this._onCheckAviability, this);
		}

		this._enterNicknameTip.visible = false;
		this._wasFocusedBefore_bl = true;
	}

	_onCheckAviability()
	{
		this._fAccept_btn.enabled = false;

		let lNickname_str = this._fNickname_tf.getValue();
		this.emit(GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHECK_REQUIRED, { nickname: lNickname_str });
	}

	_onNicknameAvailabilityChanged(aEvent_obj)
	{
		if (aEvent_obj.available)
		{
			this._notAvailableTip.visible = false;
			this._fAccept_btn.enabled = true;
		}
		else
		{
			this._notAvailableTip.visible = true;
			this._fAccept_btn.enabled = false;
		}
	}

	_onProfileInitiation(aEvent_obj)
	{
		this._fAvailableStyles_obj = aEvent_obj.availableStyles;
		this._fUserStyles_obj = aEvent_obj.userStyles;

		if (!this._fIsBGLoading_bl || this._fScreenInitiated_bln)
		{
			this._initProfile();
		}
	}

	_onCancelBtnClicked()
	{
		this.emit(GUSLobbyEditProfileScreenView.EVENT_ON_CANCEL_BUTTON_CLICKED);
	}

	_onAcceptBtnClicked()
	{
		this._fAcceptInitiated_bln = true;
		this._changeNicknameRequired();
	}

	_changeNicknameRequired()
	{
		let lNickname_str = this._fNickname_tf.getValue();

		if (lNickname_str != APP.lobbyScreen.playerInfo.nickname)
		{
			this._fNicknameValid_bln = false;
			this.emit(GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHANGE_REQUIRED, { nickname: lNickname_str });
		}
		else
		{
			this._fNicknameValid_bln = true;
			this._validateClosing();
		}
	}

	_validateClosing()
	{
		if (this._fAcceptInitiated_bln)
		{
			if (this._fNicknameValid_bln)
			{
				this._fAcceptInitiated_bln = false;
				this._fNicknameValid_bln = false;

				this.emit(GUSLobbyEditProfileScreenView.EVENT_ON_CLOSE_SCREEN);
			}
		}
	}

	_onNicknameAccepted()
	{
		let lNickname_str = this._fNickname_tf.getValue();
		this.emit(GUSLobbyEditProfileScreenView.EVENT_ON_NICKNAME_CHANGED, { nickname: lNickname_str });

		this._fNicknameValid_bln = true;
		this._validateClosing();
	}

	_onNicknameDenied()
	{
		this._fNicknameValid_bln = false;
		this._fAcceptInitiated_bln = false;
	}
	//...HANDLERS

	//METHODS...
	_updateNickname()
	{
		this._fNickname_tf.setValue(APP.lobbyScreen.playerInfo.nickname);
		this._onBlur();
	}

	_onShow()
	{
		this._updateNickname();

		this._notAvailableTip.visible = false;
		this._enterNicknameTip.visible = false;
		this._fAccept_btn.enabled = true;
	}
	//...METHODS

	destroy()
	{
		APP.lobbyScreen.off(GUSLobbyScreen.EVENT_ON_NICKNAME_AVAILABILITY_CAHNGED, this._onNicknameAvailabilityChanged, this);
		APP.lobbyScreen.off(GUSLobbyScreen.EVENT_ON_NICKNAME_ACCEPTED, this._onNicknameAccepted, this);
		APP.lobbyScreen.off(GUSLobbyScreen.EVENT_ON_NICKNAME_DENIED, this._onNicknameDenied, this);

		this.off("pointermove", this._onPointerMove, this);

		APP.lobbyScreen.off(GUSLobbyScreen.EVENT_ON_PROFILE_STYLES_INIT, this._onProfileInitiation, this);

		this._fAccept_btn && this._fAccept_btn.off("pointerclick", this._onAcceptBtnClicked, this);
		this._fCancel_btn && this._fCancel_btn.off("pointerclick", this._onCancelBtnClicked, this);

		super.destroy();

		this._fAccept_btn = null;
		this._fCancel_btn = null;
		this._fExitButton_btn = null;

		this._fNickname_tf = null;
		this._fTip_ta = null;
		this._fNotAvailableTip_ta = null;

		this._fAvailableStyles_obj = {};
		this._fUserStyles_obj = {};

		this._fAcceptInitiated_bln = false;
		this._fNicknameValid_bln = true;
	}	
}

export default GUSLobbyEditProfileScreenView;