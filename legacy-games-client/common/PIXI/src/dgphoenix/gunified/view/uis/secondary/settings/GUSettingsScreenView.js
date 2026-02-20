import SimpleUIView from '../../../../../unified/view/base/SimpleUIView';
import GUSettingsScreenSlider from './GUSettingsScreenSlider';
import Button from '../../../../../unified/view/ui/Button';

class GUSettingsScreenView extends SimpleUIView
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
	}

	_addTitleCaption()
	{
	}

	_addCaptions()
	{
	}

	_addButton()
	{
		let lOkButton_btn = this.addChild(this.__provideOkButtonInstance());
		lOkButton_btn.on("pointerclick", this._onOkBtnClicked.bind(this));

		let lCloseButton_btn = this.addChild(this.__provideCloseButtonInstance());
		lCloseButton_btn.on("pointerclick", this._onCloseBtnClicked, this);
	}

	__provideOkButtonInstance()
	{
		return new Button();
	}

	__provideCloseButtonInstance()
	{
		return new Button();
	}

	_onOkBtnClicked()
	{
		this.emit(GUSettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED);
	}

	_onCloseBtnClicked()
	{
		this.emit(GUSettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED);
	}

	_addSliders()
	{
		this._fSoundSlider_sss = this.addChild(this.__provideSoundSliderInstance());
		this._fSoundSlider_sss.on(GUSettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, this._onSoundSliderValueChanged.bind(this));

		this._fMusicSlider_sss = this.addChild(this.__provideMusicSliderInstance());
		this._fMusicSlider_sss.on(GUSettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, this._onMusicSliderValueChanged.bind(this));
	}

	__provideSoundSliderInstance()
	{
		return new GUSettingsScreenSlider();
	}

	__provideMusicSliderInstance()
	{
		return new GUSettingsScreenSlider();
	}

	_onSoundSliderValueChanged(aEvent_obj)
	{
		let lVal_num = aEvent_obj.value / 100;

		this.emit(GUSettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED, { value: lVal_num });
	}

	_onMusicSliderValueChanged(aEvent_obj)
	{
		let lVal_num = aEvent_obj.value / 100;

		this.emit(GUSettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED, { value: lVal_num });
	}

	_setSettings(aSettings_obj)
	{
		if (aSettings_obj && aSettings_obj.soundVolumes)
		{
			this._fSoundSlider_sss.value = aSettings_obj.soundVolumes.fxVolume * 100;
			this._fMusicSlider_sss.value = aSettings_obj.soundVolumes.musicVolume * 100;
		}
	}

	destroy()
	{
		super.destroy();

		this._fSoundSlider_sss = null;
		this._fMusicSlider_sss = null;
	}
}

export default GUSettingsScreenView;