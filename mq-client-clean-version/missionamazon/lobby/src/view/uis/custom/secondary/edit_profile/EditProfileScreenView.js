import Button from '../../../../../ui/LobbyButton';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import SimpleUIView from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import HorizontalSlider from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/slider/HorizontalSlider';
import HorizontalScrollBar from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/HorizontalScrollBar';
import HorizontalScrollableContainer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/HorizontalScrollableContainer';

import ProfileAvatar from '../../../../../components/profile/ProfileAvatar';
import InputField from '../../../../../ui/InputField';
import LobbyScreen from '../../../../../main/LobbyScreen'
import GameDialogButton from '../../../../../../src/view/uis/custom/dialogs/custom/game/GameDialogButton';
import PlayerInfo from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import * as FEATURES from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';

const USER_NAME_FONT_NAME = "fnt_nm_barlow";

class EditProfileScreenView extends SimpleUIView
{
	static get EVENT_ON_CANCEL_BUTTON_CLICKED()		{return "onCancelButtonClicked";}
	static get EVENT_ON_CLOSE_SCREEN()				{return "onCloseScreen";}

	static get EVENT_ON_AVATAR_CHANGE_REQUIRED()	{return "onAvatarChangeRequired";}
	static get EVENT_ON_AVATAR_CHANGED()			{return "onAvatarChanged";}
	static get EVENT_ON_AVATAR_UPDATED()			{return "onAvatarUpdated";}

	static get EVENT_ON_NICKNAME_CHECK_REQUIRED()	{return "onNicknameCheckRequired";}
	static get EVENT_ON_NICKNAME_CHANGE_REQUIRED()	{return "onNicknameChangeRequired";}
	static get EVENT_ON_NICKNAME_CHANGED()			{return "onNicknameChanged";}
	static get EVENT_ON_RENDER_TEXTURE_INVALIDATED(){return ProfileAvatar.EVENT_ON_RENDER_TEXTURE_INVALIDATED;}

	get avatarStyle()
	{
		return this._fAvatarContainer_pa.userStyles;
	}

	get avatarRenderTexture()
	{
		if (this._fAvatarContainer_pa)
		{
			return this._fAvatarContainer_pa.getAvatarRenderTexture();
		}
		else
		{
			return null;
		}
	}

	randomizeAvatar()
	{
		this._randomizeAvatar();
	}

	onShow()
	{
		this._onShow();
	}

	onHide()
	{
		this._onHide();
	}

