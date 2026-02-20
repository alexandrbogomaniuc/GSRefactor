import SettingsScreenSlider from './SettingsScreenSlider';
import Button from '../../../../../ui/LobbyButton';

import SimpleUIView from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SwitchButton from '../../../../../../src/components/tips/SwitchButton';
import GameDialogButton from '../../../../../../src/view/uis/custom/dialogs/custom/game/GameDialogButton';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

class SettingsScreenView extends SimpleUIView
{
	static get EVENT_ON_CLOSE_BTN_CLICKED()						{ return "onCloseBtnClicked"; }
	static get EVENT_ON_SOUND_VOLUME_CHANGED()					{ return "onSoundVolumeChanged"; }
	static get EVENT_ON_MUSIC_VOLUME_CHANGED()					{ return "onMusicVolumeChanged"; }
	static get EVENT_ON_OK_BUTTON_CLICKED()						{ return "onOkButtonClicked"; }
	static get EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED()		{ return "onTutorialStateChanged"; }

	static get EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE()			{ return SettingsScreenSlider.EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE; }

	static get EVENT_ON_TUTORIAL_TOGGLE_BTN_CLICK()				{ return SwitchButton.EVENT_ON_TOGGLE_BTN_CLICK; }

	get soundVolumeValue ()
	{
		return this._initSoundVolumeValue_num;
	}

	set soundVolumeValue (aValue_num)
	{
		this._initSoundVolumeValue_num = aValue_num;
	}

	get musicVolumeValue ()
	{
		return this._initMusicVolumeValue_num;
	}

	set musicVolumeValue (aValue_num)
	{
		this._initMusicVolumeValue_num = aValue_num;
	}

	setSettings(aSettings_obj)
	{
		this._setSettings(aSettings_obj);
	}

	updateTutorialToggleState(aState_bln)
	{
		this._updateTutorialToggleState(aState_bln);
	}

	constructor()
	{
		super();

		this._fTutorialCaption_ta = null;
		this._fSoundSlider_sss = null;
		this._fMusicSlider_sss = null;
		this._fShowTutorialToggle_sb = null;

		this._fTimerAsset_ta = null;
		this._fTimer_tf = null;

		this._initSoundVolumeValue_num = 100;
		this._initMusicVolumeValue_num = 100;

		this._init();
	}

	_init()
	{
		this._addBack();
		this._addTitleCaption();
		this._addCaptions();
		this._addButton();
		this._addSliders();

		if(APP.battlegroundController)
		{
			this._addTimerIfRequired();
		}

		this._initTutorialToggle();
	}

	_addBack()
	{
		this.addChild(APP.library.getSprite("settings/back"));

		let lLine_sprt = this.addChild(APP.library.getSprite("dialogs/line"));
		lLine_sprt.position.set(0, 80);
		lLine_sprt.scale.x = 0.73;
	}

	_addTitleCaption()
	{
		let lTitleCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TASettingsScreenCaption"));
		lTitleCaption_ta.position.set(-145.5, -168);

		let lIcon_sprt = this.addChild(APP.library.getSprite("settings/icon"));
		lIcon_sprt.position.set(-208, -168);
	}

	_addCaptions()
	{
		let lSoundVolumeCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TASettingsScreenSoundVolumeLabel"));
		lSoundVolumeCaption_ta.position.set(-195, -110);

		let lMusicVolumeCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TASettingsScreenMusicVolumeLabel"));
		lMusicVolumeCaption_ta.position.set(-195, -55);

		this._fTutorialCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TASettingsScreenTutorialLabel"));
		this._fTutorialCaption_ta.position.set(-195, 0);
	}

	updateSlidersPosition()
	{
		this._fSoundSlider_sss.value = this._initSoundVolumeValue_num*100;
		this._fMusicSlider_sss.value = this._initMusicVolumeValue_num*100;
	}

	_addSliders()
	{
		this._fSoundSlider_sss = this.addChild(new SettingsScreenSlider(0, 100, this._initSoundVolumeValue_num));
		this._fSoundSlider_sss.position.set(100, -110);
		this._fSoundSlider_sss.scale.x = 0.89;
		this._fSoundSlider_sss.on(SettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, this._onSoundSliderValueChanged.bind(this));
		this._fSoundSlider_sss.on(SettingsScreenSlider.EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE, this.emit, this);

		this._fMusicSlider_sss = this.addChild(new SettingsScreenSlider(0, 100, this._initMusicVolumeValue_num));
		this._fMusicSlider_sss.position.set(100, -55);
		this._fMusicSlider_sss.scale.x = 0.89;
		this._fMusicSlider_sss.on(SettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, this._onMusicSliderValueChanged.bind(this));
		this._fMusicSlider_sss.on(SettingsScreenSlider.EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE, this.emit, this);
	}

