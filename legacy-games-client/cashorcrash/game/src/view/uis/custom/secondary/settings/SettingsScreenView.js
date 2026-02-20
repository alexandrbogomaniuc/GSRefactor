import SettingsScreenSlider from './SettingsScreenSlider';
import Button from '../../../../../ui/GameButton';

import SimpleUIView from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameDialogButton from '../../../../../view/uis/custom/dialogs/custom/game/GameDialogButton';

class SettingsScreenView extends SimpleUIView
{
	static get EVENT_ON_CLOSE_BTN_CLICKED()						{ return "onCloseBtnClicked"; }
	static get EVENT_ON_SOUND_VOLUME_CHANGED()					{ return "onSoundVolumeChanged"; }
	static get EVENT_ON_MUSIC_VOLUME_CHANGED()					{ return "onMusicVolumeChanged"; }
	static get EVENT_ON_OK_BUTTON_CLICKED()						{ return "onOkButtonClicked"; }

	setSettings(aSettings_obj)
	{
		this._setSettings(aSettings_obj);
	}

	constructor()
	{
		super();

		this._fSoundSlider_sss = null;
		this._fMusicSlider_sss = null;

		this._init();
	}

	_init()
	{
		this._addBack();
		this._addTitleCaption();
		this._addCaptions();
		this._addButton();
		this._addSliders();
	}

	_addBack()
	{
		let lBack_sprt = this.addChild(APP.library.getSprite("settings/back"));
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
		lSoundVolumeCaption_ta.position.set(-195, -110+55);

		let lMusicVolumeCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TASettingsScreenMusicVolumeLabel"));
		lMusicVolumeCaption_ta.position.set(-195, -55+55);
	}

	_addSliders()
	{
		this._fSoundSlider_sss = this.addChild(new SettingsScreenSlider(0, 100, 100));
		this._fSoundSlider_sss.position.set(100, -110+55);
		this._fSoundSlider_sss.scale.x = 0.89;
		this._fSoundSlider_sss.on(SettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, this._onSoundSliderValueChanged.bind(this));

		this._fMusicSlider_sss = this.addChild(new SettingsScreenSlider(0, 100, 100));
		this._fMusicSlider_sss.position.set(100, -55+55);
		this._fMusicSlider_sss.scale.x = 0.89;
		this._fMusicSlider_sss.on(SettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, this._onMusicSliderValueChanged.bind(this));
	}

	_addButton()
	{
		let lOkButton_btn = this.addChild(new GameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TADialogButtonOK", undefined, undefined, GameDialogButton.BUTTON_TYPE_ACCEPT));
		lOkButton_btn.on("pointerclick", this._onOkBtnClicked.bind(this));
		lOkButton_btn.position.set(0, 105);

		let lCloseButton_btn = this.addChild(new Button("dialogs/exit_btn", null, true, undefined, undefined, Button.BUTTON_TYPE_CANCEL));
		lCloseButton_btn.position.set(216.5, -168);
		lCloseButton_btn.on("pointerclick", this._onCloseBtnClicked, this);
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

	//EVENT LISTENERS...
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

		this._fSoundSlider_sss = null;
		this._fMusicSlider_sss = null;
	}
}

export default SettingsScreenView;