	updateRestrictedNicknameGlyphs()
	{
		if (this._fNickname_tf)
		{
			let restrictedGlyphs = this._getRestrictedNicknameGlyphs();
			this._fNickname_tf.acceptableChars = restrictedGlyphs;
			this._fNickname_tf.updateFont(this._generateProperUsernameFontName(restrictedGlyphs));

			this._fNickname_tf.setValue(APP.lobbyScreen.playerInfo.nickname);
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

		this._fAvatarContainer_pa = null;
		this._fAccept_btn = null;
		this._fCancel_btn = null;
		this._fExitButton_btn = null;

		this._fNickname_tf = null;
		this._fTip_ta = null;
		this._fNotAvailableTip_ta = null;

		this._fHeroSlider_hs = null;
		this._fHeroSliderScrollBar_hsb = null;
		this._fBorderSlider_hs = null;
		this._fBorderSliderScrollBar_hsb = null;
		this._fBackgroundSlider_hs = null;
		this._fBackgroundSliderScrollBar_hsb = null;

		this._fHeroCounter_tf = null;
		this._fBorderCounter_tf = null;
		this._fBackCounter_tf = null;

		this._fAvailableStyles_obj = null;
		this._fUserStyles_obj = null;

		this._fScreenInitiated_bln = false;
		this._fAcceptInitiated_bln = false;
		this._fNicknameValid_bln = true;
		this._fAvatarValid_bln = true;

		this._fCaptionRightBorder_arr = [];

		this._wasFocusedBefore_bl = false;

		this._fIsBGLoading_bl = APP.appParamsInfo.backgroundLoadingAllowed;

		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_PROFILE_STYLES_INIT, this._onProfileInitiation, this);
		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_NICKNAME_ACCEPTED, this._onNicknameAccepted, this);
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

		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_NICKNAME_AVAILABILITY_CAHNGED, this._onNicknameAvailabilityChanged, this);
		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_NICKNAME_DENIED, this._onNicknameDenied, this);

		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_AVATAR_ACCEPTED, this._onAvatarAccepted, this);
		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_AVATAR_DENIED, this._onAvatarDenied, this);
	}

	_initProfile()
	{
		if (!this._fAvailableStyles_obj) return;

		if (!this._fHeroCounter_tf)
		{
			this._addCounters();
		}

		this._fNickname_tf.setValue(APP.lobbyScreen.playerInfo.nickname);

		if (this._fAvatarContainer_pa)
		{
			this._updateAvatarView();
			this._updateTracks();
		}
		else
		{
			this._addAvatarView();

			let lBordersLen_num = Math.min(this._fAvailableStyles_obj.borders.length, ProfileAvatar.TOTAL_BORDERS_COUNT);
			let lHeroesLen_num = Math.min(this._fAvailableStyles_obj.heroes.length, ProfileAvatar.TOTAL_HEROES_COUNT);
			let lBacksLen_num = Math.min(this._fAvailableStyles_obj.backs.length, ProfileAvatar.TOTAL_BACKS_COUNT);

			this._addAvatarChoiseSliders(lBordersLen_num, lHeroesLen_num, lBacksLen_num);
		}

		this.emit(EditProfileScreenView.EVENT_ON_AVATAR_CHANGED);
	}

	_addBack()
	{
		let lBack_spr = this.addChild(APP.library.getSprite("dialogs/back"));

		let lTopLine = lBack_spr.addChild(APP.library.getSprite("dialogs/line"));
		lTopLine.scale.x = 0.44;
		lTopLine.position.set(127.5, -89);

		let aIcon_sprt = this.addChild(APP.library.getSprite("profile/icon"));
		aIcon_sprt.position.set(-289, -174);
	}

	_addButtons()
	{
		this._fAccept_btn = this.addChild(new GameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TAEditProfileScreenButtonAcceptLabel", undefined, undefined, GameDialogButton.BUTTON_TYPE_ACCEPT));
		this._fAccept_btn.position.set(-59.5, 128.5);
		this._fAccept_btn.on("pointerclick", this._onAcceptBtnClicked, this);

		this._fCancel_btn = this.addChild(new GameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TAEditProfileScreenButtonCancelLabel", undefined, undefined, GameDialogButton.BUTTON_TYPE_CANCEL));
		this._fCancel_btn.position.set(59.5, 128.5);
		this._fCancel_btn.on("pointerclick", this._onCancelBtnClicked, this);

		this._fExitButton_btn = this.addChild(new Button("dialogs/exit_btn", null, true, undefined, undefined, Button.BUTTON_TYPE_CANCEL));
		this._fExitButton_btn.position.set(296, -171);
		this._fExitButton_btn.on("pointerclick", this._onCancelBtnClicked, this);
		APP.isMobile && (this._fExitButton_btn.hitArea = new PIXI.Rectangle(this._fExitButton_btn.hitArea.x * 2, this._fExitButton_btn.hitArea.y * 2, this._fExitButton_btn.hitArea.width * 2, this._fExitButton_btn.hitArea.height * 2));
	}

	_addCaptions()
	{
		let aEditProfileCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAEditProfileTitle"));
		aEditProfileCaption_ta.position.set(-204.5, -171.5);

		let aHeroSelectionCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAEditProfileScreenHeroSelectionCaption"));
		aHeroSelectionCaption_ta.position.set(127.5, -63);
		this._fCaptionRightBorder_arr[0] = 127.5 + aHeroSelectionCaption_ta.getBounds().width / 2;

		let aBorderStyleCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAEditProfileScreenBorderStyleCaption"));
		aBorderStyleCaption_ta.position.set(129, -12);
		this._fCaptionRightBorder_arr[1] = 129 + aBorderStyleCaption_ta.getBounds().width / 2;

		let aBackgroundStyleCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAEditProfileScreenBackgroundStyleCaption"));
		aBackgroundStyleCaption_ta.position.set(130, 37.5);
		this._fCaptionRightBorder_arr[2] = 130 + aBackgroundStyleCaption_ta.getBounds().width / 2;
	}

	_addUsernameInput()
	{
		let restrictedGlyphs = this._getRestrictedNicknameGlyphs();

		let fontName = this._generateProperUsernameFontName(restrictedGlyphs);
		
		let lInputConfig_obj = {
			fontColor: 0xffffff,
			fontFamily: fontName,
			fontSize: 18,
			width: 370,
			height: 39,
			selectionColor: 0x9d5525,
			textAlign: "center",
			offsetY: -4,
			selectYShift: -1,
			maxTextLength: 25,
			acceptableChars: restrictedGlyphs,
			caseSensitive: true
		}

		this._fNickname_tf = this.addChild(new InputField(null, lInputConfig_obj));
		this._fNickname_tf.position.set(126.5, -119);

		this._fNickname_tf.on(InputField.EVENT_ON_BLUR, this._onBlur, this);
		this._fNickname_tf.on(InputField.EVENT_ON_FOCUS, this._onFocus, this);
		
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

	_generateProperUsernameFontName(restrictedGlyphs)
	{
		let fontName = "sans-serif";
		if (APP.fonts.isGlyphsSupported(USER_NAME_FONT_NAME, restrictedGlyphs))
		{
			fontName = USER_NAME_FONT_NAME;
		}

		return fontName;
	}

	get _enterNicknameTip()
	{
		if (!this._fTip_ta)
		{
			this._fTip_ta = this.addChild(I18.generateNewCTranslatableAsset('TALobbyNewPlayerEnterTip'));
			this._fTip_ta.position.set(127.5, -110);
		}

		return this._fTip_ta;
	}

	get _notAvailableTip()
	{
		if (!this._fNotAvailableTip_ta)
		{
			this._fNotAvailableTip_ta = this.addChild(I18.generateNewCTranslatableAsset("TALobbyNewPlayerNotAvailableTip"));
			this._fNotAvailableTip_ta.position.set(126.5, -173);
			this._fNotAvailableTip_ta.visible = false;
		}

		return this._fNotAvailableTip_ta;
	}

	_addUsername()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_calibri",
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

	_addAvatarChoiseSliders(aBordersLen_num, aHeroesLen_num, aBacksLen_num)
	{
		let lSliderAssets_arr = ["profile/slider/slider_back", "profile/slider/slider_track"];

		this._fHeroSlider_hs = this.addChild(new HorizontalSlider(lSliderAssets_arr[0], lSliderAssets_arr[1], lSliderAssets_arr[2], aHeroesLen_num-1, 9, null));
		this._fHeroSlider_hs.on("trackUpdated", this._onHeroUpdate, this);
		this._fHeroSlider_hs.position.set(127.5, -39);

		this._fHeroSliderScrollBar_hsb = this.addChild(new HorizontalScrollBar());
		this._fHeroSliderScrollBar_hsb.slider = this._fHeroSlider_hs;
		this._fHeroSliderScrollBar_hsb.position.set(127.5, -39);
		this._fHeroSliderScrollBar_hsb.visibleArea = this._fHeroSliderScrollBar_hsb.hitArea = this._fHeroSlider_hs.getLocalBounds();

		this._fBorderSlider_hs = this.addChild(new HorizontalSlider(lSliderAssets_arr[0], lSliderAssets_arr[1], lSliderAssets_arr[2], aBordersLen_num-1, 9, null));
		this._fBorderSlider_hs.on("trackUpdated", this._onBorderUpdate, this);
		this._fBorderSlider_hs.position.set(127.5, 12);

		this._fBorderSliderScrollBar_hsb = this.addChild(new HorizontalScrollBar());
		this._fBorderSliderScrollBar_hsb.slider = this._fBorderSlider_hs;
		this._fBorderSliderScrollBar_hsb.position.set(127.5, 12);
		this._fBorderSliderScrollBar_hsb.visibleArea = this._fBorderSliderScrollBar_hsb.hitArea = this._fBorderSlider_hs.getLocalBounds();

		this._fBackgroundSlider_hs = this.addChild(new HorizontalSlider(lSliderAssets_arr[0], lSliderAssets_arr[1], lSliderAssets_arr[2], aBacksLen_num-1, 9, null));
		this._fBackgroundSlider_hs.on("trackUpdated", this._onBackUpdate, this);
		this._fBackgroundSlider_hs.position.set(127.5, 61.5);

		this._fBackgroundSliderScrollBar_hsb = this.addChild(new HorizontalScrollBar());
		this._fBackgroundSliderScrollBar_hsb.slider = this._fBackgroundSlider_hs;
		this._fBackgroundSliderScrollBar_hsb.position.set(127.5, 61.5);
		this._fBackgroundSliderScrollBar_hsb.visibleArea = this._fBackgroundSliderScrollBar_hsb.hitArea = this._fBackgroundSlider_hs.getLocalBounds();

		this._updateTracks();
	}

	_addCounters()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow",
			fontSize: 12,
			fill: 0xfccc32,
		}

		this._fHeroCounter_tf = this.addChild(new TextField(lStyle_obj));
		this._fHeroCounter_tf.text = (+this._fUserStyles_obj.hero + 1) + "/" +  this._fAvailableStyles_obj.heroes.length;
		this._fHeroCounter_tf.position.set(this._fCaptionRightBorder_arr[0] + 21, -63);
		this._fHeroCounter_tf.maxWidth = 44;
		this._fHeroCounter_tf.anchor.set(0.5, 0.5)

		this._fBorderCounter_tf = this.addChild(new TextField(lStyle_obj));
		this._fBorderCounter_tf.text = (+this._fUserStyles_obj.border + 1) + "/" +  this._fAvailableStyles_obj.borders.length;
		this._fBorderCounter_tf.position.set(this._fCaptionRightBorder_arr[1] + 21, -12);
		this._fBorderCounter_tf.maxWidth = 44;
		this._fBorderCounter_tf.anchor.set(0.5, 0.5)

		this._fBackCounter_tf = this.addChild(new TextField(lStyle_obj));
		this._fBackCounter_tf.text = (+this._fUserStyles_obj.back + 1) + "/" +  this._fAvailableStyles_obj.backs.length;
		this._fBackCounter_tf.position.set(this._fCaptionRightBorder_arr[2] + 21, 37.5);
		this._fBackCounter_tf.maxWidth = 44;
		this._fBackCounter_tf.anchor.set(0.5, 0.5)
	}

	_addAvatarView()
	{
		this._fAvatarContainer_pa = this.addChild(new ProfileAvatar(this._fAvailableStyles_obj, this._fUserStyles_obj));
		this._fAvatarContainer_pa.on(ProfileAvatar.EVENT_ON_RENDER_TEXTURE_INVALIDATED, this.emit, this);
		this._fAvatarContainer_pa.position.set(-148.5, -26);

		let lFlare_spr = this.addChild(APP.library.getSprite("profile/fare"));
		lFlare_spr.blendMode = PIXI.BLEND_MODES.ADD;
		lFlare_spr.position.set(-208.5, -103.5);
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
			this._fNickname_tf.on(InputField.EVENT_ON_VALUE_CHANGED, this._onCheckAviability, this);
		}
		
		this._enterNicknameTip.visible = false;
		this._wasFocusedBefore_bl = true;
	}

	_onCheckAviability()
	{
		this._fAccept_btn.enabled = false;

		let lNickname_str = this._fNickname_tf.getValue();
		this.emit(EditProfileScreenView.EVENT_ON_NICKNAME_CHECK_REQUIRED, {nickname: lNickname_str});
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
		this._cancel();

		this.emit(EditProfileScreenView.EVENT_ON_CANCEL_BUTTON_CLICKED);
	}

	_onAcceptBtnClicked()
	{
		this._fAcceptInitiated_bln = true;

		this._changeAvatarRequired();

		this._changeNicknameRequired();
	}

	_onAvatarUpdate()
	{
		this._changeAvatarRequired();
	}

	get _avatarChanged()
	{
		let lUserStyle_obj = this._fAvatarContainer_pa.userStyles;
		let lCurrentStyle_obj = this._fAvatarContainer_pa.currentStyles;

		if (
				lUserStyle_obj.border != lCurrentStyle_obj.border ||
				lUserStyle_obj.hero != lCurrentStyle_obj.hero ||
				lUserStyle_obj.back != lCurrentStyle_obj.back
			)
		{
			return true;
		}

		return false;
	}

	_changeAvatarRequired()
	{
		let lSettings_obj = this._fAvatarContainer_pa.currentStyles;

		if (this._avatarChanged)
		{
			this._fAvatarValid_bln = false;
			this.emit(EditProfileScreenView.EVENT_ON_AVATAR_CHANGE_REQUIRED, {settings: lSettings_obj});
		}
		else
		{
			this._fAvatarValid_bln = true;

			this._validateClosing();
		}
	}

	_changeNicknameRequired()
	{
		let lNickname_str = this._fNickname_tf.getValue();

		if (lNickname_str != APP.lobbyScreen.playerInfo.nickname)
		{
			this._fNicknameValid_bln = false;
			this.emit(EditProfileScreenView.EVENT_ON_NICKNAME_CHANGE_REQUIRED, {nickname: lNickname_str});
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
			if (this._fNicknameValid_bln && this._fAvatarValid_bln)
			{
				this._fAcceptInitiated_bln = false;
				this._fNicknameValid_bln = false;
				this._fAvatarValid_bln = false;

				this.emit(EditProfileScreenView.EVENT_ON_CLOSE_SCREEN);
			}
		}
	}

	_onAvatarAccepted()
	{
		this._save();
		this.emit(EditProfileScreenView.EVENT_ON_AVATAR_CHANGED);

		this._fAvatarValid_bln = true;
		this._validateClosing();
	}

	_onAvatarDenied()
	{
		this._cancel();
		this._fAcceptInitiated_bln = false;
	}

	_onNicknameAccepted()
	{
		let lNickname_str = this._fNickname_tf.getValue();
		this.emit(EditProfileScreenView.EVENT_ON_NICKNAME_CHANGED, {nickname: lNickname_str});

		this._fNicknameValid_bln = true;
		this._validateClosing();
	}

	_onNicknameDenied()
	{
		this._fNicknameValid_bln = false;
		this._fAcceptInitiated_bln = false;
	}
	//...HANDLERS

	//UPDATE AVATAR...
	_updateAvatarView()
	{
		this._fAvatarContainer_pa.update(this._fUserStyles_obj);
		this._fAvatarContainer_pa.save();

		this._updateTracks();

		this.emit(EditProfileScreenView.EVENT_ON_AVATAR_UPDATED);
	}

	_onBorderUpdate(aData_obj)
	{
		let lValue_num = aData_obj.value;

		if (this._fAvatarContainer_pa.currentStyles.border == lValue_num) return;

		this._fAvatarContainer_pa.setBorder(lValue_num);
		this._fAvatarContainer_pa.setHero(this._fAvatarContainer_pa._currentStyles.hero);

		let lBordersLen_num = Math.min(this._fAvailableStyles_obj.borders.length, ProfileAvatar.TOTAL_BORDERS_COUNT);
		this._fBorderCounter_tf.text = +lValue_num + 1 +"/" +  lBordersLen_num;
	}

	_onHeroUpdate(aData_obj)
	{
		let lValue_num = aData_obj.value;

		let lId_num = this._fAvailableStyles_obj.heroes[lValue_num];
		if (this._fAvatarContainer_pa.currentStyles.hero == lId_num) return;

		this._fAvatarContainer_pa.setHero(lValue_num);

		let lHeroesLen_num = Math.min(this._fAvailableStyles_obj.heroes.length, ProfileAvatar.TOTAL_HEROES_COUNT);
		this._fHeroCounter_tf.text = +lValue_num + 1 +"/" +  lHeroesLen_num;
	}

	_onBackUpdate(aData_obj)
	{
		let lValue_num = aData_obj.value;

		if (this._fAvatarContainer_pa.currentStyles.back == lValue_num) return;

		this._fAvatarContainer_pa.setBack(lValue_num);

		let lBacksLen_num = Math.min(this._fAvailableStyles_obj.backs.length, ProfileAvatar.TOTAL_BACKS_COUNT);
		this._fBackCounter_tf.text = +lValue_num + 1 +"/" +  lBacksLen_num;
	}

	_updateTracks()
	{
		let lCurrentStyles_obj = this._fAvatarContainer_pa.currentStyles;

		let lBorderIndex_num = this._fAvailableStyles_obj.borders.indexOf(lCurrentStyles_obj.border);
		let lHeroIndex_num = this._fAvailableStyles_obj.heroes.indexOf(lCurrentStyles_obj.hero);
		let lBackIndex_num = this._fAvailableStyles_obj.backs.indexOf(lCurrentStyles_obj.back);

		this._fBorderSlider_hs.moveTo(lBorderIndex_num);
		this._fHeroSlider_hs.moveTo(lHeroIndex_num);
		this._fBackgroundSlider_hs.moveTo(lBackIndex_num);

		let lBordersLen_num = Math.min(this._fAvailableStyles_obj.borders.length, ProfileAvatar.TOTAL_BORDERS_COUNT);
		let lHeroesLen_num = Math.min(this._fAvailableStyles_obj.heroes.length, ProfileAvatar.TOTAL_HEROES_COUNT);
		let lBacksLen_num = Math.min(this._fAvailableStyles_obj.backs.length, ProfileAvatar.TOTAL_BACKS_COUNT);

		this._fBorderCounter_tf.text = (lBorderIndex_num + 1) + "/" +  lBordersLen_num;
		this._fHeroCounter_tf.text = (lHeroIndex_num + 1) + "/" +  lHeroesLen_num;
		this._fBackCounter_tf.text = (lBackIndex_num + 1) + "/" +  lBacksLen_num;
	}
	//...UPDATE AVATAR

	//METHODS...
	_cancel()
	{
		if (this._fAvatarContainer_pa)
		{
			this._fAvatarContainer_pa.cancel();
			this._updateTracks();
		}
	}

	_save()
	{
		if (this._fAvatarContainer_pa)
		{
			this._fAvatarContainer_pa.save();
			this._fUserStyles_obj = this._fAvatarContainer_pa.userStyles;
			this._updateTracks();
		}
	}

	_getRandomInt(aMin_num, aMax_num)
	{
		return Math.floor(Math.random() * (aMax_num - aMin_num)) + aMin_num;
	}

	_randomizeAvatar()
	{
		this._currentStyles = {
			border: this._fAvailableStyles_obj.borders[this._getRandomInt(0, this._fAvailableStyles_obj.borders.length)],
			hero: this._fAvailableStyles_obj.heroes[this._getRandomInt(0, this._fAvailableStyles_obj.heroes.length)],
			back: this._fAvailableStyles_obj.backs[this._getRandomInt(0, this._fAvailableStyles_obj.backs.length)]
		};

		this._fAvatarContainer_pa.update(this._currentStyles);

		this._updateTracks();

		this._onAvatarUpdate();
	}

	_updateNickname()
	{
		this._fNickname_tf.setValue(APP.lobbyScreen.playerInfo.nickname);
		this._onBlur();
	}

	_onShow()
	{
		this._fHeroSliderScrollBar_hsb && this._fHeroSliderScrollBar_hsb.enableScroll();
		this._fBackgroundSliderScrollBar_hsb && this._fBackgroundSliderScrollBar_hsb.enableScroll();
		this._fBorderSliderScrollBar_hsb && this._fBorderSliderScrollBar_hsb.enableScroll();

		this._updateNickname();

		this._notAvailableTip.visible = false;
		this._enterNicknameTip.visible = false;
		this._fAccept_btn.enabled = true;
	}

	_onHide()
	{
		this._cancel();

		this._fHeroSliderScrollBar_hsb && this._fHeroSliderScrollBar_hsb.disableScroll();
		this._fBackgroundSliderScrollBar_hsb && this._fBackgroundSliderScrollBar_hsb.disableScroll();
		this._fBorderSliderScrollBar_hsb && this._fBorderSliderScrollBar_hsb.disableScroll();
	}
	//...METHODS

	destroy()
	{
		APP.lobbyScreen.off(LobbyScreen.EVENT_ON_NICKNAME_AVAILABILITY_CAHNGED, this._onNicknameAvailabilityChanged, this);
		APP.lobbyScreen.off(LobbyScreen.EVENT_ON_NICKNAME_ACCEPTED, this._onNicknameAccepted, this);
		APP.lobbyScreen.off(LobbyScreen.EVENT_ON_NICKNAME_DENIED, this._onNicknameDenied, this);
		
		this.off("pointermove", this._onPointerMove, this);

		APP.lobbyScreen.off(LobbyScreen.EVENT_ON_PROFILE_STYLES_INIT, this._onProfileInitiation, this);
		APP.lobbyScreen.off(LobbyScreen.EVENT_ON_AVATAR_ACCEPTED, this._onAvatarAccepted, this);
		APP.lobbyScreen.off(LobbyScreen.EVENT_ON_AVATAR_DENIED, this._onAvatarDenied, this);

		this._fAccept_btn && this._fAccept_btn.off("pointerclick", this._onAcceptBtnClicked, this);
		this._fCancel_btn && this._fCancel_btn.off("pointerclick", this._onCancelBtnClicked, this);

		this._fHeroSlider_hs && this._fHeroSlider_hs.off("trackUpdated", this._onHeroUpdate, this);
		this._fBorderSlider_hs && this._fBorderSlider_hs.off("trackUpdated", this._onBorderUpdate, this);
		this._fBackgroundSlider_hs && this._fBackgroundSlider_hs.off("trackUpdated", this._onBackUpdate, this);

		super.destroy();

		this._fAvatarContainer_pa = null;
		this._fAccept_btn = null;
		this._fCancel_btn = null;
		this._fExitButton_btn = null;

		this._fNickname_tf = null;
		this._fTip_ta = null;
		this._fNotAvailableTip_ta = null;

		this._fHeroSlider_hs = null;
		this._fHeroSliderScrollBar_hsb = null;
		this._fBorderSlider_hs = null;
		this._fBorderSliderScrollBar_hsb = null;
		this._fBackgroundSlider_hs = null;
		this._fBackgroundSliderScrollBar_hsb = null;

		this._fHeroCounter_tf = null;
		this._fBorderCounter_tf = null;
		this._fBackCounter_tf = null;

		this._fAvailableStyles_obj = {};
		this._fUserStyles_obj = {};

		this._fAcceptInitiated_bln = false;
		this._fNicknameValid_bln = true;
		this._fAvatarValid_bln = true;
	}
}

export default EditProfileScreenView;