	_addButton()
	{
		let lOkButton_btn = this.addChild(new GameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TAEditProfileScreenButtonAcceptLabel", undefined, undefined, GameDialogButton.BUTTON_TYPE_ACCEPT));
		lOkButton_btn.on("pointerclick", this._onOkBtnClicked.bind(this));
		lOkButton_btn.position.set(0, 105);

		let lCloseButton_btn = this.addChild(new Button("dialogs/exit_btn", null, true, undefined, undefined, Button.BUTTON_TYPE_CANCEL));
		lCloseButton_btn.position.set(216.5, -168);
		lCloseButton_btn.on("pointerclick", this._onCloseBtnClicked, this);
	}

	_addTimerIfRequired()
	{
			this._fTimerAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TASettingsScreenTimer"));
			this._fTimerAsset_ta.position.set(-80, 50);

			this._fTimer_tf = this.addChild(new TextField(this._timerStyle));
			this._fTimer_tf.position.set(35, 50-4);
			this.updateTimeIndicator();
	}

	updateTimeIndicator(aFormatedValue_str = "00:00")
	{
		if(this._fTimerAsset_ta && this._fTimer_tf)
		{
			if(APP.battlegroundController.isSecondaryScreenTimerRequired)
			{
				this._fTimerAsset_ta.visible = true;
				this._fTimer_tf.visible = true;
				this._fTimer_tf.text = aFormatedValue_str;
			}
			else
			{
				this.hideTimeIndicator();
			}
		}
	}

	hideTimeIndicator()
	{
		if(this._fTimerAsset_ta && this._fTimer_tf)
		{
			this._fTimerAsset_ta.visible = false;
			this._fTimer_tf.visible = false;
		}
	}

	get _timerStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 16,
			align: "left",
			fill: 0xffffff
		};

		return lStyle_obj;
	}

	_initTutorialToggle()
	{
		this._fShowTutorialToggle_sb = this.addChild(new SwitchButton());
		this._fShowTutorialToggle_sb.on(SwitchButton.EVENT_ON_STATE_CHANGED, this._onTutorialStateChanged, this);
		this._fShowTutorialToggle_sb.on(SwitchButton.EVENT_ON_TOGGLE_BTN_CLICK, this.emit, this);
		this._fShowTutorialToggle_sb.position.set(100, 0);
	}

	_onCloseBtnClicked()
	{
		this.emit(SettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED);
	}

	_setSettings(aSettings_obj)
	{
		if (aSettings_obj && aSettings_obj.soundVolumes)
		{
			this._fSoundSlider_sss.value = aSettings_obj.soundVolumes.fxVolume*100;
			this._fMusicSlider_sss.value = aSettings_obj.soundVolumes.musicVolume*100;
		}
	}

	_updateTutorialToggleState(aState_bln)
	{
		this._fShowTutorialToggle_sb.updateToggleState(aState_bln);

		if (this._fTutorialCaption_ta) this._fTutorialCaption_ta.visible = true;
		if (this._fShowTutorialToggle_sb) this._fShowTutorialToggle_sb.visible = true;
	}

	//EVENT LISTENERS...
	_onTutorialStateChanged(aEvent_obj)
	{
		this.emit(SettingsScreenView.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, {value: aEvent_obj.state});
	}

	_onOkBtnClicked()
	{
		this.emit(SettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED);
	}

	_onSoundSliderValueChanged(aEvent_obj)
	{
		let lVal_num = aEvent_obj.value/100;

		this.emit(SettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED, {value: lVal_num});
	}

	_onMusicSliderValueChanged(aEvent_obj)
	{
		let lVal_num = aEvent_obj.value/100;

		this.emit(SettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED, {value: lVal_num});
	}
	//...EVENT LISTENERS

	destroy()
	{
		super.destroy();

		this._fTutorialCaption_ta = null;
		this._fSoundSlider_sss.off(SettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, this._onSoundSliderValueChanged.bind(this));
		this._fSoundSlider_sss.off(SettingsScreenSlider.EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE, this.emit, this);
		this._fSoundSlider_sss = null;
		this._fMusicSlider_sss.off(SettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, this._onMusicSliderValueChanged.bind(this));
		this._fMusicSlider_sss.off(SettingsScreenSlider.EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE, this.emit, this);
		this._fMusicSlider_sss = null;
		this._fShowTutorialToggle_sb.off(SwitchButton.EVENT_ON_STATE_CHANGED, this._onTutorialStateChanged, this);
		this._fShowTutorialToggle_sb.off(SwitchButton.EVENT_ON_TOGGLE_BTN_CLICK, this.emit, this);
		this._fShowTutorialToggle_sb = null;

		this._initSoundVolumeValue_num = null;
		this._initMusicVolumeValue_num = null;
	}
}

export default SettingsScreenView;